import java.io.*; // import I/O libraries
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


/*--------------------------------------------------------

1. Nick Groos / 4/19/2014:

2. Java build 1.7.0_25-b15

3. To compile, in command window type:
	> javac [filename].java

4. To run the program:
In separate shell windows:

	> java JokeServer
	> java JokeClient
	> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For examaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java
 e. ClientObj.java

5. Notes:

The program should run correctly if you follow the instructions in these comments.
I have tested the client and admin on 2 machines, both were able to connect to the server
running on one machine. Any commands will be displayed in the command line.
Some additional informational messages will appear in the command line to
keep the user informed on what is happening.

----------------------------------------------------------*/


class Worker extends Thread{ //subclass of Thread
	Socket sock; // create the socket, local for the Worker
	
	/*
	 * construct the worker, assign its datamember Socket
	 */
	Worker (Socket s) { 
		sock = s;
	}

public void run(){
	// I/O streams for data going in/out of the socket.
	PrintStream out = null;
	BufferedReader in = null;
	
	/*
	 * initialize streams, try to make a reader from the socket's Input/Output Stream 
	 */
	try{
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintStream(sock.getOutputStream());
		
		// shut down the server if controlSwitch says so!
		if (JokeServer.controlSwitch != true){
			System.out.println("Lister is now shutting down per client request.");
			out.println("Server is now shutting down. Bu-Bye!");
		}
		/*
		 * if the server is still running...
		 * get the name from the socket, check if it says to shutdown - if so then shutdown
		 */
		else try{
			String request, clientHash;
			clientHash = in.readLine();
			request = in.readLine();
			
			
			/*
			 * If we haven't stored the hash, then it's a name 
			 * we store than name in a Client object, and put in the hashmap
			 */
			if (!(JokeServer.store.containsKey(clientHash))){
				
				ClientObj newClient = new ClientObj(clientHash);
				clientHash = newClient.getHash();
				
				JokeServer.store.put(newClient.getHash(), newClient);
				
				out.println(newClient.getHash());
				System.out.println("just sent the client hash " + newClient.getHash());
			}
			else{
				System.out.println("Found stored hash for user.");
			}
			
			if (request.indexOf("shutdown") > -1){
				JokeServer.controlSwitch = false;
				System.out.println("Worker has captured a shutdown request.");
				
				// "out" prints out to the client.
				out.println("Shutdown request has been noted by worker.");
				out.println("Please send final shutdown request to listener.");
			}
			/*
			 * If user asks for joke/proverb, access the ClientObj using the 
			 * hash code (sent first by client) and send an appropriate response.
			 * 
			 * If the server is in maintenance mode it will display the delay response.
			 */
			else if (request.toLowerCase().indexOf("give me something") > -1){
				
				ClientObj client = JokeServer.store.get(clientHash);
				boolean told = false;
				
				while (!told){
					int index = 0 + (int)(Math.random() * ((4 - 0) + 1));
					assert(0 <= index && 4 >= index);
					
					// Joke Mode sends a joke
					if (JokeServer.JOKE_MODE && !JokeServer.MAINT_MODE){
						
						if (!client.checkJoke(index)){
							String personal_joke = JokeServer.personalize(JokeServer.Jokes[index], client.getName());
							
							System.out.println("Sending client a joke.");
							out.println(personal_joke);
							client.turnOffJoke(index);
							told = true;
						}
						else{
							//System.out.println("joke " + index + " already told. It's " + client.checkJoke(index));
						}
					}
					
					// Prover mode sends a proverb
					else if (!JokeServer.JOKE_MODE && !JokeServer.MAINT_MODE){
						if (!client.checkProverb(index)){
							String personal_proverb = JokeServer.personalize(JokeServer.Proverbs[index], client.getName());
							
							System.out.println("Sending client a proverb.");
							out.println(personal_proverb);
							client.turnOffProverb(index);
							told = true;
						}
						else{
							System.out.println("proverb " + index + " already told. Getting another");
						}
					}
					// Maintenance mode sends unavailable message.
					else if (JokeServer.MAINT_MODE){
						out.println("The server is temporarily unavailable -- check-back shortly.");
						told = true;
					}
				}
			}
			else if (request.toLowerCase().indexOf("quit") > -1){
				System.out.println("A client just quit.");
			}
			
			else{
				System.out.println("Server doesnt know what to do with request");
			}
			/*
			 * if something goes wrong, print stack trace and close up shop.
			 */
		} catch (IOException x){
			System.out.println("Server read error");
			x.printStackTrace();
		}
		sock.close();
		} catch (IOException ioe) {System.out.println(ioe);}
	}
}


/*
 * main thread for the server. It sits and waits for a connection from a client.
 * Once it gets a connection it creates a worker who will handle it. I believe that
 * is how we can easily handle multiple connections... We just spawn the worker, and then
 * go back to waiting for another connection. 
 */
public class JokeServer {
	public static boolean controlSwitch = true;
	public static boolean JOKE_MODE = true;
	public static boolean MAINT_MODE = false;
	public static String [] Jokes = new String [5];
	public static String [] Proverbs = new String [5];

	public static HashMap<String, ClientObj> store = new HashMap<String, ClientObj>();
	
	public static void main(String a[]) throws IOException{
		makeJokesAndProverbs();
		int q_len = 6;
		int port = 5111;
		Socket sock;
		ServerSocket servsock = new ServerSocket(port, q_len);
		
		// Start up our IT guy, which waits for instructions from an AdminClient
		AdminListener listener = new AdminListener();
		Thread t = new Thread(listener);
		t.start();
		
		System.out.println("Nick Groos' Joke server starting up, listening at port 5111. \n");
		
		while (controlSwitch){
			sock = servsock.accept(); // continually wait and accept any client connections
			
			/*
			 * if you make it to here, that means we have connected to a client,
			 * and we create a worker to handle it
			 */
			if (controlSwitch) new Worker(sock).start(); 
			/*
			 * if this is uncommented you get some weird shutdown behavior... 
			 * Does not shutdown normally...
			 */
			 //try{Thread.sleep(10000);} catch(InterruptedException ex) {}
		}
		servsock.close();
	}
	
	/*
	 * Jokes Taken from http://thoughtcatalog.com/christopher-hudspeth/2013/09/50-terrible-quick-jokes-thatll-get-you-a-laugh-on-demand/
	 * Proverbs taken from http://www.quotationspage.com/quotes/Chinese_Proverb/
	 */
	public static void makeJokesAndProverbs(){
		Jokes[0] = "A. Xname started a band called 999 Megabytes — they haven’t gotten a gig yet.";
		Jokes[1] = "B. Xname totally understands how batteries feel because Xname is rarely ever included in things either.";
		Jokes[2] = "C. Xname, did you hear about the new corduroy pillows? They’re making headlines everywhere!";
		Jokes[3] = "D. Xname, time flies like an arrow, fruit flies like banana.";
		Jokes[4] = "E. Xname, what do you call a big pile of kittens? A meowntain!";
		
		Proverbs[0] = "A. Xname, a rat who gnaws at a cat's tail invites destruction.";
		Proverbs[1] = "B. Xname, do not fear going forward slowly; fear only to stand still.";
		Proverbs[2] = "C. Xname, do not remove a fly from your friend's forehead with a hatchet.";
		Proverbs[3] = "D. Xname, a bird does not sing because it has an answer. It sings because it has a song.";
		Proverbs[4] = "E. Xname, a diamond with a flaw is worth more than a pebble without imperfections.";
	}
	
	public static String personalize(String s, String name){
		return s.replace("Xname", name);
	}
}

/*
 * Admin thread which waits for connection by an Admin client. 
 * Upon connection it creates an ITGuy and set it to work.
 */
class AdminListener implements Runnable{
	public static boolean adminControlSwitch = true;

	public void run() {
		//System.out.println("In admin listener");
		
		int len = 4;
		int port = 6000;
		Socket sock;
		
		try {
			ServerSocket serversock = new ServerSocket(port, len);
			adminControlSwitch = true;
			
			while(adminControlSwitch){
				sock = serversock.accept();
				
				if (adminControlSwitch) new ITGuy(sock).start();
			}
			serversock.close();
		} catch (IOException ioe) { System.out.println(ioe); }
		
	}
	
	
	/*
	 * Controls interaction with AdminClient. Admin has ability to set 
	 * server into maintenance-mode which pauses client responses. Once
	 * the Admin has changed the server to joke/proverb mode it exits 
	 * maintenance mode and resumes normal operation. 
	 */
	private class ITGuy extends Thread{
		Socket sock;
		
		private ITGuy(Socket sock){
			this.sock = sock;
		}
		
		public void run(){
			String request;
			PrintStream out = null;
			BufferedReader in = null;
			
			System.out.println("IT Guy is starting up for Admin access.");
			
			try {
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new PrintStream(sock.getOutputStream());
				
				if (!adminControlSwitch){
					System.out.println("Ending Admin Session");
					out.println("Shutting down Admin Mode.");
				}
				/*
				 * asks Admin to turn on maintenance mode, which allows admin to change 
				 * server joke/proverb mode. The server will not enter maintenance mode
				 * until the connected admin instructs it to. 
				 */
				else {
					request = in.readLine();
					
					if (request.toLowerCase().indexOf("maintenance") > -1){
						JokeServer.MAINT_MODE = true;
						System.out.println("Server entered Maintenance-Mode per Admin");
						out.println("Server currently held in maintenance mode");
						
						request = in.readLine();
						
						/*
						 * Handle response for either joke or proverb mode. And then
						 * return Server to normal operation.
						 */
						if (request.toLowerCase().indexOf("joke") > -1){
							JokeServer.JOKE_MODE = true;
							out.println("Changed server to Jokes mode.");
						}
						else if (request.toLowerCase().indexOf("proverb") > -1){
							JokeServer.JOKE_MODE = false;
							out.println("Changed server to Proverbs mode.");
						}
						else if (request.toLowerCase().indexOf("shutdown") > -1){
							JokeServer.controlSwitch = false;
							System.out.println("Client requested shutdown");
						}
						else {
							System.out.println("Unrecognized request");
							out.println("Unrecognized request.");
						}
						JokeServer.MAINT_MODE = false;
					}
					else {
						out.println("Server Message: You must type 'maintenance-mode' to make any changes to server.");
					}
					sock.close();
				}
			} catch (IOException e) {
				System.out.println("Server Read Error");
				e.printStackTrace();
			}
		} 
	}


}
