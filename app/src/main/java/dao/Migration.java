package dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dbServices.BillDbServices;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;
import modals.Bill;
import modals.Client;
import modals.Transection;
import utils.ProjectUtils;

public class Migration {
    public static Connection getConnection() {
        Connection con = null;
        File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File file = new File(dir,"Database1.accdb");
            if (!file.exists())
                throw new Exception("Database file not exist !!");
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            con = DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath());
            return con;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return con;
    }
    public static List<Client> getClientsList() {
        List<Client> clientList = new ArrayList<>();
        try {
            String query = "select * from Client order by ClientName";
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Client order by ClientName");
            while (rs.next()) {
                Client client = new Client();
                client.setName(rs.getString("ClientName"));
                client.setAddress(rs.getString("Address"));
                client.setContact(rs.getString("ContactNo"));
                client.setFee(rs.getInt("ClientFee"));
                client.setBalance(rs.getInt("Balance"));
                clientList.add(client);
                ClientDbServices.addClient(client.getName(),client.getAddress(),client.getFee(),client.getContact());
            }
            con.close();
            return clientList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientList;
    }

    public static List<String> msClientname() {
        List<String> clientList = new ArrayList<>();
        try {
            String query = "select ClientName from Client";
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Client order by ClientName");
            while (rs.next()) {
                clientList.add(rs.getString("ClientName"));
            }
            con.close();
            return clientList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientList;
    }

    public static int getmsClientId(String name){
        int id = 0;
        try {
            String query = "select ClientID from Client where ClientName = '"+name+"'";
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                id=rs.getInt("ClientID");
            }
            con.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int getsqClientId(String name){
        int id=0;
        SQLiteDatabase db = DbHandler.getInstance().getReadableDatabase();
        String select = "SELECT "+DBParameters.KEY_CLIENT_ID+" FROM " + DBParameters.DB_CLIENT_TABLE + " WHERE "+DBParameters.KEY_CLIENT_NAME + "='"+name+"'";
        try(Cursor cursor = db.rawQuery(select, null)){
            if(cursor.moveToFirst()){
                do{
                    id=Integer.parseInt(cursor.getString(0));
                }while(cursor.moveToNext());
            }
            db.close();
        }catch (Exception e){
            e.printStackTrace();;
            Log.d("mk_logs", "Error while getting client");
        }
        return id;
    }

    public static List<Transection> getTransection(int msclientid,int sqclientid){
        List<Transection> list = new ArrayList<>();
        try {
            Connection con = getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("select * from ClientTransection where ClientID = "+msclientid);
            while (rs.next()) {
                Transection transection = new Transection();

                transection.setTransecType(rs.getString("TransectionType"));

                transection.setDesc(rs.getString("Description"));

                transection.setAmount(rs.getInt("Amount"));

                transection.setDate(ProjectUtils.parseStringToDate(rs.getString("TransectionDate"),"MMMM dd, yyyy"));

                transection.setBill_details(rs.getString("BillDetails"));

                transection.setClientId(sqclientid);

                try{
                    TransectionDbServices.addTransectioin(transection.getAmount(),
                            transection.getClientId(),
                            transection.getDesc(),
                            ProjectUtils.parseDateToString(transection.getDate(),"MMMM dd, yyyy"),
                            transection.getTransecType());
                }catch (Exception e){
                    Log.d("mk_logs","error in inserting " + transection);
                    e.printStackTrace();
                }
                //transection.setBillId(rs.getInt("BillID"));
            }
            Collections.sort(list);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Bill> getBill(Integer sqClientId,Integer msclientId) {
        List<Bill> bill_list = new ArrayList<>();
        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Bill where ClientId='" + msclientId + "'");
            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBill_no(rs.getInt("BillNo"));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                bill.setFrom_date(simpleDateFormat.parse(rs.getString("FromDate")));

                bill.setTo_date(simpleDateFormat.parse(rs.getString("ToDate")));

                bill.setBill_year(rs.getString("FinancialYear"));

                bill.setGenerationDate(rs.getString("GenerationDate"));

                bill.setClient_id(sqClientId);

                if (rs.getInt("IsShared") == 1)
                    bill.setBillShared(true);
                else
                    bill.setBillShared(false);
                System.out.println(bill);
                System.out.println(msclientId);
                BillDbServices.addBill(bill);

            }
            con.close();
            return bill_list;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("Database file not exist !!"))
                e.printStackTrace();
        }
        return bill_list;
    }


}

