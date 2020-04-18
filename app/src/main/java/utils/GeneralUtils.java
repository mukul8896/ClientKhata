package utils;

import java.text.SimpleDateFormat;
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
}
