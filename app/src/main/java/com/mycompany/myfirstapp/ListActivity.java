package com.mycompany.myfirstapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListActivity extends AppCompatActivity {

    private List<MyUser> userList=new ArrayList<MyUser>();
    private UserAdapter adapter;
    private ListView myListView;
    Socket socket = null;
    private Intent socketIntent;
    private ListAsyncTask listAsyncTask;
    String savedusername;
    private SocketService.ConnectionBinder connectionBinder;
    SharedPreferences sharedPreferences;

    private Handler chathHandler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 2:
                    /*socketSendMessage(savedusername);*/
                    break;
                case 3:
                    socket = ((Socket) msg.obj);
                    if (socket!=null) {
                        initConnect();
                    }
                    break;
                case 4://add friends
                    break;
                case 5://get chat message
                    //得到传回来的message，存储至数据库并且更新UI
                    /*String message1=msg.obj.toString();
                    savePrivate(message1);*/
                    break;
                case 6:
                    /*String message2=msg.obj.toString();
                    savePublic(message2);*/
                    break;
                default:
                    break;
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionBinder = (SocketService.ConnectionBinder) service;
            try {
                connectionBinder.setHandler(chathHandler);
                connectionBinder.getSocket();//建立连接
            } catch (Exception e) {

            }
        }

    };

    public void initConnect(){
        listAsyncTask =
                new ListAsyncTask(socket, myListView, adapter, userList, chathHandler, savedusername);
        listAsyncTask.execute();
        connectionBinder.sendMsg(savedusername);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedusername = sharedPreferences.getString("nameKey","qwesd");

        socketIntent = new Intent(getBaseContext(), SocketService.class);
        bindService(socketIntent,conn,BIND_AUTO_CREATE);
        //绑定服务

        initUsers();
        //初始化用户列表-群聊列表

        adapter=new UserAdapter(
                ListActivity.this,R.layout.user_item,userList);
        myListView=(ListView)findViewById(R.id.listView);
        myListView.setAdapter(adapter);
        //增加点击名字的响应函数
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user=userList.get(position);
                Toast.makeText(ListActivity.this, user.getUserName(),
                        Toast.LENGTH_SHORT).show();

                //jump to chat panel
                unbindService(conn);
                connectionBinder.sendMsg("stop task");//服务器返回stop task终止当前asynctask
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("username",user.getUserName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
    }

    private void initUsers(){
        MyUser user1 = new MyUser("多人群聊",R.drawable.aaa);
        user1.setRecentMsg("点击进入多人群聊");
        userList.add(user1);
    }

    @Override
    protected void onStart(){

        super.onStart();

        setContentView(R.layout.activity_list);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedusername = sharedPreferences.getString("nameKey","qwesd");

        socketIntent = new Intent(getBaseContext(), SocketService.class);
        bindService(socketIntent,conn,BIND_AUTO_CREATE);

        adapter=new UserAdapter(
                ListActivity.this,R.layout.user_item,userList);
        myListView=(ListView)findViewById(R.id.listView);
        myListView.setAdapter(adapter);
        //增加点击名字的响应函数
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user=userList.get(position);
                Toast.makeText(ListActivity.this, user.getUserName(),
                        Toast.LENGTH_SHORT).show();

                //jump to chat panel
                unbindService(conn);
                connectionBinder.sendMsg("stop task");//服务器返回stop task终止当前asynctask
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("username",user.getUserName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy(){
        //unbindService(conn);
        super.onDestroy();
    }

    /*public void savePublic(String message){
        *//*先将发送方，信息内容 提取出来*//*
        String infos[]=message.split(":");
        String name=infos[0].trim();
        String content=infos[1].trim();
        *//*更新UI
        * 效率低 可优化*//*
        ContentValues values = new ContentValues();
        *//*a是系统信息就去掉之前一部分*//*
        if(name.equals("sys")){
            userList.get(0).setRecentMsg(content);
            values.put("msg",content);
        }
        else{
            userList.get(0).setRecentMsg(message);
            values.put("msg",message);
        }
        values.put("msg_type",0);//信息类型都是收到的
        values.put("talking man","All");
        int seq=userList.get(0).getMessageSeq();
        values.put("seq",seq);
        userList.get(0).setMessageSeq(seq+1);
        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
        *//*存储至数据库*//*
        Uri uri = getContentResolver().insert(MsgProvider.CONTENT_URI, values);
        values.clear();

    }*/

    /*public void savePrivate(String message){
        *//*先将发送方，信息内容 提取出来*//*
        String infos[]=message.split(":");
        String name=infos[0].trim();
        String content=infos[1].trim();
        *//*更新UI
        * 效率低 可优化*//*
        ContentValues values = new ContentValues();
        int n = userList.size();
        MyUser user;
        int seq;
        *//*找到名字匹配的一个user类 更新对应UI*//*
        for(int i=0;i<n;i++) {
            user = userList.get(i);
            if (user.getUserName().equals(name)) {
                user.setRecentMsg(infos[1]);
                seq = user.getMessageSeq();
                values.put("seq",seq);
                user.setMessageSeq(seq + 1);
                adapter.notifyDataSetChanged();
                break;
            }
        }
        *//*存储至数据库*//*
        values.put("msg",content);
        values.put("talking man",name);
        values.put("msg_type",0);//信息类型都是收到的
        Uri uri = getContentResolver().insert(MsgProvider.CONTENT_URI, values);
        values.clear();

    }*/

}
