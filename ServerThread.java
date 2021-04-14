import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerThread extends Thread{
    Socket client;
    DataInputStream fromClientRead;
    DataOutputStream toClientWrite;
    String basePath, credentialsPath, currentUser, UserPath;
    int ID;
    ServerThread(Socket c){
        //Receiving client socket as a parameter
        try {
           //Creating client instance variables
            ID = 1;
            client = c;
            basePath = "server";
            credentialsPath = basePath + "\\Credentials.txt";
            fromClientRead = new DataInputStream(c.getInputStream());
            toClientWrite = new DataOutputStream(c.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
            //Handling client Requests
            try {
                String process = fromClientRead.readUTF();
                String username = fromClientRead.readUTF();
                String password = fromClientRead.readUTF();
                boolean loggedIn = false;
                if (process.equals("~LOGIN")) loggedIn = login(new String[]{username, password});
                else if (process.equals("~REGISTER")) loggedIn = register(new String[]{username, password});
                if (!loggedIn){
                    toClientWrite.writeUTF("535 SMTP Authentication unsuccessful/Bad username or password");
                }
                else{ //Handling New User
                    toClientWrite.writeUTF("250");
                    while(true){
                        String choice = fromClientRead.readUTF();
                        if (choice.equalsIgnoreCase("send")){ //Handling New Mail
                            String RCV = fromClientRead.readUTF();
                            String rcvPath = basePath + "\\" + RCV;
                            ID = getRecvID(rcvPath);
                            toClientWrite.writeUTF("250");
                            fromClientRead.readUTF();
                            toClientWrite.writeUTF("354");
                            String msg = fromClientRead.readUTF();
                            FileWriter writeMsg = new FileWriter(rcvPath + "\\" + Integer.toString(ID) + ".txt");
                            updateID(rcvPath);
                            writeMsg.write(msg + "\n" + "FROM: " + username + "@server.com");
                            toClientWrite.writeUTF("250");
                            writeMsg.close();
                        }
                        else if(choice.equalsIgnoreCase("show mailbox")) { //Outputing Mailbox and mail
                            ID = getRecvID(UserPath);
                            toClientWrite.writeUTF(String.valueOf(ID));
                            int mailID= Integer.parseInt(fromClientRead.readUTF());
                            Scanner sendMsg = new Scanner(new File(UserPath + "\\" + Integer.toString(mailID) + ".txt"));
                            StringBuilder mail = new StringBuilder("");
                            while (sendMsg.hasNext()){
                                mail.append(sendMsg.nextLine() + '\n');
                            }
                            toClientWrite.writeUTF(mail.toString());
                            sendMsg.close();
                        }
                    }
                }
            }
            catch (SocketException Ignored){ //Client disconnection
                System.out.println("A Client has disconnected");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }
    public boolean login(String[] creds){ //Login Function
        try {
            File credsFile = new File(credentialsPath);
            Scanner getCreds = new Scanner(credsFile);
            StringBuilder users = new StringBuilder();
            while(getCreds.hasNextLine()){
                users.append(getCreds.nextLine()).append("\n");
            }
            String[] usersData = users.toString().split("\n");
            for (int i = 0; i < usersData.length; i++){
                if (creds[0].equals(usersData[i].split(" ")[0]) && creds[1].equals(usersData[i].split(" ")[1])){
                    currentUser = creds[0];
                    UserPath = basePath + "\\" + currentUser;
                    return createFolder(UserPath);
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }
    public boolean register(String[] creds){ //registration function
        try {
            FileWriter writeCreds = new FileWriter(credentialsPath, true);
            writeCreds.write(creds[0] + " " + creds[1] + "\n");
            writeCreds.close();
            UserPath = basePath + "\\" + creds[0];
            return createFolder(UserPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean createFolder(String path) { //creating folder for server & users
        try{
            File userFolder = new File(path);
            boolean created = userFolder.mkdir();
            if (created) System.out.println("File " + currentUser + " has been created");
            else System.out.println("File " + currentUser + " exists already");
            if (!new File(path + "\\IDs.txt").exists()){
                FileWriter IDs = new FileWriter(path + "\\IDs.txt");
                IDs.write("1");
                IDs.close();
            }
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public int getRecvID(String path){ //getting Receiver Email ID from Path
        File IDsFile = new File(path + "\\IDs.txt");
        try {
            Scanner IDs = new Scanner(IDsFile);
            int temp = Integer.parseInt(IDs.nextLine());
            IDs.close();
            return temp;
        } catch (FileNotFoundException|NoSuchElementException z){
            createFolder(path);
            return 1;
        }
    }
    public void updateID(String path){ //Setting New Email Id to User
        try {
            FileWriter IDs = new FileWriter(path + "\\IDs.txt");
            int newID = ID+1;
            IDs.write(Integer.toString(newID));
            IDs.close();
        } catch (FileNotFoundException e) {
            createFolder(path);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

