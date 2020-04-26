package utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.mukul.client_billing_activity.BuildConfig;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import BeanClasses.Bill;
import BeanClasses.Transection;
import db_services.BillDbServices;

public class BillUtils {
    private Bill bill;

    public BillUtils(Bill bill) {
        this.bill = bill;
    }

    public String getBillDetails() {
        String details = "Bill No. " + bill.getBill_no() + " / ";
        details += bill.getBill_year() + " / ";
        details += getFormatedDate();
        return details;
    }

    public List<Transection> getParticulars() throws Exception {
        return BillDbServices.getBillParticulars(bill.getClient_id(), bill.getFrom_date(), bill.getTo_date());
    }

    private String getFormatedDate() {
        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
        return fmt.format(date);
    }

    public File getFile(String bill_year) {
        File dir = new File(ProjectUtil.createDirectoryFolder().getPath() + File.separator + bill_year);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "Bill No-" + bill.getBill_no() + ".pdf");
        return file;
    }

    public void sharePdfFile(Context context) throws Exception {
        Intent intent = new Intent(Intent.ACTION_SEND);
        File file = getFile(bill.getBill_year());
        Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "Share File"));
    }

    public void openPdfFile(Context context) {
        File file = getFile(bill.getBill_year());
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i("BillUtils->openFile if",file.getPath());
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Log.i("BillUtils else",file.getPath());
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "application/pdf");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }
}
