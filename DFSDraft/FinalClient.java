import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FinalClient {


    private static final String STOREDATA = "nextnode.txt";
    private final int BLOCKSIZE=64*1024;
    private Socket socket;
    private Socket mdsSocket;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;
    private ObjectOutputStream mdsOut;
    private ObjectInputStream mdsIn;
    private String path;
    private int blockNum;
    private DataInputStream in;
    private DataOutputStream out;
    private int start_Idx;

    public FinalClient(String path){
        this.path=path;
        try{
            System.out.println("Establishing connection with MDS...");
            mdsSocket = new Socket("127.0.0.1",9001);
            mdsOut = new ObjectOutputStream(mdsSocket.getOutputStream());
            mdsIn = new ObjectInputStream(mdsSocket.getInputStream());
            System.out.println("Established connection with MDS.");
        } catch(Exception e){
            System.out.println("Error connecting: "+e.toString());
            e.printStackTrace();
        }
    }


    private static String getMD5(byte[] inputBytes){
        try{
            MessageDigest md=MessageDigest.getInstance("MD5");
            byte[] messageDigest=md.digest(inputBytes);
            BigInteger no=new BigInteger(1,messageDigest);
            StringBuilder hashtext= new StringBuilder(no.toString(16));
            while(hashtext.length()<32){
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    public int createClient(String filepath, String filename){
        System.out.println("Breaking "+path+" into chunks...");
        System.out.println();
        BreakIntoChunks bic=new BreakIntoChunks(path);
        System.out.println("Finished.");
        int i=bic.write(filepath);
        blockNum=i;
        if(i==-1){
            System.out.println("Error Occured1");
            return -1;
        }

/////////////////////////////////////////////////
        int st=readFile(); // to read the number of the node to which value will be sent next in Round Robin Scheduling.
        if(st==-1){
            System.out.println("Error Occured3");
            return -1;
        }
///////////////////////////////////////////////////

        int[] ports=new int[]{5001,5002,5003};
        int j=0,iter=0;
        while(blockNum>0&&iter<ports.length){
            j=(st++)%ports.length;
            System.out.print("Sending "+blockNum+" packets to "+ports[j]+" node... ");
            sendPacket("127.0.0.1", ports[j],filepath,filename);
            System.out.println("Done.");
            iter++;
        }

///////////////////////////////////////////
        st= writeFile(st%ports.length); // to save the number of  the node to which value will be sent next in Round Robin Scheduling.
        if(blockNum>0)
            return -1;
        return st;
//////////////////////////////////////////
    }

    public int createClient(String filename){
        return createClient(filename,filename);
    }
    public int sendPacket(String address, int port,String filepath, String filename){

        byte [][]content;
        int idx=0;
        int num_of_blocks=0;
        RequestPacket rqsPckt;
        ResponsePacket rpsPckt;
        try{
            System.out.print("Connecting... ");
            socket = new Socket(address, port);
            //srvin  = new DataInputStream(socket.getInputStream());
            //out = new DataOutputStream(socket.getOutputStream());
            //testout=new DataOutputStream(new FileOutputStream(new File(path+"test.dss")));

            objOut=new ObjectOutputStream(socket.getOutputStream());
            objIn=new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected");

            rqsPckt=new RequestPacket("getStatus","",0,new byte [1][1]);
            objOut.writeObject(rqsPckt);
            rpsPckt=(ResponsePacket)objIn.readObject();
            idx=(int)rpsPckt.getFreeblocks();
            if(idx>0){
                System.out.println("Blocks available: "+ idx);
                num_of_blocks=Math.min(idx,blockNum);
                blockNum-=num_of_blocks;
            }
            else{
                System.out.println("Error Occured2");
                return -1;
            }
        }
        catch(Exception u){
            System.out.println(u.toString());
        }

        content=new byte[num_of_blocks][64*1024];
        for(int i=start_Idx;i<start_Idx+num_of_blocks;i++){
            try{
                in=new DataInputStream
                        (new FileInputStream(new File(path+"Block"+i+".dss")));
                try{
                    if(in.read(content[i-start_Idx])==-1){
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
        rqsPckt=new RequestPacket("copyBlocks",filename,num_of_blocks,content);
        try {
            System.out.print("Sending request packet... ");
            objOut.writeObject(rqsPckt);
            System.out.println("Sent.");
            rpsPckt=(ResponsePacket)objIn.readObject();
            System.out.print("Receiving response packet... ");
            if(rpsPckt.getStatus()<0){
                start_Idx-=num_of_blocks;
                blockNum+=num_of_blocks;
                System.out.println("Operation unsuccessful...");
                return -1;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        try
        {
            objOut.close();
            objIn.close();
            socket.close();
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
        return 0;
    }

    public int readFile(){
        int status=0;
        BufferedReader br;
        try{
            br=new BufferedReader
                    (new FileReader(new File(STOREDATA)));
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

    public int writeFile(int st){
        int status=0;
        try {
            FileWriter fw=new FileWriter(STOREDATA);
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

    public int readClient(String filename){
        int status=-1;

        return status;
    }

    class SortByIndex implements Comparator<MDS> {
        public int compare(MDS a, MDS b){
            return Integer.parseInt(a.getIndex())-Integer.parseInt(b.getIndex());
        }
    }

    public HashMap<MDS,ArrayList<Integer>> getNodeIndexToUpdate(MDS[] mapping,ArrayList<Integer> diffIdx){
        HashMap<MDS,ArrayList<Integer>> res=new HashMap<>();
        int[] diffIdxArr=new int[diffIdx.size()];
        for(int i=0;i<diffIdx.size();i++){
            diffIdxArr[i]=diffIdx.get(i);
        }
        Arrays.sort(mapping,new SortByIndex());
        Arrays.sort(diffIdxArr);
        int k=0;
        for(int i=0;i<diffIdxArr.length;i++){
            int start=Integer.parseInt(mapping[k].getIndex());
            int end=start+Integer.parseInt(mapping[k].getNum());
            if(diffIdxArr[i]>=start&&diffIdxArr[i]<end){
                ArrayList<Integer> tmp=res.get(mapping[k]);
                tmp.add(diffIdxArr[i]);
                res.put(mapping[k],tmp);
            }else{
                k+=1;
            }
        }
        return res;
    }

    public int updateClient(String fileName){
//        ArrayList<MDS> myMds=JSONTest.getMapping(fileName);
        int status=-1;
        MDSPacket mdsPacket=new MDSPacket("getHashes",fileName,new MDS("","","",""));
        try {
            mdsOut.writeObject(mdsPacket);
            MDSPacket mdsPacket1=(MDSPacket) mdsIn.readObject();
            String[] oldHashes=mdsPacket1.getHashes();
            File file=new File(path+fileName);
            long filelength=file.length();
            String[] newHashes=new String[(int)Math.ceil(filelength/BLOCKSIZE)];

            byte[][] filebytes=new byte[newHashes.length][64*1024];
            int read=1;
            FileInputStream fis=new FileInputStream(file);
            for(int i=0;i<newHashes.length&&read>0;i++){
                read=fis.read(filebytes[i]);
            }

            for(int i=0;i<filebytes.length;i++){
                newHashes[i]=getMD5(filebytes[i]);
            }

            mdsPacket=new MDSPacket("getMapping",fileName,new MDS("","","",""));
            mdsOut.writeObject(mdsPacket);

            mdsPacket1=(MDSPacket) mdsIn.readObject();

            ArrayList<MDS> mapping=mdsPacket1.getMapping();
            MDS[] mappingArr=new MDS[mapping.size()];
            for(int i=0;i<mapping.size();i++){
                mappingArr[i]=mapping.get(i);
            }



            if(oldHashes!=null){
                if(oldHashes.length>newHashes.length){
                    // new file is smaller in size
                    ArrayList<Integer> diffIdx=new ArrayList<>();
                    for(int i=0;i<newHashes.length;i++){
                        assert newHashes[i] != null;
                        if(!newHashes[i].equals(oldHashes[i])){
                            diffIdx.add(i);
                        }
                    }

                    HashMap<MDS, ArrayList<Integer>> nodeAndIdx=getNodeIndexToUpdate(mappingArr, diffIdx);
                    for(Map.Entry i: nodeAndIdx.entrySet()){
                        MDS node=(MDS) i.getKey();
                        @SuppressWarnings("unchecked")
                        ArrayList<Integer> idxs=(ArrayList<Integer>) i.getValue();
                        String nodeId=node.getNode();
                        String ip=nodeId.split(":")[0];
                        int port=Integer.parseInt(nodeId.split(":")[1]);
                        Socket tempSocket=new Socket(ip,port);
                        byte[][] data=new byte[idxs.size()][64*1024];
                        for(int j=0;j<idxs.size();j++){
                            data[j]=filebytes[idxs.get(j)];
                        }
                        RequestPacket req=new RequestPacket("updateFile",fileName,0,data,node,"updateBlocks");
                        ObjectOutputStream tempOut=new ObjectOutputStream(tempSocket.getOutputStream());
                        tempOut.writeObject(req);

                        ObjectInputStream tempIn=new ObjectInputStream(tempSocket.getInputStream());
                        ResponsePacket res=(ResponsePacket) tempIn.readObject();

                        tempOut.close();
                        tempSocket.close();
                        // done for each of the nodes
                    }


                    Arrays.sort(mappingArr,new SortByIndex());
                    int pos=-1,toDelete=-1,offset=-1;
                    for(int j=0;j<mappingArr.length;j++){
                        int start=Integer.parseInt(mappingArr[j].getIndex());
                        int end=start+Integer.parseInt(mappingArr[j].getNum());
                        if(newHashes.length>=start){
                            pos=j;
                            toDelete=end-newHashes.length;
                            offset=newHashes.length-start;
                            break;
                        }
                    }

                    RequestPacket req=new RequestPacket("updateFile",fileName,0,new byte[1][1],mappingArr[pos],"deleteBlocks",new int[]{offset},toDelete);
                    String ip=mappingArr[pos].getAddress().split(":")[0];
                    int port=Integer.parseInt(mappingArr[pos].getAddress().split(":")[1]);

                    Socket tempSocket=new Socket(ip,port);
                    ObjectOutputStream tempOut=new ObjectOutputStream(tempSocket.getOutputStream());
                    ObjectInputStream tempIn=new ObjectInputStream(tempSocket.getInputStream());

                    tempOut.writeObject(req);
                    ResponsePacket res=(ResponsePacket) tempIn.readObject();
                    int j=pos+1;
                    while(res.getStatus()>=0&&j<mappingArr.length){
                        toDelete=Integer.parseInt(mappingArr[j].getNum());
                        req=new RequestPacket("updateFile",fileName,0,new byte[1][1],mappingArr[j],"deleteBlocks",new int[]{0},toDelete);
                        j++;
                        tempOut.writeObject(req);
                        res=(ResponsePacket) tempIn.readObject();
                    }
                    if(j==mappingArr.length){
                        MDSPacket mdsPacket2=new MDSPacket("truncateMapping",fileName,mappingArr[pos]);
                        MDSPacket mdsPacket3=(MDSPacket) mdsIn.readObject();
                        if(mdsPacket3.isSuccess()){
                            return 1;
                        }else{
                            return -1;
                        }
                    }else{
                        return -1;
                    }

//                    RequestPacket req=new RequestPacket("updateFile",fileName,0,new byte[1][1],)
//                    for(int i=newHashes.length;i<oldHashes.length;i++){
//                        // deleteBlocks(); carefully add the block to freelist too
//                        int start=Integer.parseInt(mappingArr[k].getIndex());
//                        int end=start+Integer.parseInt(mappingArr[k].getNum());
//                        while(i>=start&&i<end){
//                            i++;
//                        }
//                        RequestPacket req=new RequestPacket("updateFile",fileName,0,new byte[1][1],new MDS(),"deleteBlocks");
//                    }
                }else if(newHashes.length>oldHashes.length){
                    // new file is larger in size
                    ArrayList<Integer> diffIdx=new ArrayList<>();
                    for(int i=0;i<oldHashes.length;i++){
                        assert newHashes[i] != null;
                        if(!newHashes[i].equals(oldHashes[i])){
                            diffIdx.add(i);
                        }
                    }

                    HashMap<MDS, ArrayList<Integer>> nodeAndIdx=getNodeIndexToUpdate(mappingArr, diffIdx);
                    for(Map.Entry i: nodeAndIdx.entrySet()){
                        MDS node=(MDS) i.getKey();
                        @SuppressWarnings("unchecked")
                        ArrayList<Integer> idxs=(ArrayList<Integer>) i.getValue();
                        String nodeId=node.getNode();
                        String ip=nodeId.split(":")[0];
                        int port=Integer.parseInt(nodeId.split(":")[1]);
                        Socket tempSocket=new Socket(ip,port);
                        byte[][] data=new byte[idxs.size()][64*1024];
                        for(int j=0;j<idxs.size();j++){
                            data[j]=filebytes[idxs.get(j)];
                        }
                        RequestPacket req=new RequestPacket("updateFile",fileName,0,data,node,"updateBlocks");
                        ObjectOutputStream tempOut=new ObjectOutputStream(tempSocket.getOutputStream());
                        tempOut.writeObject(req);

                        ObjectInputStream tempIn=new ObjectInputStream(tempSocket.getInputStream());
                        ResponsePacket res=(ResponsePacket) tempIn.readObject();
                        // done for each of the nodes
                    }

                    // for index i=oldHashes.length to newHashes.length-1
                    // allocate these to the freelist
                    start_Idx=oldHashes.length;
                    File file1=new File(path+"tmp/tmp");
                    FileOutputStream fos=new FileOutputStream(file1);
                    for(int j=oldHashes.length;j<newHashes.length;j++){
                        fos.write(filebytes[j]);
                    }
                    fos.close();
                    return createClient(path+"tmp/tmp",fileName);



                }else{
                    ArrayList<Integer> diffIdx=new ArrayList<>();
                    for(int i=0;i<newHashes.length;i++){
                        assert newHashes[i] != null;
                        if(!newHashes[i].equals(oldHashes[i])){
                            diffIdx.add(i);
                        }
                    }
//                    for(int i=0;i<diffIdx.size();i++){
//                        // updateTheBlock();
//                    }



                    HashMap<MDS, ArrayList<Integer>> nodeAndIdx=getNodeIndexToUpdate(mappingArr, diffIdx);
                    for(Map.Entry i: nodeAndIdx.entrySet()){
                        MDS node=(MDS) i.getKey();
                        @SuppressWarnings("unchecked")
                        ArrayList<Integer> idxs=(ArrayList<Integer>) i.getValue();
                        String nodeId=node.getNode();
                        String ip=nodeId.split(":")[0];
                        int port=Integer.parseInt(nodeId.split(":")[1]);
                        Socket tempSocket=new Socket(ip,port);
                        byte[][] data=new byte[idxs.size()][64*1024];
                        for(int j=0;j<idxs.size();j++){
                            data[j]=filebytes[idxs.get(j)];
                        }
                        RequestPacket req=new RequestPacket("updateFile",fileName,0,data,node,"updateBlocks");
                        ObjectOutputStream tempOut=new ObjectOutputStream(tempSocket.getOutputStream());
                        tempOut.writeObject(req);

                        ObjectInputStream tempIn=new ObjectInputStream(tempSocket.getInputStream());
                        ResponsePacket res=(ResponsePacket) tempIn.readObject();

                        tempOut.close();
                        tempSocket.close();
                        // done for each of the nodes
                    }
                }
            }else{
                status=-1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return status;
    }

    public int deleteClient(String filename){
        int status=-1;

        return status;
    }

    public void run(String command,String file){
        System.out.println(command+" on "+file);
        int status=-1;
        switch(command){
            case "create" : {
                System.out.println("To upload "+path+file);
                status=createClient(file);
                System.out.println("Successfully uploaded.");
                break;
            }

            case "read" : {
                status=readClient(file);
                break;
            }

            case "update" : {
                status=updateClient(file);
                break;
            }

            case "delete" : {
                status=deleteClient(file);
                break;
            }

            default: {
                System.out.println("Unrecognised command: "+command);
                break;
            }
        }
        if(status<0){
            System.out.println("Some error occured");
        }else{
            System.out.println("Operation successful");
        }
    }

    public void close(){
        MDSPacket mdsPacket=new MDSPacket("eof","",new MDS("","","",""));
        try{
            mdsOut.writeObject(mdsPacket);
            System.out.println("Sent eof signal.");
            mdsIn.close();
            mdsOut.close();
            mdsSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        if(args.length==2) {
            String path=args[0].substring(0,args[0].lastIndexOf("/")+1);
            String file=args[0].substring(args[0].lastIndexOf("/")+1);
            System.out.println("{\n\tOperation: "+args[1]+"\n\tFile: "+path+file+"\n}");
            String command=args[1];
            FinalClient finalClient = new FinalClient(path);
            System.out.println();
            finalClient.run(command,file);
            finalClient.close();
        }
    }
}
