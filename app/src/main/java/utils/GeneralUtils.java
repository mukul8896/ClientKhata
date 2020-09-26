package utils;

import android.util.Log;

import com.mukul.client_billing_activity.GeneratedBillFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GeneralUtils {
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
        Log.i(GeneratedBillFragment.class.getSimpleName(), year1 + ":" + year2);
        return year1 + "-" + year2.substring(2, year2.length());
    }

    public static String getFormatedDate() {
        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
        return fmt.format(date);
    }
}
