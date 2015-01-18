import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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


public class JokeClientAdmin {
	public static String request;
	Socket sock;
	PrintStream toServer;
	
	/*
	 * set up the connections to the server
	 */
	public static void main(String args []) throws UnknownHostException, IOException{
	String serverName, response;
	Socket sock;
	PrintStream toServer;
	BufferedReader in, fromServer;
	
	if (args.length < 1) serverName = "localhost";
	else serverName = args[0];
	
	in = new BufferedReader(new InputStreamReader(System.in));

	
	try{
		/*
		 * Create sockets, and exchange a few lines with the server
		 * to set it into maintenance-mode
		 * We are using the admin port 6000 here.
		 */
		do{
			sock = new Socket(serverName, 6000);
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			System.out.println("Admin access to JokeServer. " +
							"Type 'maintenance-mode' to change server settings.");
			System.out.flush();
			request = in.readLine();
			System.out.println("request is: " + request);
			
			if (request.toLowerCase().indexOf("quit") > -1){
				sock.close();
				return;
			}
			toServer.println(request); toServer.flush();
			response = fromServer.readLine();
			
			if (response != null) { System.out.println(response); }
			
			
			/*
			 * Once in maintenance mode, the user can set it to return
			 * jokes or proverbs. After this selection the server will 
			 * leave maintenance mode until the admin sends a new request
			 * with 'maintenance-mode'
			 */
			System.out.println("Choose: 'joke-mode' or 'proverb-mode'.");
			System.out.flush();
			request = in.readLine();
			
			toServer.println(request); toServer.flush();
			response = fromServer.readLine();
			
			// print out the response and close the socket.
			if (response != null) { System.out.println(response); }
			
			sock.close();
			
			
		} while (request.indexOf("quit") < 0);
		sock.close();
		System.out.println("Admin mode cancelled by user request.");
		} catch (IOException x ) {x.printStackTrace(); }
	
	
	}

}
