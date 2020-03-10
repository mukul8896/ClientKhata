package db_services;

import android.os.Environment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import utils.ProjectUtil;

public class DBConnect {
    public static Connection getConnection() throws Exception {
        Connection con = null;
        try {
            File file=new File(ProjectUtil.createDirectoryFolder(),"Database1.accdb");
            if(!file.exists())
                throw new Exception("Database file not exist !!");
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            con = DriverManager.getConnection("jdbc:ucanaccess://"+file.getAbsolutePath());
            return con;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e1){
            throw new Exception(e1.getMessage());
        }
        return con;
    }
}
