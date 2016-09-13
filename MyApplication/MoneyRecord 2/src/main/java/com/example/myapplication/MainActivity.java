package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SQLite.DBManager;
import SQLite.Money;
import SQLite.Project;
import Socket.SocketServer;
import WebService.callWebservice;
public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private DBManager mgr;
    private SimpleAdapter adapter = null; // 进行数据的转换操作
    private GoogleApiClient client;
    private static Context context;
    private int SHOW_DIALOG_TYPE_ADD=0;
    private int SHOW_DIALOG_TYPE_SET=1;
    private String tag="MoneyRecord";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.e("MoneyRecord", "get Message(query)");
                    Toast.makeText(getApplicationContext(),(String)msg.obj, Toast.LENGTH_SHORT).show();
                    query();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(),(String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    SocketServer sServer=new SocketServer(handler);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project);
        listView=(ListView)findViewById(R.id.listViewProject);
        mgr = new DBManager(this);
        this.registerForContextMenu(listView);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        MainActivity.context = getApplicationContext();
        sServer.serverStart(context);
        query();


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                return showLongCheckDialog(arg2);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                showShortCheckDialog(position);
            }
        });

    }
    public static Context getAppContext() {
        return MainActivity.context;
    }
    public void set(View view)
    {
        showSetManagerDialog();
    }
    public void add(View view) {
        showProjectDialog(SHOW_DIALOG_TYPE_ADD,-1);
    }

    //QuerySqlite
    public void query(){
        List<Project> projects = mgr.queryProject();
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        int number=1;
        for (Project project : projects) {
            //Toast.makeText(getApplicationContext(), project.getPno(), Toast.LENGTH_SHORT).show();
            HashMap<String, String> map = new HashMap<String, String>();
            String m= String.format(project.getPmoney(), "%.2f");
            map.put("name",number+"："+project.getPname()+" 经费"+m + "元");
            map.put("info", project.getPdate());
            list.add(map);
            number++;
        }
        adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
                new String[]{"name", "info"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);
        //listView.setOnItemClickListener(new OnItemClickListenerImpl());
    }


    //ShowDialog
    private void showProjectDialog(int getType,int arg2)
    {
        String title="";
        final int type=getType;
        if(type==SHOW_DIALOG_TYPE_ADD) title="增加项目";
        final ArrayList<Project> projects = new ArrayList<Project>();
        final  Project project=new Project();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.setproject, null);
        final EditText ProjectName=(EditText)v.findViewById(R.id.ProjectName) ;
        final EditText ProjectPassword=(EditText)v.findViewById(R.id.ProjectPassword) ;
        final EditText ProjectMoney=(EditText)v.findViewById(R.id.ProjectMoney) ;
        if(type==SHOW_DIALOG_TYPE_SET)
        {
            Map<String, String> map = (Map<String, String>) MainActivity.this.adapter.getItem(arg2);
            final String info = map.get("info");
            final Project getProject = mgr.selectProject(info);
            title="修改项目";
            ProjectName.setText(getProject.getPname());
            ProjectPassword.setText(getProject.getPpassword());
            ProjectMoney.setText(getProject.getPmoney());
            project.setPno(getProject.getPno());
        }
        ProjectMoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setTitle(title);
        builder.setView(v);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                Boolean flag1=false,flag2=false,flag3=false;
                Boolean strResult = ProjectMoney.getText().toString().trim().matches("-?[0-9]+.*[0-9]*");
                if(!ProjectName.getText().toString().trim().equals("") ) {project.setPname(ProjectName.getText().toString().trim());flag1=true;}
                if(!ProjectPassword.getText().toString().trim().equals("") ) {project.setPpassword(ProjectPassword.getText().toString().trim());flag2=true;}
                if((!ProjectMoney.getText().toString().trim().equals(""))&&strResult)
                {
                    project.setPmoney(new java.text.DecimalFormat("#.00").format(Float.parseFloat(ProjectMoney.getText().toString().trim())));flag3=true;
                }
                if(type==SHOW_DIALOG_TYPE_ADD) {
                    project.setPdate(new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒").format(new Date()));
                    project.setPno(project.getPname() + project.getPdate());
                    project.setPremember(0);
                    projects.add(project);
                    if (flag1 && flag2 && flag3) mgr.addProject(projects);
                    else Toast.makeText(getApplicationContext(), "输入错误", Toast.LENGTH_SHORT).show();
                }
                if(type==SHOW_DIALOG_TYPE_SET){
                    if (flag1 && flag2 && flag3)  mgr.updateProject(project);
                    else Toast.makeText(getApplicationContext(), "输入错误", Toast.LENGTH_SHORT).show();
                }
                query();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showSetManagerDialog()
    {
        final SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        if(sp.getString("SA_PASS", "").equals("")) {
            showCreatManager();
        }
        else{
            showUpdateManager();
        }
    }
    private void showCreatManager()
    {
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
        final View v = factory.inflate(R.layout.editbox_layout, null);//这里必须是final的
        final EditText edit = (EditText) v.findViewById(R.id.editText);//获得输入框对象
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("请创建管理员密码")//提示框标题
                .setView(v)
                .setPositiveButton("确定",//提示框的两个按钮
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //事件
                                if(edit.getText().toString().equals(""))  Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                                else {
                                    SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                                    //存入数据
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("SA_PASS", edit.getText().toString());
                                    editor.commit();
                                }
                            }
                        }).setNegativeButton("取消", null).create().show();
    }
    private void showUpdateManager()
    {
        final SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
        final View vsa = factory.inflate(R.layout.setsa, null);//这里必须是final的
        final EditText p1 = (EditText) vsa.findViewById(R.id.password1);//获得输入框对象
        final EditText p2 = (EditText) vsa.findViewById(R.id.password2);//获得输入框对象
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("更改管理员密码")//提示框标题
                .setView(vsa)
                .setPositiveButton("确定",//提示框的两个按钮
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //事件
                                if(p1.getText().toString().equals(sp.getString("SA_PASS", ""))&&(!p2.getText().toString().equals("")))
                                {
                                    SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                                    //存入数据
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("SA_PASS", p2.getText().toString());
                                    editor.commit();
                                    Toast.makeText(getApplicationContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                                }
                                else Toast.makeText(getApplicationContext(), "密码错误/密码不能为空", Toast.LENGTH_SHORT).show();

                            }
                        }).setNegativeButton("取消", null).create().show();
    }
    private boolean showLongCheckDialog(int getarg2)
    {
        final int arg2=getarg2;
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
        final View v = factory.inflate(R.layout.editbox_layout, null);//这里必须是final的
        final EditText edit = (EditText) v.findViewById(R.id.editText);//获得输入框对象
        Map<String, String> map = (Map<String, String>) MainActivity.this.adapter
                .getItem(arg2);
        final String info = map.get("info");
        final Project getProject = mgr.selectProject(info);
        if (checkPassword(getProject, "")) {
            // TODO Auto-generated method stub
            // When clicked, show a toast with the TextView text
            showMenu(info,arg2);
            return true;
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请输入项目密码")//提示框标题
                    .setView(v)
                    .setPositiveButton("确定",//提示框的两个按钮
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    //事件
                                    if (checkPassword(getProject, edit.getText().toString())) {

                                        // TODO Auto-generated method stub
                                        // When clicked, show a toast with the TextView text
                                        showMenu(info,arg2);
                                    } else
                                        Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("取消", null).create().show();
            return true;
        }
    }
    private void showShortCheckDialog(int position)
    {
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
        final View v = factory.inflate(R.layout.editbox_layout, null);//这里必须是final的
        final EditText edit = (EditText) v.findViewById(R.id.editText);//获得输入框对象
        Map<String, String> map = (Map<String, String>) MainActivity.this.adapter
                .getItem(position);
        final String info = map.get("info");
        final Project getProject = mgr.selectProject(info);
        if (checkPassword(getProject, "")) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ProjectActivity.class);
            //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
            intent.putExtra("info", info);
            startActivity(intent);
            //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请输入项目密码")//提示框标题
                    .setView(v)
                    .setPositiveButton("确定",//提示框的两个按钮
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    //事件
                                    if (checkPassword(getProject, edit.getText().toString())) {
                                        Intent intent = new Intent();
                                        intent.setClass(MainActivity.this, ProjectActivity.class);
                                        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                                        intent.putExtra("info", info);
                                        startActivity(intent);
                                        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("取消", null).create().show();

        }
    }
    private void showConnectDialog(String info)
    {
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
        final Project getProject = mgr.selectProject(info);
        final View vsa = factory.inflate(R.layout.connection, null);//这里必须是final的
        final EditText ipAddress = (EditText) vsa.findViewById(R.id.ipaddress);//获得输入框对象
        final EditText port = (EditText) vsa.findViewById(R.id.port);//获得输入框对象
        ipAddress.setText(getIp());
        port.setText(getPort());

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("发送给IP 本机("+/*+new NetWorkUtils().getLocalIp(context)+*/")")//提示框标题
                .setView(vsa)
                .setPositiveButton("确定",//提示框的两个按钮
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //事件
                                if((!ipAddress.getText().toString().equals("")))
                                {
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
                                    sServer.clientStart(ipAddress.getText().toString(),port.getText().toString(),true,obj);
                                    saveIp(ipAddress.getText().toString(),port.getText().toString());

                                }
                                else Toast.makeText(getApplicationContext(), "ip地址不能为空", Toast.LENGTH_SHORT).show();

                            }
                        }).setNegativeButton("取消", null).create().show();
    }
    private void showMenu(final String info,int getArg)
    {
        final Project getProject = mgr.selectProject(info);
        final int arg=getArg;
        sServer.clientStop(false,"");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(getItemString(getProject), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO 自动生成的方法存根
                if (arg1 == 0) {
                    showProjectDialog(SHOW_DIALOG_TYPE_SET,arg);
                }
                if (arg1 == 1) {
                    final SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                    LayoutInflater factory = LayoutInflater.from(MainActivity.this);//提示框
                    final View v = factory.inflate(R.layout.editbox_layout, null);//这里必须是final的
                    final EditText edit = (EditText) v.findViewById(R.id.editText);//获得输入框对象
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("请输入管理员密码删除")//提示框标题
                            .setView(v)
                            .setPositiveButton("确定",//提示框的两个按钮
                                    new android.content.DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            //事件
                                            if(sp.getString("SA_PASS", "").equals("")) Toast.makeText(getApplicationContext(), "请设置管理员密码", Toast.LENGTH_SHORT).show();
                                            else if(edit.getText().toString().equals(sp.getString("SA_PASS", "")))
                                            {
                                                mgr.deleteProject(getProject.getPno());
                                                query();
                                                Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                            }
                                            else Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("取消", null).create().show();
                }
                if (arg1 == 2) {
                    if(getProject.getPremember() > 0)  mgr.updateRemember(getProject.getPno(), 0);
                    else  mgr.updateRemember(getProject.getPno(), 1);
                }
                if (arg1 == 3) {
                    showConnectDialog(info);
                }
                if (arg1 == 4) {
                    Map<String, String> values = new HashMap<String, String>();
                    values.put("msg", "这是Android手机发出的信息");
                    Request("EchoMessage", values);
                }
                if (arg1 == 5) {
                    //蓝牙传输菜单按钮
                    Intent intent = new Intent();
                    intent.putExtra("info",info);
                    intent.setClass(MainActivity.this, BTMainActivity.class);
                    startActivity(intent);



                }
                arg0.dismiss();
            }
        });
        builder.show();
    }

    private List<Object> getObjList(int arg2)
    {
        Map<String, String> map = (Map<String, String>) MainActivity.this.adapter
                .getItem(arg2);
        final String info = map.get("info");//传这个
        final Project getProject = mgr.selectProject(info);
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



    //OperationFunction
    private String[] getItemString(Project getProject)
    {
        String[] arr=new String[]{"修改","删除","","发送","呼叫","蓝牙传输"};
        if(getProject.getPremember() > 0)  arr[2]="忘记密码";
        else  arr[2]="记住密码";
        return arr;
    }
    private boolean checkPassword(Project project,String password)
    {
        final SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        if(project.getPremember()>0) return true;
        else if(project.getPpassword().equals(password)) return true;
        else if((!sp.getString("SA_PASS", "").equals(""))&&password.equals(sp.getString("SA_PASS", ""))) return true;
        else return false;
    }
    private void saveIp(String ip,String port)
    {
        SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("IP", ip);
        editor.putString("PORT", port);
        editor.commit();
    }
    private String getIp()
    {
        SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        return sp.getString("IP", "192.168.1.");
    }
    private String getPort()
    {
        SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        return sp.getString("PORT", "8888");
    }



    /**
     * 执行异步任务
     *
     * @param params
     *            方法名+参数列表（哈希表形式）
     */
    public void Request(Object... params) {
        new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... params) {
                if (params != null && params.length == 2) {
                    Log.e(tag, "----参数----"+(String) params[0]);
                    return new callWebservice().CallWebService((String) params[0],
                            (Map<String, String>) params[1]);
                } else if (params != null && params.length == 1) {
                    Log.e(tag, "----参数----"+(String) params[0]);
                    return new callWebservice().CallWebService((String) params[0], null);
                } else {
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    //tvMessage.setText("服务器回复的信息 : " + result);
                }
            };

        }.execute(params);
    }


    //None
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


}
