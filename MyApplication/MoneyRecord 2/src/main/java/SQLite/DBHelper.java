package SQLite;

/**
 * Created by chen on 2016/9/7.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "MR.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Project" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, Pno TEXT, Pname TEXT, Ppassword TEXT,Pmoney TEXT,Pdate TEXT,Premember INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS MoneyRecord" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, Mreason TEXT, Money TEXT, Mdate TEXT,Mimage BLOB ,Maccept BLOB,Pno TEXT )");
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE Project ADD COLUMN other STRING");
    }
}
