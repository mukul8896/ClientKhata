package db_services;

import android.util.Log;

import com.mukul.client_billing_activity.MainActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import BeanClasses.Client;

public class ClientDbServices {

    public static void addClient(String name, String adress, String contact) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("insert into Client (ClientName,Address,ContactNo) " +
                    "values ('" + name + "','" + adress + "','" + contact + "')");
            con.commit();
            con.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static void updateClient(String name, String adress, String contact, Integer id) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("UPDATE Client SET Client.ClientName = '" + name + "', Client.Address = '" + adress + "', Client.ContactNo = '" + contact + "' WHERE Client.ClientID=" + id + "");
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
                if (interval.equals("all")) {
                    client.setBalance(rs.getInt("Balance"));
                } else {
                    Integer balance = getClientBalanceWithinInterval(rs.getInt("ClientID"), interval, filter_type);
                    if (balance > 0)
                        client.setBalance(balance);
                }

                clientList.add(client);
            }
            Log.i(MainActivity.class.getSimpleName(), "");
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
                Log.i(ClientDbServices.class.getSimpleName(), rs.getString("ContactNo"));
            }
            Log.i(MainActivity.class.getSimpleName(), "");
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
            return total_balance;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error in previus balance !!");
        }
    }
}
