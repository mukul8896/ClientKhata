package com.mukul.clientbilling;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import AdapterClasses.ClientListAdapter;
import AdapterClasses.TransectionListAdapter;
import BeanClasses.Transection;
import DbConnect.DBServices;

public class ClientDataActivity extends AppCompatActivity {
    private ListView transection_lstView;
    private Integer client_id;
    private List<Transection> transectionList;
    private TransectionListAdapter adapter;
    private Integer index;
    private ActionBar toolbar;
    private String client_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_data);
        toolbar = getSupportActionBar();
        client_name=getIntent().getStringExtra("Client Name");
        toolbar.setTitle(client_name);
        toolbar.setDisplayHomeAsUpEnabled(false);

        client_id=getIntent().getIntExtra("id",0);

        transection_lstView= (ListView) findViewById(R.id.transection_list);
        transectionList= DBServices.getClientsTransections(client_id);
        Collections.sort(transectionList);
        adapter=new TransectionListAdapter(this,R.layout.transection_list_item,transectionList);
        transection_lstView.setAdapter(adapter);

        Button add_new_transection = (Button) findViewById(R.id.add_transection_btn);
        add_new_transection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_to_add_transec = new Intent(ClientDataActivity.this,
                        AddTransecActivity.class);
                intent_to_add_transec.putExtra("id",client_id);
                startActivityForResult(intent_to_add_transec,1);
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
            Log.i(MainActivity.class.getSimpleName(),"Delete button clicked");
            try {
                DBServices.deleteTransection(transectionList.get(index).getTransecId());
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
            data.putInt("id",transectionList.get(index).getTransecId());
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
        Log.i(ClientDataActivity.class.getSimpleName(),transectionList.size()+"::"+client_id);
        adapter=new TransectionListAdapter(this,R.layout.transection_list_item,transectionList);
        transection_lstView.setAdapter(adapter);
    }
}
