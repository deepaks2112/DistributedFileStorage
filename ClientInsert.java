import java.net.*; 
import java.io.*; 
import java.lang.*;
import java.util.*;

public class ClientInsert 
{
    private Socket socket = null; 
    private DataInputStream in = null; 
    private DataInputStream srvin = null; 
    private DataOutputStream out = null; 
    private DataOutputStream testout=null;
    private static final String STOREDATA="nodeselect.txt";
    private static long blockNum=0;
    private static long start_Idx=0;
    private final int SIZE=64*1024;
    private static final String path="/home/pramit/Desktop/DistributedFileSystem/ClientFiles/";

    public ClientInsert(String address, int port){

        byte []content=new byte[SIZE];
        long idx=0, num_of_blocks=0;
        try{
            socket = new Socket(address, port); 
            srvin  = new DataInputStream(socket.getInputStream()); 
            out = new DataOutputStream(socket.getOutputStream()); 
            testout=new DataOutputStream(new FileOutputStream(new File(path+"test.dss")));
            System.out.println("Connected"); 
            if(srvin.read(content)!=-1){
                idx=Long.parseLong(new String(content).trim());
                System.out.println("Blocks available: "+ idx);
                num_of_blocks=Math.min(idx,blockNum);
                blockNum-=num_of_blocks;
            }
            else{
                System.out.println("Error Occured2");
                System.exit(0);
            }
            Arrays.fill(content,(byte)0);
            byte []temparr=(""+num_of_blocks).getBytes();
            for(int itr=0;itr<Math.min(temparr.length,SIZE);itr++)
                content[itr]=temparr[itr];
            out.write(content);
            // out.flush();
            Arrays.fill(content,(byte)0);
        }
        catch(UnknownHostException u){
            System.out.println(u); 
        }
        catch(IOException ioe){
            System.out.println(ioe); 
        }

        for( long i=start_Idx;i<start_Idx+num_of_blocks;i++){
            try{
                in=new DataInputStream(new FileInputStream(new File(path+"Block"+i+".dss")));
                try{
                    if(in.read(content)!=-1){
                        out.write(content);
                        // out.flush();
                        testout.write(content);
                        // testout.flush();
                        Arrays.fill(content,(byte)0);
                    }
                    else{
                        System.out.println("Can't read from the block: "+i);
                        break;
                    }
                }
                catch(IOException ioe){
                    System.out.println(ioe); 
                }
                in.close();
            }
            catch(IOException ioe){
                System.out.println(ioe); 
            }
        }
        start_Idx+=num_of_blocks;
        try
        {

            out.close(); 
            socket.close(); 
        }
        catch(IOException ioe){
            System.out.println(ioe); 
        }
    }
  
    public static void main(String args[]) 
    {
        BreakIntoChunks bic=new BreakIntoChunks();
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter the path of a valid file: ");
        int i=bic.write(sc.nextLine());
        blockNum=i;
        if(i==-1){
            System.out.println("Error Occured1");
            System.exit(0);
        }

        int st=readFile();
        if(st==-1){
            System.out.println("Error Occured3");
            System.exit(0);
        }
        int ports[]={5000,5001,5002};
        ClientInsert client[]=new ClientInsert[ports.length];
        int j=0;
        while(blockNum>0){
            j=(st++)%ports.length;
            client[j] = new ClientInsert("127.0.0.1", ports[j]);
        }
        writeFile(st%ports.length);
        // client = new ClientInsert("127.0.0.1", 5000,2,3,"/home/pramit/Desktop/NewFiles/"); 
    }
    public static int readFile(){
        int status=0;
        BufferedReader br;
        try{
            br=new BufferedReader(new FileReader(new File(path+STOREDATA)));
            String line;
            if((line=br.readLine())!=null){
                status=Integer.parseInt(line.trim());
            }
            else{
                status=-1;
                System.out.println("File is empty");
            }
        }
        catch(FileNotFoundException fne){
            status=-1;
            fne.printStackTrace();
        }
        catch(IOException ioe){
            status=-1;
            ioe.printStackTrace();
        }
        catch(Exception e){
            status=-1;
            e.printStackTrace();
        }
        return status;
    }
    public static int writeFile(int st){
        int status=0;
        try {
            FileWriter fw=new FileWriter(path+STOREDATA);
            fw.write(st+"\n");
            fw.close();
            status=1;
        }catch(IOException ex){
            status=-1;
            ex.printStackTrace();
        }
        catch(Exception e){
            status=-1;
            e.printStackTrace();
        }
        return status;
    }
} 
///home/pramit/Desktop/Missllennious/footback.jpg
