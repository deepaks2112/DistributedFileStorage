import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FreeList {
    private Metadata mt;
    private static final long DEFAULT=20;
    private static long freeblocks=DEFAULT;
    private final String STOREDATA="freelist.txt";
    private String BASEDIR="/home/deepak/JavaProjects/DFSTesting/ServerFiles/S1/files/";
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
                String []arr=line.trim().split(" ");
                this.freeblocks=Long.parseLong(arr[1].trim());
                status=this.mt.setFilename(arr[0].trim());
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
    public int updateData(){
        return this.updateData(this.mt.getFilename(),this.freeblocks);
    }
    public int updateData(long freeblocks){
        return this.updateData(this.mt.getFilename(),freeblocks);
    }
    public int updateData(String filename){
        return this.updateData(filename,this.freeblocks);
    }
    public int updateData(String filename,long freeblocks){
        this.mt.setFilename(filename);
        this.freeblocks=freeblocks;
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
        if(blocklength<=0)
            return 1; // May need to change this later.
        if(doAllocate&&blocklength>freeblocks){
            System.out.println("blocklength > freeblocks");
            return -1;
        }
        freeblocks-=((doAllocate?1:-1)*blocklength);
        this.mt.setFilename(mt.getFilename());
        this.mt.updateMetaData(doAllocate);
        while(blocklength>1&&!this.mt.getNextfilename().equals("null")){
            this.mt.setFilename(this.mt.getNextfilename());
            this.mt.updateMetaData(doAllocate);
            blocklength--;
        }
        // failure of half allocation may arise. Handle it later.
        if(blocklength>1){
            System.out.println("blocklength>1");
            return -1;
        }

        Metadata meta=new Metadata(this.mt.getFilename());
        this.mt.setFilename(this.mt.getNextfilename());
        meta.updateMetaData(endFileName,doAllocate);
        if(doAllocate&&this.updateData()==-1){
            System.out.println("Updation Failed");
            return -1;
        }
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
    public String getFilename(){
        return this.mt.getFilename();
    }
    public int setFilename(String filename){
        return this.mt.setFilename(filename);
    }
    // public void setPath(String path){
    //     BASEDIR=path;
    // }
}
