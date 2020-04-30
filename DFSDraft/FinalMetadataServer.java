import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class FinalMetadataServer extends Thread{

    private static HashMap<String, ArrayList<MDS>> mappings=new HashMap<>();
    private static HashMap<String,String[]> blockHashes=new HashMap<>();
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public FinalMetadataServer(Socket skt){
        this.socket=skt;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<MDS> getMapping(String fileName){
        if(mappings.containsKey(fileName)) {
            return mappings.get(fileName);
        }
        return null;
    }

    public static void putMapping(String fileName, int blockIndex, int blockNum, String nodeId, String blockAddress){
        String index=String.valueOf(blockIndex);
        String num=String.valueOf(blockNum);
        MDS myMds=new MDS(index,num,blockAddress,nodeId);
        if(mappings.containsKey(fileName)){
            ArrayList<MDS> mdsList=mappings.get(fileName);
            mdsList.add(myMds);
            mappings.put(fileName,mdsList);
        }else{
            ArrayList<MDS> mdsList=new ArrayList<>();
            mdsList.add(myMds);
            mappings.put(fileName,mdsList);
        }
//        return 1;
    }

    public static String [] getBlockHashes(String fileName){
        if(blockHashes.containsKey(fileName)){
            return blockHashes.get(fileName);
        }
        return null;
    }

    private static void putBlockHashes(String fileName,int index,int num,String [] hashes){
        String str_idx=String.valueOf(index);
        String str_num=String.valueOf(num);
        if(blockHashes.containsKey(fileName)) {
            String[] existingHashes = blockHashes.get(fileName);
            if (existingHashes.length > index) {
                for (int i = 0; i < num; i++) {
                    existingHashes[i + index] = hashes[i];
                }
                blockHashes.put(fileName, existingHashes);
            } else {
                String[] newHashArray = new String[index + num];
                for (int i = 0; i < num; i++) {
                    newHashArray[i + index] = hashes[i];
                }
                for (int i = 0; i < existingHashes.length; i++) {
                    newHashArray[i] = existingHashes[i];
                }
                blockHashes.put(fileName, newHashArray);
            }
        } else{
            String[] newHashArray=new String[index+num];
            for(int i=0;i<num;i++){
                newHashArray[index+i]=hashes[i];
            }
            blockHashes.put(fileName,newHashArray);
        }
    }


    public static int updateMappings(String fileName, ArrayList<MDS> mdsList){
        mappings.put(fileName, mdsList);
        return 1;
    }

    public static int truncateMappings(String fileName,int index){
        int status=0;
        ArrayList<MDS> mdsList=mappings.getOrDefault(fileName,null);
        ArrayList<MDS> newMetadata=new ArrayList<>();
        if(mdsList!=null){
            for(MDS mds:mdsList){
                int start=Integer.parseInt(mds.getIndex());
                int last=start+Integer.parseInt(mds.getNum());
                if(index>=start&&index<last){
                    mds.setNum(String.valueOf(index-start));
                    newMetadata.add(mds);
                    break;
                }else{
                    newMetadata.add(mds);
                }
            }
            if(newMetadata.size()>0){
                mappings.put(fileName,newMetadata);
                status=1;
            }
        }
        return status;
    }

    @Override
    public void run() {
//        System.out.println("Started the MDS Handler");

        try{
//            ServerSocket serverSocket = new ServerSocket(9001);
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                try {


//                    Socket socket = serverSocket.accept();


                    MDSPacket mdsPacket;
                    boolean eof = false;
                    while (!eof) {
                        Object obj = in.readObject();
                        mdsPacket = (MDSPacket) obj;
                        String command = mdsPacket.getCommand();
                        MDS mds = mdsPacket.getMds();
                        switch (command) {
                            case "putMapping": {
                                String file = mdsPacket.getFile();
                                putMapping(file, Integer.parseInt(mds.getIndex()), Integer.parseInt(mds.getNum()), mds.getNode(), mds.getAddress());
                                System.out.println("Modified for " + file);
                                break;
                            }
                            case "getMapping": {
                                String file = mdsPacket.getFile();
                                System.out.println("Called get " + file);
                                ArrayList<MDS> mdsList = getMapping(file);
                                if (mdsList != null) {
                                    out.writeObject(mdsList);
                                }
                                break;
                            }
                            case "eof":
                                eof = true;
                                break;
                            case "truncateMapping": {
                                String file = mdsPacket.getFile();
                                System.out.println("Called truncateMapping");
                                int num = Integer.parseInt(mdsPacket.getMds().getNum());
                                truncateMappings(file, num);
                                ArrayList<MDS> mdsList = getMapping(file);
                                if (mdsList != null) {
                                    out.writeObject(mdsList);
                                }
                                break;
                            }
                        }
                    }
                    out.close();
                    in.close();
                    socket.close();
//                    serverSocket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }catch(Exception e) {
            e.printStackTrace();
        }

    }
}
