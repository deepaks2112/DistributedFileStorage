import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Metadata {
    private String filename;
    private String nextfilename;
    private boolean inuse;
    private final String BASEDIR="./dfs/";
    private byte[] content;
    private final int SIZE=64*1024;
    private final long fixedOffset=4*1024;
    private final int METADATASIZE=4*1024;

    Metadata(String filename){
        this.filename=filename;
    }


    public int writeToFile(byte[] data, long offset){
        int status=0;
        try {
            try (RandomAccessFile file = new RandomAccessFile(BASEDIR+filename,"rw")) {
                file.seek(offset);
                file.write(data);
            } finally {
                status = 1;

            }
        }catch(IOException ex){
            status=-1;
            ex.printStackTrace();
        }
        return status;
    }

    public int writeToFile(byte[] data){
        return writeToFile(data,fixedOffset);
    }

    public int readFromFile(long offset){
        int status=0;
        try{
            try(RandomAccessFile file=new RandomAccessFile(BASEDIR+filename,"r")) {
                file.seek(offset);
                byte[] buffer=new byte[SIZE];
                int bytes = file.read(buffer);
                if (bytes > 0) {
                    content = buffer;
                    status = 1;
                }else{
                    status = -1;
                }
            }finally {
                status=1;
            }
        }catch(IOException ex){
            status=-1;
            ex.printStackTrace();
        }
        return status;
    }

    public int readFromFile(){
        return readFromFile(fixedOffset);
    }

    public int fetchMetaData(){
        int status=0;
        try{
            RandomAccessFile fis=new RandomAccessFile(BASEDIR+filename,"r");
            byte[] buffer=new byte[METADATASIZE];
            int read=fis.read(buffer);
            if(read>0){
                byte valid=buffer[0];
                inuse= valid == 1;

                int len;
                for(len=1;buffer[len]!=0;len++);
                len=len-1;
                byte[] nextpathbytes=new byte[len];
                System.arraycopy(buffer, 1, nextpathbytes, 0, len);
                nextfilename=new String(nextpathbytes);
                status=1;
            }else{
                status=-1;
            }
        }catch(IOException ex){
            status=-1;
            ex.printStackTrace();
        }
        return status;
    }

    public int updateMetaData(String nextfilename,boolean inuse){
        int status=0;
        byte valid=(byte)(inuse?1:0);
        byte[] nextpath=nextfilename.getBytes();
        byte[] data=new byte[nextpath.length+2];
        int i;
        for(i=0;i<nextpath.length;i++){
            data[i+1]=nextpath[i];
        }
        data[0]=valid;
        data[nextpath.length+1]=0;
        try {
            try (RandomAccessFile file=new RandomAccessFile(BASEDIR+filename,"rw")) {
               file.seek(0);
               file.write(data);
            } finally {
                status = 1;
            }
        }catch(IOException ex){
            status=-1;
            ex.printStackTrace();
        }
        return status;
    }

    public String getFilename(){
        return this.filename;
    }

    public String getNextfilename() {
        return this.nextfilename;
    }

    public boolean isInuse(){
        return inuse;
    }

    public byte[] getContent(){
        return content;
    }

}
