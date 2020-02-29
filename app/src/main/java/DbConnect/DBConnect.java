package DbConnect;

import android.os.Environment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    public static Connection getConnection(){
        Connection con = null;
        try {
            File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file=new File(dir,"Database1.accdb");
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            con = DriverManager.getConnection("jdbc:ucanaccess://"+file.getAbsolutePath());
            return con;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return con;
    }
}
