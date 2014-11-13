package pack;

import java.io.*;
import java.net.Socket;

/**
 * Client connects to the command socket for sending commands to
 * the server. And to the transfer socket for transferring data.
 * @author ksolodovnik
 *
 */
public class FileTransferClient {
    /* client socket for command */
    static Socket commandSocket = null;
    /* client socket for data transferring */
    static Socket transferSocket = null;
    static OutputStream out = null;
    static InputStream in = null;
    static DataOutputStream dout = null;
    static BufferedOutputStream bos;
    static BufferedInputStream bis = null;
    /* host */
    static final String HOST = "localhost";
    /* client folder */
    static final String CLIENT_FOLDER = "/Users/ksolodovnik/IdeaProjects/FileTransferServer/ClientFiles";
    /* buffer size */
    static final int FILE_SIZE = 300;
    static  BufferedReader is = null;

    /**
     * Main method of client.
     * Opens two sockets: for command and for transferring data.
     * It shows a menu and waiting for user command.
     * @param args
     */
    public static void main(String[] args) {
        try {
            is = new BufferedReader(new InputStreamReader(System.in));
            commandSocket = new Socket(HOST, 8080);
            transferSocket = new Socket(HOST, 8081);
            while (true) {
                System.out.println("Enter: \n" +
                        "dir - returns a list of files \n" +
                        "put filename(with path) - uploads a file to the server folder \n" +
                        "get filename - downloads a file from server folder to the client folder \n" +
                        "bye - stops the client");
                String input = is.readLine();
                String command = input.substring(0, 3);
                String path = input.substring(3);
                if (input.equalsIgnoreCase("dir")) {
                    sendCommandToServer(input);
                    showFilesClient();
                } else if (command.equalsIgnoreCase("put")) {
                    sendCommandToServer(input);
                    uploadFile(path.substring(1));
                } else if (command.equalsIgnoreCase("get")) {
                    sendCommandToServer(input);
                    downloadFile(path.substring(1));
                } else if (input.equalsIgnoreCase("bye")) {
                    is.close();
                    transferSocket.close();
                    commandSocket.close();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Connection error. Server sockets were closed");
            e.printStackTrace();
        }catch (NullPointerException npe){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Connection was closed");
            System.exit(0);
        }finally {
            try{
                transferSocket.close();
                commandSocket.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

    /**
     * Sends command to the server
     * @param input - input command
     */
    static void sendCommandToServer(String input){
        try {
            dout = new DataOutputStream(commandSocket.getOutputStream());
            dout.writeUTF(input);
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try{
                dout.close();
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

    /**
     * Receives files from server folder,
     * which client can downloads.
     */
    static void showFilesClient(){
        try {
            in = commandSocket.getInputStream();
            byte[] readBuffer = new byte[1000];
            int num = in.read(readBuffer);
            if (num > 0) {
                String readLine = new String(readBuffer, 0, num);
                System.out.println(readLine);
            }
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try {
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Sends file to the server.
     * @param path - path to client file
     */
    static void uploadFile(String path){
        try {
            out = transferSocket.getOutputStream();
            File file = new File(path);
            FileInputStream fileStream = new FileInputStream(file);
            byte[] buff = new byte[(int) file.length()];
            bis = new BufferedInputStream(fileStream);
            bis.read(buff, 0, buff.length);
            System.out.println("Sending file " + path + " file size: " + file.length());
            out.write(buff, 0, buff.length);
            out.flush();
            System.out.println("Complete");
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try {
                bis.close();
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Receives file from server
     * @param filename - file name for downloading
     */
    static void downloadFile(String filename)throws IOException {
        try {
            in = transferSocket.getInputStream();
            byte[] buff = new byte[FILE_SIZE];
            String fileToDownload = CLIENT_FOLDER + "/" + filename;
            FileOutputStream fout = new FileOutputStream(fileToDownload);
            bos = new BufferedOutputStream(fout);
            int byteReader;
            do {
                byteReader = in.read(buff);
                fout.write(buff, 0, byteReader);
            } while (byteReader == FILE_SIZE);
            fout.close();
            System.out.println("File " + fileToDownload + " downloaded");
            //    bos.close();
        }catch (IOException e){
            System.out.println("Connection error. Socket is closed");
            e.printStackTrace();
            try{
                bos.close();
                in.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }
}
