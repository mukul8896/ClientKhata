package utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

    public static boolean requestPassword(SharedPreferences sharedPref, Context context) {
        String password = sharedPref.getString("app_password", "");
        if (password == null || password.equals("")) {
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.create_password_dialoge, null);
            final EditText pass1 = view.findViewById(R.id.new_password);
            final EditText pass2 = view.findViewById(R.id.confirm_password);
            builder.setView(view)
                    .setTitle("Create Password")
                    .setCancelable(false)
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (pass1.getText().toString().equals(pass2.getText().toString())) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("app_password", pass2.getText().toString());
                                editor.apply();
                                flag[0] = true;
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Password doesn't match", Toast.LENGTH_SHORT).show();
                                flag[0] = false;
                            }

                        }
                    }).show();
            builder.create();
            return flag[0];
        } else {
            Dialog dialog = new Dialog(context);
            dialog.setTitle("Enter Password");
            dialog.show();
            String user_password = "NA";
        }
        return false;
    }
}
