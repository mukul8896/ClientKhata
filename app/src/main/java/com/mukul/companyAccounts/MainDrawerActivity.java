package com.mukul.companyAccounts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.navigation.NavigationView;
import com.mukul.companyAccounts.ui.ClientListFragment;
import com.mukul.companyAccounts.ui.SettingFragment;
import com.mukul.companyAccounts.ui.SignInFragment;
import com.mukul.companyAccounts.ui.SummeryFragment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import dao.DbHandler;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;
import utils.ProjectUtils;

public class MainDrawerActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private DbHandler dbHandler;
    private ClientListFragment clientListFragment;

    private static final int storage_request_code = 1;
    private static final int internet_request_code = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        initprerequisite();

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView=(NavigationView)findViewById(R.id.navmenu);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        View headerView = navigationView.getHeaderView(0);
        ImageView imageView=headerView.findViewById(R.id.navheaderImage);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            Uri uri = account.getPhotoUrl();
            Picasso.with(this.getApplicationContext())
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(imageView);
            ((TextView)headerView.findViewById(R.id.navsignIntag)).setText("Signed In as");
            ((TextView)headerView.findViewById(R.id.navsignedUserName)).setText(account.getDisplayName());
        } else {
            ((TextView)headerView.findViewById(R.id.navsignIntag)).setText("Sign In as");
            ((TextView)headerView.findViewById(R.id.navsignedUserName)).setText("NA");
        }


        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        clientListFragment = ClientListFragment.newInstance(ClientDbServices.getClientsList("all", "Balance"));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, clientListFragment)
                .commit();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment=null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        Log.d(MainDrawerActivity.class.getSimpleName(),"Nav Summery");
                        getSupportActionBar().setTitle("Client Billing");
                        fragment=ClientListFragment.newInstance(ClientDbServices.getClientsList("all", "Balance"));;
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_summery:
                        Log.d(MainDrawerActivity.class.getSimpleName(),"Nav Summery");
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                        String year = ProjectUtils.getFinancialYear(calendar.getTime());
                        getSupportActionBar().setTitle("Summery | "+year);
                        fragment= SummeryFragment.newInstance(ClientDbServices.getClientsList("all", "Balance"),
                                                                TransectionDbServices.getFinancialYearTransection(year),
                                                                    year);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_signin:
                        Log.d(MainDrawerActivity.class.getSimpleName(),"Nav Sign In");
                        fragment = SignInFragment.newInstance();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_settings:
                        Log.d(MainDrawerActivity.class.getSimpleName(),"Nav Settings");
                        fragment = SettingFragment.newInstance();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case storage_request_code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("mk_logs","Directory created");
                    ProjectUtils.getBillFolder();
                } else {
                    Toast.makeText(this, "Storage Permission denied...!", Toast.LENGTH_SHORT).show();
                }
                break;
            case internet_request_code :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("mk_logs","Internet permission granted");
                } else {
                    Toast.makeText(this, "Internet Permission denied...!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean isHavePermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if(!isHavePermission(Manifest.permission.INTERNET))
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},internet_request_code);
            else
                Log.d("mk_logs","Already internet permission");

            if (!isHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && !isHavePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        ,storage_request_code);
            } else {
                ProjectUtils.getBillFolder();
                Log.d("mk_logs","Already has storage permission");
            }
        } else {
            //system OS < Marshmallow, call save pdf method
            Log.d("mk_logs","Already has storage permission");
        }
    }

    private void initprerequisite(){
        /*start alarm*/
        //setAlarm();

        /* requesting permission for application */
        requestPermission();

        /* Initilize database */
        dbHandler= DbHandler.getInstance(this);
    }
}