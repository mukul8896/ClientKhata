package com.mukul.client_billing_activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import AdapterClasses.TransectionListAdapter;
import BeanClasses.Bill;
import BeanClasses.Transection;
import billing_services.BillGenerator;
import db_services.BillDbServices;
import db_services.TransectionDbServices;

public class BillEditActivity extends AppCompatActivity {
    private ListView transection_lstView;
    private TransectionListAdapter adapter;
    private List<Transection> transectionList;
    private Bill bill;
    private int index;
    private ActionBar toolbar;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_edit);
        toolbar = getSupportActionBar();

        Log.i(BillEditActivity.class.getSimpleName(), "Inside BillEdit activity create method");
        Log.i(BillEditActivity.class.getSimpleName(), "Bill Id-" + getIntent().getIntExtra("bill_id", 0));
        try {
            bill = BillDbServices.getBill(getIntent().getIntExtra("bill_id", 0));
            String bill_details = bill.getBill_year() + " | Bill No-" + bill.getBill_no();
            toolbar.setTitle(bill_details);
            transectionList = BillDbServices.getBillTransection(bill_details);
        } catch (Exception e) {
            Toast.makeText(this, "Some error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        updateButton = (Button) findViewById(R.id.update_bill);
        transection_lstView = (ListView) findViewById(R.id.listview_for_edit_bill_transection);
        adapter = new TransectionListAdapter(this, R.layout.transection_list_item, transectionList);
        transection_lstView.setAdapter(adapter);

        registerForContextMenu(transection_lstView);
        transection_lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TransectionFragment.class.getSimpleName(), "Logn press done");
                index = position;
                return false;
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFGenerationTask task = new PDFGenerationTask();
                task.execute();
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.client_transection_list_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            try {
                TransectionDbServices.deleteTransection(bill.getClient_id(), transectionList.get(index));
                transectionList.remove(index);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Done !!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.edit) {
            Intent intent = new Intent(BillEditActivity.this, AddTransecActivity.class);
            Bundle data = new Bundle();
            data.putString("mode", "Edit");
            data.putInt("clientid", bill.getClient_id());
            data.putInt("transecid", transectionList.get(index).getTransecId());
            data.putString("parentActivity", BillEditActivity.class.getSimpleName() + "-" + bill.getBillId());
            intent.putExtra("data", data);
            startActivity(intent);
            Log.i(BillEditActivity.class.getSimpleName(), "Inside bill transection list click event");
        }
        return super.onContextItemSelected(item);
    }

    private class PDFGenerationTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                BillGenerator.generateBill(bill);
                //DBServices.addBill(bill);
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(GeneratedBillFragment.class.getSimpleName(), e.getMessage() + "Error while bill generation");
                return "not success";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("success")) {
                Toast.makeText(BillEditActivity.this, "Bill updated successfully!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BillEditActivity.this, ClientActivity.class);
                intent.putExtra("id", bill.getClient_id());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(BillEditActivity.this, "Some error while bill generation !!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
