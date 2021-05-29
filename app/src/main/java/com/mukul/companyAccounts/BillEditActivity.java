package com.mukul.companyAccounts;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import adapterClasses.TransectionListAdapter;
import modals.Bill;
import modals.Transection;
import services.BillGenerationServices;
import dbServices.BillDbServices;
import dbServices.TransectionDbServices;

public class BillEditActivity extends AppCompatActivity implements TransectionListAdapter.ItemEventListner {
    private RecyclerView transection_lstView;
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

        Log.i(BillEditActivity.class.getSimpleName(), "Bill Id-" + getIntent().getIntExtra("bill_id", 0));
        try {
            bill = BillDbServices.getBill(getIntent().getIntExtra("bill_id", 0));
            String bill_details = bill.getBill_year() + " | Bill No-" + bill.getBill_no();
            toolbar.setTitle(bill_details);
            transectionList = BillDbServices.getBillTransection(bill_details);
            Log.i(BillEditActivity.class.getSimpleName(),transectionList.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Some error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        updateButton = (Button) findViewById(R.id.update_bill);

        transection_lstView = (RecyclerView) findViewById(R.id.listview_for_edit_bill_transection);
        transection_lstView.setHasFixedSize(true);
        transection_lstView.setLayoutManager(new LinearLayoutManager(BillEditActivity.this));
        adapter = new TransectionListAdapter(BillEditActivity.this,  transectionList, BillEditActivity.this);
        transection_lstView.setAdapter(adapter);
        registerForContextMenu(transection_lstView);
        ItemTouchHelper touchHelper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transection removeTrans=transectionList.get(viewHolder.getAdapterPosition());
                transectionList.remove(position);
                adapter.notifyItemRemoved(position);
                Snackbar.make(transection_lstView,"Transection removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                transectionList.add(position, removeTrans);
                                adapter.notifyItemInserted(position);
                                TransectionDbServices.removeBillDetails(transectionList.get(position));
                            }
                        }).show();
            }
        });
        if(transectionList.size()>1)
            touchHelper.attachToRecyclerView(transection_lstView);

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
        getMenuInflater().inflate(R.menu.transection_context_menu, menu);
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

    @Override
    public void onClick(View view, int position) {

    }

    @Override
    public boolean onLongClick(View view, int position) {
        Log.i(TransectionTabFragment.class.getSimpleName(), "Logn press done");
        index = position;
        return false;
    }

    private class PDFGenerationTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                BillGenerationServices services=new BillGenerationServices();
                services.updateBill(bill,BillEditActivity.this.getApplicationContext());
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(BillTabFragment.class.getSimpleName(), e.getMessage() + "Error while bill generation");
                return "not success";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("success")) {
                Toast.makeText(BillEditActivity.this, "Bill updated successfully!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BillEditActivity.this, ClientActivity.class);
                intent.putExtra("currentTab",1);
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
