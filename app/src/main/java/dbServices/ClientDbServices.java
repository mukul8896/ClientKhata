package dbServices;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
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

import modals.Client;
import dao.DBParameters;
import dao.DbHandler;

public class ClientDbServices {
    public static void addClient(String name, String adress,Integer fee,String contact)  {
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put(DBParameters.KEY_CLIENT_NAME, name);
            values.put(DBParameters.KEY_CLIENT_ADDRESS, adress);
            values.put(DBParameters.KEY_CLIENT_CONTACT, contact);
            values.put(DBParameters.KEY_CLIENT_FEE, fee);
            values.put(DBParameters.KEY_CLIENT_BALANCE,0);
            db.insert(DBParameters.DB_CLIENT_TABLE, null, values);
            Log.d("mk_logs", "Successfully inserted");
        }catch (Exception e) {
            Log.d("mk_logs", "Error while adding company");
            e.printStackTrace();
        }
    }

    public static int updateClient(String name, String adress,Integer fee,String contact, Integer id) throws Exception {
        SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_NAME, name);
        values.put(DBParameters.KEY_CLIENT_ADDRESS, adress);
        values.put(DBParameters.KEY_CLIENT_CONTACT, contact);
        values.put(DBParameters.KEY_CLIENT_FEE, fee);
        //Lets update now
        int rowsEffected=db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsEffected;
    }

    public static List<Client> getClientsList(String interval, String filter_type) {
        List<Client> filteredClientList = new ArrayList<>();
        List<Client> allClients=getClientsList();
        for(Client client:allClients) {
            if (interval.equals("all")) {
                filteredClientList.add(client);
            } else {
                Integer balance = getClientBalanceWithinInterval(client.getId(), interval, filter_type);
                if (balance > 0) {
                    client.setBalance(balance);
                    filteredClientList.add(client);
                }
            }
        }
        Collections.sort(filteredClientList);
        return filteredClientList;
    }

    public static boolean deleteClient(int clientID) throws SQLiteConstraintException {
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();){
            db.delete(DBParameters.DB_CLIENT_TABLE, DBParameters.KEY_CLIENT_ID +"=?", new String[]{String.valueOf(clientID)});
            Log.d("mk_logs", "Deleted successfully ");
            return true;
        }catch (Exception e){
            Log.d("mk_logs", "Error while deleting company");
            e.printStackTrace();
            return false;
        }
    }

    public static Client getClient(int id){
        Client client = new Client();
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
        String select = "SELECT * FROM " + DBParameters.DB_CLIENT_TABLE + " WHERE "+DBParameters.KEY_CLIENT_ID + "=" + id;
        try(Cursor cursor = db.rawQuery(select, null)){
            if(cursor.moveToFirst()){
                do{
                    client.setId(Integer.parseInt(cursor.getString(0)));
                    client.setName(cursor.getString(1));
                    client.setAddress(cursor.getString(2));
                    client.setBalance(Integer.parseInt(cursor.getString(3)));
                    client.setContact(cursor.getString(4));
                    client.setFee(Integer.parseInt(cursor.getString(5)));
                }while(cursor.moveToNext());
            }
            db.close();
        }catch (Exception e){
            e.printStackTrace();;
            Log.d("mk_logs", "Error while getting client");
        }
        return client;
    }

    public static int updateClientBalance(Integer clientId, Integer amount, Integer operator) {
        int current_balance = getClientBalance(clientId);
        int rowsEffected=0;
        Integer updated_balance = current_balance + (amount * operator);
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put(DBParameters.KEY_CLIENT_BALANCE,updated_balance);
            //Lets update now
            rowsEffected=db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                    new String[]{String.valueOf(clientId)});
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting client balance");
        }
        return rowsEffected;
    }

    public static int updateClientBalance(Integer clientId) {
        int balance = TransectionDbServices.getBalanceOfClient(clientId);
        int rowsEffected = 0;
        try(SQLiteDatabase db = DbHandler.getInstance().getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put(DBParameters.KEY_CLIENT_BALANCE,balance);

            rowsEffected = db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                    new String[]{String.valueOf(clientId)});
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting client");
        }
        return rowsEffected;

    }

    public static int getClientBalance(Integer clientId) {
        int balance=0;
        String select = "SELECT Balance FROM " + DBParameters.DB_CLIENT_TABLE + " WHERE "+DBParameters.KEY_CLIENT_ID + "=" + clientId;
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(select, null)){
            if(cursor.moveToFirst()){
                do{
                    balance = Integer.parseInt(cursor.getString(0));
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting client balance");
        }
        return balance;
    }

    public static Integer getClientBalanceWithinInterval(Integer clientID, String interval, String filter_type){
        int total_balance = 0;
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_CLIENT_ID+"='" + clientID + "'";
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);){
            if(cursor.moveToFirst()){
                do{
                    String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                    Date transectiondate = simpleDateFormat.parse(cursor.getString(3));
                    Calendar calender = Calendar.getInstance();
                    calender.setTime(transectiondate);
                    if (monthNames[calender.get(Calendar.MONTH)].equals(interval)) {
                        if (filter_type.equals("Credit")) {
                            if (cursor.getString(5).equals("Credit"))
                                total_balance += cursor.getInt(6);
                        } else if (filter_type.equals("Debit")) {
                            if (cursor.getString(5).equals("Debit"))
                                total_balance += cursor.getInt(6);
                        } else if (filter_type.equals("Balance")) {
                            if (cursor.getString(5).equals("Credit"))
                                total_balance -= cursor.getInt(6);
                            else
                                total_balance += cursor.getInt(6);
                        }
                    }
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting client balance within interval");
        }
        return total_balance;
    }

    public static List<Client> getClientsList(){
        List<Client> contactList = new ArrayList<>();
        String select = "SELECT * FROM " + DBParameters.DB_CLIENT_TABLE;
        try(SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
            Cursor cursor = db.rawQuery(select, null);){
            if(cursor.moveToFirst()){
                do{
                    Client client = new Client();
                    client.setId(Integer.parseInt(cursor.getString(0)));
                    client.setName(cursor.getString(1));
                    client.setAddress(cursor.getString(2));
                    client.setBalance(Integer.parseInt(cursor.getString(3)));
                    client.setContact(cursor.getString(4));
                    client.setFee(Integer.parseInt(cursor.getString(5)));
                    contactList.add(client);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("mk_logs", "Error while getting client list");
        }
        return contactList;
    }
}
