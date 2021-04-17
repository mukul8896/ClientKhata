package utils;

import android.os.Environment;
import android.util.Log;

import com.mukul.companyAccounts.ClientBillListFragment;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dao.DBParameters;

public class ProjectUtils {

    public static File getDataBaseFolder() {
        File dir = new File("/data"+File.separator+"data"+File.separator+"com.mukul.companyAccounts"+File.separator+"databases");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public static File getExternalDataFolder() {
        File dir = Environment.getExternalStorageDirectory();
        File folder = new File(Environment.getExternalStorageDirectory() , "ClientsData");
        if (!folder.exists()) {
            Log.d(ProjectUtils.class.getSimpleName(),"Folder created:"+folder.mkdir());
        }
        Arrays.asList(dir.listFiles()).forEach(f-> System.out.println(f.getName()));
        Log.d(ProjectUtils.class.getSimpleName(),"Folder path: "+folder.getPath());
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
        Log.i(ClientBillListFragment.class.getSimpleName(), year1 + ":" + year2);
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
