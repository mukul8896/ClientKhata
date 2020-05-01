package com.mukul.client_billing_activity;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import AdapterClasses.ClientListAdapter;
import AdapterClasses.SummeryListAdapter;
import BeanClasses.Client;
import BeanClasses.Transection;
import db_services.TransectionDbServices;
import utils.GeneralUtils;


public class SummeryActivity extends AppCompatActivity {
    private String year;
    private ListView listView;
    private SummeryListAdapter adapter;
    private List<Client> clinetList;
    private List<Client> filteredClientList;
    private List<Transection> transectionlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summery);

        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR,2019);
        year=GeneralUtils.getFinancialYear(calendar.getTime());
        getSupportActionBar().setTitle(year);

        clinetList=getIntent().getParcelableArrayListExtra("clientList");
        transectionlist=TransectionDbServices.getFinancialYearTransection(year);

        TextView credit=findViewById(R.id.total_credit);
        TextView debit=findViewById(R.id.total_debit);
        TextView due=findViewById(R.id.total_due);

        int total_credit=0;
        int total_debit=0;
        for(Transection transection:transectionlist){
            if(transection.getTransecType().equals("Credit"))
                total_credit+=transection.getAmount();
            if(transection.getTransecType().equals("Debit"))
                total_debit+=transection.getAmount();
        }
        int total_due=total_debit-total_credit;
        credit.setText(total_credit+"");
        debit.setText(total_debit+"");
        due.setText(total_due+"");

        adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, clinetList,transectionlist);
        listView=(ListView)findViewById(R.id.sumery_list);
        listView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.summery_download_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_summery, menu);

        MenuItem item_year2 = menu.findItem(R.id.year2);
        item_year2.setTitle(GeneralUtils.getFinancialYear(Calendar.getInstance().getTime()));

        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.YEAR,Calendar.getInstance().get(Calendar.YEAR)+1);
        MenuItem item_year3 = menu.findItem(R.id.year3);
        item_year3.setTitle(GeneralUtils.getFinancialYear(cal.getTime()));

        MenuItem item_year1 = menu.findItem(R.id.year1);
        cal.set(Calendar.YEAR,Calendar.getInstance().get(Calendar.YEAR)-1);
        item_year1.setTitle(GeneralUtils.getFinancialYear(cal.getTime()));

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Enter Client Name...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filteredClientList = new ArrayList<>();
                for (Client client : clinetList) {
                    if (client.getName().toLowerCase().startsWith(newText.toLowerCase()))
                        filteredClientList.add(client);
                }
                adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, filteredClientList,transectionlist);
                listView.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        year=item.getTitle().toString();
        onRestart();
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        transectionlist=TransectionDbServices.getFinancialYearTransection(year);
        getSupportActionBar().setTitle(year);
        TextView credit=findViewById(R.id.total_credit);
        TextView debit=findViewById(R.id.total_debit);
        TextView due=findViewById(R.id.total_due);

        int total_credit=0;
        int total_debit=0;
        for(Transection transection:transectionlist){
            if(transection.getTransecType().equals("Credit"))
                total_credit+=transection.getAmount();
            if(transection.getTransecType().equals("Debit"))
                total_debit+=transection.getAmount();
        }
        int total_due=total_debit-total_credit;
        credit.setText(total_credit+"");
        debit.setText(total_debit+"");
        due.setText(total_due+"");

        adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, clinetList,transectionlist);
        if(listView==null)
            listView=(ListView)findViewById(R.id.sumery_list);
        listView.setAdapter(adapter);
    }
}
