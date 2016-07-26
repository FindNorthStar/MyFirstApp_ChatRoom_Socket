package test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server serves multiple clients simultaneously
 * Created by FindNS@outlook.com on 2015/11/20.
 * @author Yue Zhao
 */
public class Server {

    //socket maintains the connection information of all clients
    private static List<Connect> connects = new ArrayList<Connect>();
    //receiveInfo maintains the message sent by clients
    private static String receiveInfo = new String();

    public static List<Connect> getConnects() {
        return connects;
    }

    public static void setConnects(List<Connect> connects) {
        Server.connects = connects;
    }

    public static String getReceiveInfo() {
        return receiveInfo;
    }

    public static void setReceiveInfo(String receiveInfo) {
        Server.receiveInfo = receiveInfo;
    }

    public Server() throws IOException, InterruptedException {
        //bind a port
        ServerSocket serverSocket = new ServerSocket(9000);
        System.out.println("Server start......waiting for connection......");

        while (true){
            //waiting for connection
            connects.add(new Connect(serverSocket.accept()));
            Connect currentConnect = connects.get(connects.size()-1);
            InetAddress inetAddress = currentConnect.getSocket().getInetAddress();
            //print IP of the client
            System.out.println(currentConnect.toString()+" is trying to connect. The IP is " + inetAddress);
            HandleAClient task = new HandleAClient(connects.get(connects.size()-1));
            //start a thread to serve a client
            new Thread(task).start();
            System.out.println(currentConnect.toString()+" has connected. The IP is " + inetAddress);
        }
    }

    /**
     * start server
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String args[]) throws IOException, InterruptedException {
        new Server();
    }
}
