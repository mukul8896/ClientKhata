package db_services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import BeanClasses.Bill;
import BeanClasses.Transection;

public class TransectionDbServices {
    public static void addTransectioin(Integer amount, Integer clientId, String desc, String date, String type) throws Exception {
        try {
            if (!type.equals("Credit") && !type.equals("Debit"))
                throw new Exception("Invalid transection type");
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "insert into ClientTransection (ClientID,TransectionType,Amount,TransectionDate,Description) " +
                    "values ('" + clientId + "','" + type + "','" + amount + "','" + date + "','" + desc + "')";
            stm.executeUpdate(query);
            con.commit();
            con.close();
            if (type.equals("Credit"))
                ClientDbServices.updateClientBalance(clientId, amount, -1);
            else
                ClientDbServices.updateClientBalance(clientId, amount, 1);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static List<Transection> getClientsTransections(int clientId) {
        List<Transection> list = new ArrayList<>();
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("select * from ClientTransection where ClientID='" + clientId + "'");
            while (rs.next()) {
                Transection transection = new Transection();
                transection.setTransecType(rs.getString("TransectionType"));
                transection.setDesc(rs.getString("Description"));
                transection.setAmount(rs.getInt("Amount"));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                Date date = simpleDateFormat.parse(rs.getString("TransectionDate"));
                transection.setDate(date);

                transection.setTransecId(rs.getInt("TransectionID"));
                transection.setBill_details(rs.getString("BillDetails"));
                list.add(transection);
            }
            Collections.sort(list);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void deleteTransection(int client_id, Transection transection) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("DELETE * FROM ClientTransection WHERE TransectionID=" + transection.getTransecId() + "");
            con.commit();
            con.close();
            ClientDbServices.updateClientBalance(client_id,
                    transection.getAmount(),
                    transection.getTransecType().equals("Credit") ? 1 : -1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not delete: Some error occurred !!");
        }
    }

    public static void updateTransection(Integer amount, Integer trasectionId, String desc, String date, String type, Integer clientId) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            stm.executeUpdate("UPDATE ClientTransection SET " +
                    "ClientTransection.ClientID = '" + clientId + "', " +
                    "ClientTransection.TransectionType = '" + type + "', " +
                    "ClientTransection.Amount = '" + amount + "', " +
                    "ClientTransection.TransectionDate = '" + date + "', " +
                    "ClientTransection.Description = '" + desc + "' " +
                    "WHERE ClientTransection.TransectionID=" + trasectionId + "");
            con.commit();
            con.close();
            ClientDbServices.updateClientBalance(clientId);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static int getBalanceOfClient(Integer clientId) {
        try {
            int total_balance = 0;
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where ClientID='" + clientId + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                if (rs.getString("TransectionType").equals("Credit"))
                    total_balance -= rs.getInt("Amount");
                else
                    total_balance += rs.getInt("Amount");
            }
            return total_balance;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static Transection getTransection(int transectionID) {
        Transection transection = new Transection();
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where TransectionID='" + transectionID + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                transection.setTransecId(rs.getInt("TransectionID"));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
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
        return transection;
    }

    public static void addBillDetailsToTransection(Transection transection, Bill bill) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String bill_details = bill.getBill_year() + " | Bill No-" + bill.getBill_no();
            stm.executeUpdate("UPDATE ClientTransection SET " +
                    "ClientTransection.BillDetails = '" + bill_details + "'" +
                    "WHERE ClientTransection.TransectionID='" + transection.getTransecId() + "'");
            con.commit();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
}
