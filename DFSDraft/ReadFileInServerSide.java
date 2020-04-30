import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class ReadFileInServerSide {
    public static void main(String[] args){
        String path="/home/deepak/JavaProjects/DFSDraft/src/Servers/S1/A.abc";
        Metadata md=new Metadata(path);
        md.fetchMetaData();
        md.readFromFile();
        String next=md.getNextfilename();
        byte[][] data=new byte[25][64*1024];
        data[0]=md.getContent();
        for(int i=0;i<24;i++){
            md=new Metadata(next);
            md.fetchMetaData();
            md.readFromFile();
            data[i+1]=md.getContent();
        }
        try{
            File fw= new File("./reconstructed/a.pdf");
            FileOutputStream fos=new FileOutputStream(fw);
            for(int i=0;i<25;i++){
                fos.write(data[i]);
            }
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
