package DFS;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FinalClientUpdated {


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
    private ArrayList<MDS> list=new ArrayList<>();

    public FinalClientUpdated(String path){
        this.path=path;
        try{
            mdsSocket = new Socket("127.0.0.1",9001);
            mdsOut = new ObjectOutputStream(mdsSocket.getOutputStream());
            mdsIn = new ObjectInputStream(mdsSocket.getInputStream());
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
//        BreakIntoChunks bic=new BreakIntoChunks();
//        int i=bic.write(filepath);
//        blockNum=i;
//        if(i==-1){
//            System.out.println("Error Occured1");
//            return -1;
//        }

/////////////////////////////////////////////////
        int st=readFile(); // to read the number of the node to which value will be sent next in Round Robin Scheduling.
        if(st==-1){
            System.out.println("Error Occured3");
            return -1;
        }
///////////////////////////////////////////////////

        int[] ports=new int[]{5000,5001,5002};
        int j=0,iter=0;
        while(blockNum>0&&iter<ports.length){
            j=(st++)%ports.length;
            sendPacket("127.0.0.1", ports[j],filepath,filename);
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
            socket = new Socket(address, port);
            //srvin  = new DataInputStream(socket.getInputStream());
            //out = new DataOutputStream(socket.getOutputStream());
            //testout=new DataOutputStream(new FileOutputStream(new File(path+"test.dss")));
            objIn=new ObjectInputStream(socket.getInputStream());
            objOut=new ObjectOutputStream(socket.getOutputStream());
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
            objOut.writeObject(rqsPckt);
            rpsPckt=(ResponsePacket)objIn.readObject();
            if(rpsPckt.getStatus()==-1){
                start_Idx-=num_of_blocks;
                blockNum+=num_of_blocks;
                return -1;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        try
        {
            out.close();
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
                    (new FileReader(new File(path+STOREDATA)));
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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int readClient(String filename) throws IOException, ClassNotFoundException {
        int status=-1;

        //client = new Client("127.0.0.1", 5000,filename,null,0);
        list=new ArrayList<>();

        MDS mds=new MDS("","","","");
        MDSPacket mdsPacket=new MDSPacket("getMapping",filename,mds);
        mdsOut.writeObject( mdsPacket);

        list= (ArrayList<MDS>) mdsIn.readObject();
        if(list.size()==0){
            System.out.println("There is no such file in the database");
            return -1;
        }
        HashMap<String,byte[][]> hashMap=new HashMap<>();
        int ff=0;


        for(int j=0;j<list.size();j++){
            ff=ff+Integer.parseInt(list.get(j).getNum());
            // client1 = new Client( list.get(j).node_id[0],Integer.parseInt(list.get(j).node_id[1] ),"",list.get(j),1);
            Socket tmpSocket=new Socket(list.get(j).getNode().split(":")[0],
                    Integer.parseInt(list.get(j).getNode().split(":")[1] ));
            ObjectOutputStream tmpOut=new ObjectOutputStream(tmpSocket.getOutputStream());
            ObjectInputStream tmpIn=new ObjectInputStream(tmpSocket.getInputStream());
            RequestPacket req=new RequestPacket("readFile",filename,0,new byte[1][1],list.get(j));
            tmpOut.writeObject(req);
            ResponsePacket res=(ResponsePacket) tmpIn.readObject();
            //MDS mds=res.getMds();
            hashMap.put(list.get(j).getIndex(),res.getData());
        }
        System.out.println("received");
        //byte[] bytes=new byte[64*1000*ff];
        List<byte[][]> lis=new ArrayList<>();
        int cc=0;
        for(int j=0;j<ff;j++){
            if(hashMap.get(j)!=null){
                //  for(int i=0;i<hashMap.get(j).length;i++){
// bytes[j*64*1024*j+i]=hashMap.get(j)[i];
                lis.add(cc,hashMap.get(j));
                cc++;
                //  }
            }
            System.out.println();
        }
//   for(int i=0;i<ff*64*1000;i++){
        //     System.out.print((char)bytes[i]);
        //}
        File file=new File("/home/vaibhav/Desktop/ClientDatabase/"+filename);
        writeByte(lis,file);
        System.out.println("--------------------------------------------------------------------------");
        return  1;
    }
    static void writeByte(List<byte[][]>  lis,File file)
    {
        try {

            OutputStream os = new FileOutputStream(file);
            for(int j=0;j<lis.size();j++){
                for(int i=0;i<lis.get(j).length;i++) {
                    os.write(lis.get(j)[i]);
                }
            }
            System.out.println("Successfully" + " byte inserted");
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    public void run(String[] args) throws IOException, ClassNotFoundException {
        if(args.length==4){
            String command=args[2];
            String file=args[3];
            int status=-1;
            switch(command){
                case "create" : {

                    status=createClient(file);
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
                    System.exit(0);
                    break;
                }
            }
            if(status<0){
                System.out.println("Some error occured");
            }else{
                System.out.println("Operation successful");
            }
        }

    }
}
