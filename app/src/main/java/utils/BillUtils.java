package utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.mukul.companyAccounts.ClientActivity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import modals.Bill;
import modals.Transection;
import dbServices.BillDbServices;

public class BillUtils {
    private Bill bill;

    public BillUtils(Bill bill) {
        this.bill = bill;
    }

    public String getBillDetails() {
        String details = "Bill No. " + bill.getBill_no() + " / ";
        details += bill.getBill_year() + " / ";
        details += bill.getGenerationDate()==null? ProjectUtils.getFormatedDate():bill.getGenerationDate();
        return details;
    }

    public File getFile(String bill_year) {
        File dir = new File(ProjectUtils.getBillFolder().getPath() + File.separator + bill_year);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "Bill No-" + bill.getBill_no() + ".pdf");
        return file;
    }

    public boolean deleteBill(){
        File file = getFile(bill.getBill_year());
        if(!file.exists())
            return true;
        else
            return file.delete();
    }

    public void sharePdfFile(Context context) throws Exception {
        Intent intent = new Intent(Intent.ACTION_SEND);
        File file = getFile(bill.getBill_year());
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share File"));
    }

    public void openPdfFile(Context context) {
        File file = getFile(bill.getBill_year());
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i("BillUtils->openFile if",file.getPath());
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
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
