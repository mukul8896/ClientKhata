package com.mukul.clientbilling;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import AdapterClasses.ClientListAdapter;
import BeanClasses.Client;
import DbConnect.DBServices;

public class MainActivity extends AppCompatActivity {
    private List<Client> clientsList;
    private ListView listView;
    private int index;
    private ClientListAdapter adapter;
    private TextView total_balance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i(MainActivity.class.getSimpleName(),"In MainActivity oncreate");

        clientsList=DBServices.getClientsList();
        Log.i(MainActivity.class.getSimpleName(),clientsList.toString());
        total_balance=(TextView)findViewById(R.id.total_balance);
        total_balance.setText(getTotalBalance(clientsList)+" Rs");

        listView= findViewById(R.id.client_list);
        adapter=new ClientListAdapter(this,R.layout.client_list_item,clientsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,
                        ClientDataActivity.class);
                intent.putExtra("id",clientsList.get(position).getId());
                intent.putExtra("Client Name",clientsList.get(position).getName());
                startActivity(intent);
                Log.i("Mukul Sharma", clientsList.get(position).getName());
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
        getMenuInflater().inflate(R.menu.client_list_menu,menu);
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
            builder.setTitle("Bhai pakka delete kar du !!");
            builder.setMessage("All data related to client will deleted.\n" +
                    "Do you want to continue ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DBServices.deleteClient(clientsList.get(index).getId());
                        clientsList.remove(index);
                        adapter.notifyDataSetChanged();
                        clientsList=DBServices.getClientsList();
                        Log.i(MainActivity.class.getSimpleName(),clientsList.toString());
                        total_balance.setText(getTotalBalance(clientsList)+" Rs");
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
        clientsList=DBServices.getClientsList();
        total_balance.setText(getTotalBalance(clientsList)+" Rs");
        adapter=new ClientListAdapter(this,R.layout.client_list_item,clientsList);
        listView.setAdapter(adapter);
    }

    private int getTotalBalance(List<Client> list){
        int total=0;
        for(Client client:list){
            total+=client.getBalance();
        }
        return total;
    }
}
