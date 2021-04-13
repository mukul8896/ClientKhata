package dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

public class DbHandler extends SQLiteOpenHelper {
    private static DbHandler dbInstanse;
    private static Context context;
    private DbHandler(Context context) {
        super(context, DBParameters.DB_NAME, null, DBParameters.DB_VERSION);
        this.context=context;
        this.getReadableDatabase();
    }

    public static synchronized DbHandler getInstance(Context context) {
        if (dbInstanse == null) {
            dbInstanse = new DbHandler(context.getApplicationContext());
        }
        return dbInstanse;
    }

    public static synchronized DbHandler getInstance() {
        if (dbInstanse == null) {
            dbInstanse = new DbHandler(context);
        }
        return dbInstanse;
    }


    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createClientTable = "CREATE TABLE " + DBParameters.DB_CLIENT_TABLE + "("
                + DBParameters.KEY_CLIENT_ID + " INTEGER PRIMARY KEY,"
                + DBParameters.KEY_CLIENT_NAME + " INTEGER, "
                + DBParameters.KEY_CLIENT_ADDRESS + " TEXT, "
                + DBParameters.KEY_CLIENT_BALANCE + " INTEGER, "
                + DBParameters.KEY_CLIENT_CONTACT + " TEXT, "
                + DBParameters.KEY_CLIENT_FEE + " INTEGER "
                + ")";
        Log.d("mk_logs", "Client table creation query: "+createClientTable);
        db.execSQL(createClientTable);
        Log.d("mk_logs", "Client table Created Successfully");

        String createBillTable = "CREATE TABLE " + DBParameters.DB_BILL_TABLE + "("
                + DBParameters.KEY_BILL_ID + " INTEGER PRIMARY KEY,"
                + DBParameters.KEY_BILL_NO + " INTEGER, "
                + DBParameters.KEY_BILL_YEAR + " TEXT, "
                + DBParameters.KEY_BILL_TODATE + " TEXT, "
                + DBParameters.KEY_BILL_FROMDATE + " TEXT, "
                + DBParameters.KEY_BILL_CLIENTID + " INTEGER references "+DBParameters.DB_CLIENT_TABLE+"("+DBParameters.KEY_CLIENT_ID+"),"
                + DBParameters.KEY_BILL_ISSHARED + " INTEGER, "
                + DBParameters.KEY_BILL_GENERATIONDATE + " INTEGER "
                + ")";
        Log.d("mk_logs", "Bill table creation query: "+createClientTable);
        db.execSQL(createBillTable);
        Log.d("mk_logs", "Bill table Created Successfully");

        String createTransectionTable = "CREATE TABLE " + DBParameters.DB_TRANSECTION_TABLE + "("
                + DBParameters.KEY_TRANSECTION_ID + " INTEGER PRIMARY KEY,"
                + DBParameters.KEY_TRANSECTION_BILLID + " INTEGER references "+DBParameters.DB_BILL_TABLE+"("+DBParameters.KEY_BILL_ID+"),"
                + DBParameters.KEY_CLIENT_ID + " INTEGER references "+DBParameters.DB_CLIENT_TABLE+"("+DBParameters.KEY_CLIENT_ID+") ON DELETE CASCADE,"
                + DBParameters.KEY_TRANSECTION_DATE + " TEXT, "
                + DBParameters.KEY_TRANSECTION_DESC + " TEXT, "
                + DBParameters.KEY_TRANSECTION_TYPE + " TEXT, "
                + DBParameters.KEY_TRANSECTION_AMOUNT + " INTEGER, "
                + DBParameters.KEY_TRANSECTION_BILLDETAILS + " TEXT "
                + ")";
        Log.d("mk_logs", "Transection table creation query: "+createClientTable);
        db.execSQL(createTransectionTable);
        Log.d("mk_logs", "Transection table Created Successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Do Nothing
    }
}


