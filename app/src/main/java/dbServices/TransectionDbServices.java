package dbServices;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dao.DBParameters;
import dao.DbHandler;
import modals.Bill;
import modals.Transection;
import utils.ProjectUtils;

public class TransectionDbServices {

    private DbHandler dbHandler;

    public TransectionDbServices(DbHandler handler){
        this.dbHandler=handler;
    }

    public void addTransectioin(Integer amount, Integer clientId, String desc, String date, String type) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TRANSECTION_CLIENTID, clientId);
        values.put(DBParameters.KEY_TRANSECTION_AMOUNT, amount);
        values.put(DBParameters.KEY_TRANSECTION_DATE, date);
        values.put(DBParameters.KEY_TRANSECTION_DESC, desc);
        values.put(DBParameters.KEY_TRANSECTION_TYPE,type);

        db.insert(DBParameters.DB_TRANSECTION_TABLE, null, values);
        Log.d("mk_logs", "Transection Successfully added");
        db.close();

        ClientDbServices services=new ClientDbServices(dbHandler);
        if (type.equals("Credit"))
            services.updateClientBalance(clientId, amount, -1);
        else
            services.updateClientBalance(clientId, amount, 1);
    }

    public List<Transection> getClientsTransections(int clientId) {
        List<Transection> list = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_CLIENTID+ "="+clientId;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                Transection transection = new Transection();
                transection.setTransecType(cursor.getString(5));
                transection.setDesc(cursor.getString(4));
                transection.setAmount(cursor.getInt(6));
                transection.setClientId(Integer.parseInt(cursor.getString(2)));
                transection.setDate(ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy"));
                transection.setTransecId(Integer.parseInt(cursor.getString(0)));
                transection.setBill_details(cursor.getString(7));
                list.add(transection);
            }while(cursor.moveToNext());
        }
        return list;
    }

    public void deleteTransection(int client_id, Transection transection)  {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.delete(DBParameters.DB_TRANSECTION_TABLE, DBParameters.KEY_TRANSECTION_ID +"=?", new String[]{String.valueOf(transection.getTransecId())});
        db.close();

        ClientDbServices services=new ClientDbServices(dbHandler);
        services.updateClientBalance(client_id,
                transection.getAmount(),
                transection.getTransecType().equals("Credit") ? 1 : -1);

    }

    public int updateTransection(Integer amount, Integer trasectionId, String desc, String date, String type, Integer clientId) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TRANSECTION_CLIENTID, clientId);
        values.put(DBParameters.KEY_TRANSECTION_AMOUNT, amount);
        values.put(DBParameters.KEY_TRANSECTION_DATE, date);
        values.put(DBParameters.KEY_TRANSECTION_DESC, desc);
        values.put(DBParameters.KEY_TRANSECTION_TYPE,type);
        //Lets update now
        int rowsEffected=db.update(DBParameters.DB_TRANSECTION_TABLE, values, DBParameters.KEY_TRANSECTION_ID + "=?",
                new String[]{String.valueOf(trasectionId)});
        db.close();
        return rowsEffected;
    }

    public int getBalanceOfClient(Integer clientId) {
        int total_balance = 0;
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_CLIENTID+ "="+clientId;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                    if (cursor.getString(5).equals("Credit"))
                        total_balance -= cursor.getInt(6);
                    else
                        total_balance += cursor.getInt(6);

            }while(cursor.moveToNext());
        }
        return total_balance;
    }

    public Transection getTransection(int transectionID) {
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_ID+ "="+transectionID;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Transection transection = new Transection();

        if(cursor.moveToFirst()){
            do{
                transection.setTransecType(cursor.getString(5));
                transection.setDesc(cursor.getString(4));
                transection.setAmount(cursor.getInt(6));
                transection.setClientId(Integer.parseInt(cursor.getString(2)));
                transection.setDate(ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy"));
                transection.setTransecId(Integer.parseInt(cursor.getString(0)));
                transection.setBill_details(cursor.getString(7));
            }while(cursor.moveToNext());
        }
        return transection;
    }

    public int addBillDetailsToTransection(Transection transection, String  billdetails) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TRANSECTION_BILLDETAILS, billdetails);

        int rowsEffected=db.update(DBParameters.DB_TRANSECTION_TABLE, values, DBParameters.KEY_TRANSECTION_ID + "=?",
                new String[]{String.valueOf(transection.getTransecId())});

        db.close();
        return rowsEffected;

    }

    public List<Transection> getFinancialYearTransection(String financilaYear){
        List<Transection> list = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                Transection transection = new Transection();
                transection.setTransecType(cursor.getString(5));
                transection.setDesc(cursor.getString(4));
                transection.setAmount(cursor.getInt(6));
                transection.setClientId(Integer.parseInt(cursor.getString(2)));
                transection.setDate(ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy"));
                transection.setTransecId(Integer.parseInt(cursor.getString(0)));
                transection.setBill_details(cursor.getString(7));

                int year=Integer.parseInt(financilaYear.split("-")[0].trim());

                Calendar calendar=Calendar.getInstance();
                calendar.set(year,Calendar.APRIL,1);
                Date first_date= calendar.getTime();

                calendar.set(year+1,Calendar.MARCH,31);
                Date last_date=calendar.getTime();

                if((transection.getDate().after(first_date) && transection.getDate().before(last_date))
                        || ProjectUtils.isDatesEqual(transection.getDate(), first_date)
                        || ProjectUtils.isDatesEqual(transection.getDate(), last_date)){
                    list.add(transection);
                }

                list.add(transection);
            }while(cursor.moveToNext());
        }
        return list;
    }
}
