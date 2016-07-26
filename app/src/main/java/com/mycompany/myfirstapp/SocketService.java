package com.mycompany.myfirstapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketService extends Service {

    private static Socket socket;
    private static DataOutputStream outToServer;
    private int port;
    private InetAddress ip;
    private static boolean isConnect = false;
    //private Messenger mMessenger = null;
    private Handler mHandler;

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        SocketService.socket = socket;
    }

    public static boolean isConnect() {
        return isConnect;
    }

    public static void setIsConnect(boolean isConnect) {
        SocketService.isConnect = isConnect;
    }

    private ConnectionBinder cBinder = new ConnectionBinder();

    class ConnectionBinder extends Binder{

        public void startConnection() throws IOException {
            new Thread() {
                @Override
                public void run() {
                    if(isConnect == false) {
                        initSocket();
                    }
                    else{
                        Message message = new Message();
                        message.what = 2;
                        mHandler.sendMessage(message);
                    }
                }
            }.start();
        }

        public void getSocket(){
            Message message = new Message();
            message.what = 3;
            message.obj = socket;
            mHandler.sendMessage(message);
        }

        /**
         * 返回service实例
         * @return
         */
        public SocketService getService(){
            return SocketService.this;
        }

        public void sendMsg(String content){
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            pw.println(content);
            pw.flush();

        }

        public void setHandler(Handler handler){
            mHandler = handler;
        }

        public void setIP(String content){
            try {
                ip = InetAddress.getByName(content);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        public void setPort(String content){
            port = Integer.parseInt(content);
        }


    }

    public SocketService() {

    }

    @Override
    public void onCreate(){

    }

    private void initSocket() {
        Message message = new Message();

        socket = new Socket();
        SocketAddress socAddress = new InetSocketAddress(ip, port);
        try {
            socket.connect(socAddress,1000);
        } catch (IOException e) {
            message.what = 2;
            isConnect = false;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            message.what = 2;
            isConnect = false;
        }

        if(socket.isConnected()){
            message.what = 1;
            try {
                outToServer = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnect = true;
        }
        else{
            message.what = 2;
            isConnect = false;
        }
        mHandler.sendMessage(message);
    }

    /*class ReceiveBinder extends Binder{

    }*/

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        return cBinder;

    }
}
