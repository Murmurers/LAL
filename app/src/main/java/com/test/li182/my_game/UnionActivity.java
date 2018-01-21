package com.test.li182.my_game;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.test.li182.my_game.Utils.addToQue;
import static com.test.li182.my_game.Utils.intToIp;
import static com.test.li182.my_game.Utils.toast;

public class UnionActivity extends AppCompatActivity {
    Button button1;
    Button button2;
    boolean isHouseOwner;
    boolean receiving;

    float dataOld=0f,dataNew=0x0f;
    Queue<Integer> que;
    private final int CAPACITY =30;
    private final int PORT = 6666;
    private String IP;
    long sumA,sumB;
    Socket socket;
    ServerSocket serverSocket;
    private RecyclerView recyclerView;

    public RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Player> players;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                button2.setText("开始");
                String usrname = ((String) msg.obj).substring(1);
                players.add(new Player(usrname,123));
                mAdapter.notifyDataSetChanged();


            }
            if(msg.what==2){
                button2.setText("准备");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_union);
        //initData
        button1 = findViewById(R.id.button_ustart);
        button2 = findViewById(R.id.button_uadd);
        recyclerView = findViewById(R.id.recycleview);

        que = new LinkedBlockingDeque<>(CAPACITY);
        UnionActivity.SensorListener sensorListener = new UnionActivity.SensorListener();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER );
        sensorManager.registerListener(sensorListener,accelerometer,SensorManager.SENSOR_DELAY_UI);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        players = new ArrayList<>();
        mAdapter = new MyAdapter(players);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //bindListener
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!receiving){
                    receive();
                    receiving = true;
                    isHouseOwner = true;
                    //button2.setText("开始");
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isHouseOwner){
                    connect();
                }
                //button2.setText("开始");
            }
        });
    }

    private void connect() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(IP, PORT);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("chello");
                    out.close();
                    socket.close();
                    Message msg = new Message();
                    msg.what=2;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
       new Thread(runnable).start();
    }

    private void receive() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (receiving){
                    try {
                        serverSocket = new ServerSocket(PORT);
                        socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String data = in.readLine();
                        if (data.matches("c.*")){
                            Message msg = new Message();
                            msg.what=1;
                            msg.obj = data;
                            handler.sendMessage(msg);
                        }
                        in.close();
                        socket.close();
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runnable).start();
    }


    class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (Math.abs(sensorEvent.values[1])<100){
                dataOld = dataNew;
                dataNew = sensorEvent.values[1];
                int result = (int) (Math.abs(dataNew-dataOld)*10);
                addToQue(que,result, CAPACITY);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu1:
                final EditText editText = new EditText(UnionActivity.this);
                editText.setText(getIP());
                new AlertDialog.Builder(UnionActivity.this).setTitle("输入对方ip：")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                IP = editText.getText().toString();
                            }
                        })
                        .create().show();
                return true;

            case R.id.menu3:
                String ip = getIP();
                toast(UnionActivity.this,ip);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private String getIP() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }
}
