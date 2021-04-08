package db_services;

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

import BeanClasses.Client;
import dao.DBParameters;
import dao.DbHandler;

public class ClientDbServices {
    DbHandler dbHandler;

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

    public static void updateClient(String name, String adress,Integer fee,String contact, Integer id) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("UPDATE Client SET Client.ClientName = '" + name + "', Client.Address = '" + adress + "', Client.ContactNo = '" + contact + "', Client.ClientFee = '"+fee+"' WHERE Client.ClientID=" + id + "");
            con.commit();
            con.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static List<Client> getClientsList(String interval, String filter_type) throws Exception {
        List<Client> clientList = new ArrayList<>();
        try {
            String query = "select * from Client order by ClientName";
            Connection con = DBConnect.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Client order by ClientName");
            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("ClientID"));
                client.setName(rs.getString("ClientName"));
                client.setAddress(rs.getString("Address"));
                client.setContact(rs.getString("ContactNo"));
                client.setFee(rs.getInt("ClientFee"));
                if (interval.equals("all")) {
                    client.setBalance(rs.getInt("Balance"));
                    clientList.add(client);
                } else {
                    Integer balance = getClientBalanceWithinInterval(rs.getInt("ClientID"), interval, filter_type);
                    if (balance > 0) {
                        client.setBalance(balance);
                        clientList.add(client);
                    }
                }
            }
            con.close();
            return clientList;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("Database file not exist !!"))
                throw new Exception(e.getMessage());
        }
        return clientList;
    }

    //
    public static void deleteClient(int id) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("DELETE * FROM Client WHERE ClientID=" + id + "");
            //con.commit();
            con.close();
            //deleteAllTransectionOFClient(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }

    public static Client getClient(int id) {
        Client client = new Client();
        client.setId(id);
        try {
            Connection con = DBConnect.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Client where ClientID='" + id + "'");
            while (rs.next()) {
                client.setName(rs.getString("ClientName"));
                client.setAddress(rs.getString("Address"));
                client.setContact(rs.getString("ContactNo"));
                client.setFee(rs.getInt("ClientFee"));
            }
            con.close();
            return client;
        } catch (Exception e) {
            e.printStackTrace();
            return client;
        }
    }

    public static void updateClientBalance(Integer clientId, Integer amount, Integer operator) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            Integer current_balance = getClientBalance(clientId);
            Integer updated_balance = current_balance + (amount * operator);
            stm.executeUpdate("UPDATE Client SET Client.Balance = '" + updated_balance + "' WHERE Client.ClientID=" + clientId + "");
            con.commit();
            con.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static void updateClientBalance(Integer clientId) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            int balance = TransectionDbServices.getBalanceOfClient(clientId);
            stm.executeUpdate("UPDATE Client SET Client.Balance = '" + balance + "' WHERE Client.ClientID=" + clientId + "");
            con.commit();
            con.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static int getClientBalance(Integer clientId) {
        try {
            Integer balance = 0;
            Connection con = DBConnect.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select Balance from Client where ClientID='" + clientId + "'");
            while (rs.next()) {
                balance = rs.getInt("Balance");
            }
            con.close();
            return balance;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static Integer getClientBalanceWithinInterval(Integer clientID, String interval, String filter_type) throws Exception {
        try {
            int total_balance = 0;
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where ClientID='" + clientID + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                Date transectiondate = simpleDateFormat.parse(rs.getString("TransectionDate"));
                Calendar calender = Calendar.getInstance();
                calender.setTime(transectiondate);
                if (monthNames[calender.get(Calendar.MONTH)].equals(interval)) {
                    if (filter_type.equals("Credit")) {
                        if (rs.getString("TransectionType").equals("Credit"))
                            total_balance += rs.getInt("Amount");
                    } else if (filter_type.equals("Debit")) {
                        if (rs.getString("TransectionType").equals("Debit"))
                            total_balance += rs.getInt("Amount");
                    } else if (filter_type.equals("Balance")) {
                        if (rs.getString("TransectionType").equals("Credit"))
                            total_balance -= rs.getInt("Amount");
                        else
                            total_balance += rs.getInt("Amount");
                    }
                }
            }
            con.close();
            return total_balance;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error in previus balance !!");
        }
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
