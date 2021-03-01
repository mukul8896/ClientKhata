package backupEngine;

import android.util.Log;

import java.io.File;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.ProjectUtil;

public class BackupEngine {
   public static void run() {
      RestAssured.baseURI ="https://www.googleapis.com/upload/drive/v3/files?uploadType=media";
      RequestSpecification request = RestAssured.given();
      File data= ProjectUtil.createDirectoryFolder();
      request.body(data);
      request.header("Content-Type", "multipart/related; boundary=foo_bar_baz");
      Response response = request.post();
      Log.i(BackupEngine.class.getSimpleName(),response.getStatusCode()+"");
   }
}
