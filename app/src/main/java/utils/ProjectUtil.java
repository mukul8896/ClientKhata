package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.mukul.client_billing_activity.R;

import java.io.File;

public class ProjectUtil {
    public static File createDirectoryFolder() {
        File dir = Environment.getExternalStorageDirectory();
        File folder = new File(dir.getPath() + File.separator + "ClientsData");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }
}
