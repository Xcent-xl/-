package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import BlueTooth.BTConnectService;
import SQLite.DBManager;
import SQLite.Money;
import SQLite.Project;

public class BTMainActivity extends AppCompatActivity {

    /**
     * Tag for Log
     */
    private static final String TAG = BTMainActivity.class.getSimpleName();

    private boolean isClient = false;
    private Button sendMsgBtn;
    //private Button scanButton;
   // private Button ClientBtn;
    private Button ServerBtn;
    private BTConnectService mConnectService;

    private ListView findedDevices;
    private TextView Info;

    private ArrayAdapter<BluetoothDevice> NewDevicesAdapter;
    private BluetoothDevice connectDev;

    private List<Object> msg;

    private String sendInfo;
    public static final int MESSAGE_CLIENT_CONNECTED = 1;
    public static final int MESSAGE_SERVER_CONNECTED = 2;
    private static final int MESSAGE_SEND_FALSE = 3;
    private static final int MESSAGE_SEND_SUCCEED = 4;
    private static final int CONNECT_SUCCEED = 5;
    /**
     * Member fields
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CONNECT_SUCCEED:
                    sendMsgBtn.setVisibility(View.VISIBLE);
                    //吧发送按钮设为可见
                    break;

                case MESSAGE_CLIENT_CONNECTED:
                    Info.setText("已和服务端连接..."+"准备数据传输...");

                    break;
                case MESSAGE_SERVER_CONNECTED:
                    Info.setText("已和客户端连接...等待数据传输...");
                    break;
                case MESSAGE_SEND_FALSE:
                    Info.setText("数据传输成功，但是存储失败哈哈哈哈哈哈");
                    break;
                case MESSAGE_SEND_SUCCEED:
                    Info.setText("数据传输成功，存储成功");
                    break;


            }
        }
    };
    private BluetoothAdapter mBtAdapter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG,"蓝牙设备："+device.getName());




                    NewDevicesAdapter.add(device);

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG,"搜索结束");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        sendInfo = intent.getStringExtra("info");
        if(sendInfo.equals(""))
            Toast.makeText(this,"读取文件失败，没得玩了。",Toast.LENGTH_SHORT);


        setContentView(R.layout.bluetooth_main);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        Info = (TextView)findViewById(R.id.ConnectInfo);
        mConnectService = new BTConnectService(this, mHandler);
     /*   ClientBtn = (Button)findViewById(R.id.ClientBtn);
        ClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"进入客户端连接模式...");

            }

        });
        */
        ServerBtn = (Button)findViewById(R.id.serverMode);
        ServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG,"进入服务监听模式");
                mConnectService.start();
                isClient = false;
               //doListen();
            }

        });
     /*   scanButton = (Button) findViewById(R.id.Scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NewDevicesAdapter.clear();
                doDiscovery();
            }
        });
        */
        sendMsgBtn = (Button)findViewById(R.id.sentMsg);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = getSendMsg(sendInfo);
                Log.d(TAG,"准备发送数据"+msg);
                mConnectService.write(msg);
            }
        });

        findedDevices = (ListView)findViewById(R.id.deviceList);
        NewDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,R.layout.bt_device_info);
        findedDevices.setAdapter(NewDevicesAdapter);
        findedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"点击的是第"+position+"\n");
                connectDev = NewDevicesAdapter.getItem(position);
                mConnectService.connect(connectDev, BTMainActivity.this,mHandler);
                isClient = true;



            }
        });


        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if( !mBtAdapter.isEnabled()){
            Intent enabler=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler,1);
        }
        NewDevicesAdapter.clear();
        doDiscovery();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);




    }
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "start onPause~~~");
        mConnectService.stop();
    }
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "start onRestart~~~");
        if(isClient){
            mConnectService.connect(connectDev, BTMainActivity.this,mHandler);
        }else{

            mConnectService.start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        mConnectService.stop();
        this.unregisterReceiver(mReceiver);

    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "开始搜索蓝牙...");




        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }


    private List<Object> getSendMsg(String sendInfo){
        DBManager mgr = new DBManager(this);;
        final Project getProject = mgr.selectProject(sendInfo);
        List<Project> projects=new ArrayList<Project>();
        List<Money> moneys=new ArrayList<Money>();
        List<Object> obj = new ArrayList<Object>();
        projects.add(getProject);
        for (Money e : mgr.query()) {
            if(e.getPno().equals(getProject.getPno())) {
                moneys.add(e);
            }
        }
        obj.add(projects);
        obj.add(moneys);
        return obj;
    }


}
