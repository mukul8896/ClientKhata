package com.mukul.companyAccounts.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import dao.DBParameters;
import driveBackup.GoogleDriveHandler;

public class SignInFragment extends Fragment implements
        View.OnClickListener {
    private TextView mStatusTextView;
    private ImageView profileImageView;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleDriveHandler backupHandler;
    private Drive driveService;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = SignInFragment.class.getSimpleName();

    private static SignInFragment signInFragment;
    public SignInFragment() {}

    public static SignInFragment newInstance() {
        if(signInFragment==null)
            signInFragment=new SignInFragment();
        return signInFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_sign_in, container, false);
        // Views
        mStatusTextView = view.findViewById(R.id.status);
        profileImageView = view.findViewById(R.id.google_icon);

        // Button listeners
        view.findViewById(R.id.sign_in_button).setOnClickListener(this);
        view.findViewById(R.id.sign_out_button).setOnClickListener(this);
        view.findViewById(R.id.disconnect_button).setOnClickListener(this);
        view.findViewById(R.id.backup_data).setOnClickListener(this);
        view.findViewById(R.id.restore_latest).setOnClickListener(this);
        view.findViewById(R.id.restore_old).setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this.getActivity().getApplicationContext(), gso);

        backupHandler=new GoogleDriveHandler();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getActivity().getApplicationContext());
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            updateUI(account);
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(this.getActivity().getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
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
                    .usingOAuth2(this.getActivity().getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));

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

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            Log.d(TAG,getString(R.string.signed_in_fmt, account.getDisplayName()));
            mStatusTextView.setText(account.getDisplayName());
            Uri uri = account.getPhotoUrl();
            Picasso.with(this.getActivity().getApplicationContext())
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(profileImageView);

            getView().findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            getView().findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            profileImageView.setImageResource(R.drawable.googleg_color);
            getView().findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            getView().findViewById(R.id.backup_restore).setVisibility(View.GONE);
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this.getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                updateUI(null);
            }
        });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this.getActivity(),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }

    // [START downloadData]
    private void downloadLatestDB(String fileName) {
        ProgressBar progressbar=(ProgressBar) getView().findViewById(R.id.progressbar);
        getView().findViewById(R.id.backup_restore).setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);

        Executor executor= Executors.newSingleThreadExecutor();
        Task<Object> download= Tasks.call(executor,()->{
            try {
                return backupHandler.restoreLatestFile(driveService);
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
                getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Download File fail ");
                progressbar.setVisibility(View.GONE);
                getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        });
    }

    // [START uploadData]
    private void uploadData() {
        ProgressBar progressbar=(ProgressBar) getView().findViewById(R.id.progressbar);
        getView().findViewById(R.id.backup_restore).setVisibility(View.GONE);
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
                getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Upload File fail: ");
                e.printStackTrace();
                progressbar.setVisibility(View.GONE);
                getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        });
    }

    TreeMap<String,Map<String,String>> dbFilesmap=new TreeMap<>();
    public void downloadOldDB(){
        getView().findViewById(R.id.backup_restore).setVisibility(View.GONE);
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        Spinner spinner = (Spinner) getView().findViewById(R.id.db_version_spinner);

        Executor executor= Executors.newSingleThreadExecutor();
        Task getAllDblist= Tasks.call(executor,()-> {
            try {
                dbFilesmap=backupHandler.getAllDBFilesMap(driveService);
                System.out.println(dbFilesmap);
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), RC_SIGN_IN);
            }
            return "success";
        });
        getAllDblist.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG,"List of files: "+(String)o);
                progressBar.setVisibility(View.GONE);
                List<String> file_date_name=dbFilesmap.keySet().stream().collect(Collectors.toList());
                file_date_name.add(0,"--SELECT FILE OF DATE--");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignInFragment.this.getContext(), android.R.layout.simple_spinner_dropdown_item, file_date_name);
                spinner.setAdapter(adapter);
                spinner.setVisibility(View.VISIBLE);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position!=0){
                            spinner.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            Executor executor= Executors.newSingleThreadExecutor();
                            Task restoreOld= Tasks.call(executor,()-> {
                                try {
                                    Map<String,String> data= dbFilesmap.get(adapter.getItem(position));
                                    String fileId = new ArrayList<>(data.keySet()).get(0);
                                    System.out.println("File ID is : "+fileId);
                                    backupHandler.restoreOldFile(driveService,fileId);
                                } catch (UserRecoverableAuthIOException e) {
                                    startActivityForResult(e.getIntent(), RC_SIGN_IN);
                                }
                                return id;
                            });
                            restoreOld.addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    progressBar.setVisibility(View.GONE);
                                    spinner.setVisibility(View.GONE);
                                    SignInFragment.this.getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
                                    Toast.makeText(SignInFragment.this.getContext(),"Data downloaded successfully !!",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    spinner.setVisibility(View.GONE);
                                    SignInFragment.this.getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
                                    Toast.makeText(SignInFragment.this.getContext(),"Data downloaded failure !!",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Unable get list of db files !!");
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                getView().findViewById(R.id.backup_restore).setVisibility(View.VISIBLE);
            }
        });
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
            case R.id.restore_latest:
                downloadLatestDB(DBParameters.DB_NAME);
                break;
            case R.id.restore_old:
                downloadOldDB();
                break;
        }
    }
}