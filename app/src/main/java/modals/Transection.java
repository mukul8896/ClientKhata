package modals;

import java.util.Date;

public class Transection implements Comparable<Transection> {
    private Integer transecId;
    private Integer clientId;
    private Date date;
    private String transecType;
    private String desc;
    private Integer amount;
    private String bill_details;
    private Integer billId;

    public Date getDate() {
        return date;
    }

    public Integer getAmount() {
        return amount;
    }

    public Integer getClientId() {
        return clientId;
    }

    public Integer getTransecId() {
        return transecId;
    }

    public String getDesc() {
        return desc;
    }

    public String getTransecType() {
        return transecType;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setTransecId(Integer transecId) {
        this.transecId = transecId;
    }

    public void setTransecType(String transecType) {
        this.transecType = transecType;
    }

    public String getBill_details() {
        return bill_details;
    }

    public void setBill_details(String bill_details) {
        this.bill_details = bill_details;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    @Override
    public int compareTo(Transection transection) {
        if (this.date.after(transection.getDate()))
            return -1;
        else if (this.date.before(transection.getDate()))
            return 1;
        else
            return 0;
    }

    @Override
    public String toString() {
        return "Transection{" +
                "transecId=" + transecId +
                ", clientId=" + clientId +
                ", date=" + date +
                ", transecType='" + transecType + '\'' +
                ", desc='" + desc + '\'' +
                ", amount=" + amount +
                ", bill_details='" + bill_details + '\'' +
                '}';
    }
}
