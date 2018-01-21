package com.test.li182.my_game;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.test.li182.my_game.Utils.*;

public class PKActivity extends AppCompatActivity {
    float dataOld=0f,dataNew=0x0f;
    private TextView etTest;
    Queue<Integer> que;
    private final int CAPACITY =30;
    private final int PORT = 6666;
    private String IP;
    boolean sensorIsOpen = false;
    String aData;
    String bData;
    long sumA,sumB;
    TextView tvA;
    TextView tvB;
    Socket socket;
    ServerSocket serverSocket;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what ==1){
                sumA = sumA+Long.parseLong(aData);
                sumB = sumB+Long.parseLong(bData);
                LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,sumA);
                tvA.setLayoutParams(p1);
                tvA.setText(""+sumA);
                LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,sumB);
                tvB.setLayoutParams(p2);
                tvB.setText(""+sumB);
                etTest.setText(aData+" : "+bData);
            }
        }
    };
    private ServiceConnection conn =new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.activity_pk);
        Intent intent = new Intent(PKActivity.this,BgmService.class);
        bindService(intent,conn,Context.BIND_AUTO_CREATE);
        etTest = findViewById(R.id.et_test);
        Button btStart = (Button) findViewById(R.id.button_start);
        Button btAdd = (Button) findViewById(R.id.button_add);
        tvA = findViewById(R.id.tv_a);
        tvB = findViewById(R.id.tv_b);

        que = new LinkedBlockingDeque<>(CAPACITY);

        SensorListener sensorListener = new SensorListener();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER );
        sensorManager.registerListener(sensorListener,accelerometer,SensorManager.SENSOR_DELAY_UI);


        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumA = 0L;
                sumB = 0L;
                receive();
            }
        });

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumA = 0L;
                sumB = 0L;
                send();
            }
        });


    }

    private void send() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    for (int i = 0;i<10;i++){
                        aData = ""+queMean(que);
                        socket = new Socket(IP, PORT);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(aData);
                        out.close();


                        socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bData = in.readLine();
                        in.close();
                        Thread.sleep(500);


                        Message msg = new Message();
                        msg.what=1;
                        handler.sendMessage(msg);
                    }
                    socket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();

    }

    private void receive() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    for (int i = 0;i<10;i++){
                        socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bData = in.readLine();
                        in.close();
                        Thread.sleep(500);

                        aData = ""+queMean(que);
                        socket = new Socket(IP, PORT);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(aData);
                        out.close();
                        Message msg = new Message();
                        msg.what=1;
                        handler.sendMessage(msg);
                    }
                    socket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task).start();
    }

    private void refresh() {
        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,1000);
                etTest.setText(""+ queMean(que));
            }
        };
        handler.postDelayed(task,1000);
    }


    class SensorListener implements SensorEventListener{
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
                final EditText editText = new EditText(PKActivity.this);
                editText.setText(getIP());
                new AlertDialog.Builder(PKActivity.this).setTitle("输入对方ip：")
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
                etTest.setText(ip);
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

    @Override
    protected void onStop() {
        unbindService(conn);
        super.onStop();
    }
}
