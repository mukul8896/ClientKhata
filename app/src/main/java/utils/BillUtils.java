package utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import BeanClasses.Bill;
import BeanClasses.Transection;
import db_services.DBServices;

public class BillUtils {
    private Bill bill;

    public BillUtils(Bill bill){
        this.bill=bill;
    }

    public String getBillDetails() {
        String details="Bill No. "+bill.getBill_no()+" / ";
        details+=bill.getBill_year()+" / ";
        details+=getFormatedDate();
        return details;
    }

    public Set<Transection> getParticulars() throws Exception {
        return DBServices.getParticulars(bill.getClient_id(),bill.getFrom_date(),bill.getTo_date());
    }

    private String getFormatedDate(){
        Date date=new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
        return fmt.format(date);
    }

    public File getFile() {
        File file=new File(ProjectUtil.createDirectoryFolder(),"Bill No-"+bill.getBill_no()+".pdf");
        return file;
    }
}
