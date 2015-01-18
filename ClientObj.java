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


/*
 * An object used to hold data on each client. This is stored server side based
 * on the hash value assigned when the client first connects. 
 * 
 * Pretty straight forward here, a few helper functions for accessing data members,
 * as well as the functions which control the joke/proverb state.
 */
public class ClientObj{
	private String name;
	private String hash;
	private boolean [] jokes;
	private boolean [] proverbs;
	
	public ClientObj(String name){
		this.name = name;
		this.hash = ((Integer) this.hashCode()).toString();
		this.jokes = new boolean [5];
		this.proverbs = new boolean [5];
		
		/*
		for (boolean joke : jokes){
			joke = false;
		}
		*/
	}
	
	public String getHash(){
		return this.hash;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean checkJoke(int i){
		return jokes[i];
	}
	
	public boolean checkProverb(int i){
		return proverbs[i];
	}
	
	/*
	 * Controls joke state. If every joke has been told we reset.
	 */
	public void turnOffJoke(int i){
		this.jokes[i] = true;
		
		/*
		 * check if all the jokes are true, if so reset 
		 */
		for (boolean joke : jokes){
			if (!joke){
				//System.out.println("joke hasnt been told");
				return;
			}
		}
		
		jokes = new boolean [5];
		
		for (boolean joke : jokes){
			assert(joke == false);
		}
	}
	
	/*
	 * Controls proverb state. If every proverb has been told we reset.
	 */
	public void turnOffProverb(int i){
		this.proverbs[i] = true;
		
		/*
		 * check if all the proverbs are true, if so reset
		 */
		for (boolean proverb : proverbs){
			if (!proverb){
				return;
			}
		}
		
		System.out.println("resetting proverbs array");
		proverbs = new boolean[5];
		
		for (boolean proverb : proverbs){
			assert(proverb == false);
		}
	}
	
}