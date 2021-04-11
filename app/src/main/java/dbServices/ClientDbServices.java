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
import java.util.Date;
import java.util.List;

import modals.Client;
import dao.DBParameters;
import dao.DbHandler;

public class ClientDbServices {
    private DbHandler dbHandler;

    public ClientDbServices(DbHandler dbHandler){
        this.dbHandler=dbHandler;
    }

    public void addClient(String name, String adress,Integer fee,String contact) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_NAME, name);
        values.put(DBParameters.KEY_CLIENT_ADDRESS, adress);
        values.put(DBParameters.KEY_CLIENT_CONTACT, contact);
        values.put(DBParameters.KEY_CLIENT_FEE, fee);
        values.put(DBParameters.KEY_CLIENT_BALANCE,0);
        db.insert(DBParameters.DB_CLIENT_TABLE, null, values);
        Log.d("mk_logs", "Successfully inserted");
        db.close();
    }

    public int updateClient(String name, String adress,Integer fee,String contact, Integer id) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
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

    public List<Client> getClientsList(String interval, String filter_type) throws Exception {
        List<Client> filteredClientList = new ArrayList<>();
        List<Client> allClients=getClientsList(dbHandler);
        for(Client client:allClients) {
            if (interval.equals("all")) {
                client.setBalance(client.getBalance());
                filteredClientList.add(client);
            } else {
                Integer balance = getClientBalanceWithinInterval(client.getId(), interval, filter_type);
                if (balance > 0) {
                    client.setBalance(balance);
                    filteredClientList.add(client);
                }
            }
        }
        return filteredClientList;
    }

    public void deleteClient(int id) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.delete(DBParameters.DB_CLIENT_TABLE, DBParameters.KEY_CLIENT_ID +"=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Client getClient(int id){
        Client client = new Client();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String select = "SELECT * FROM " + DBParameters.DB_CLIENT_TABLE + " WHERE "+DBParameters.KEY_CLIENT_ID + "=" + id;
        Cursor cursor = db.rawQuery(select, null);
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
        return client;
    }

    public int updateClientBalance(Integer clientId, Integer amount, Integer operator) {
        int current_balance = getClientBalance(clientId);
        Integer updated_balance = current_balance + (amount * operator);

        SQLiteDatabase db = dbHandler.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_BALANCE,updated_balance);
        //Lets update now
        int rowsEffected=db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)});
        db.close();
        return rowsEffected;
    }

    public int updateClientBalance(Integer clientId) throws Exception {
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        TransectionDbServices transectionDbServices=new TransectionDbServices(dbHandler);
        int balance = transectionDbServices.getBalanceOfClient(clientId);

        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_BALANCE,balance);

        //Lets update now
        int rowsEffected = db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)});
        db.close();
        return rowsEffected;

    }

    public int getClientBalance(Integer clientId) {
        int balance=0;
        List<Client> clientList = new ArrayList<>();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String select = "SELECT Balance FROM " + DBParameters.DB_CLIENT_TABLE + " WHERE "+DBParameters.KEY_CLIENT_ID + "=" + clientId;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.moveToFirst()){
            do{
                balance = Integer.parseInt(cursor.getString(0));
            }while(cursor.moveToNext());
        }
        db.close();
        return balance;
    }

    public Integer getClientBalanceWithinInterval(Integer clientID, String interval, String filter_type) throws Exception {
        int total_balance = 0;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = "select * from "+DBParameters.DB_TRANSECTION_TABLE+" where "+DBParameters.KEY_CLIENT_ID+"='" + clientID + "'";
        Cursor cursor = db.rawQuery(query, null);

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
        db.close();
        return total_balance;
    }

    //================================================================================================//
    public static List<Client> getClientsList(DbHandler handler){
        List<Client> contactList = new ArrayList<>();
        SQLiteDatabase db = handler.getReadableDatabase();
        // Generate the query to read from the database
        String select = "SELECT * FROM " + DBParameters.DB_CLIENT_TABLE;
        Cursor cursor = db.rawQuery(select, null);

        //Loop through now
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
        db.close();
        return contactList;
    }

    public int updateClient(Client client){
        SQLiteDatabase db = dbHandler.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_NAME, client.getName());
        values.put(DBParameters.KEY_CLIENT_ADDRESS, client.getAddress());
        values.put(DBParameters.KEY_CLIENT_CONTACT, client.getContact());
        values.put(DBParameters.KEY_CLIENT_FEE, client.getFee());
        values.put(DBParameters.KEY_CLIENT_BALANCE, client.getBalance());

        //Lets update now
        return db.update(DBParameters.DB_CLIENT_TABLE, values, DBParameters.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(client.getId())});
    }

    public void deleteClientById(int clientId){
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.delete(DBParameters.DB_CLIENT_TABLE, DBParameters.KEY_CLIENT_ID +"=?", new String[]{String.valueOf(clientId)});
        db.close();
    }

    public void deleteContact(Client client){
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.delete(DBParameters.DB_CLIENT_TABLE, DBParameters.KEY_CLIENT_ID +"=?", new String[]{String.valueOf(client.getId())});
        db.close();
    }

    public int getCount(){
        String query = "SELECT  * FROM " + DBParameters.DB_CLIENT_TABLE;
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    public void addClient(Client client){
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBParameters.KEY_CLIENT_NAME, client.getName());
        values.put(DBParameters.KEY_CLIENT_ADDRESS, client.getAddress());
        values.put(DBParameters.KEY_CLIENT_CONTACT, client.getContact());
        values.put(DBParameters.KEY_CLIENT_FEE, client.getFee());
        values.put(DBParameters.KEY_CLIENT_BALANCE, client.getBalance());
        db.insert(DBParameters.DB_CLIENT_TABLE, null, values);
        Log.d("mk_logs", "Successfully inserted");
        db.close();
    }

}
