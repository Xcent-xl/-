package SQLite;

/**
 * Created by chen on 2016/9/7.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add persons
     * @param money
     */
    public boolean add(List<Money> moneys) {
        db.beginTransaction();  //开始事务
        boolean flag=false;
        try {
            for (Money money : moneys) {
               if(select(money.getMdate()).getMdate().equals("none"))
                {
                    db.execSQL("INSERT INTO MoneyRecord VALUES(null, ?, ?, ?, ?, ?,?)"
                            , new Object[]{money.getMreason(), money.getMoney(), money.getMdate(), money.getMimage(), money.getMaccept(), money.getPno()});
                    flag=true;
                }
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
        return flag;
    }
    public void updateMoney(List<Money> moneys) {
        db.beginTransaction();  //开始事务
        try {
            for (Money money : moneys) {
                ContentValues cv = new ContentValues();
                cv.put("Mreason",money.getMreason());
                cv.put("Pno",money.getPno());
                cv.put("Maccept",money.getMaccept());
                cv.put("Mimage",money.getMimage());
                cv.put("Money",money.getMoney());
                String[] whereArgs={String.valueOf(money.getMdate())};
                db.update("MoneyRecord", cv, "Mdate = ?", whereArgs);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }
    public boolean addProject(List<Project> projects) {
        db.beginTransaction();  //开始事务
        boolean flag=false;
        try {
            for (Project project : projects) {
               if(selectProject(project.getPdate()).getPdate().equals("none"))
                {
                db.execSQL("INSERT INTO Project VALUES(null, ?, ?, ?, ?, ? ,?)"
                        , new Object[]{project.getPno(),project.getPname(),project.getPpassword(),project.getPmoney(),project.getPdate(),project.getPremember()});
                    flag=true;
                }
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
        return flag;
    }
    public List<Project> queryProject() {
        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor c = db.rawQuery("SELECT * FROM Project", null);
        while (c.moveToNext()) {
            Project project = new Project();
            project.setPno(c.getString(c.getColumnIndex("Pno")));
            project.setPname(c.getString(c.getColumnIndex("Pname")));
            project.setPpassword(c.getString(c.getColumnIndex("Ppassword")));
            project.setPmoney(c.getString(c.getColumnIndex("Pmoney")));
            project.setPdate(c.getString(c.getColumnIndex("Pdate")));
            project.setPremember(c.getInt(c.getColumnIndex("Premember")));
            projects.add(project);
        }
        c.close();
        return projects;
    }
    public void updateProject(Project project)
    {
        ContentValues cv = new ContentValues();
        cv.put("Pname",project.getPname());
        cv.put("Ppassword",project.getPpassword());
        cv.put("Pmoney",project.getPmoney());
        String[] whereArgs={String.valueOf(project.getPno())};
        db.update("Project", cv, "Pno = ?", whereArgs);
    }
    public void deleteProject(String Pno) {
        String[] whereArgs={String.valueOf(Pno)};
        db.delete("Project", "Pno = ?", whereArgs);
    }
    public void updateRemember(String Pno,int remember)
    {
        ContentValues cv = new ContentValues();
        cv.put("Premember",remember);
        String[] whereArgs={String.valueOf(Pno)};
        db.update("Project", cv, "Pno = ?", whereArgs);
    }
    public Project selectProject(String Pdate) {
        Project project = new Project();
        String[] whereArgs={String.valueOf(Pdate)};
        project.setPdate("none");
        Cursor c = db.rawQuery("select * from Project where Pdate =?", whereArgs);
        while (c.moveToNext()) {
            project.setPno((c.getString(c.getColumnIndex("Pno"))));
            project.setPname((c.getString(c.getColumnIndex("Pname"))));
            project.setPpassword((c.getString(c.getColumnIndex("Ppassword"))));
            project.setPmoney((c.getString(c.getColumnIndex("Pmoney"))));
            project.setPdate((c.getString(c.getColumnIndex("Pdate"))));
            project.setPremember((c.getInt(c.getColumnIndex("Premember"))));
        }
        c.close();
        return project;
    }
    /**
     * query all persons, return list
     * @return List<Person>
     */
    public List<Money> query() {
        ArrayList<Money> moneys = new ArrayList<Money>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            Money money = new Money();
            money.setMno(c.getInt(c.getColumnIndex("_id")));
            money.setMreason((c.getString(c.getColumnIndex("Mreason"))));
            money.setMoney(c.getString(c.getColumnIndex("Money")));
            money.setMdate(c.getString(c.getColumnIndex("Mdate")));
            money.setMimage(c.getBlob(c.getColumnIndex("Mimage")));
            money.setMaccept(c.getBlob(c.getColumnIndex("Maccept")));
            money.setPno(c.getString(c.getColumnIndex("Pno")));
            moneys.add(money);
        }
        c.close();
        return moneys;
    }
    public Money select(String name) {
        Money money = new Money();
        money.setMdate("none");
       // Log.e("MneyRecord", money.getMdate());
        String[] whereArgs={String.valueOf(name)};
        Cursor c = db.rawQuery("select * from MoneyRecord where Mdate =?", whereArgs);
        while (c.moveToNext()) {
            money.setMno(c.getInt(c.getColumnIndex("_id")));
            money.setMreason((c.getString(c.getColumnIndex("Mreason"))));
            money.setMoney(c.getString(c.getColumnIndex("Money")));
            money.setMdate(c.getString(c.getColumnIndex("Mdate")));
            money.setMimage(c.getBlob(c.getColumnIndex("Mimage")));
            money.setMaccept(c.getBlob(c.getColumnIndex("Maccept")));
        }
        //Log.e("MneyRecord", money.getMdate());
        c.close();
        return money;
    }
    public void updateImage(String date, byte[] image) {
        ContentValues cv = new ContentValues();
        cv.put("Mimage",image);
        String[] whereArgs={String.valueOf(date)};
        db.update("MoneyRecord", cv, "Mdate = ?", whereArgs);
    }
    public void delete(String date) {
        String[] whereArgs={String.valueOf(date)};
        db.delete("MoneyRecord", "Mdate = ?", whereArgs);
    }
    public void updateAccept(String date, byte[] accept) {
        ContentValues cv = new ContentValues();
        cv.put("Maccept",accept);
        String[] whereArgs={String.valueOf(date)};
        db.update("MoneyRecord", cv, "Mdate = ?", whereArgs);
    }
    public void updateRecord(String date,String reason,String money)
    {
        ContentValues cv = new ContentValues();
        cv.put("Mreason",reason);
        cv.put("Money",money);
        String[] whereArgs={String.valueOf(date)};
        db.update("MoneyRecord", cv, "Mdate = ?", whereArgs);
    }
    /**
     * query all persons, return cursor
     * @return  Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM MoneyRecord", null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}
