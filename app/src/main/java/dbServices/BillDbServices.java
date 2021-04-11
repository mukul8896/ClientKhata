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
    private DbHandler dbHandler;

    public BillDbServices(DbHandler dbHandler) {
        this.dbHandler=dbHandler;
    }

    public Integer getMaxBillNo(String financialYear){
        int bill_no = 0;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = "select max("+DBParameters.KEY_BILL_NO+") as BillNo from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_YEAR+"='" + financialYear + "'";
        Cursor cursor = db.rawQuery(query, null);
        Log.d("mk_logs","No of rows"+cursor.getCount());
        if(cursor.getCount()>0)
            cursor.moveToFirst();
        do{
            bill_no = cursor.getInt(0);
        }while(cursor.moveToNext());

        db.close();
        return bill_no;
    }

    public  Integer getPreviousBalance(Integer clientId, Date from_date) {
        int previous_balance = 0;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_CLIENTID+"='" + clientId + "'";
        Cursor cursor = db.rawQuery(query, null);
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
        db.close();
        return previous_balance;
    }

    public List<Transection> getBillParticulars(Integer clientId, Date from_date, Date to_date) {
        List<Transection> transections = new ArrayList<>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_CLIENTID+"='" + clientId + "'";
        Cursor cursor = db.rawQuery(query, null);
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
        db.close();
        return transections;
    }

    public void addBill(Bill bill) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_BILL_YEAR, bill.getBill_year());
        values.put(DBParameters.KEY_BILL_NO, bill.getBill_no());
        values.put(DBParameters.KEY_BILL_CLIENTID, bill.getClient_id());
        values.put(DBParameters.KEY_BILL_FROMDATE, ProjectUtils.parseDateToString(bill.getFrom_date(),"MMMM dd, yyyy"));
        values.put(DBParameters.KEY_BILL_TODATE,ProjectUtils.parseDateToString(bill.getTo_date(),"MMMM dd, yyyy"));
        values.put(DBParameters.KEY_BILL_GENERATIONDATE, bill.getGenerationDate());

        db.insert(DBParameters.DB_BILL_TABLE, null, values);
        Log.d("mk_logs", "Bill Successfully inserted");
        db.close();
    }

    public List<Bill> getBillList(Integer clientId) {
        List<Bill> bill_list = new ArrayList<>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();

        String query = "select * from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_CLIENTID+"='" + clientId + "'";
        Cursor cursor = db.rawQuery(query, null);
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
        db.close();
        return bill_list;
    }

    /*public static void updateBillAsShared(Bill bill) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String bill_details = bill.getBill_year() + " | Bill No-" + bill.getBill_no();
            stm.executeUpdate("UPDATE Bill SET " +
                    "Bill.IsShared = '1'" +
                    "WHERE Bill.BillID='" + bill.getBillId() + "'");
            con.commit();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }*/

    public Bill getBill(int bill_id) {
        Bill bill = new Bill();
        SQLiteDatabase db = dbHandler.getReadableDatabase();

        String query = "select * from "+DBParameters.DB_BILL_TABLE+" where "+DBParameters.KEY_BILL_ID+"='" + bill_id + "'";
        Cursor cursor = db.rawQuery(query, null);
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
        db.close();
        return bill;
    }

    public List<Transection> getBillTransection(String bill_details) {
        List<Transection> transections = new ArrayList<>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_TRANSECTION_BILLDETAILS+"='" + bill_details + "'";
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
                transections.add(transection);
            }while(cursor.moveToNext());
        }
        Collections.sort(transections);
        db.close();
        return transections;
    }
}
