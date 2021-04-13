package googleSignIn;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.mukul.companyAccounts.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dao.DBParameters;
import utils.ProjectUtils;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile, which also adds a request dialog to access the user's Google Drive.
 */
public class SignInActivityWithDrive extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private Drive driveService;

    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Views
        mStatusTextView = findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        // [END customize_button]
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            updateUI(account);
        } else {
            updateUI(null);
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());
        try {
            // Signed in successfully, show authenticated U
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);

            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));

            credential.setSelectedAccount(account.getAccount());

            driveService=new Drive.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("ClientKhata")
                    .build();


            Executor executor= Executors.newSingleThreadExecutor();
            Task uploadFile= Tasks.call(executor,()->restoreData());
            uploadFile.addOnSuccessListener(System.out::println).addOnFailureListener(System.out::println);

        } catch (ApiException e) {
            // Signed out, show unauthenticated UI.
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }

    private String createFolderInDrive(String companydata) {
        File file = null;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(companydata);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            Log.d("mk_logs",file.getId());
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), RC_SIGN_IN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getId();
    }

    private String searchCompanyDataFolder(){
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
                    System.out.printf("Found file: %s (%s)\n",
                            file.getName(), file.getId());
                    if(file.getName().equals("Companydata"))
                        return file.getId();
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), RC_SIGN_IN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadSqlitDbFile(){
        String folderId=searchCompanyDataFolder();
        if(folderId==null){
            folderId=createFolderInDrive("Companydata");
        }
        String fileId=getDbFileId();
        File file=null;
        try {
            java.io.File filePath = ProjectUtils.getDBFile();
            FileContent mediaContent = new FileContent("application/x-sqlite3", filePath);

            if(fileId==null){
                File fileMetadata = new File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(DBParameters.DB_NAME);

                file = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, parents")
                        .execute();
            }else{
                File currentFile = driveService.files().get(fileId).execute();
                currentFile.setName(DBParameters.DB_NAME);

                File fileMetadata = new File();
                fileMetadata.setName(currentFile.getName());
                fileMetadata.setParents(currentFile.getParents());

                file = driveService.files().update(fileId, fileMetadata, mediaContent).execute();
            }
        }catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), RC_SIGN_IN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getId();
    }

    public String restoreData(){
        String fileId=getDbFileId();
        if(fileId==null){
            return "File not exist in drive";
        }
        try {
            Arrays.asList(ProjectUtils.getDataFolder().listFiles()).forEach(file-> System.out.println(file.getName()));

            OutputStream outputStream = new FileOutputStream(ProjectUtils.getDBFile());
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.flush();
            outputStream.close();
            Arrays.asList(ProjectUtils.getDataFolder().listFiles()).forEach(file-> System.out.println(file.getName()));

            return "Success";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failure";
        }
    }

    private String getDbFileId() {
        String pageToken = null;
        try {
            do {
                FileList result = null;
                result = driveService.files().list()
                        .setQ("mimeType='application/x-sqlite3'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    System.out.printf("Found file: %s (%s)\n",
                            file.getName(), file.getId());
                    if(file.getName().equals(DBParameters.DB_NAME))
                        return file.getId();
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), RC_SIGN_IN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }
}
