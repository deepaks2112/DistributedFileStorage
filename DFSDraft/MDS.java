import java.io.Serializable;

public class MDS implements Serializable {
    private String index;
    private String num;
    private String address;
    private String node;

    MDS(String index_of_first_block,String no_of_blocks,String address_of_starting_block,String node_id){
        this.index=index_of_first_block;
        this.address=address_of_starting_block;
        this.num=no_of_blocks;
        this.node=node_id;
    }

    public String getAddress() {
        return address;
    }

    public String getIndex() {
        return index;
    }

    public String getNode() {
        return node;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num){
        this.num=num;
    }
}

