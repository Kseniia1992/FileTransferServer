package pack;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author ksolodovnik
 * File Transfer Server supports multiple clients and listens to two tcp socket:
 * one for commands, one for data transfer.
 */
public class FileTransferServer {
    /* Server socket for command */
    static ServerSocket serverCommand = null;
    /* Server socket for transfer data */
    static ServerSocket serverTransfer = null;
    static Socket commandSocket = null;
    static Socket transferSocket = null;
    /* Counter for connections */
    static int numConnections = 0;

    /**
     * Starting of server.
     * Creates new thread for each connection
     */
    private void startServer(){
        try {
            serverCommand = new ServerSocket(8080);
            serverTransfer = new ServerSocket(8081);
            System.out.println("Server is started and waiting...");
            while (true){
                commandSocket = serverCommand.accept();
                transferSocket = serverTransfer.accept();
                numConnections ++;
                Connection conn = new Connection(commandSocket,transferSocket,numConnections,this);
                new Thread(conn).start();
                if(serverCommand.isClosed() && serverTransfer.isClosed()){
                    System.out.println("Server sockets were closed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection error");
            e.printStackTrace();
        } finally {
            try {
                serverTransfer.close();
                serverCommand.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        FileTransferServer server = new FileTransferServer();
        server.startServer();
    }
}
