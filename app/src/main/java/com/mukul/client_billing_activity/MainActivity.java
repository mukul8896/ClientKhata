package com.mukul.client_billing_activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import AdapterClasses.ClientListAdapter;
import BeanClasses.Client;
import db_services.DBServices;
import utils.ProjectUtil;

public class MainActivity extends AppCompatActivity {
    private List<Client> clientsList;
    private ListView listView;
    private int index;
    private ClientListAdapter adapter;
    private TextView total_balance;
    private static final int storage_request_code=1;
    private int total_bal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(MainActivity.class.getSimpleName(),"In MainActivity oncreate");

        requestPermission();
        total_balance=(TextView)findViewById(R.id.total_balance);
        listView= findViewById(R.id.client_list);
        try {
            clientsList=DBServices.getClientsList();
            Log.i(MainActivity.class.getSimpleName(),clientsList.toString());
            total_bal=getTotalBalance(clientsList);
            if (total_bal<0) {
                total_bal=total_bal*-1;
                total_balance.setText("(" + total_bal + " Rs)");
            }else
                total_balance.setText(total_bal+" Rs");
            adapter=new ClientListAdapter(this,R.layout.client_list_item,clientsList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,
                        ClientActivity.class);
                intent.putExtra("id",clientsList.get(position).getId());
                Log.i(MainActivity.class.getSimpleName(),"Client clicked");
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                index=position;
                return false;
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        AddClientAvtivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.client_transection_list_menu,menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            Log.i(MainActivity.class.getSimpleName(),"Delete button clicked");
            AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert !!");
            builder.setMessage("All data related to this client will be deleted.\n" +
                    "Do you want to continue ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Log.i(MainActivity.class.getSimpleName(),"delete alert submited");
                        DBServices.deleteClient(clientsList.get(index).getId());
                        //int client_bal=clientsList.get(index).getBalance();
                        clientsList.remove(index);
                        adapter.notifyDataSetChanged();
                        Log.i(MainActivity.class.getSimpleName(),"list notified");
                        //clientsList=DBServices.getClientsList();
                        Log.i(MainActivity.class.getSimpleName(),clientsList.toString());
                        total_bal=getTotalBalance(clientsList);
                        if (total_bal<0) {
                            total_bal=total_bal*-1;
                            total_balance.setText("(" + total_bal + " Rs)");
                        }else
                            total_balance.setText(total_bal+" Rs");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }else if(id == R.id.edit){
            Intent intent = new Intent(MainActivity.this,
                    AddClientAvtivity.class);
            Bundle data=new Bundle();
            data.putString("mode","Edit");
            data.putInt("id",clientsList.get(index).getId());
            intent.putExtra("data",data);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(MainActivity.class.getSimpleName(),"In restart");
        try {
            clientsList=DBServices.getClientsList();
            int total_bal=getTotalBalance(clientsList);
            if (total_bal<0) {
                total_bal=total_bal*-1;
                total_balance.setText("(" + total_bal + " Rs)");
            }else
                total_balance.setText(total_bal+" Rs");
            if(listView==null)
                listView= findViewById(R.id.client_list);
            adapter=new ClientListAdapter(this,R.layout.client_list_item,clientsList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void requestPermission(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){

            if(!isHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE) && !isHavePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        ,storage_request_code);
            }else{
                ProjectUtil.createDirectoryFolder();
            }
        } else {
            //system OS < Marshmallow, call save pdf method
            ProjectUtil.createDirectoryFolder();
        }
    }
    private boolean isHavePermission(String permission){
        return ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private int getTotalBalance(List<Client> list){
        int total=0;
        for(Client client:list){
            total+=client.getBalance();
        }
        return total;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case storage_request_code:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission was granted from popup, call savepdf method
                    ProjectUtil.createDirectoryFolder();
                }
                else {
                    //permission was denied from popup, show error message
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
