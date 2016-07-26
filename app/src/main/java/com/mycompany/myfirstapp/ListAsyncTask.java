package com.mycompany.myfirstapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/7/8.
 */
public class ListAsyncTask extends AsyncTask<Integer, String, Void> {

    private Socket clientSocket;
    private Handler handler;
    private String savedusername;


    private List<MyUser> userList=new ArrayList<MyUser>();
    private UserAdapter adapter;
    private ListView myListView;

    public ListAsyncTask(Socket clientSocket, ListView listView, UserAdapter adapter, List<MyUser> userList, Handler handler, String savedusername) {
            this.clientSocket = clientSocket;
            this.myListView = listView;
            this.adapter = adapter;
            this.userList = userList;
            this.handler = handler;
            this.savedusername = savedusername;
        }

    @Override
    protected Void doInBackground(Integer... params) {
        Message msg = new Message();
        msg.what = 2;
        handler.sendMessage(msg);
        String receiveInfo = "";
        try {
            while (clientSocket.isConnected()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                receiveInfo = bufferedReader.readLine();

                if (receiveInfo.contains(" friendslist")){
                    Log.i("chatRoom",receiveInfo);
                    //如果是更新好友列表，直接在异步任务里更新列表
                    String list=receiveInfo.replace(" friendslist","");
                    //列表不为空 更新GUI
                    if(!list.equals("")) {
                        publishProgress(list);
                    }
                }
                        /*else if(receiveInfo.contains(" messagepublic")){//得到群聊信息，传回去保存\
                            Log.i("chatRoom",receiveInfo);
                            String message=receiveInfo.replace(" messagepublic","");
                            Message msg1=new Message();
                            msg1.what = 6;
                            msg1.obj=message;
                            handler.sendMessage(msg1);
                        }
                        else if(receiveInfo.contains(" messageprivate")){//得到私聊信息，传回去保存
                            Log.i("chatRoom",receiveInfo);
                            String message=receiveInfo.replace(" messageprivate","");
                            Message msg1=new Message();
                            msg1.what = 5;
                            msg1.obj=message;
                            handler.sendMessage(msg1);
                        }*/
                else if(receiveInfo.contains("stop task")) {
                    Log.i("chatRoom",receiveInfo);
                    break;
                }
                else {
                    Log.i("chatRoom","unknown message："+receiveInfo);
                }
            }
        } catch (IOException e) {
            publishProgress(e.getMessage());
            e.printStackTrace();
        }
        //publishProgress("sys;Connection closed");
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        String names[]=progress[0].split(";");
        for(String name:names){
            MyUser user=new MyUser(name,R.drawable.female_on);
            userList.add(user);
        }
        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
        myListView.setSelection(0);
    }


}
