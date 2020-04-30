import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.lang.*;
public class FinalServer{
    private Socket socket = null;
    private ServerSocket server = null;
    private Socket mdsSocket=null;
    private FreeList fl=null;
    private final int SIZE=64*1024;
    private String path;
    private ObjectInputStream objIn=null;
    private ObjectOutputStream objOut=null;
    private ObjectInputStream mdsIn=null;
    private ObjectOutputStream mdsOut=null;
    private int port;
    private String mdsIp;
    private int mdsPort;
    private String SERVERSTORAGE="/home/deepak/JavaProjects/DFSDraft/src/Servers/S";



    FinalServer(String path,int port){
        this.path=path;
        this.port=port;
        this.mdsIp="127.0.0.1";
        this.mdsPort=9001;
        this.SERVERSTORAGE+=""+this.port%10+"/";
        try {
            mdsSocket = new Socket(mdsIp, mdsPort);
            mdsIn = new ObjectInputStream(mdsSocket.getInputStream());
            mdsOut = new ObjectOutputStream(mdsSocket.getOutputStream());
            server = new ServerSocket(this.port);
            this.fl=new FreeList(this.port,SERVERSTORAGE+"A.abc");
        }catch(Exception e){
            System.out.println("Error in constructor: "+e.toString());
            e.printStackTrace();
        }
    }
    public int allocate(int toAlloc){
        int st;
//        System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
        st=fl.reserve(toAlloc,true,"null");
        if(st==-1){
            System.out.println("Something Error 3");
            return st;
        }
//        System.out.println(fl.getFreeblocks()+" "+fl.getFilename()
//                +" "+fl.getNextfilename()+" "+fl.isInuse());
        return st;
    }
    public int deallocate(String startfile,long toFree){
        int st;
        FreeList fl=new FreeList(port,path+"A.abc");
        FreeList temp=new FreeList(port,startfile);
        FreeList fl1=new FreeList(port,startfile);
        st=fl.readFile();
        long free=fl.getFreeblocks();
        if(st==-1){
            System.out.println("Something Wrong1");
            return -1;
        }
//        System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
        st=fl1.reserve(toFree,false,""+fl.getFilename());
        if(st==-1){
            System.out.println("Something Wrong2");
            return -1;
        }
        fl=temp;
        st=fl.updateData(free+toFree);
//        System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
        return st;
    }

    public ResponsePacket writeInBlocks(RequestPacket request){
        int i=0;
        String filename=request.getFilename();
        int numOfBlocks=request.getNumOfBlocks();
        int indexOfBlock=request.getIndexOfBlock();
        byte[][] data=request.getData();
        if(numOfBlocks>fl.getFreeblocks()){
            // return response status -1;
            return new ResponsePacket(request.getCommand(),0,-1);
        }
        String file=fl.getFilename();
        FreeList fl1=new FreeList(port,file);
        if(allocate(numOfBlocks)==-1){
            // check this later.
            deallocate (file, numOfBlocks);
            return new ResponsePacket(request.getCommand(),0,-1);
        }

        // this is just for testing purpose.
        // testout=new DataOutputStream
        //(new FileOutputStream(new File(path+"test.dss")));
        System.out.println("Allocated successfully.");

        for(i=0;i<numOfBlocks&&!fl1.getFilename().equals("null");i++){
            if(fl.getmetadata().writeToFile(data[i])==-1){
                System.out.println("Some Error4");
                deallocate (file, numOfBlocks);
                return new ResponsePacket(request.getCommand(),0,-1);
            }

            //testout.write(data[i]); // for test

            if(fl1.setFilename(fl1.getNextfilename())==-1){
                System.out.println("Some Error2");
                deallocate (file, numOfBlocks);
                return new ResponsePacket(request.getCommand(),0,-1);
            }
        }
        System.out.println("Blocks inserted: "+i); // for debugging
        // create an entry in the MDS server.
        String index=String.valueOf(request.getIndexOfBlock());
        String numOfBlockString=String.valueOf(numOfBlocks);
        String nodeId=String.valueOf(port);
        System.out.print("Sending to MDS....");
        MDSPacket mdsPacket=new MDSPacket("putMapping",request.getFilename(), new MDS(index,numOfBlockString,nodeId, file));
        int st=-1;
//        JSONTest.putMapping(request.getFilename(),request.getIndexOfBlock(),numOfBlocks,String.valueOf(port),file);
        try {
            mdsOut.writeObject(mdsPacket);
            System.out.println("Sent");
            st=1;
        }catch(Exception e){
            System.out.println("Error sending request to MDS: "+e.toString());
            e.printStackTrace();
            st=-1;
        }
        return new ResponsePacket(request.getCommand(),0,st);
    }


    public ResponsePacket getNodeStatus(){
        fl=new FreeList(port,SERVERSTORAGE+"A.abc");
        fl.readFile();
        return new ResponsePacket("getStatus",fl.getFreeblocks(),0);
    }


    public ResponsePacket readFile(RequestPacket request){
        try {
            MDS mds = request.getMds();
            int startIndex = Integer.parseInt(request.getMds().getIndex());
            int noOfBlocks = Integer.parseInt(mds.getNum());
            String startingAddress = mds.getAddress();
            byte[][] data=new byte[noOfBlocks][64*1024];
            Metadata md;
            String next=startingAddress;
            for (int j = 0; j < noOfBlocks; j++) {
                // check for file name
//                File file = new File(path+String.valueOf(j));
//                FileInputStream fileInputStream=new
//                        FileInputStream(file);
                byte[] bytes=new byte[64*1024];
                md=new Metadata(next);
                md.fetchMetaData();
                md.readFromFile();
                next=md.getNextfilename();
                bytes=md.getContent();
//                fileInputStream.read(bytes,0,64*1024);
                data[j]=bytes;
            }
            return new ResponsePacket("readFile",0,0,data);
        } catch (Exception i) {
            System.out.println("Closing connection");
            System.out.println(i.toString());
            return new ResponsePacket("readFile",0,-1);
        }
    }

    private String getMD5(byte[] inputBytes){
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

    public int updateBlockServer(RequestPacket request){
        int status=-1;
        Metadata md;
        String next=request.getMds().getAddress();
        int[] offset=request.getOffset();
        byte[][] data=request.getData();
        int k=0;
        for(int i=0;i<=offset[offset.length-1];i++){
            md=new Metadata(next);
            md.fetchMetaData();
            next=md.getNextfilename();
            if(i==offset[k]){

                if(!md.getFilename().equals("null")) {
                    status=md.writeToFile(data[k]);
                    if(status==-1){
                        return status;
                    }
                }
                k++;
            }
        }

        return status;
    }


    public int deleteBlockServerUpdate(RequestPacket request){
        int status=-1;
        Metadata md = null;
        String next=request.getMds().getAddress();
        int[] offset=request.getOffset();
        int num=request.getBlocksToDelete();
        for(int i=0;i<=offset[0];i++){
            md=new Metadata(next);
            md.fetchMetaData();
            next=md.getNextfilename();
        }
        if(md!=null&&!md.getFilename().equals("null")) {
            status=deallocate (md.getFilename(), num);
            // send request to metadata server

            if(status>=0){
                try{
                    mdsOut.writeObject(new Object()); // send request to metadata server
                    MDSPacket mdsPacket=(MDSPacket)mdsIn.readObject();
                    if(!mdsPacket.isSuccess()){
                        return -1;
                    }
                }catch(Exception e){
                    System.out.println("Error occured: "+e.toString());
                    e.printStackTrace();
                    return -1;

                }
            }
        }
        return status;
    }




    public ResponsePacket updateFile(RequestPacket request){
        ResponsePacket response=null;
        String secondaryCommand=request.getSecondaryCommand();
        switch(secondaryCommand){
            case "deleteBlocks":{
                int st=deleteBlockServerUpdate(request);
                response=new ResponsePacket("updateFile:deleteBlocks",0,st);
                break;
            }

            case "updateBlocks":{
                int st=updateBlockServer(request);
                response=new ResponsePacket("updateFile:updateBlocks",0,st);
                break;
            }

            case "createBlocks":{
                response=writeInBlocks(request);
                response.setCommand("updateFile:createBlocks");
                break;
            }

            default:{
                response=new ResponsePacket("updateFile:unrecognisedCommand",0,-1);
                break;
            }
        }
        return response;
    }

    public void run(String[] args){
        while(true) {
            try {
                socket = server.accept();
                System.out.println("New connection from "+socket.getInetAddress().toString());

                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
                int eof=0;
                while(eof==0) {
                ResponsePacket resPckt;
//                    System.out.println("Listening for request packet... ");
                RequestPacket request = (RequestPacket) objIn.readObject();

                String command = request.getCommand();
                System.out.println("Received "+command);


                    switch (command) {
                        case "getStatus": {
                            // send back status
                            resPckt = getNodeStatus();
                            objOut.writeObject(resPckt);
                            System.out.println("Sent response.");
                            break;
                        }

                        case "copyBlocks": {
//                            System.out.println("Received copy request...");
                            resPckt = writeInBlocks(request);
                            objOut.writeObject(resPckt);
                            if (resPckt.getStatus() > 0) {
                                System.out.println("Successful.");
                            } else {
                                System.out.println("Unsuccessful.");
                            }
                            eof = 1;
                            break;
                        }

                        case "eof": {
                            break;
                        }

                        case "readFile": {
                            resPckt = readFile(request);
                            objOut.writeObject(resPckt);
                            break;
                        }

                        case "updateFile": {
                            resPckt = updateFile(request);
                            objOut.writeObject(resPckt);
                            break;
                        }

                        default: {
                            System.out.println("Unrecognised command: " + command);
                            objOut.writeObject(new ResponsePacket("UnrecognisedCommand", 0, -1));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error :" + e.toString());
                e.printStackTrace();
            }
        }
    }

//    public static void main(String[] args){
//        try{
//            server=new ServerSocket(5000);
//            socket=server.accept();
//            objIn=new ObjectInputStream(socket.getInputStream());
//            objOut=new ObjectOutputStream(socket.getOutputStream());
//
//            int eof=0;
//            ResponsePacket resPckt;
//            while(eof==0){
//                RequestPacket request=(RequestPacket)objIn.readObject();
//                String command=request.getCommand();
//
//                switch(command){
//                    case "getStatus":{
//                        // send back status
//                        resPckt=getNodeStatus();
//                        objOut.writeObject(resPckt);
////
//                        break;
//                    }
//
//                    case "copyBlocks":{
//                        resPckt=writeInBlocks(request);
//                        objOut.writeObject(resPckt);
//                        break;
//                    }
//
//                    case "eof":{
//                        eof=1;
//                    }
//
//                    case "readFile": {
//                        resPckt=readFile(request);
//                        objOut.writeOjbect(resPckt);
//                        break;
//                    }
//
//                    case "updateFile": {
//                        resPckt=updateFile(request);
//                        objOut.writeObject(resPckt);
//                        break;
//                    }
//
//                    default:{
//                        System.out.println("Unrecognised command: "+command);
//                        objOut.writeObject(new ResponsePacket("UnrecognisedCommand",0,-1));
//                    }
//                }
//            }
//        }catch(Exception e){
//            System.out.println("Error :"+e.toString());
//            e.printStackTrace();
//        }
//    }
    public static void main(String[] args){
        if(args.length==2) {
            String BASEPATH = args[0];
            int port = Integer.parseInt(args[1]);
            System.out.println("Starting server at " + BASEPATH + ", port: " + port);
            FinalServer finalServer = new FinalServer(BASEPATH, port);
            finalServer.run(args);
        }
    }
}
