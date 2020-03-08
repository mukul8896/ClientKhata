package utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class ProjectUtil {
    public static void createDirectoryFolder(){
        File dir= Environment.getExternalStorageDirectory();
        File folder=new File(dir.getPath()+File.separator+"ClientsData");
        if(!folder.exists()){
            folder.mkdir();
        }
    }
}
