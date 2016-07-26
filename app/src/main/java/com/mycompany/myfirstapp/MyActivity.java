package com.mycompany.myfirstapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    private EditText enterIP;
    private EditText enterPort;
    private EditText enterName;
    private Button loginButton;
    private String ip;
    private String port;
    private String userName;

    private Socket socket;
    private ClientAsyncTask clientAsyncTask;

    private SocketService.ConnectionBinder connectionBinder;
    private Intent socketIntent;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Name = "nameKey";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main_activity.xml file

        setContentView(R.layout.activity_my);//读进说明,形成界面
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);//工具栏,XML定义
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);//浮动按钮
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        enterIP = (EditText) findViewById(R.id.enter_IP);
        enterPort = (EditText) findViewById(R.id.enter_Port);
        enterName = (EditText) findViewById(R.id.enter_Name);

        loginButton = (Button) findViewById(R.id.sign_in_button);

        socketIntent = new Intent(this, SocketService.class);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {


        ip = enterIP.getText().toString();
        port = enterPort.getText().toString();
        userName = enterName.getText().toString();

        if (ip.equals("") || port.equals("") || userName.equals("")) {
            Toast.makeText(getBaseContext(), "连接信息不完整", Toast.LENGTH_SHORT).show();
        } else {

            startService(socketIntent);

            bindService(socketIntent,conn,BIND_AUTO_CREATE);//绑定服务

        }

    }

    public void onLoginSuccess(){
        unbindService(conn);

        userName = enterName.getText().toString();
        //String savedUserName = sharedPreferences.getString(Name,"qwesd");
        //enterName.setText(savedUserName);

        String n = enterName.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Name,n);
        editor.commit();

        Intent intent = new Intent(getBaseContext(), ListActivity.class);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        startActivity(intent);
        Toast.makeText(getBaseContext(), "连接成功", Toast.LENGTH_SHORT).show();
    }

    public void onLoginFail(){
        unbindService(conn);
        stopService(socketIntent);
        Toast.makeText(getBaseContext(), "连接失败", Toast.LENGTH_SHORT).show();
    }

    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    onLoginSuccess();
                    break;
                case 2:
                    onLoginFail();
                    break;
                case 3:
                    socket = ((Socket) msg.obj);
                    break;
                default:
                    break;
            }
        }

    };



    private ServiceConnection conn = new ServiceConnection() {

        /**
         * 服务解除绑定时调用
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        /**
         * 服务成功绑定时调用
         * @param name
         * @param service
         */

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionBinder = (SocketService.ConnectionBinder) service;
            try {
                connectionBinder.setIP(ip);
                connectionBinder.setPort(port);
                connectionBinder.setHandler(handler);
                connectionBinder.startConnection();

                //connectionBinder.getSocket();
            } catch (Exception e) {

            }
        }

    };

}
