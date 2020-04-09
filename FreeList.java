import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FreeList {
    private Metadata mt;
    private static long freeblocks=20;
    private final int DEFAULT=20;
    private final String STOREDATA="freelist.txt";
    private final String BASEDIR="./files/";
    private final int SIZE=64*1024;
    private final long fixedOffset=4*1024;
    private final int METADATASIZE=4*1024;

    FreeList(String filename){
        mt=new Metadata(filename);
    }

    public int readFile(){
        int status=0;
        BufferedReader br;
        try{
            br=new BufferedReader(new FileReader(new File(BASEDIR+STOREDATA)));
            String line;
            if((line=br.readLine())!=null){
                String []arr=line.split(" ");
                this.freeblocks=Long.parseLong(arr[1]);
                status=this.mt.setFilename(arr[0]);
            }
            else{
                if(updateData(this.mt.getFilename(),freeblocks)==-1)
                    status=-1;
                else
                    status=1;
            }
        }
        catch(FileNotFoundException fne){
            if(updateData(this.mt.getFilename(),freeblocks)==-1)
                status=-1;
            else
                status=1;
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
    public int updateData(long freeblocks){
        return this.updateData(this.mt.getFilename(),freeblocks);
    }
    public int updateData(String filename){
        return this.updateData(filename,this.freeblocks);
    }
    public int updateData(String filename,long freeblocks){
        this.mt.setFilename(filename);
        int status=0;
        try {
            FileWriter fw=new FileWriter(BASEDIR+STOREDATA);
            fw.write(filename+" "+freeblocks);
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

    public int reserve(long blocklength, boolean doAllocate, String endFileName){
        if(doAllocate&&blocklength>freeblocks)
            return -1;
        freeblocks-=((doAllocate?1:-1)*blocklength);
        this.mt.setFilename(mt.getFilename());
        this.mt.updateMetaData(true);
        while(blocklength>1&&!this.mt.getNextfilename().equals("null")){
            this.mt.setFilename(this.mt.getNextfilename());
            this.mt.updateMetaData(doAllocate);
            blocklength--;
        }
        if(blocklength>1)
            return -1;
        Metadata meta=new Metadata(this.mt.getFilename());
        this.mt.setFilename(this.mt.getNextfilename());
        meta.updateMetaData(endFileName,doAllocate);
        return 1;
    }


    public long getFreeblocks(){
        return this.freeblocks;
    }
    public Metadata getmetadata(){
        return this.mt;
    }

    public String getNextfilename() {
        return ""+this.mt.getNextfilename();
    }

    public boolean isInuse(){
        return this.mt.isInuse();
    }

    public byte[] getContent(){
        return this.mt.getContent();
    }
}