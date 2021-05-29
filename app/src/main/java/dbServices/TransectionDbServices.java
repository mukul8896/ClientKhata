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

    public static void addTransectioin(Integer amount, Integer clientId, String desc, String date, String type) throws Exception{
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put(DBParameters.KEY_TRANSECTION_CLIENTID, clientId);
            values.put(DBParameters.KEY_TRANSECTION_AMOUNT, amount);
            values.put(DBParameters.KEY_TRANSECTION_DATE, date);
            values.put(DBParameters.KEY_TRANSECTION_DESC, desc);
            values.put(DBParameters.KEY_TRANSECTION_TYPE,type);

            db.insert(DBParameters.DB_TRANSECTION_TABLE, null, values);
            Log.d("mk_logs", "Transection Successfully added");
        }catch (Exception e){
            throw e;
        }
        if (type.equals("Credit"))
            ClientDbServices.updateClientBalance(clientId, amount, -1);
        else
            ClientDbServices.updateClientBalance(clientId, amount, 1);
    }

    public static List<Transection> getClientsTransections(int clientId,String financialyear) {
        List<Transection> list = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_CLIENTID+ "="+clientId;
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
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
                if(financialyear.equalsIgnoreCase("all"))
                    list.add(transection);
                else{
                    int year=Integer.parseInt(financialyear.split("-")[0].trim());

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
                }
                Collections.sort(list);
            }while(cursor.moveToNext());
        }
        return list;
    }

    public static void deleteTransection(int client_id, Transection transection)  {
        SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();
        db.delete(DBParameters.DB_TRANSECTION_TABLE, DBParameters.KEY_TRANSECTION_ID +"=?", new String[]{String.valueOf(transection.getTransecId())});
        db.close();
        ClientDbServices.updateClientBalance(client_id,
                transection.getAmount(),
                transection.getTransecType().equals("Credit") ? 1 : -1);

    }

    public static int updateTransection(Integer amount, Integer trasectionId, String desc, String date, String type, Integer clientId) {
        SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();
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
        ClientDbServices.updateClientBalance(clientId);
        return rowsEffected;
    }

    public static int getBalanceOfClient(Integer clientId) {
        int total_balance = 0;
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_CLIENTID+ "="+clientId;
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
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

    public static Transection getTransection(int transectionID) {
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where " +DBParameters.KEY_TRANSECTION_ID+ "="+transectionID;
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
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

    public static int addBillDetailsToTransection(Transection transection, String  billdetails) throws Exception {
        SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TRANSECTION_BILLDETAILS, billdetails);

        int rowsEffected=db.update(DBParameters.DB_TRANSECTION_TABLE, values, DBParameters.KEY_TRANSECTION_ID + "=?",
                new String[]{String.valueOf(transection.getTransecId())});

        db.close();
        return rowsEffected;

    }

    public static List<Transection> getFinancialYearTransection(String financilaYear){
        List<Transection> list = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE;
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
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
            }while(cursor.moveToNext());
        }
        return list;
    }

    public static List<String> getAllUniqueDescription(){
        List<String> suggetionlist=new ArrayList<>();
        String query = "select distinct "+DBParameters.KEY_TRANSECTION_DESC+" from "+DBParameters.DB_TRANSECTION_TABLE;
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.moveToFirst()){
                do{
                    suggetionlist.add(cursor.getString(0));
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting suggetion list");
        }
        return suggetionlist;
    }

    public static int removeBillDetails(Transection transection){
        SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_TRANSECTION_BILLDETAILS, "");
        //Lets update now
        int rowsEffected=db.update(DBParameters.DB_TRANSECTION_TABLE, values, DBParameters.KEY_TRANSECTION_ID + "=?",
                new String[]{String.valueOf(transection.getTransecId())});
        db.close();
        return rowsEffected;
    }
}
