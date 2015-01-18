import java.io.*; // import I/O libraries
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/*
	Nick Groos InetServer
*/

class IWorker extends Thread{ //subclass of Thread
	Socket sock; // create the socket, local for the Worker
	
	/*
	 * construct the worker, assign its datamember Socket
	 */
	IWorker (Socket s) { 
		sock = s;
	}

public void run(){
	// I/O streams for data going in/out of the socket..?
	PrintStream out = null;
	BufferedReader in = null;
	
	/*
	 * initialize streams, try to make a reader from the socket's Input/Output Stream 
	 */
	try{
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintStream(sock.getOutputStream());
		
		// shut down the server if controlSwitch says so!
		if (InetServer.controlSwitch != true){
			System.out.println("Lister is now shutting down per client request.");
			out.println("Server is now shutting down. Bu-Bye!");
		}
		/*
		 * if the server is still running...
		 * get the name from the socket, check if it says to shutdown - if so then shutdown
		 */
		else try{
			String name;
			name = in.readLine();
			if (name.indexOf("shutdown") > -1){
				InetServer.controlSwitch = false;
				System.out.println("Worker has captured a shutdown request.");
				
				// "out" prints out to the client.
				out.println("Shutdown request has been noted by worker.");
				out.println("Please send final shutdown request to listener.");
			}
			/*
			 * if it's a normal request, look up the name
			 */
			else{
				System.out.println("Looking up " + name);
				printRemoteAddress(name, out);
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

	/*
	 * print out the name/address for the connected machine.
	 * Uses some built-in java functions
	 */
	static void printRemoteAddress(String name, PrintStream out){
		try{
			out.println("Looking up " + name + "...");
			InetAddress machine = InetAddress.getByName(name); // built in java function
			out.println("Host name : " + machine.getHostName());
			out.println("Host IP : " + toText(machine.getAddress()));
		} catch (UnknownHostException ex){
			out.println("Failed in attempt to look up " + name);
		}
	}
	
	/*
	 * prepare the ip address, adds a period after each set of numbers. Uses bit masking.
	 */
	static String toText(byte ip[]){
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; ++i){
			if (i > 0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
}

/*
 * main thread for the server. It sits and waits for a connection from a client.
 * Once it gets a connection it creates a worker who will handle it. I believe that
 * is how we can easily handle multiple connections... We just spawn the worker, and then
 * go back to waiting for another connection. 
 */
public class InetServer {
	public static boolean controlSwitch = true;
	public static void main(String a[]) throws IOException{
		int q_len = 6;
		int port = 5111;
		Socket sock;
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("Nick Groos' Inet server starting up, listening at port 5111. \n");
		
		while (controlSwitch){
			sock = servsock.accept(); // continually wait and accept any client connections
			
			/*
			 * if you make it to here, that means we have connected to a client,
			 * and we create a worker to handle it
			 */
			if (controlSwitch) new IWorker(sock).start(); 
			
			/*
			 * if this is uncommented you get some weird shutdown behavior... 
			 * Does not shutdown normally...
			 */
			 //try{Thread.sleep(10000);} catch(InterruptedException ex) {}
		}
	}
}
