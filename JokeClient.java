import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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


public class JokeClient {
	public static String hash;
	public static String name;
	public static boolean opening;
	public static String request;

	public static void main (String args []){
		String serverName;
		opening = true;
		
		// server is local unless we are given an arg variable
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Nick Groos' Inet Client, version 0.01\n");
		System.out.println("Using server: " + serverName + ", Port: 5111");
		
		// reads incoming data
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			/*
			 * Boolean opening controls this section to only run the first time. 
			 * The user sends over their name (in function setHashWithRequest(),
			 * and in return gets a hash value. 
			 * The hash is stored server side, and is sent as a cookie every
			 * time the user connects.
			 */
			do {
				if (opening){
					System.out.println("Please enter your name, or type 'quit' to quit.");
					name = in.readLine();
					
					if (name.toLowerCase().equals("quit")) { System.exit(0); }
					assert(!name.equals("quit"));
					
					setHashWithRequest(name, serverName);
					assert(hash != null);
					
					opening = false;
				}
				/*
				 * if this client has connected before, we will send a normal
				 * request using the hash that has already been stored client-side.
				 */
				else{
					//System.out.println("sending normal request");
					sendNormalRequest(serverName);
				}
				
			} while (request.indexOf("quit") < 0);
			
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}
	}

	
	/*
	 * follows similar execution as the opening event above, except we don't 
	 * need to ask the client for their name. The user simply asks 'give me something'
	 * and their hash/cookie will be sent over to the server.
	 */
	static void sendNormalRequest(String serverName) throws IOException{
		Socket sock;
		PrintStream toServer;
		BufferedReader in, fromServer;
		String response;
		
		sock = new Socket(serverName, 5111);
		in = new BufferedReader(new InputStreamReader(System.in));
		fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		toServer = new PrintStream(sock.getOutputStream());
		
		
		System.out.print("Enter a server request 'give me something' - or type" +
				" (quit) to end: ");

		request = in.readLine();
		System.out.println("request is: " + request);
		
		if (request.toLowerCase().indexOf("quit") > -1){
			sock.close();
			return;
		}
		
		/*
		 * send the hash first, then the request
		 */
		toServer.println(hash);
		toServer.println(request);
		
		/*
		 * accept the response, print it, and close the socket.
		 */
		response = fromServer.readLine();
		System.out.println("Server Response: " + response);
		sock.close();
	}
	
	/*
	 * Function used when the user connects for the first time. The client
	 * has already gotten the user name, and passes it to this function, where
	 * we ask for the actual server request. 
	 * 
	 * Upon receiving the response, we store the hash value assigned by the server,
	 * this hash will be used from here on when connecting to the server.
	 */
	static void setHashWithRequest(String name, String serverName) throws IOException{
		Socket sock;
		BufferedReader fromServer;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintStream toServer;
		String response;
		
		System.out.print("Enter a server request 'give me something' - or type" +
				" (quit) to end: ");

		request = in.readLine();
		System.out.println("request is: " + request);
		
		if (request.toLowerCase().indexOf("quit") > 0){
			return;
		}
		
		/*
		 * set up the socket connections, receive and store the hash client-sisde.
		 */
		try {
			sock = new Socket(serverName, 5111);
			
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			toServer.println(name);  toServer.flush();
			toServer.println(request);  toServer.flush();
			
			hash = fromServer.readLine();
			
			if (hash != null) { 
				System.out.println("Hash has been set to : " + hash + "on client side\n"); 
				response = fromServer.readLine();
				System.out.println("Server response: " + response);
			}
			else{
				sock.close();
				System.out.println("didn't get a hash back from server");
				throw new IOException();
			}
			
			sock.close();
		} catch (IOException x){
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
}
