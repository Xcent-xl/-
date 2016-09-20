package BlueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import SQLite.Money;
import SQLite.Project;
import SQLite.DBManager;

/**
 * Created by Neoy on 16/9/12.
 */
public class BTConnectService {

    public static final int MESSAGE_CLIENT_CONNECTED = 1;
    public static final int MESSAGE_SERVER_CONNECTED = 2;
    private static final int CONNECT_SUCCEED = 5;
    private Context mContext;
    // Debugging
    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";


    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;

    private AcceptThread mSecureAcceptThread;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mhandler;


    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context

     */
    public BTConnectService(Context context, Handler mhandler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mhandler = mhandler;
        this.mContext = context;

    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }


        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            Log.d(TAG,"准备开启一个接受线程");
            mSecureAcceptThread = new AcceptThread(mContext, mhandler);
            mSecureAcceptThread.start();
        }

    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect

     */
    public synchronized void connect(BluetoothDevice device, Context context, Handler handler) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }


        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        Log.d(TAG,"准备连接设备："+device.getName());
        mConnectThread = new ConnectThread(device, context, handler);
        mConnectThread.start();

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, Context context, Handler handler) {


        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        Log.d(TAG,device.getName()+"连接完成");
        mConnectedThread = new ConnectedThread(socket, context, handler);
        mConnectedThread.start();
        Message msg = mhandler.obtainMessage(MESSAGE_CLIENT_CONNECTED);
        mhandler.sendMessage(msg);

    }

    /**
     * Stop all threads
     */
    public void AcceptStop(){
        if (mSecureAcceptThread != null) {
            Log.d(TAG,"acceptstop方法关闭acceptthread");
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
    }
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            Log.d(TAG,"stop方法关闭connectthread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            Log.d(TAG,"stop方法关闭connectedthread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            Log.d(TAG,"stop方法关闭acceptthread");
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }


    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write

     */
    public void write(List<Object> out) {
        // Create temporary object
        ConnectedThread r;
        Log.d(TAG,"准备发送数据"+out);
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {

            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
        Log.d(TAG,"数据发送完成");
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private Context mmContext;
        private Handler mmhandler;

        public AcceptThread(Context context, Handler handler) {
            BluetoothServerSocket tmp = null;
            this.mmContext = context;
            this.mmhandler = handler;

            // Create a new listening server socket
            try {

                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);

            } catch (IOException e) {
                Log.e(TAG, "创建服务端连接socket失败", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {


            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
while(true){
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
                Log.d(TAG, "开启监听线程，等待连接");
            } catch (IOException e) {
                Log.e(TAG, "server socket被关掉了哈哈哈哈哈哈");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                Log.d(TAG, "服务端socket建立成功，socket信息：" + socket + "\n" + "正在建立connectedthread");
                mConnectedThread = new ConnectedThread(socket, mmContext, mmhandler);
                mConnectedThread.start();
                Message msg = mhandler.obtainMessage(MESSAGE_SERVER_CONNECTED);
                mhandler.sendMessage(msg);


            }
        }
            }




        public void cancel() {


            try {

                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"CANCEL调用过程中关闭socket出错");
            }

        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private Context mmContext;
        private Handler mmHandler;


        public ConnectThread(BluetoothDevice device, Context context, Handler handler) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            this.mmContext = context;
            this.mmHandler = handler;


            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {

                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);

            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        public void run() {


            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG,"连接中途出错，关闭socket");
                } catch (IOException e2) {

                }

                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BTConnectService.this) {
                mConnectThread = null;
            }
            Log.d(TAG,"连接完成,连接到"+mmDevice.getName());
            // Start the connected thread
            connected(mmSocket, mmDevice,mmContext, mmHandler);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private static final int MESSAGE_SEND_FALSE = 3;
        private static final int MESSAGE_SEND_SUCCEED = 4;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final ObjectInputStream inp;
        private final ObjectOutputStream out;
        private Context mmmContext;
        private Handler mmmhandler;
        private boolean insertMoney(List<Money> moneys, Context context)
        {
            DBManager mgr=new DBManager(context);
            mgr.updateMoney(moneys);
            return  mgr.add(moneys);
        }
        private boolean insertProject(List<Project> projects, Context context)
        {
            DBManager mgr=new DBManager(context);
            mgr.updateProject(projects.get(0));
            return mgr.addProject(projects);
        }

        public ConnectedThread(BluetoothSocket socket, Context context, Handler mmhandler) {

            mmSocket = socket;
            this.mmmContext = context;
            this.mmmhandler = mmhandler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            ObjectInputStream TempInp  = null;
            ObjectOutputStream TempOut = null;


            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                Log.d(TAG,"connectedThread正在获取输入流");
                tmpOut = socket.getOutputStream();
                Log.d(TAG,"connectedThread正在获取输出流");
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            try {
                Log.d(TAG,"connectedThread正在转变object输入流");//在蓝牙socket中outputstream转变objectoutputstream要flush();
                TempOut = new ObjectOutputStream(mmOutStream);
                TempOut.flush();
                Log.d(TAG,"connectedThread正在转变object输入流");
                TempInp = new ObjectInputStream(mmInStream);



            } catch (IOException e) {
                e.printStackTrace();
            }
            inp = TempInp;
            out = TempOut;

        }
        public void run() {
            Log.i(TAG, "ConnectedThread开始运行...监听输入");
            Message msg = mhandler.obtainMessage(CONNECT_SUCCEED);
            mhandler.sendMessage(msg);


            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    Log.d(TAG,"输入流信息："+inp);

                    ArrayList<Object> readMsg = (ArrayList<Object>) inp.readObject();
                    Log.d(TAG,"读取到："+readMsg);
                    boolean flag = false;
                    if(insertProject((List<Project>)readMsg.get(0),mmmContext)) flag=true;
                    if(insertMoney((List<Money>)readMsg.get(1),mmmContext)) flag=true;
                    if(flag)
                    {
                        msg = mhandler.obtainMessage(MESSAGE_SEND_SUCCEED);
                        mhandler.sendMessage(msg);
                    }
                    else {
                        msg = mhandler.obtainMessage(MESSAGE_SEND_FALSE);
                        mhandler.sendMessage(msg);
                    }
                    // Toast.makeText(context, "已插入数据表，请手动刷新页面", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Log.e(TAG, "连接失败", e);
                    connectLost();
                    // Start the service over to restart listening mode
                    BTConnectService.this.start();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(List<Object> buffer) {
            try {
                Log.d(TAG,"正在发送数据");
                out.writeObject(buffer);
                Log.d(TAG,"发送完成");

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private void connectLost() {
        start();
    }
}

