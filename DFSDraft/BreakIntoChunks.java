import java.util.*;
import java.io.*;
public class BreakIntoChunks{
	private String path;//="/home/deepak/JavaProjects/DFSDraft/Client/tmp/";
	private byte[] content;
    private final int SIZE=64*1024;
    DataInputStream in;
    DataOutputStream out;

    public BreakIntoChunks(String path){
    	this.path=path;
	}
	public int write(String filename){
		try{
			in=new DataInputStream(
				new FileInputStream(new File(path+filename)));
			content=new byte[SIZE];
			int i=0;
			while(in.read(content)!=-1){
				out=new DataOutputStream(new FileOutputStream(new File(path+"Block"+i+".dss")));
				out.write(content);
				out.close();
				Arrays.fill(content,(byte)0);
				i++;
			}
			return i;
		}
		catch(FileNotFoundException fne){
			System.out.println("Error: "+fne);
			return -1;
		}
		catch(IOException ioe){
			System.out.println("Error: "+ioe);
			return -1;
		}
		catch(Exception e){
			System.out.println("Error: "+e);
			return -1;
		}
	}
	public int read(int idx,String filename){
		try{
			int i=0;
			out=new DataOutputStream(new FileOutputStream(new File(path+filename)));
			content=new byte[SIZE];
			while(i<idx){
				in=new DataInputStream(
					new FileInputStream(new File(path+"Block"+i+".dss")));
				if(in.read(content)!=-1){
					out.write(content);
					Arrays.fill(content,(byte)0);
				}
				else{
					System.out.println("Can't read from the blocks");
					break;
				}
				i++;
			}
			return 1;
		}
		catch(FileNotFoundException fne){
			System.out.println("Error: "+fne);
			return -1;
		}
		catch(IOException ioe){
			System.out.println("Error: "+ioe);
			return -1;
		}
		catch(Exception e){
			System.out.println("Error: "+e);
			return -1;
		}
	}
//	public static void main(String[] args) {
//		BreakIntoChunks bic=new BreakIntoChunks();
//		int i=bic.write("ma2016.pdf");
//		if(i==-1){
//			System.out.println("Error Occured1");
//			System.exit(0);
//		}
//		i=bic.read(i,"test.jpg");
//		if(i==-1){
//			System.out.println("Error Occured2");
//			System.exit(0);
//		}
//	}
}