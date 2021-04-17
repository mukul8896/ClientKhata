package googleSignIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import driveBackup.GoogleDriveHandler;

public class SignInActivityWithDrive extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private ImageView profileImageView;
    private GoogleSignInClient mGoogleSignInClient;
    private Drive driveService;
    private GoogleDriveHandler backupHandler;

    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Views
        mStatusTextView = findViewById(R.id.status);
        profileImageView = findViewById(R.id.google_icon);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        findViewById(R.id.backup_data).setOnClickListener(this);
        findViewById(R.id.restore_data).setOnClickListener(this);


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

        backupHandler=new GoogleDriveHandler();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
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


        } catch (ApiException e) {
            // Signed out, show unauthenticated UI.
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
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

    // [START downloadData]
    private void downloadData() {
        ProgressBar progressbar=(ProgressBar) findViewById(R.id.progressbar);
        findViewById(R.id.backup_restore).setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);

        Executor executor= Executors.newSingleThreadExecutor();
        Task<Object> download= Tasks.call(executor,()->{
            try {
                return backupHandler.restoreData(driveService);
            }catch (UserRecoverableAuthIOException e){
                startActivityForResult(e.getIntent(), RC_SIGN_IN);
                return "Failure";
            }
        });
        download.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG,"Download File success: "+(String)o);
                progressbar.setVisibility(View.GONE);
                findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Download File fail ");
                progressbar.setVisibility(View.GONE);
                findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        });
    }

    // [START uploadData]
    private void uploadData() {
        ProgressBar progressbar=(ProgressBar) findViewById(R.id.progressbar);
        findViewById(R.id.backup_restore).setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);

        Executor executor= Executors.newSingleThreadExecutor();
        Task uploadFile= Tasks.call(executor,()-> {
            String id="";
            try {
                id=backupHandler.uploadSqlitDbFile(driveService);
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), RC_SIGN_IN);
            }
            return id;
        });
        uploadFile.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG,"Upload File success: "+(String)o);
                progressbar.setVisibility(View.GONE);
                findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Upload File fail: ");
                e.printStackTrace();
                progressbar.setVisibility(View.GONE);
                findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));
            Uri uri = account.getPhotoUrl();
            Picasso.with(SignInActivityWithDrive.this)
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(profileImageView);

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            profileImageView.setImageResource(R.drawable.googleg_color);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            findViewById(R.id.backup_restore).setVisibility(View.GONE);
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
            case R.id.backup_data:
                uploadData();
                break;
            case R.id.restore_data:
                downloadData();
                break;
        }
    }
}
