package Socket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import SQLite.Project;
import SQLite.DBManager;
import SQLite.Money;

/**
 * Created by chen on 2016/9/10.
 */
public class SocketServer {

    String tag = "MoneyRecord";
    private static int defaultPort = 8888;
    public  ArrayList<Socket> socketList=new ArrayList<Socket>();
    private DBManager mgr;
    private Socket s = null;
    private ObjectOutputStream out=null;
    private Handler handler ;
    public SocketServer(Handler handler)
    {
        this.handler=handler;
    }
    public void serverStart(Context c)
    {
        try {
            final Context context=c;
            final ServerSocket ss = new ServerSocket(defaultPort);
            Log.e(tag, "on serverStart");
            new Thread()
            {
                public void run()
                {
                    while(true)
                    {
                        try {
                            Log.e(tag, "on serverStart: ready to accept");
                            s=ss.accept();
                            //socketList.add(s);
                            ArrayList<Socket> getSocketList=new  ArrayList<Socket>();
                            getSocketList.add(s);
                            socketList=getSocketList;
                            List<Object> obj  ;
                            boolean flag=false;
                            int i=1;
                            while ((obj=readFromClient())!=null) {
                                Log.e(tag, "In while "+i);
                                i++;
                                if(insertProject((List<Project>)obj.get(0),context)) flag=true;
                                if(insertMoney((List<Money>)obj.get(1),context)) flag=true;
                                if(flag) handler.sendMessage(getMessage(0,"接收到来自"+s.getInetAddress()+"的项目[" +
                                        ((List<Project>)obj.get(0)).get(0).getPname()+
                                        "](已存储)"));
                                else handler.sendMessage(getMessage(0,"接收到来自"+s.getInetAddress()+"的项目[" +
                                        ((List<Project>)obj.get(0)).get(0).getPname()+
                                        "](检验重复,已更新)"));
                               // Toast.makeText(context, "已插入数据表，请手动刷新页面", Toast.LENGTH_SHORT).show();
                                for (Project e:(List<Project>)obj.get(0))
                                {
                                    Log.e(tag, "Project:  "+e.getPno());
                                }
                                for (Money e:(List<Money>)obj.get(1))
                                {
                                    Log.e(tag, "Money:  "+e.getPno());
                                }
                                if(socketList.size()>0) Log.e(tag, "SocketList Length:  "+socketList.size());
                                if(socketList.size()>1)
                                {
                                        for(int j=0;j<socketList.size()-1;j++)
                                        {
                                            socketList.remove(j);
                                            Log.e(tag, "socketList remove:"+j+" socketList Length="+socketList.size());
                                        }

                                }
                                for (Socket socket:socketList)
                                {
                                    Log.e(tag, "Socket socket:socketList");
                                   // socketList.remove(s);
                                    //Log.e(tag, "remove Socket");
                                    out = new ObjectOutputStream(socket.getOutputStream());
                                }
                            }

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            Log.e(tag, "Server Error UnsupportedEncodingException:  "+e.getMessage());
                            handler.sendMessage(getMessage(0,"来自"+s.getInetAddress()+"的数据接收失败"));
                            clientStop(false,"遇到问题关闭连接");
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            Log.e(tag, "Server Error IOException:  "+e.getMessage());
                            handler.sendMessage(getMessage(0,"来自"+s.getInetAddress()+"的数据接收失败"));
                            clientStop(false,"遇到问题关闭连接");
                            e.printStackTrace();
                        }

                    }
                }
            }.start();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void SendMessage(  List<Object> obj)
    {
        try {
            if(out == null)
            {
                Log.e(tag,"out is null");
                return;
            }
            out.writeObject(obj);
            Log.e(tag, "send success");
           // Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
            handler.sendMessage(getMessage(1,"发送成功"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(tag, "send failed "+e.getMessage());
            //Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
            handler.sendMessage(getMessage(1,"发送失败"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(tag, "send failed "+e.getMessage());
            //Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
            handler.sendMessage(getMessage(1,"发送失败"));
        }
    }
    public void clientStart(final String getIp, final String getPort,final  boolean flag,final List<Object> obj)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String ip=getIp;
                    final String port=getPort;
                    if(!port.equals("") && port != null)
                    {
                        s=new Socket(ip, defaultPort);
                    }
                    else
                    {
                        s=new Socket(ip, Integer.parseInt(port));
                    }
                    out = new ObjectOutputStream(s.getOutputStream());
                    SendMessage(obj);
                    Log.e(tag, "clientStart success");
                   // Toast.makeText(context, "连接"+getIp+":"+getPort+"成功", Toast.LENGTH_SHORT).show();
                    //if(flag) handler.sendMessage(getMessage(1,"连接"+getIp+":"+getPort+"成功"));
                } catch (IOException e) {
                    handler.sendMessage(getMessage(1,"发送失败(发送对象没有开启接收服务)"));
                    e.printStackTrace();
                    Log.e(tag, "clientStart failed "+e.getMessage());
                    //Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
                   // if(flag) handler.sendMessage(getMessage(1,"连接失败"));
                }
            }
        }).start();

    }
    public void clientStop(final boolean flag,String text)
    {
        try {
            if(s != null)
                s.close();
            if(out != null)
               out.close();
           // Toast.makeText(context, "断开连接", Toast.LENGTH_SHORT).show();
            //if(flag) handler.sendMessage(getMessage(1,text));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
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
    private ArrayList<Object> readFromClient(){
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            Log.e(tag, "return  (ArrayList<Object>) ois.readObject();");
            return  (ArrayList<Object>) ois.readObject();
        } catch (Exception e) {
            //删除此Socket
            if(socketList.size()>0) socketList.remove(s);
            //handler.sendMessage(getMessage(0,"来自"+s.getInetAddress()+"的数据接收失败"));
            Log.e(tag, "remove Socket");
        }
        return null;
    }
    private Message getMessage(int what,String obj)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj=obj;
        return msg;
    }

}
