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
	private DataOutputStream clout=null;
	private DataOutputStream out=null;
	private DataOutputStream testout=null;
	private FreeList fl=null;
	private final int SIZE=64*1024;
	private static String path="/home/pramit/Desktop/DistributedFileSystem/ServerFiles/S1/files/";
	// constructor with port 
	public ServerInsert(int port) 
	{ 
		// starts server and waits for a connection 
		try{ 
			server = new ServerSocket(port); 
			fl=new FreeList("Block0.dss");
			// fl.setPath(path);
			// fl.getmetadata().setBaseDir(path);
			// fl
			fl.readFile();
			System.out.println("Server started"); 
			while(true){
				System.out.println("Waiting for a client ..."); 
				socket = server.accept(); 
				System.out.println("Client accepted"); 
				// takes input from the client socket 
				in = new DataInputStream(
					(socket.getInputStream())); 
				clout = new DataOutputStream(
					(socket.getOutputStream())); 
				long idx=0;
				byte[] bytes = new byte[SIZE];
				try
				{
					byte []temparr=(""+fl.getFreeblocks()).getBytes();
					for(int itr=0;itr<Math.min(temparr.length,SIZE);itr++)
						bytes[itr]=temparr[itr];
					clout.write(bytes);
					// clout.flush();
					Arrays.fill(bytes,(byte)0);
					System.out.println(fl.getFreeblocks()+"Sent"); 
					if(in.read(bytes)!=-1){
						idx=Long.parseLong(new String(bytes).trim());
						System.out.println("Blocks to write: "+ idx);
						// in.flush();
						if(idx>fl.getFreeblocks()){
							System.out.println("Requested Number of blocks not available");
							// error may arise
							socket.close(); 
							clout.close();
							in.close(); 
							continue;
						}
					}
					else{
						System.out.println("Some Error1");
						System.exit(0);
					}
					if(idx>0){
						long i=0;
						String file=fl.getFilename();
						FreeList fl1=new FreeList(file);
						this.allocate(idx);
						Arrays.fill(bytes,(byte)0);
						testout=new DataOutputStream(new FileOutputStream(new File(path+"test.dss")));
						while(in.read(bytes)!=-1&&!fl1.getFilename().equals("null")){
							if(fl.getmetadata().writeToFile(bytes)==-1){
								System.out.println("Some Error4");
								System.exit(0);
							}
							testout.write(bytes);
							Arrays.fill(bytes,(byte)0);
							if(fl1.setFilename(fl1.getNextfilename())==-1){
								System.out.println("Some Error2");
								System.exit(0);
							}
							i++;
						}
						System.out.println("Blocks inserted: "+i);
					}
				} 
				catch(IOException ioe) 
				{ 
					System.out.println(ioe); 
					System.exit(0);
				} 
				catch(Exception e){
					System.out.println(e);
				}
				System.out.println("Closing connection"); 
				// close connection 
				socket.close(); 
				in.close(); 
			} 
		}
		catch(IOException ioe) 
		{ 
			System.out.println(ioe); 
			System.exit(0);
		} 
		catch(Exception e){
			System.out.println(e);
		}
	} 
	public void allocate(long toAlloc){
		int st;
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
		st=fl.reserve(toAlloc,true,"null");
		if(st==-1){
			System.out.println("Something Error 3");
			System.exit(0);
		}
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
	}
	public static void main(String args[]) { 
		int ports[]={5000,5001,5002};
		ServerInsert server = new ServerInsert(ports[0]);
	} 
} 
