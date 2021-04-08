package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import BeanClasses.Client;

public class DbHandler extends SQLiteOpenHelper {

    public DbHandler(Context context) {
        super(context, DBParameters.DB_NAME, null, DBParameters.DB_VERSION);
        this.getReadableDatabase();
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}


