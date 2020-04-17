import java.util.*;
import java.io.*;
public class Test{
	private String path="./files/";
	private byte[] content;
    private final int SIZE=64*1024;
    private Metadata MT=null;
	public void write(Metadata mt,String nextfilename){
		if(mt==null)
			return;
		try{
			DataInputStream in=new DataInputStream(
				new FileInputStream(new File(path+mt.getFilename())));
			StringBuilder sbr=new StringBuilder();
			content=new byte[SIZE];
			while(in.read(content)!=-1){
				sbr.append(new String(content));
				Arrays.fill(content,(byte)0);
			}
			int res;
			res=mt.updateMetaData(nextfilename,false);
			if(res==-1){
				System.out.println("Some error happened: ");
				System.exit(0);
			}
			res=mt.writeToFile(sbr.toString().getBytes());
			if(res==-1){
				System.out.println("Some error happened: ");
				System.exit(0);
			}
		}
		catch(FileNotFoundException fne){
			System.out.println("Error: "+fne);
			System.exit(0);
		}
		catch(IOException ioe){
			System.out.println("Error: "+ioe);
			System.exit(0);
		}
		catch(Exception e){
			System.out.println("Error: "+e);
			System.exit(0);
		}
	}
	public void read(){
		try{
			DataInputStream in=new DataInputStream(
					new FileInputStream(new File(path+"Block1.dss")));
			StringBuilder sbr=new StringBuilder();
			content=new byte[SIZE];
			while(in.read(content)!=-1){
				sbr.append(new String(content));
				Arrays.fill(content,(byte)0);
			}
			System.out.println(sbr.toString());
		}
		catch(FileNotFoundException fne){
			System.out.println("Error: "+fne);
			System.exit(0);
		}
		catch(IOException ioe){
			System.out.println("Error: "+ioe);
			System.exit(0);
		}
		catch(Exception e){
			System.out.println("Error: "+e);
			System.exit(0);
		}
	}
	public void printInfo(Metadata mt){
		if(mt==null)
			return;
		int res;
		// if(mt.getNextfilename()==null){
			res=mt.fetchMetaData();
			if(res==-1){
				System.out.println("Some error happened: ");
				System.exit(0);
			}
		// }
		// if(mt.getContent()==null){
			res=mt.readFromFile();
			if(res==-1){
				System.out.println("Some error happened: ");
				System.exit(0);
			}
		// }
		System.out.println(mt.isInuse()+" "+mt.getFilename()
			+" "+mt.getNextfilename());
		// System.out.println(mt.getContent()!=null?new String(mt.getContent()):null);
	}
	public void update(Metadata mt){
		if(mt==null)
			return;
		mt.updateMetaData(false);
	}
	public static void main(String[] args) {
		
		Test tt=new Test();
		Metadata[]mt =new Metadata[10];
		for(int i=0;i<7;i++){
		// int i=new Scanner(System.in).nextInt();
			mt[i]=new Metadata("Block"+i+".dss");
			// tt.write(mt[i],(i<6)?"Block"+(i+1)+".dss":"null");
			// tt.printInfo(mt[i]);
		}
		// tt.write(mt[4],"Block5.dss");
		// tt.printInfo(mt[4]);
	////////////////////////////////////////////////////////////////
		// tt.write();
		// tt.read();
		// tt.printInfo();
		// tt.update();
		// tt.printInfo();
		///////////////////////////////////////
		allocate(6l);
		for(int i=0;i<7;i++)
			 tt.printInfo(mt[i]);
		allocate(1l);
		for(int i=0;i<7;i++)
			 tt.printInfo(mt[i]);
		deallocate("Block6.dss",1l);
		for(int i=0;i<7;i++)
			 tt.printInfo(mt[i]);
		deallocate("Block0.dss",6l);
		for(int i=0;i<7;i++)
			 tt.printInfo(mt[i]);
	}
	public static void allocate(long toAlloc){
		int st;
		FreeList fl=new FreeList("Block0.dss");// String is needed if storefile is not present.
		// st=fl.updateData(0);
		st=fl.readFile();
		// long toAlloc=7;
		if(st==-1){
			System.out.println("Something Wrong1");
			System.exit(0);
		}
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
		st=fl.reserve(toAlloc,true,"null");
		if(st==-1){
			System.out.println("Something Wrong2");
			System.exit(0);
		}
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
	}
	public static void deallocate(String startfile,long toFree){
		int st;
		FreeList fl=new FreeList("Block0.dss");
		FreeList temp=new FreeList(startfile);
		FreeList fl1=new FreeList(startfile);
		// st=fl.updateData(3);
		st=fl.readFile();
		long free=fl.getFreeblocks();
		if(st==-1){
			System.out.println("Something Wrong1");
			System.exit(0);
		}
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
		st=fl1.reserve(toFree,false,""+fl.getFilename());
		if(st==-1){
			System.out.println("Something Wrong2");
			System.exit(0);
		}
		fl=temp;
		st=fl.updateData(free+toFree);
		System.out.println(fl.getFreeblocks()+" "+fl.getFilename()+" "+fl.getNextfilename()+" "+fl.isInuse());
		// System.out.println(new String(fl.getContent()));
	}
}
