import java.net.*; 
import java.io.*; 
import java.lang.*;
  
public class Insert 
{
    // initialize socket and input output streams 
    private Socket socket = null; 
    private DataInputStream input = null; 
    private DataOutputStream out = null; 
  
    // constructor to put ip address and port 
    public Insert(String address, int port, int start_Idx, int num_of_blocks,String path) 
    {
        // establish a connection 
        try
        {
            socket = new Socket(address, port); 
            System.out.println("Connected"); 
  
            // takes input from terminal 
            // input  = new DataInputStream(System.in); 
  
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
  
        // string to read message from input 
        String line = ""; 
  
        // keep reading until "Over" is input 
        // while (!line.equals("Over")) 
        // {
        //     try
        //     {
        //         line = input.readLine(); 
        //         out.writeUTF(line); 
        //     }
        //     catch(IOException i) 
        //     {
        //         System.out.println(i); 
        //     }
        // }
        ///*
        // keeping reading from the blocks until it gets over
        for( int i=start_Idx;i<start_Idx+num_of_blocks;i++){
            try{
                BufferedReader br=new BufferedReader(new FileReader(new File(path+"Block"+i+".dss")));
                line="";
                    while((line=br.readLine())!=null){
                        try{
                            out.writeUTF(line); 
                        }
                        catch(IOException ioe){
                            System.out.println(ioe); 
                        }
                    }
                    try{
                        out.writeUTF("One block Completed."); 
                    }
                    catch(IOException ioe){
                        System.out.println(ioe); 
                    }
                }
            catch(IOException ioe){
                System.out.println(ioe); 
            }
            // catch(FileNotFoundException fe){
            //     System.out.println(fe); 
            // }
            // catch(Exception e){
            //     System.out.println("Other exception"+ e);
            // }
        }
        try{
            out.writeUTF("Over"); 
            // Thread.sleep(2000);
        }
        catch(IOException ioe){
            System.out.println(ioe); 
        }
        // catch(InterruptedException ie){
        //     System.out.println(ie); 
        // }
        //*/
        
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
        Insert client = new Insert("127.0.0.1", 5000,0,2,"/home/pramit/Desktop/NewFiles/"); 
        client = new Insert("127.0.0.1", 5000,2,3,"/home/pramit/Desktop/NewFiles/"); 
    }
} 