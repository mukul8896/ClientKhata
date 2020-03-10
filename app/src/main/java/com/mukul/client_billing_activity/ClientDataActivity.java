package com.mukul.client_billing_activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import AdapterClasses.TransectionListAdapter;
import BeanClasses.Client;
import BeanClasses.Transection;
import BeanClasses.Bill;
import billing_services.BillGenerator;
import db_services.DBServices;

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
        getMenuInflater().inflate(R.menu.client_transection_list_menu,menu);
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
        getMenuInflater().inflate(R.menu.client_optins_menu, menu);
        return true;
    }
    Dialog dialog;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.download_bill) {
            dialog = new Dialog(ClientDataActivity.this);
            dialog.setContentView(R.layout.download_bill_dialoge);
            TextView submit=(TextView)dialog.findViewById(R.id.bill_doigole_submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PDFGenerationTask task=new PDFGenerationTask();
                        task.execute();
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ClientDataActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            TextView cancel=(TextView)dialog.findViewById(R.id.bill_dialoge_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(ClientDataActivity.class.getSimpleName(),"Bill generation dialoge dismiss");
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        } else if(id == R.id.billing_history){
            Intent intent_to_bill_list = new Intent(ClientDataActivity.this,
                    BillListActivity.class);
            intent_to_bill_list.putExtra("id",client_id);
            Log.i(ClientDataActivity.class.getSimpleName(),"about to start bill list activity");
            startActivity(intent_to_bill_list);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public String getFinancialYear(){
        String year1="";
        String year2="";
        Calendar date=Calendar.getInstance();
        if(date.get(Calendar.MONTH)>2 && date.get(Calendar.MONTH)<=11){
            year1=date.get(Calendar.YEAR)+"";
            year2=date.get(Calendar.YEAR)+1+"";
        }else if(date.get(Calendar.MONTH)>=0 && date.get(Calendar.MONTH)<=2){
            year1=date.get(Calendar.YEAR)-1+"";
            year2=date.get(Calendar.YEAR)+"";
        }
        return year1+"-"+year2.substring(0,2);
    }

    private class PDFGenerationTask extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                final DatePicker from_date_picer=(DatePicker)dialog.findViewById(R.id.from_date);
                final DatePicker to_date_picer=(DatePicker)dialog.findViewById(R.id.to_date);
                Bill bill=new Bill();
                bill.setClient_id(client_id);
                bill.setBill_year(getFinancialYear());
                bill.setBill_no(DBServices.getMaxBillNo(getFinancialYear())+1);
                Date from_date=new Date(from_date_picer.getYear()-1900,from_date_picer.getMonth(),from_date_picer.getDayOfMonth());
                bill.setFrom_date(from_date);
                Date to_date=new Date(to_date_picer.getYear()-1900,to_date_picer.getMonth(),to_date_picer.getDayOfMonth());
                bill.setTo_date(to_date);
                BillGenerator.generateBill(bill);
                DBServices.addBill(bill);
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(ClientDataActivity.class.getSimpleName(),e.getMessage()+"Error while bill generation");
                return "not success";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("success"))
                Toast.makeText(ClientDataActivity.this, "Bill generated successfully!!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ClientDataActivity.this, "Some error while bill generation !!", Toast.LENGTH_SHORT).show();
        }
    }
}
