import java.io.Serializable;
import java.util.ArrayList;

public class MDSPacket implements Serializable {
    private String command;
    private String file;
    private MDS mds;
    private String[] hashes;
    private ArrayList<MDS> mapping;
    private boolean success;

    public MDSPacket(String command, String file, MDS mds){
        this.command=command;
        this.mds=mds;
        this.file=file;
        this.mapping=new ArrayList<>();
        this.hashes=new String[0];
        this.success=true;
    }

    public MDSPacket(String command, String file, ArrayList<MDS> mapping,boolean success){
        this.command=command;
        this.mds=new MDS("","","","");
        this.file=file;
        this.mapping=mapping;
        this.hashes=new String[0];
        this.success=success;
    }

    public MDSPacket(String command, String file, String[] hashes,boolean success){
        this.command=command;
        this.mds=new MDS("","","","");
        this.file=file;
        this.mapping=new ArrayList<>();
        this.hashes=hashes;
        this.success=success;
    }

    public boolean isSuccess() {
        return success;
    }

    public String[] getHashes() {
        return hashes;
    }

    public ArrayList<MDS> getMapping() {
        return mapping;
    }

    public MDS getMds() {
        return mds;
    }

    public String getCommand() {
        return command;
    }

    public String getFile() {
        return file;
    }
}
