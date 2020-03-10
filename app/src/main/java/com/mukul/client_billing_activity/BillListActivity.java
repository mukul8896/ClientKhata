package com.mukul.client_billing_activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import AdapterClasses.BillListAdapder;
import BeanClasses.Bill;
import db_services.DBServices;

public class BillListActivity extends AppCompatActivity {
    ListView listView;
    BillListAdapder adapter;
    List<Bill> bill_list;
    Integer client_id;
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
}
