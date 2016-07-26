package com.mycompany.myfirstapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView msgListView;
    private EditText inputText;
    private Button send;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<Msg>();
    String savedusername;
    String targetName;
    Socket socket = null;
    private Intent socketIntent;
    private ClientAsyncTask clientAsyncTask;
    SharedPreferences sharedPreferences;
    int seq;
    private SocketService.ConnectionBinder connectionBinder;

    private Handler chathHandler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 3:
                    socket = ((Socket) msg.obj);
                    if (socket!=null) {
                        initConnect();
                    }
                    break;
                case 4://public
                    dealPublic(msg.obj.toString());
                    break;
                case 5://private
                    dealPrivate(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    public void initConnect(){
        clientAsyncTask =
                new ClientAsyncTask(socket, msgListView, adapter, msgList, chathHandler, savedusername);
        clientAsyncTask.execute();
        if(targetName.equals("All")) {
            connectionBinder.sendMsg("enter the room");//建立连接后发送进入群聊信息
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        seq=0;
        targetName=getIntent().getStringExtra("username");

        if(targetName.equals("多人群聊")) {
            targetName = targetName.replace("多人群聊", "All");
        }

        setTitle(targetName);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedusername = sharedPreferences.getString("nameKey","qwesd");

        socketIntent = new Intent(getBaseContext(), SocketService.class);

        bindService(socketIntent,conn,BIND_AUTO_CREATE);
        //绑定服务
        //initMsg();

        //setupUI
        setupUI(findViewById(R.id.msg_list_view));
        adapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgListView = (ListView) findViewById(R.id.msg_list_view);
        msgListView.setAdapter(adapter);
        //发送消息响应事件
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "don't press me", Toast.LENGTH_SHORT).show();
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                    msgListView.setSelection(msgList.size()); // 将ListView定位到最后一行
                    inputText.setText(""); // 清空输入框中的内容
                    connectionBinder.sendMsg(targetName+";"+savedusername+":"+content);
                }

            }
        });

    }


    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.

        view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(ChatActivity.this);
                return false;
            }

        });
    }

    /*public void initMsg(){
        Cursor c = getContentResolver().query(MsgProvider.CONTENT_URI, null, null, null, "seq");

        if (c.moveToFirst()) {
            do{
                String target=c.getString(c.getColumnIndex("talking man"));
                if(target.equals(targetName)){
                    String msgContent=c.getString(c.getColumnIndex("msg"));
                    updateMsgUI(msgContent,Msg.TYPE_RECEIVED);
                *//*删除展示的条目*//*
                }
               *//* Toast.makeText(this, msg,
                        Toast.LENGTH_SHORT).show();*//*
            } while (c.moveToNext());
        }
    }*/

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private ServiceConnection conn = new ServiceConnection() {

        /*
         * 服务解除绑定时调用
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        /*
         * 服务成功绑定时调用
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionBinder = (SocketService.ConnectionBinder) service;
            try {
                connectionBinder.setHandler(chathHandler);
                connectionBinder.getSocket();
            } catch (Exception e) {

            }
        }

    };

    @Override
    protected void onDestroy(){
        /*if(targetName.equals("All")) {
            connectionBinder.sendMsg("enter the room");//建立连接后发送进入群聊信息
        }
        connectionBinder.sendMsg(targetName+";"+savedusername+":leaves the chat");*/
        connectionBinder.sendMsg("stop task");//stop asynctask

        unbindService(conn);

        super.onDestroy();
    }

    void dealPublic(String receiveMessage){
        //如果当前是群聊，展示，否则，存储
        //if(targetName.equals("All")){
            String s[]=receiveMessage.split(":");
            String name=s[0].trim();
            if(name.equals("sys"))
                updateMsgUI(s[1].trim(),Msg.TYPE_SYS);
            else
                updateMsgUI(receiveMessage,Msg.TYPE_RECEIVED);
        /*}
        else {
            ContentValues values = new ContentValues();
            values.put("msg",receiveMessage);
            values.put("msg_type",Msg.TYPE_RECEIVED);
            values.put("seq",seq);
            values.put("talking man","All");//将发送方设置为ALL 可能有问题
            seq++;
            //Uri uri = getContentResolver().insert(MsgProvider.CONTENT_URI, values);

        }*/
    }

    void dealPrivate(String receiveMessage){
        String items[]=receiveMessage.split(":");
        String sender=items[0];
        String content=items[1];
        if(targetName.equals(sender)) {
            updateMsgUI(content,Msg.TYPE_RECEIVED);
        }
        /*else{
            ContentValues values = new ContentValues();
            values.put("msg",receiveMessage);
            values.put("msg_type",Msg.TYPE_RECEIVED);
            values.put("seq",seq);
            values.put("talking man",sender);
            seq++;
            Uri uri = getContentResolver().insert(MsgProvider.CONTENT_URI, values);
        }*/
    }

    void updateMsgUI(String message,int type){
        Msg msg = new Msg(message, type);
        msgList.add(msg);
        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
        msgListView.setSelection(msgList.size()); // 将ListView定位到最后一行
    }

}
