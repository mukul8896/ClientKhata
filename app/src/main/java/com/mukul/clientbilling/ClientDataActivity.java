package com.mukul.clientbilling;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import AdapterClasses.TransectionListAdapter;
import BeanClasses.Client;
import BeanClasses.Transection;
import DbConnect.DBServices;

public class ClientDataActivity extends AppCompatActivity {
    private ListView transection_lstView;
    private Integer client_id;
    private List<Transection> transectionList;
    private TransectionListAdapter adapter;
    private int index;
    private ActionBar toolbar;
    private TextView contact_txt;
    private TextView address_txt;
    private TextView name_txt;
    private Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_data);
        client_id=getIntent().getIntExtra("id",0);
        client=DBServices.getClient(client_id);
        toolbar = getSupportActionBar();
        toolbar.setTitle(getIntent().getStringExtra("Client Name"));
        toolbar.setDisplayHomeAsUpEnabled(false);

        name_txt=(TextView)findViewById(R.id.client_name_data);
        contact_txt=(TextView)findViewById(R.id.client_contact_data);
        address_txt=(TextView)findViewById(R.id.client_address_data);
        contact_txt.setText(client.getContact());
        address_txt.setText(client.getAddress());
        name_txt.setText(client.getName());

        transectionList= DBServices.getClientsTransections(client_id);
        Collections.sort(transectionList);
        transection_lstView= (ListView) findViewById(R.id.transection_list);

        adapter=new TransectionListAdapter(this,R.layout.transection_list_item,transectionList);
        transection_lstView.setAdapter(adapter);

        Button add_new_transection= (Button) findViewById(R.id.add_transection_btn);
        add_new_transection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_to_add_transec = new Intent(ClientDataActivity.this,
                        AddTransecActivity.class);
                intent_to_add_transec.putExtra("id",client_id);
                Log.i(ClientDataActivity.class.getSimpleName(),"about to starrt transection add activity");
                startActivity(intent_to_add_transec);
            }
        });
        registerForContextMenu(transection_lstView);
        transection_lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(ClientDataActivity.class.getSimpleName(),"Logn press done");
                index=position;
                return false;
            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.client_list_menu,menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            try {
                DBServices.deleteTransection(client,transectionList.get(index));
                transectionList.remove(index);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Done !!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if(id == R.id.edit){
            Intent intent = new Intent(ClientDataActivity.this,
                    AddTransecActivity.class);
            Bundle data=new Bundle();
            data.putString("mode","Edit");
            data.putInt("clientid",client_id);
            data.putInt("transecid",transectionList.get(index).getTransecId());
            intent.putExtra("data",data);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(MainActivity.class.getSimpleName(),"In client data restart");
        client_id=getIntent().getIntExtra("id",0);
        toolbar.setTitle(getIntent().getStringExtra("Client Name"));
        transectionList= DBServices.getClientsTransections(client_id);
        Collections.sort(transectionList);
        Log.i(ClientDataActivity.class.getSimpleName(),transectionList.size()+"::"+client_id);
        adapter=new TransectionListAdapter(this,R.layout.transection_list_item,transectionList);
        transection_lstView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.download_bill) {
            final Dialog dialog = new Dialog(ClientDataActivity.this);
            dialog.setTitle("Enter Dates:");
            dialog.setContentView(R.layout.download_bill_dialoge);
            dialog.setTitle("Enter Dates: ");
            final DatePicker from_date=(DatePicker)dialog.findViewById(R.id.from_date);
            TextView submit=(TextView)dialog.findViewById(R.id.bill_doigole_submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(ClientDataActivity.class.getSimpleName(),from_date.getDayOfMonth()+"/"+from_date.getMonth());
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
