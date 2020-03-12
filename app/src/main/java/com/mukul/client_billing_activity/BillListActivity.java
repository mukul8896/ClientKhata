package com.mukul.client_billing_activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import AdapterClasses.BillListAdapder;
import BeanClasses.Bill;
import db_services.DBServices;
import utils.BillUtils;

public class BillListActivity extends AppCompatActivity {
    private ListView listView;
    private BillListAdapder adapter;
    private List<Bill> bill_list;
    private Integer client_id;
    private int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        client_id=getIntent().getIntExtra("id",0);
        listView= findViewById(R.id.bill_listview);
        try {
            bill_list= DBServices.getBillList(client_id);
        } catch (Exception e) {
            Toast.makeText(this, "Someting went wrong !!", Toast.LENGTH_LONG).show();
        }
        adapter=new BillListAdapder(this,R.layout.bill_list_item,bill_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFile(position);
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.share){
            BillUtils utils=new BillUtils(bill_list.get(index));
            utils.shareFile(this);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.bill_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Intent intent = new Intent(BillListActivity.this,
                    ClientDataActivity.class);
            intent.putExtra("id",client_id);
            intent.putExtra("Client Name",DBServices.getClient(client_id).getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }
    public void openFile(int index){
        BillUtils utils=new BillUtils(bill_list.get(index));
        utils.openFile(this);
    }
}
