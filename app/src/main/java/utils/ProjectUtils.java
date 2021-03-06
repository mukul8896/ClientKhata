package utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.mukul.companyAccounts.BillTabFragment;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import dao.DBParameters;
import driveBackup.GoogleDriveHandler;

public class ProjectUtils {

    public static File getAppFolder(){
        return new File("/data"+File.separator+"data"+File.separator+"com.mukul.companyAccounts");
    }

    public static File getDataBaseFolder() {
        File dir = new File(getAppFolder(),"databases");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public static File getBillFolder() {
        File folder = new File(getAppFolder() , "ClientBills");
        if (!folder.exists()) {
            Log.d(ProjectUtils.class.getSimpleName(),folder.getPath()+" created: "+folder.mkdir());
        }
        return folder;
    }

    public static File getDBFile(){
        File dir = getDataBaseFolder();
        return new File(dir.getPath()+File.separator+ DBParameters.DB_NAME);
    }

    public static boolean isDatesEqual(Date date1, Date date2){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String from=simpleDateFormat.format(date1);
        String from2=simpleDateFormat.format(date2);
        if(from.equals(from2)){
            return true;
        }else
            return false;
    }

    public static String getFinancialYear(Date date) {
        String year1 = "";
        String year2 = "";
        Calendar cal_date = Calendar.getInstance();
        cal_date.set(Calendar.YEAR, date.getYear() + 1900);
        cal_date.set(Calendar.MONTH, date.getMonth());
        cal_date.set(Calendar.DAY_OF_MONTH, date.getDate());
        if (cal_date.get(Calendar.MONTH) > 2 && cal_date.get(Calendar.MONTH) <= 11) {
            year1 = Integer.toString(cal_date.get(Calendar.YEAR));
            year2 = Integer.toString(cal_date.get(Calendar.YEAR) + 1);
        } else if (cal_date.get(Calendar.MONTH) >= 0 && cal_date.get(Calendar.MONTH) <= 2) {
            year1 = Integer.toString(cal_date.get(Calendar.YEAR) - 1);
            year2 = Integer.toString(cal_date.get(Calendar.YEAR));
        }
        Log.i(BillTabFragment.class.getSimpleName(), year1 + ":" + year2);
        return year1 + "-" + year2.substring(2, year2.length());
    }

    public static String getFormatedDate() {
        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
        return fmt.format(date);
    }

    public static String getDriveDbFileName(){
        LocalDate date=LocalDate.now();
        String name=date.getDayOfMonth()+"_"+date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+"_"+date.getYear();
        return name;
    }

    public static String parseDateToString(Date date,String pattern) {
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);
        return fmt.format(date);
    }

    public static Date parseStringToDate(String date_string, String pattern){
        Date temp_date=new Date();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            temp_date =  simpleDateFormat.parse(date_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp_date;
    }
}
