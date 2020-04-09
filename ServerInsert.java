// A Java program for a Server 
import java.net.*; 
import java.io.*; 
import java.util.*;

public class ServerInsert 
{ 
	//initialize socket and input stream 
	private Socket socket = null; 
	private ServerSocket server = null; 
	private DataInputStream in = null; 
	private final int SIZE=64*1024;
	// constructor with port 
	public ServerInsert(int port) 
	{ 
		// starts server and waits for a connection 
		try{ 
			server = new ServerSocket(port); 
			System.out.println("Server started"); 
			while(true){
				System.out.println("Waiting for a client ..."); 
				socket = server.accept(); 
				System.out.println("Client accepted"); 
				// takes input from the client socket 
				in = new DataInputStream( 
					(socket.getInputStream())); 
				byte[] bytes = new byte[SIZE];
				try
				{
					while(in.read(bytes)!=-1){
						System.out.print(new String(bytes)); 
						Arrays.fill(bytes,(byte)0);
					}
					// System.out.println();
				} 
				catch(IOException i) 
				{ 
					System.out.println(i); 
					System.exit(0);
				} 
				System.out.println("Closing connection"); 
				// close connection 
				socket.close(); 
				in.close(); 
			} 
		}
		catch(IOException i) 
		{ 
			System.out.println(i); 
			// System.exit(0);
		} 
	} 

	public static void main(String args[]) 
	{ 
		ServerInsert server = new ServerInsert(5000); 
	} 
} 
