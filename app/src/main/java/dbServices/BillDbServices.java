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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dao.DBParameters;
import dao.DbHandler;
import modals.Bill;
import modals.Client;
import modals.Transection;
import utils.ProjectUtils;

public class BillDbServices {

    public static Integer getMaxBillNo(String financialYear){
        int bill_no = 0;
        String query = "select max("+DBParameters.KEY_BILL_NO+") as BillNo from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_YEAR+"='" + financialYear + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.getCount()>0)
                cursor.moveToFirst();
            do{
                bill_no = cursor.getInt(0);
            }while(cursor.moveToNext());
            Log.d("mk_logs","Maximum bill no is: "+bill_no);
        }catch (Exception e){
            Log.d("mk_logs","error in max bill no");
        }
        return bill_no;
    }

    public static Integer getPreviousBalance(Integer clientId, Date from_date) {
        int previous_balance = 0;
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_CLIENTID+"='" + clientId + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.moveToFirst()){
                do{
                    Date trnsectiondate = ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy");
                    if (trnsectiondate.before(from_date)) {
                        if (cursor.getString(5).equals("Credit"))
                            previous_balance -= cursor.getInt(6);
                        else
                            previous_balance += cursor.getInt(6);
                    }
                }while(cursor.moveToNext());
            }
            Log.d("mk_logs","Previous balance is: "+previous_balance);
        }catch (Exception e){
            Log.d("mk_logs","error in get previous balance");
        }
        return previous_balance;
    }

    public static List<Transection> getBillParticulars(Integer clientId, Date from_date, Date to_date) {
        List<Transection> transections = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_CLIENTID+"='" + clientId + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.moveToFirst()){
                do{
                    Transection tr = new Transection();
                    Date transectiondate = ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy");

                    if ((transectiondate.after(from_date) && transectiondate.before(to_date))
                            || ProjectUtils.isDatesEqual(transectiondate, from_date)
                            || ProjectUtils.isDatesEqual(transectiondate, to_date)) {
                        tr.setDate(transectiondate);
                        tr.setTransecId(cursor.getInt(0));
                        tr.setClientId(cursor.getInt(2));
                        tr.setAmount(cursor.getInt(6));
                        tr.setDesc(cursor.getString(4));
                        tr.setTransecType(cursor.getString(5));
                        transections.add(tr);
                    }
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d("mk_logs","error in getBillParticulars");
        }
        return transections;
    }

    public static void addBill(Bill bill) {
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put(DBParameters.KEY_BILL_YEAR, bill.getBill_year());
            values.put(DBParameters.KEY_BILL_NO, bill.getBill_no());
            values.put(DBParameters.KEY_BILL_CLIENTID, bill.getClient_id());
            values.put(DBParameters.KEY_BILL_FROMDATE, ProjectUtils.parseDateToString(bill.getFrom_date(),"MMMM dd, yyyy"));
            values.put(DBParameters.KEY_BILL_TODATE,ProjectUtils.parseDateToString(bill.getTo_date(),"MMMM dd, yyyy"));
            values.put(DBParameters.KEY_BILL_GENERATIONDATE, bill.getGenerationDate());
            db.insert(DBParameters.DB_BILL_TABLE, null, values);
            Log.d("mk_logs", "Bill Successfully inserted");
        }catch (Exception e){
            Log.d("mk_logs","Error while inserting bill");
        }
    }

    public static List<Bill> getBillList(Integer clientId) {
        List<Bill> bill_list = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_CLIENTID+"='" + clientId + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.moveToFirst()){
                do{
                    Bill bill = new Bill();
                    bill.setBillId(cursor.getInt(0));
                    bill.setBill_no(cursor.getInt(1));
                    bill.setFrom_date(ProjectUtils.parseStringToDate(cursor.getString(4),"MMMM dd, yyyy"));
                    bill.setTo_date(ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy"));
                    bill.setBill_year(cursor.getString(2));
                    bill.setGenerationDate(cursor.getString(7));
                    bill.setBillShared(cursor.getInt(6) == 1);
                    bill_list.add(bill);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d("mk_logs","Error in getBillList");
        }
        return bill_list;
    }

    public static Bill getBill(int bill_id) {
        Bill bill = new Bill();
        String query = "select * from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_ID+"='" + bill_id + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null)){
            if(cursor.moveToFirst()){
                do{
                    bill.setBillId(cursor.getInt(0));
                    bill.setBill_no(cursor.getInt(1));
                    bill.setFrom_date(ProjectUtils.parseStringToDate(cursor.getString(4),"MMMM dd, yyyy"));
                    bill.setTo_date(ProjectUtils.parseStringToDate(cursor.getString(3),"MMMM dd, yyyy"));
                    bill.setBill_year(cursor.getString(2));
                    bill.setGenerationDate(cursor.getString(7));
                    bill.setClient_id(cursor.getInt(5));
                    bill.setBillShared(cursor.getInt(6) == 1);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d("mk_logs","Error in getBill");
        }
        return bill;
    }

    public static List<Transection> getBillTransection(String bill_details) {
        List<Transection> transections = new ArrayList<>();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_BILLDETAILS+"='" + bill_details + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
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
                    transections.add(transection);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d("mk_logs","Error in getBillTransection");
        }
        Collections.sort(transections);
        return transections;
    }
}
