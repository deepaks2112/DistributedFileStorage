import java.net.*; 
import java.io.*; 
import java.lang.*;
import java.util.*;

public class ClientInsert 
{
    // initialize socket and input output streams 
    private Socket socket = null; 
    private DataInputStream in = null; 
    private DataOutputStream out = null; 
    private final int SIZE=64*1024;
    // constructor to put ip address and port 
    public ClientInsert(String address, int port, int start_Idx, int num_of_blocks,String path) 
    {
        // establish a connection 
        try
        {
            socket = new Socket(address, port); 
            System.out.println("Connected"); 
  
            // takes input from terminal 
            // in  = new DataInputStream(System.in); 
  
            // sends output to the socket 
            out    = new DataOutputStream(socket.getOutputStream()); 
        }
        catch(UnknownHostException u) 
        {
            System.out.println(u); 
        }
        catch(IOException ioe) 
        {
            System.out.println(ioe); 
        }
        //byte array to read bytes from file
        byte []bytes=new byte[SIZE];
        // keeping reading from the blocks until it gets over
        for( int i=start_Idx;i<start_Idx+num_of_blocks;i++){
            try{
                    in=new DataInputStream(new FileInputStream(new File(path+"Block"+i+".dss")));
                    while(in.read(bytes)!=-1){
                        try{
                            out.write(bytes); 
                        }
                        catch(IOException ioe){
                            System.out.println(ioe); 
                        }
                        Arrays.fill(bytes,(byte)0);
                    }
                    try{
                        out.write(("\nOne block Completed.\n").getBytes()); 
                    }
                    catch(IOException ioe){
                        System.out.println(ioe); 
                    }
                }
            catch(IOException ioe){
                System.out.println(ioe); 
            }
        }
        // close the connection 
        try
        {
            // input.close(); 
            out.close(); 
            socket.close(); 
        }
        catch(IOException ioe){
            System.out.println(ioe); 
        }
    }
  
    public static void main(String args[]) 
    {
        ClientInsert client = new ClientInsert("127.0.0.1", 5000,0,2,"/home/pramit/Desktop/NewFiles/"); 
        client = new ClientInsert("127.0.0.1", 5000,2,3,"/home/pramit/Desktop/NewFiles/"); 
    }
} 
