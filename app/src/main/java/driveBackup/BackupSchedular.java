package driveBackup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.mukul.companyAccounts.R;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BackupSchedular extends BroadcastReceiver {
    private Drive driveService=null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BackupSchedular", "Alarm just fired");

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        nb.setContentTitle("ClientBilling")
                .setProgress(0,0,true)
                .setContentText("Taking data backup...")
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(false);
        notificationHelper.getManager().notify(1, nb.build());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            driveService=new Drive.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("ClientKhata")
                    .build();
        } else {
            NotificationHelper notice = new NotificationHelper(context);
            NotificationCompat.Builder noticeBuilder = notificationHelper.getChannelNotification();
            nb.setContentTitle("ClientBilling")
                    .setContentText("Sign to your google account for backup")
                    .setSmallIcon(R.drawable.app_logo)
                    .setAutoCancel(false);
            notificationHelper.getManager().notify(2, nb.build());
        }

        GoogleDriveHandler handler=new GoogleDriveHandler();
        Executor executor= Executors.newSingleThreadExecutor();
        Task<Object> download= Tasks.call(executor,()->{
            try {
                return handler.uploadSqlitDbFile(driveService);
            }catch (UserRecoverableAuthIOException e){
                return "Failure";
            }
        });
        download.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d("mk_logs","Data backup success : "+(String)o);
                notificationHelper.getManager().cancel(1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("mk_logs","Failed to backup data");
                notificationHelper.getManager().cancel(1);
            }
        });
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(1000*60*24*7), pendingIntent);
    }
}
