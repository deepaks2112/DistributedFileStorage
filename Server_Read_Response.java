package distributedProject;

import java.net.*;
import java.io.*;

public class Server
{
    //initialize socket and input stream 
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;
    private DataOutputStream out     = null;
    // constructor with port 
    public Server(int port) throws IOException {
        // starts server and waits for a connection 
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");


            while (true){
                socket = server.accept();
                System.out.println("Client accepted");

                // takes input from the client socket
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());

                String line = "";



                try {

                    line = in.readUTF();
                    System.out.println(line);

                    String[] ans = line.split(" ");
                    int startIndex = Integer.parseInt(ans[0]);
                    int noOfBlocks = Integer.parseInt(ans[1]);
                    String path = null;
                    File file = null;
                    BufferedReader br = null;
                    for (int j = startIndex; j < startIndex + noOfBlocks; j++) {
                        path = "/home/vaibhav/Desktop/Database/Block" + String.valueOf(j);
                        file = new File(path);

                        br = new BufferedReader(new FileReader(file));
                        String st;
                        while ((st = br.readLine()) != null) {
                            out.writeUTF(st);
//                            System.out.println(st);
                        }
                        out.writeUTF("#Block " + String.valueOf(j) + " " + "is read.\n");

                    }
                    out.writeUTF("#completed");

                } catch (IOException i) {
                    System.out.println("Closing connection");
//                    socket.close();
//                    in.close();
                    System.out.println(i);
                }

                // System.out.println("Closing connection");

                // close connection


            }
        }
        catch(IOException i)
            {

                System.out.println(i);
            }



    }


    public static void main(String args[]) throws IOException {
        Server server = new Server(5000);
    }
} 
