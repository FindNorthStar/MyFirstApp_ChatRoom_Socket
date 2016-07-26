package com.mycompany.myfirstapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by FindNS on 2016/7/7.
 */
public class ClientAsyncTask extends AsyncTask<Integer, String, Void> {

    private Socket clientSocket;
    private ListView msgListView;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<Msg>();
    private Handler handler;
    private String savedusername;

    public ClientAsyncTask(Socket clientSocket, ListView msgListView, MsgAdapter adapter, List<Msg> msgList, Handler handler, String savedusername) {
        this.clientSocket = clientSocket;
        this.msgListView = msgListView;
        this.adapter = adapter;
        this.msgList = msgList;
        this.handler = handler;
        this.savedusername = savedusername;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        String receiveInfo = "";
        try {
            while (clientSocket.isConnected()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                receiveInfo = bufferedReader.readLine();
                if(receiveInfo.contains(" messagepublic")){
                    receiveInfo=receiveInfo.replace(" messagepublic","");
                    Message msg = new Message();
                    msg.what=4;
                    msg.obj=receiveInfo;
                    handler.sendMessage(msg);
                    /*此处可以考虑publish信息改变GUI*/
                }
                else if(receiveInfo.contains(" messageprivate")){
                    receiveInfo=receiveInfo.replace(" messageprivate","");
                    Message msg = new Message();
                    msg.what=5;
                    msg.obj=receiveInfo;
                    handler.sendMessage(msg);
                }
                else if(receiveInfo.contains("stop task")) {
                    Log.i("chatRoom close",receiveInfo);
                    break;
                }
                else{
                    Log.i("chatRoom","unknown message");
                }

            }
        } catch (IOException e) {
            publishProgress(e.getMessage());
            e.printStackTrace();
        }

        publishProgress("sys;Connection closed");
        return null;
    }

    protected void onProgressUpdate(String... progress) {
      /*  Toast.makeText(context, progress[0], Toast.LENGTH_SHORT).show();*/

        if(progress[0].contains("sys;")){
            String s=progress[0].replace("sys;","");
            Msg msg=new Msg(s,Msg.TYPE_SYS);
            msgList.add(msg);
        }
        else{
            Msg msg = new Msg(progress[0], Msg.TYPE_RECEIVED);
            msgList.add(msg);
        }
        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
        msgListView.setSelection(msgList.size()); // 将ListView定位到最后一行
    }


}
