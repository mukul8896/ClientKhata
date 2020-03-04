package db_services;

import android.util.Log;

import com.mukul.clientbilling.MainActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import BeanClasses.Client;
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
            deleteAllTransectionOFClient(id);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }

    public static void deleteAllTransectionOFClient(Integer clientID) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("DELETE * FROM ClientTransection WHERE ClientID="+clientID+"");
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
            if(type.equals("Credit"))
                updateClientBalance(clientId,amount,-1);
            else
                updateClientBalance(clientId,amount,1);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static void updateClientBalance(Integer clientId,Integer amount,Integer operator) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            Integer current_balance=getClientBalance(clientId);
            Integer updated_balance=current_balance+(amount*operator);
            stm.executeUpdate("UPDATE Client SET Client.Balance = '"+updated_balance+"' WHERE Client.ClientID="+clientId+"");
            con.commit();
            con.close();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static int getClientBalance(Integer clientId){
        try {
            Integer balance=0;
            Connection con = DBConnect.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery("select Balance from Client where ClientID='"+clientId+"'");
            while (rs.next()){
                balance=rs.getInt("Balance");
                Log.i(DBServices.class.getSimpleName(),"inside getClientBalance method");
            }
            con.close();
            return  balance;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
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

    public static void deleteTransection(Client client,Transection transection) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("DELETE * FROM ClientTransection WHERE TransectionID="+transection.getTransecId()+"");
            con.commit();
            con.close();
            updateClientBalance(client.getId(),
                    transection.getAmount(),
                    transection.getTransecType().equals("Credit")?-1:1);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }

    public static void updateTransection(Integer amount, Integer trasectionId, String desc, String date,String type,Integer clientId) throws Exception{
        try{
            if(!date.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}"))
                throw new Exception("Invalid date formate!!");
            if(!type.equals("Credit") && !type.equals("Debit"))
                throw new Exception("Invalid transection type");
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            stm.executeUpdate("UPDATE ClientTransection SET " +
                    "ClientTransection.ClientID = '"+clientId+"', " +
                    "ClientTransection.TransectionType = '"+type+"', " +
                    "ClientTransection.Amount = '"+amount+"', " +
                    "ClientTransection.TransectionDate = '"+date+"', " +
                    "ClientTransection.Description = '"+desc+"' " +
                    "WHERE ClientTransection.TransectionID="+trasectionId+"");
            con.commit();
            con.close();
            updateClientBalance(clientId);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static void updateClientBalance(Integer clientId) throws Exception{
        try{
            Connection con= DBConnect.getConnection();
            Statement stm=con.createStatement();
            int balance=getTransectionsSumOfClient(clientId);
            stm.executeUpdate("UPDATE Client SET Client.Balance = '"+balance+"' WHERE Client.ClientID="+clientId+"");
            con.commit();
            con.close();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static int getTransectionsSumOfClient(Integer clientId){
        try{
            int total_balance=0;
            Connection con=DBConnect.getConnection();
            Statement stm=con.createStatement();
            String query="select * from ClientTransection where ClientID='"+clientId+"'";
            ResultSet rs=stm.executeQuery(query);
            while (rs.next()){
                if(rs.getString("TransectionType").equals("Credit"))
                    total_balance+=rs.getInt("Amount");
                else
                    total_balance-=rs.getInt("Amount");
            }
            return total_balance;
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public static Transection getTransection(int transectionID) {
        Transection transection = new Transection();
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where TransectionID='"+transectionID+"'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                transection.setTransecId(rs.getInt("TransectionID"));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = simpleDateFormat.parse(rs.getString("TransectionDate"));
                transection.setDate(date);

                transection.setClientId(rs.getInt("ClientID"));
                transection.setAmount(rs.getInt("Amount"));
                transection.setDesc(rs.getString("Description"));
                transection.setTransecType(rs.getString("TransectionType"));
                return transection;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return  transection;
    }
}
