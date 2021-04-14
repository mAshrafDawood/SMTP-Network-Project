import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    public static void main(String[] args) {
        try{
            //Initializing Server
            System.out.println("Initializing Server");
            ServerSocket server = new ServerSocket(25);
            //Creating login credentials.txt
            String credentialsPath = "server";
            File creds = new File(credentialsPath);
            // checking if the folder is created or not
            boolean created = creds.mkdir();
            credentialsPath += "\\Credentials.txt";
            File credsFile = new File(credentialsPath);
            if (credsFile.createNewFile()) {
                System.out.println("File created: " + credsFile.getName());
            } else {
                System.out.println("File already exists.");
            }
            while(true){
                //Accepting clients and creating a thread for each client
                Socket client = server.accept();
                System.out.println("A Client has connected");
                ServerThread clientThread = new ServerThread(client);
                clientThread.start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
