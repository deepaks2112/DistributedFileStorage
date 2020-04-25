import java.io.Serializable;

public class ResponsePacket implements Serializable {
    private String command;
    private long freeblocks;
    private int status;
    private byte[][] data;

    public ResponsePacket(String command,long freeblocks,int status){
        this.command=command;
        this.freeblocks=freeblocks;
        this.status=status;
        this.data=new byte[1][1024];
    }

    public ResponsePacket(String command,long freeblocks,int status,byte[][] data){
        this.command=command;
        this.freeblocks=freeblocks;
        this.status=status;
        this.data=data;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public long getFreeblocks() {
        return freeblocks;
    }

    public int getStatus() {
        return status;
    }
}
