import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.lang.System.exit;

public class Client {
    public static void main(String[] args) {
        try {
            //connecting to the server
            InetAddress IP = InetAddress.getLocalHost();
            int PORT = 25;
            Scanner scan = new Scanner(System.in);
            Socket server = new Socket(IP, PORT);
            //Connected, Getting I/O Streams
            DataInputStream fromServerRead = new DataInputStream(server.getInputStream());
            DataOutputStream toServerWrite = new DataOutputStream(server.getOutputStream());
            //Got Streams
            System.out.println("Welcome to your mail service");
            System.out.println("What would you like to do");
            while(true){ //Making sure that I have sent login/register request
                System.out.println("1.Log into an existing account\n2.Register a new account");
                int choice = 0;
                try{
                    choice = scan.nextInt();
                }catch (InputMismatchException Ignored) {
                    continue;
                }

               /* scan.close();
                scan = new Scanner(System.in);*/
                // getting username and password from the client
                scan.nextLine();
                System.out.print("Enter your Username ->");
                String username = scan.nextLine();
                System.out.print("Enter your Password ->");
                String password  = scan.nextLine();
                if (choice == 1){
                    //Logging into an account
                    toServerWrite.writeUTF("~LOGIN");
                    toServerWrite.writeUTF(username);
                    toServerWrite.writeUTF(password);
                    break;
                }
                else if (choice == 2){
                    //Creating a new account
                    toServerWrite.writeUTF("~REGISTER");
                    toServerWrite.writeUTF(username);
                    toServerWrite.writeUTF(password);
                    break;
                }
                System.out.println("Please choose a valid operation using the numbers 1 or 2");
            }
            String response = fromServerRead.readUTF();
            boolean authenticated = !(response.split(" ")[0].equals("535"));
            if (!authenticated){
                System.out.println(response);
                System.out.println("Closing Connecting ...");
                server.close();
            }
            else {
                System.out.println("You are logged in");
                // showing operations list to users who are already logged in
                while (true){
                    System.out.println("Choose an operation");
                    System.out.print("~Send new\n~Show Mailbox\n~Quit\n->");
                    String choice = scan.nextLine();
                    if (choice.equalsIgnoreCase("send new")) sendNew(fromServerRead, toServerWrite, server);
                    else if (choice.equalsIgnoreCase("show mailbox")) showMail(fromServerRead, toServerWrite, server);
                    else if (choice.equalsIgnoreCase("quit")) exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendNew(DataInputStream DIS, DataOutputStream DOS, Socket s) {
        // sending emails and creating Mailbox at the side of the reciever
        try{
            Scanner inp = new Scanner(System.in);
            DOS.writeUTF("SEND");
            System.out.print("RCPT TO: ");
            DOS.writeUTF(inp.nextLine());
            System.out.println(DIS.readUTF());
            DOS.writeUTF("DATA");
            System.out.println(DIS.readUTF());
            StringBuilder msg = new StringBuilder();
            //inp.nextLine();
            for (String line = inp.nextLine(); !line.equalsIgnoreCase("."); line=inp.nextLine()){
                msg.append(line+ "\n");
            }
            DOS.writeUTF(msg.toString());
            System.out.println(DIS.readUTF());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void showMail(DataInputStream DIS, DataOutputStream DOS, Socket s){
        // showing the mailbox and letting user choose an email to show by chosing the ID of the required email
        try{
            Scanner inp = new Scanner(System.in);
            DOS.writeUTF("Show Mailbox");
            int numEmail = Integer.parseInt(DIS.readUTF()) - 1;
            for (int i = 0; i <= numEmail ; i++) {
                if (i==0){continue;}
                System.out.println("Email: " + i);
            }
            System.out.print("Choose an Email to print from "+numEmail+"->");
            DOS.writeUTF(String.valueOf(inp.nextInt()));
            System.out.println(DIS.readUTF());

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
