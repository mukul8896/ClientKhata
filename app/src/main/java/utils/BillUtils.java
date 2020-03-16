package utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.mukul.client_billing_activity.BuildConfig;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    public List<Transection> getParticulars() throws Exception {
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

    public void shareFile(Context context) throws Exception {
        File file=getFile(bill.getBill_year());
        Uri uri= FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".provider",file);
        Intent intent=new Intent(android.content.Intent.ACTION_SEND,uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share File"));
        //DBServices.updateBillAsShared(bill);
    }

    public void openFile(Context context){
        File file=getFile(bill.getBill_year());
        Intent browseStorage =new Intent(Intent.ACTION_VIEW);
        browseStorage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        browseStorage.setDataAndType(Uri.parse("file://"+file.getAbsolutePath()),URLConnection.guessContentTypeFromName(file.getName()));
        context.startActivity(Intent.createChooser(browseStorage, "Select PDF"));
    }


}
