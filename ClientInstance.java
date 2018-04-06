import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * 
 * @author Zhenkun Wu-201298999-x7zw
 *
 */
public class ClientInstance {
	private int portNumber = 2048;
	private String welcome = "Please enter your username:";
	private String accepted = "Your username has been accepted.";
	private Socket socket = null;
	private BufferedReader fromServer;
	private PrintWriter toServer;
	private String clientName;
	private double startTime;
	private boolean isAllowedToChat = false;
	private boolean isServerConnected = false;
	/**
	 * The process of the client program.
	 * Firstly, establish the connection between server and client.
	 * Then, create two threads.
	 * One is for handling outgoing messages, the other
	 * is for handling inputing messages. 
	 */
	public void start(){
		establishConnection();
		handleOutgoingMessages();
		handleIncomingMessages();
	}
	/**
	 * This method is to establish the connection between client and server.
	 */
	private void establishConnection(){
		String serverAddress = getClientInput("What is the address of the server you "
				+ "wish to coonect to?");
		try{
			socket = new Socket(serverAddress, portNumber);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toServer = new PrintWriter(socket.getOutputStream());
			isServerConnected = true;
			startTime = System.currentTimeMillis();
		}catch(IOException e){
			System.err.println(e);
		}
		handleProfileSetUp();
	}
	 /**
	  * This method is used to set up the client. It will get the 
	  * username from the user and send it to server.
	  */
	private void handleProfileSetUp(){
		String line = null;
		while(!isAllowedToChat){
			try{
				line = fromServer.readLine();
			}catch(IOException e){
				System.err.println(e);
			}
			if(line.startsWith(welcome)){
				toServer.println(getClientInput(welcome));
				toServer.flush();
			}else if(line.startsWith(accepted)){
				isAllowedToChat = true;
				System.out.println(accepted+"\nYou can type message.");
				System.out.println("You could type //Help to get the command list.");
			}else{
				System.out.println(line);
			}
		}
	}
	/**
	 * This method is used to handling the outgoing message.
	 */
	private void handleOutgoingMessages(){
		Thread thread = new Thread(new Runnable(){
			public void run(){
				while(isServerConnected){
					String message = getClientInput(null);
					if(message.trim().equalsIgnoreCase("//ServerRunningTime")){
						toServer.println(message);
						toServer.flush();
					}else if(message.trim().equalsIgnoreCase("//ChatTime")){
						toServer.println(message);
						toServer.println(startTime);
						toServer.flush();
					}else{
						toServer.println(message);
						toServer.flush();
					}
				}
			}
		});
		thread.start();
	}
	/**
	 * This method is used to handle the incoming messages.
	 */
	private void handleIncomingMessages(){
		Thread thread = new Thread(new Runnable(){
			public void run(){
				String line = null;
				while(isServerConnected){
					try{
						line = fromServer.readLine();
						if(line==null){
							isServerConnected = false;
							System.err.println("Disconnected from the server.");
							closeConnection();
							break;
						}
						while((line = fromServer.readLine())!=null){
							System.out.println(line);
						}
					}catch(IOException e){
						System.err.println("IOE in handleIncomingMessages().");
						closeConnection();
						break;
					}
				}
			}
		});
		thread.start();
	}
	/**
	 * This method is used to get the input from the keyboard or the server.
	 * @param hints is the string which from the keyboard or the server.
	 * @return
	 */
	private String getClientInput(String hints) {
		String message = null;
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			if(hints != null){
				System.out.println(hints);
			}
			message = reader.readLine();
			if(!isAllowedToChat){
				clientName = message;
			}
		}catch(IOException e){
			System.err.println("Exception in getClientInput() "+e);
		}
		return message;
	}
	/**
	 * This method is used to close the connection.
	 */
	private void closeConnection(){
		try{
			socket.close();
			System.exit(0);
		}catch(IOException e){
			System.err.println("Exception when closing the socket.");
			System.err.println(e.getMessage());
		}
	}
}
