package DbConnect;

import android.util.Log;

import com.mukul.clientbilling.MainActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import BeanClasses.Client;
import BeanClasses.ClientAndBalance;
import BeanClasses.Transection;

public class DBServices {

    public static void addClient(String name,String adress,String contact) throws Exception {
        try{
            if(!name.matches("[0-9a-zA-Z\\s.-]+"))
                throw new Exception("Invalid Name !!");
            if(!contact.matches("[+]?[0-9\\s]+"))
                throw new Exception("Invalid Contact number");
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("insert into Client (ClientName,Address,ContactNo) " +
                    "values ('"+name+"','"+adress+"','"+contact+"')");
            con.commit();
            con.close();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static void updateClient(String name,String adress,String contact,Integer id) throws Exception {
        try{
            if(!name.matches("[0-9a-zA-Z\\s.-]+"))
                throw new Exception("Invalid Name !!");
            if(!contact.matches("[+]?[0-9\\s]+"))
                throw new Exception("Invalid Contact number");
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("UPDATE Client SET Client.ClientName = '"+name+"', Client.Address = '"+adress+"', Client.ContactNo = '"+contact+"' WHERE Client.ClientID="+id+"");
            con.commit();
            con.close();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static List<Client> getClientsList(){
        List<Client> clientList=new ArrayList<>();
        try {
            Connection con = DBConnect.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery("select * from Client order by ClientName");
            while (rs.next()){
                Client client=new Client();
                client.setName(rs.getString("ClientName"));
                client.setBalance(rs.getInt("Balance"));
                client.setId(rs.getInt("ClientID"));
                client.setAddress(rs.getString("Address"));
                client.setContact(rs.getString("ContactNo"));
                clientList.add(client);
            }
            Log.i(MainActivity.class.getSimpleName(), "");
            con.close();
            return  clientList;
        }catch (Exception e){
            e.printStackTrace();
            return clientList;
        }
    }
//
    public static void deleteClient(int id) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("DELETE * FROM Client WHERE ClientID="+id+"");
            con.commit();
            con.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }

    public static Client getClient(int id){
        Client client=new Client();
        client.setId(id);
        try {
            Connection con = DBConnect.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery("select * from Client where ClientID='"+id+"'");
            while (rs.next()){
                client.setName(rs.getString("ClientName"));
                client.setAddress(rs.getString("Address"));
                client.setContact(rs.getString("ContactNo"));
                Log.i(DBServices.class.getSimpleName(),rs.getString("ContactNo"));
            }
            Log.i(MainActivity.class.getSimpleName(), "");
            con.close();
            return  client;
        }catch (Exception e){
            e.printStackTrace();
            return client;
        }
    }

    public static void addTransectioin(Integer amount, Integer clientId, String desc, String date,String type) throws Exception {
        try{
            if(!date.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}"))
                throw new Exception("Invalid date formate!!");
            if(!type.equals("Credit") && !type.equals("Debit"))
                throw new Exception("Invalid transection type");
            Connection con=DBConnect.getConnection();
            Statement stm=con.createStatement();
            String query="insert into ClientTransection (ClientID,TransectionType,Amount,TransectionDate,Description) " +
                    "values ('"+clientId+"','"+type+"','"+amount+"','"+date+"','"+desc+"')";
            stm.executeUpdate(query);
            con.commit();
            con.close();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static List<Transection> getClientsTransections(int id){
        List<Transection> list=new ArrayList<>();
        try{
            Connection con=DBConnect.getConnection();
            Statement stm=con.createStatement();
            ResultSet rs=stm.executeQuery("select * from ClientTransection where ClientID='"+id+"'");
            while(rs.next()){
                Transection transection=new Transection();
                transection.setTransecType(rs.getString("TransectionType"));
                transection.setDesc(rs.getString("Description"));
                transection.setAmount(rs.getInt("Amount"));
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
                Date date=simpleDateFormat.parse(rs.getString("TransectionDate"));
                transection.setDate(date);
                transection.setTransecId(rs.getInt("TransectionID"));
                list.add(transection);
            }
            con.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public static void deleteTransection(int id) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("DELETE * FROM ClientTransection WHERE TransectionID="+id+"");
            con.commit();
            con.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }
}
