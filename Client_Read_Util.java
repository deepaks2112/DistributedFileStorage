import java.net.*;
import java.io.*;

public class Client
{
    // initialize socket and input output streams 
    private Socket socket            = null;
    private DataInputStream  input   = null;
    private DataOutputStream out     = null;
    private DataInputStream in       = null;

    // constructor to put ip address and port 
    public Client(String address, int port,int startindex,int noOfBlock) throws IOException {
        // establish a connection 
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal 
            input  = new DataInputStream(System.in);

            // sends output to the socket

            out    = new DataOutputStream(socket.getOutputStream());
            in     = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        } catch(IOException u)
        {
            System.out.println(u);
        }

        // string to read message from input 
//        String line = "";
        String str=String.valueOf(startindex)+" "+String.valueOf(noOfBlock);
        out.writeUTF(str);
        String line="";
        FileWriter fileWriter=null;
        BufferedWriter bufferedWriter=null;
        int blockNo=startindex;
        while(!line.equals("#completed")){
            line=in.readUTF();
            try {
            if(line.charAt(0) != '#') {
                String path="/home/vaibhav/Desktop/ClientDatabase/Block"+String.valueOf(blockNo)+".txt";
                bufferedWriter=new BufferedWriter(new FileWriter(path,true));


                    for (int j = 0; j < line.length(); j++) {
                            bufferedWriter.write(line.charAt(j));
                    }
                    bufferedWriter.write(System.lineSeparator());
            }
            else {
                System.out.println(line);
                blockNo++;
            }
            }
                catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    } else {
                        System.out.println("File writer is not initialized.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


//        out.writeInt(startindex);
//        out.writeInt(noOfBlock);
        // keep reading until "Over" is input 
//        while (!line.equals("Over"))
//        {
//            try
//            {
//                line = input.readUTF();
//                out.writeUTF(line);
//            }
//            catch(IOException i)
//            {
//                System.out.println(i);
//            }
//        }

        // close the connection 
        try
        {
            input.close();
            out.close();
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[]) throws IOException {
        Client client = new Client("127.0.0.1", 5000,3,2);
    }
} 
