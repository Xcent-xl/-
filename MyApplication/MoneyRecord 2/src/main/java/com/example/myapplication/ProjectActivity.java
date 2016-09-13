package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

public class ProjectActivity extends AppCompatActivity {
    private TextView moneyCount, money,titleName;
    private ListView listView;
    private DBManager mgr;
    private SimpleAdapter adapter = null; // 进行数据的转换操作
    float MONEY_COUNT=0;
    float MONEY_TAKE=0;
    Project project=new Project();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //getSupportActionBar().hide();
        listView = (ListView) findViewById(R.id.listView);
        moneyCount = (TextView) findViewById(R.id.moneyCount);
        titleName = (TextView) findViewById(R.id.titleName);
        money = (TextView) findViewById(R.id.money);
        mgr = new DBManager(this);

        Bundle bundle = this.getIntent().getExtras();
        String info=bundle.getString("info");
        //Toast.makeText(getApplicationContext(),info, Toast.LENGTH_SHORT).show();
        project=mgr.selectProject(info);
        MONEY_COUNT=  Float.parseFloat(project.getPmoney()) ;
        //Toast.makeText(getApplicationContext(), project.getPmoney(), Toast.LENGTH_SHORT).show();
        moneyCount.setText(MONEY_COUNT+"元");
        titleName.setText("  "+project.getPname());
        this.registerForContextMenu(listView);
        query();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                // TODO Auto-generated method stub
                // When clicked, show a toast with the TextView text
                return showMenu(arg2);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Map<String, String> map = (Map<String, String>) ProjectActivity.this.adapter
                        .getItem(position);
                String name = map.get("info");
                Intent intent = new Intent();
                intent.setClass(ProjectActivity.this, Image.class);
                name=name.substring(0,  name.indexOf("\n"));
                //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                intent.putExtra("name", name);
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        //应用的最后一个Activity关闭时应释放DB
        mgr.closeDB();
    }
    public void add(View view) {
        showAddMoneyRecordDialog();
    }

    //QuerySqlite
    public void query(){
        List<Money> moneys = mgr.query();
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        MONEY_TAKE=0;
        int number=1;
        for (Money money : moneys) {
            if(money.getPno().equals(project.getPno())) {
                HashMap<String, String> map = new HashMap<String, String>();
                String m = String.format(money.getMoney(), "%.2f");
                map.put("name", number + "：" + money.getMreason() + " 消费" + m + "元");
                String image = "无", accept = "无";
                if (!(new String(money.getMimage()).equals(""))) image = "有";
                if (!(new String(money.getMaccept()).equals(""))) accept = "有";
                map.put("info", money.getMdate() + "\n实物图: " + image + " 收据/发票图: " + accept);
                list.add(map);
                MONEY_TAKE += Float.parseFloat(money.getMoney());
                number++;
            }
        }
        money.setText(new java.text.DecimalFormat("#.00").format(MONEY_COUNT-MONEY_TAKE)+"元");
        if(MONEY_COUNT-MONEY_TAKE==0)  money.setText("0元");
        adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
                new String[]{"name", "info"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);
    }

    //ShowDialog
    private boolean showMenu(int arg2)
    {
        Map<String, String> map = (Map<String, String>) ProjectActivity.this.adapter
                .getItem(arg2);
        final String name = map.get("name");
        final String info = map.get("info");
        final String _id=info.substring(0,  info.indexOf("\n"));
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);

        builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface arg0, int arg1)
            {
                // TODO 自动生成的方法存根
                if (arg1 == 0)
                {
                    showUpdateMoneyRecordDialog(info,name);
                }
                if (arg1 == 1)
                {
                    AlertDialog.Builder builder2=new AlertDialog.Builder(ProjectActivity.this);
                    builder2.setTitle("操作提示");
                    builder2.setMessage("是否确定要删除？");
                    builder2.setPositiveButton("是的",new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // TODO 自动生成的方法存根
                            mgr.delete(_id);
                            query();
                        }
                    });
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    });
                    builder2.show();
                }
                arg0.dismiss();
            }
        });
        builder.show();
        return true;
    }

    private  void showAddMoneyRecordDialog()
    {
        final ArrayList<Money> moneys = new ArrayList<Money>();
        final  Money money=new Money();

        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.set, null);
        final EditText reason=(EditText)v.findViewById(R.id.reason) ;
        final EditText getmoney=(EditText)v.findViewById(R.id.getmoney) ;
        getmoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setTitle("增加账目");
        builder.setView(v);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                Boolean flag1=false,flag2=false;
                Boolean strResult = getmoney.getText().toString().trim().matches("-?[0-9]+.*[0-9]*");
                if(!reason.getText().toString().trim().equals("") ) {money.setMreason(reason.getText().toString().trim());flag1=true;}
                if((!getmoney.getText().toString().trim().equals(""))&&strResult)
                {
                    money.setMoney(new java.text.DecimalFormat("#.00").format(Float.parseFloat(getmoney.getText().toString().trim())));flag2=true;
                }
                money.setMdate(new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒").format(new Date()));
                byte[] b = new byte[0];
                money.setMimage(b);
                money.setMaccept(b);
                money.setPno(project.getPno());
                moneys.add(money);
                if(flag1&&flag2)  mgr.add(moneys);
                else Toast.makeText(getApplicationContext(), "输入错误", Toast.LENGTH_SHORT).show();
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

    private void showUpdateMoneyRecordDialog(String info,String name)
    {
        final String _id=info.substring(0,  info.indexOf("\n"));
        final String getReasonFromList=name.substring(name.indexOf("：")+1,name.indexOf(" "));
        final String getMoneyFromList=name.substring(name.indexOf("费")+1,name.indexOf("元"));
        final ArrayList<Money> moneys = new ArrayList<Money>();
        final  Money money=new Money();

        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.set, null);
        final EditText reason=(EditText)v.findViewById(R.id.reason) ;
        final EditText getmoney=(EditText)v.findViewById(R.id.getmoney) ;
        getmoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        reason.setText(getReasonFromList);
        getmoney.setText(getMoneyFromList);
        builder.setTitle("修改账目");
        builder.setView(v);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                Boolean flag1=false,flag2=false;
                Boolean strResult = getmoney.getText().toString().trim().matches("-?[0-9]+.*[0-9]*");
                if(!reason.getText().toString().trim().equals("") ) {money.setMreason(reason.getText().toString().trim());flag1=true;}
                if((!getmoney.getText().toString().trim().equals(""))&&strResult)
                {
                    money.setMoney(new java.text.DecimalFormat("#.00").format(Float.parseFloat(getmoney.getText().toString().trim())));flag2=true;
                }
                if(flag1&&flag2)  mgr.updateRecord(_id,money.getMreason(),money.getMoney());
                else Toast.makeText(getApplicationContext(), "输入错误", Toast.LENGTH_SHORT).show();
                query();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
