package distributedProject;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MDS_Handler {

   private static class MDS{
       String no_of_blocks;
       String address_of_starting_block;
       String node_id;

       MDS(String no_of_blocks,String address_of_starting_block,String node_id){

                this.address_of_starting_block=address_of_starting_block;
                this.no_of_blocks=no_of_blocks;
                this.node_id=node_id;
       }

   }
   static HashMap<String,HashMap<String ,MDS>> hashMap=new HashMap<>();
   private static void read(String filename) throws IOException, ParseException {
       Object obj = new JSONParser().parse(new FileReader("/home/vaibhav/IdeaProjects/Codes/src/distributedProject/file.json"));
       JSONObject jo = (JSONObject) obj;
       JSONArray jsonArray= (JSONArray) jo.get(filename);

       HashMap<String ,MDS> hashMap1=new HashMap<>();
        for(int j=0;j<jsonArray.size();j++){
            JSONArray jsonArray1= (JSONArray) jsonArray.get(j);
            hashMap1.put(String.valueOf(j),new MDS(jsonArray1.get(0).toString(),jsonArray1.get(1).toString(),jsonArray1.get(2).toString()));
        }
        hashMap.put(filename, hashMap1 );
        HashMap<String ,MDS> hashMap2;
        hashMap2=hashMap.get(filename);
        for(String i:hashMap2.keySet()){
            System.out.println(hashMap2.get(i).address_of_starting_block);
            System.out.println(hashMap2.get(i).no_of_blocks);
            System.out.println(hashMap2.get(i).node_id);
        }

   }

    public static void main(String[] args) throws IOException, ParseException {
        read("myfile");
    }

}
