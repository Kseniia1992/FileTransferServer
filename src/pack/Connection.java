package pack;

import java.io.*;
import java.net.Socket;

/**
 * Initialize two sockets.
 * Receives commands from the Server, and transmits the data.
 * @author ksolodovnik
 */
public class Connection implements Runnable {
    DataInputStream inStream = null;
    FileOutputStream fout = null;
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    /* socket for commands */
    Socket commandSocket = null;
    /* socket for data transferring */
    Socket transferSocket = null;
    /* counter of connections */
    int id = 0;
    FileTransferServer server = null;
    InputStream in = null;
    OutputStream out = null;
    /* folder for receiving data */
    static final String SERVER_FOLDER = "/Users/ksolodovnik/IdeaProjects/FileTransferServer/ServerFiles";
    /* buffer size */
    static final int FILE_SIZE = 300;

    /**
     * Constructor
     * @param commandSocket - socket fot commands
     * @param transferSocket - socket for data transferring
     * @param id - id of connection
     * @param server - server object
     * @throws IOException
     */
    public Connection(Socket commandSocket, Socket transferSocket, int id, FileTransferServer server){
        this.commandSocket = commandSocket;
        this.transferSocket = transferSocket;
        this.id = id;
        this.server = server;
        System.out.println("Connection" + id + " with " + commandSocket + " and " + transferSocket);
    }

    @Override
    public synchronized void run() {
        receiveCommandFromClient();
    }

    /**
     * Receive command from client
     */
    void receiveCommandFromClient(){
        try {
            inStream = new DataInputStream(commandSocket.getInputStream());

            while (true) {
                String readCommand = inStream.readUTF();
                String command = readCommand.substring(0, 3);
                String file = readCommand.substring(3);
                if (command.equalsIgnoreCase("dir")) {
                    showFilesServer();
                } else if (command.equalsIgnoreCase("put")) {
                    receiveFile(file.substring(1));
                } else if (command.equalsIgnoreCase("get")) {
                    sendFile(file.substring(1));
                }
            }
        } catch (IOException eofe){
            System.out.println("Connection" + id + " is closed \n"
                    +"New connection is expected");
            try {
                inStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Command dir - shows files by the commandSocket
     */
    void showFilesServer(){
        try {
            out = commandSocket.getOutputStream();
            File dir = new File(SERVER_FOLDER);
            File[] list = dir.listFiles();
            String line;
            for (int i = 0; i < list.length; i++) {
                line = list[i].getName() + "\n";
                out.write(line.getBytes());
            }
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try {
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Command put - upload.
     * Receives file from client by the transferSocket.
     * @param filepath - path for saving file on server
     */
    void receiveFile(String filepath){
        try {
            in = transferSocket.getInputStream();
            byte[] buff = new byte[FILE_SIZE];
            File f = new File(filepath);
            String filename = f.getName();
            String fileToReceive = SERVER_FOLDER + "/" + filename;
            fout = new FileOutputStream(fileToReceive);
            bos = new BufferedOutputStream(fout);
            int byteReader;
            do {
                byteReader = in.read(buff);
                fout.write(buff, 0, byteReader);
            } while (byteReader == FILE_SIZE);
            fout.close();
            System.out.println("File " + fileToReceive + " uploaded");
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try {
                fout.close();
                bos.close();
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Command get - download.
     * Sends file to client by the transferSocket
     * @param filename - file name
     */
    void sendFile(String filename){
        try {
            out = transferSocket.getOutputStream();
            File file = new File(SERVER_FOLDER + "/" + filename);
            FileInputStream fileStream = new FileInputStream(file);
            byte[] buff = new byte[(int) file.length()];
            bis = new BufferedInputStream(fileStream);
            bis.read(buff, 0, buff.length);
            System.out.println("Sending file " + filename + " file size: " + file.length());
            out.write(buff, 0, buff.length);
            out.flush();
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try{
                bis.close();
                out.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }
}
