package BeanClasses;

import java.util.Date;
import java.util.Map;

public class Bill {
    private Integer billId;
    private Integer bill_no;
    private String bill_year;
    private Integer client_id;
    private Date to_date;
    private Date from_date;
    private boolean isBillShared;

    public void setBill_no(Integer bill_no) {
        this.bill_no = bill_no;
    }

    public void setBill_year(String bill_year) {
        this.bill_year = bill_year;
    }

    public String getBill_year() { return bill_year; }

    public void setClient_id(Integer client_id) {
        this.client_id = client_id;
    }

    public Integer getClient_id() {
        return client_id;
    }

    public Integer getBill_no() {
        return bill_no;
    }

    public Date getFrom_date() {
        return from_date;
    }

    public void setFrom_date(Date from_date) { this.from_date = from_date; }
    public Date getTo_date() {
        return to_date;
    }

    public void setTo_date(Date to_date) {
        this.to_date = to_date;
    }

    public void setBillShared(boolean billShared) {
        isBillShared = billShared;
    }

    public boolean getBillShared(){
        return isBillShared;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }
}
