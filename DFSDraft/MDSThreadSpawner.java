import java.net.ServerSocket;
import java.net.Socket;

public class MDSThreadSpawner {

    public static void main(String[] args){
        ServerSocket serverSocket=null;
        Socket socket=null;

        try{
            serverSocket=new ServerSocket(9001);
        }catch(Exception e){
            e.printStackTrace();
        }
        while(true){
            try{
                socket=serverSocket.accept();
                System.out.println("New connection from "+socket.getInetAddress().toString());
                new FinalMetadataServer(socket).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
