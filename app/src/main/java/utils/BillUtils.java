package utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.mukul.client_billing_activity.ClientDataActivity;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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

    public File getFile(String bill_year) {
        Log.i(BillUtils.class.getSimpleName(),ProjectUtil.createDirectoryFolder().getPath()+File.separator+bill_year);
        File dir=new File(ProjectUtil.createDirectoryFolder().getPath()+File.separator+bill_year);
        if(!dir.exists()) {
            dir.mkdir();
            Log.i(BillUtils.class.getSimpleName(),"in year dir creation ");
        }
        File file=new File(dir,"Bill No-"+bill.getBill_no()+".pdf");
        return file;
    }

    public void shareFile(Context context){
        Intent intent=new Intent(android.content.Intent.ACTION_SEND);
        File file=getFile(bill.getBill_year());
        intent.setType(URLConnection.guessContentTypeFromName(file.getName()));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
        context.startActivity(Intent.createChooser(intent, "Share File"));
    }

    public void openFile(Context context){
        Intent browseStorage =new Intent(Intent.ACTION_VIEW);
        browseStorage.setDataAndType(Uri.fromFile(getFile(bill.getBill_year())),"application/pdf");
        context.startActivity(Intent.createChooser(browseStorage, "Select PDF"));
    }
}
