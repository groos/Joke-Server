import java.io.*;
import java.net.Socket;

/*
	Nick Groos InetServer
*/

public class InetClient {

	public static void main (String args []){
		String serverName;
		
		// server is local unless we are given an arg variable
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Nick Groos' Inet Client, version 0.0\n");
		System.out.println("Using server: " + serverName + ", Port: 5111");
		
		// reads incoming data
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			String name;
			do {
				System.out.print("Enter a hostname or an IP address, (quit) to end: ");
				System.out.flush();
				name = in.readLine();
				
				// if we have a valid request, call function to get name info
				if (name.indexOf("quit") < 0)
					getRemoteAddress(name, serverName);
			} 
			while (name.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}
	}
	
	/*
	 * returns a string representation of a given byte array. 
	 * Uses bit masking and adds periods when appropriate.
	 */
	static String toText (byte ip[]){
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < ip.length; ++i){
			if (i > 0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
	
	/*
	 * called from main when there is a valid request for server name/info
	 */
	static void getRemoteAddress(String name, String serverName){
		// prepare tools
		Socket sock; 
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try{
			sock = new Socket(serverName, 5111); // open connection to server port
			
			// create I/O streams for the socket connection
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			/*
			 * send name info to the server. This is an Output stream because
			 * it is output from this computer.
			 */
			toServer.println(name); toServer.flush();
			
			/*
			 * wait for some feedback from the server, print it out.
			 * What if there is no feedback? does readLine just sit there waiting?
			 */
			for (int i = 1; i <= 3; i++){
				textFromServer = fromServer.readLine();
				
				if (textFromServer != null) System.out.println(textFromServer);
			}
			
			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
}
