package db_services;

import android.util.Log;

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
import utils.GeneralUtils;

public class BillDbServices {
    public static Integer getMaxBillNo(String financialYear) throws Exception {
        Integer bill_no = 0;
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select max(BillNo) as BillNo from Bill where FinancialYear='" + financialYear + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                bill_no = rs.getInt("BillNo");
                return bill_no;
            }
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error while getting bill no !!");
        }
        return bill_no;
    }

    public static Integer getPreviousBalance(Integer clientId, Date from_date) throws Exception {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            int total_balance = 0;
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where ClientID='" + clientId + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                Date trnsectiondate = simpleDateFormat.parse(rs.getString("TransectionDate"));
                if (trnsectiondate.before(from_date)) {
                    if (rs.getString("TransectionType").equals("Credit"))
                        total_balance -= rs.getInt("Amount");
                    else
                        total_balance += rs.getInt("Amount");
                }
            }
            con.close();
            return total_balance;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Erro in previus balance !!");
        }
    }

    public static List<Transection> getBillParticulars(Integer clientId, Date from_date, Date to_date) throws Exception {
        List<Transection> transections = new ArrayList<>();
        try {
            int total_balance = 0;
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where ClientID='" + clientId + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                Transection tr = new Transection();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                Date transectiondate = simpleDateFormat.parse(rs.getString("TransectionDate"));
                if ((transectiondate.after(from_date) && transectiondate.before(to_date))
                        || GeneralUtils.isDatesEqual(transectiondate, from_date)
                        || GeneralUtils.isDatesEqual(transectiondate, to_date)) {
                    tr.setDate(transectiondate);
                    tr.setTransecId(rs.getInt("TransectionID"));
                    tr.setClientId(rs.getInt("ClientID"));
                    tr.setAmount(rs.getInt("Amount"));
                    tr.setDesc(rs.getString("Description"));
                    tr.setTransecType(rs.getString("TransectionType"));
                    transections.add(tr);
                }
            }
            con.close();
            Collections.sort(transections);
            return transections;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Erro in previus balance !!");
        }
    }

    public static void addBill(Bill bill) throws Exception {
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            String from = simpleDateFormat.format(bill.getFrom_date());
            String to = simpleDateFormat.format(bill.getTo_date());
            stm.executeUpdate("insert into Bill (FinancialYear,BillNo,ClientId,FromDate,ToDate,IsShared) " +
                    "values ('" + bill.getBill_year() + "','" + bill.getBill_no() + "','" + bill.getClient_id() + "','" + from + "','" + to + "','" + 0 + "')");
            con.commit();
            con.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static List<Bill> getBillList(Integer clientId) throws Exception {
        List<Bill> bill_list = new ArrayList<>();
        try {
            Connection con = DBConnect.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Bill where ClientId='" + clientId + "'");
            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBillId(rs.getInt("BillID"));
                bill.setBill_no(rs.getInt("BillNo"));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                bill.setFrom_date(simpleDateFormat.parse(rs.getString("FromDate")));
                bill.setTo_date(simpleDateFormat.parse(rs.getString("ToDate")));
                bill.setBill_year(rs.getString("FinancialYear"));
                if (rs.getInt("IsShared") == 1)
                    bill.setBillShared(true);
                else
                    bill.setBillShared(false);
                bill_list.add(bill);
            }
            con.close();
            return bill_list;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("Database file not exist !!"))
                throw new Exception(e.getMessage());
        }
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

    public static Bill getBill(int bill_id) throws Exception {
        Bill bill = new Bill();
        try {
            Connection con = DBConnect.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from Bill where BillID='" + bill_id + "'");
            while (rs.next()) {
                bill.setBillId(rs.getInt("BillID"));
                bill.setBill_no(rs.getInt("BillNo"));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                bill.setFrom_date(simpleDateFormat.parse(rs.getString("FromDate")));
                bill.setTo_date(simpleDateFormat.parse(rs.getString("ToDate")));
                bill.setBill_year(rs.getString("FinancialYear"));
                bill.setClient_id(rs.getInt("ClientId"));
                if (rs.getInt("IsShared") == 1)
                    bill.setBillShared(true);
                else
                    bill.setBillShared(false);
            }
            Log.i(ClientDbServices.class.getSimpleName(), bill.getClient_id() + "");
            con.close();
            return bill;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("Database file not exist !!"))
                throw new Exception(e.getMessage());
        }
        return bill;
    }

    public static List<Transection> getBillTransection(String bill_details) throws Exception {
        List<Transection> transections = new ArrayList<>();
        try {
            int total_balance = 0;
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            String query = "select * from ClientTransection where BillDetails='" + bill_details + "'";
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                Transection tr = new Transection();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                Date transectiondate = simpleDateFormat.parse(rs.getString("TransectionDate"));
                tr.setDate(transectiondate);
                tr.setTransecId(rs.getInt("TransectionID"));
                tr.setClientId(rs.getInt("ClientID"));
                tr.setAmount(rs.getInt("Amount"));
                tr.setDesc(rs.getString("Description"));
                tr.setTransecType(rs.getString("TransectionType"));
                tr.setBill_details(rs.getString("BillDetails"));
                transections.add(tr);
            }
            Collections.sort(transections);
            con.close();
            return transections;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Erro in previus balance !!");
        }
    }
}
