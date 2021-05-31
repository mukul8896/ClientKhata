package driveBackup;

import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dao.DBParameters;
import utils.ProjectUtils;

public class GoogleDriveHandler {

    private static final int RC_SIGN_IN = 9001;
    private static final String folderName = "Companydata";

    private String createFolderInDrive(Drive driveService,String folderName) throws UserRecoverableAuthIOException {
        File file = null;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            Log.d("mk_logs",file.getId());
        } catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getId();
    }

    public static String searchFolderByName(Drive driveService,String folderName) throws UserRecoverableAuthIOException{
        String pageToken = null;
        try {
            do {
                FileList result = null;
                result = driveService.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    System.out.printf("Found folder: %s (%s)\n",
                            file.getName(), file.getId());
                    if(file.getName().equals(folderName))
                        return file.getId();
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadSqlitDbFile(Drive driveService) throws UserRecoverableAuthIOException {
        String folderId=searchFolderByName(driveService,folderName);
        if(folderId==null){
            folderId=createFolderInDrive(driveService,folderName);
        }
        String fileId=getFileIdByName(driveService,DBParameters.DB_NAME,folderName);
        File file=null;
        try {
            java.io.File filePath = ProjectUtils.getDBFile();
            FileContent mediaContent = new FileContent("application/x-sqlite3", filePath);

            if(fileId==null){
                //Creating a new File
                File newfileMetadata = new File();
                newfileMetadata.setParents(Collections.singletonList(folderId));
                newfileMetadata.setName(DBParameters.DB_NAME);

                Log.d(GoogleDriveHandler.class.getSimpleName(),ProjectUtils.getDriveDbFileName());

                file = driveService.files().create(newfileMetadata, mediaContent)
                        .setFields("id, parents")
                        .execute();
            }else{
                //Creating a new file
                File newfileMetadata = new File();
                newfileMetadata.setParents(Collections.singletonList(folderId));
                newfileMetadata.setName(String.valueOf(System.currentTimeMillis()));

                Log.d(GoogleDriveHandler.class.getSimpleName(),ProjectUtils.getDriveDbFileName());

                file = driveService.files().create(newfileMetadata, mediaContent)
                        .setFields("id, parents")
                        .execute();

                //Updating a file
                File currentFile = driveService.files().get(fileId).execute();
                currentFile.setName(DBParameters.DB_NAME);

                Log.d(GoogleDriveHandler.class.getSimpleName(),ProjectUtils.getDriveDbFileName());

                File fileMetadata = new File();
                fileMetadata.setName(currentFile.getName());
                fileMetadata.setParents(currentFile.getParents());

                file = driveService.files().update(fileId, fileMetadata, mediaContent).execute();
            }

        }catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file.getId();
    }

    public TreeMap<String,String> getAllDBFilesMap(Drive driveService) throws UserRecoverableAuthIOException {
        TreeMap<String,String> dbFiles=new TreeMap<>();
        String folderId=searchFolderByName(driveService,folderName);
        String pageToken = null;
        try {
            do {
                FileList result = null;
                result = driveService.files().list()
                        .setQ("'"+folderId+"' in parents")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    System.out.printf("Found file: %s (%s)\n",
                            file.getName(), file.getId());
                    if(!file.getName().equals(DBParameters.DB_NAME)){
                        dbFiles.put(getdateFromMillies(file.getName()),file.getId());
                    }
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(dbFiles);
        return dbFiles;
    }

    public String restoreLatestFile(Drive driveService) throws UserRecoverableAuthIOException{
        String fileId=getFileIdByName(driveService,DBParameters.DB_NAME,folderName);
        if(fileId==null){
            return "File not exist in drive";
        }
        try {
            OutputStream outputStream = new FileOutputStream(ProjectUtils.getDBFile());
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.flush();
            outputStream.close();
            return "Success";
        } catch (UserRecoverableAuthIOException e) {
            throw e;
        }catch (IOException e) {
            e.printStackTrace();
            return "Failure";
        }
    }

    public String restoreOldFile(Drive driveService,String fileId) throws UserRecoverableAuthIOException{
        if(fileId==null){
            return "File not exist in drive";
        }
        try {
            OutputStream outputStream = new FileOutputStream(ProjectUtils.getDBFile());
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.flush();
            outputStream.close();
            return "Success";
        } catch (UserRecoverableAuthIOException e) {
            throw e;
        }catch (IOException e) {
            e.printStackTrace();
            return "Failure";
        }
    }

    public static String getFileIdByName(Drive driveService,String fileName,String folderName) throws UserRecoverableAuthIOException{
        String folderId=searchFolderByName(driveService,folderName);
        String pageToken = null;
        try {
            do {
                FileList result = null;
                result = driveService.files().list()
                        .setQ("'"+folderId+"' in parents")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    System.out.printf("Found file: %s (%s)\n",
                            file.getName(), file.getId());
                    if(file.getName().equals(fileName))
                        return file.getId();
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getdateFromMillies(String milies) {
        long milliTime=Long.parseLong(milies);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliTime);
        return ProjectUtils.parseDateToString(calendar.getTime(),"dd/MM/yyyy  HH:mm:ss");
    }
}
