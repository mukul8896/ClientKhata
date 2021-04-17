package com.mukul.companyAccounts;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import adapterClasses.SummeryListAdapter;
import dao.DbHandler;
import modals.Client;
import modals.Transection;
import dbServices.TransectionDbServices;
import services.AnnualSummeryServices;
import utils.ProjectUtils;


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

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        year = ProjectUtils.getFinancialYear(calendar.getTime());
        getSupportActionBar().setTitle(year);

        clinetList = getIntent().getParcelableArrayListExtra("clientList");
        transectionlist = TransectionDbServices.getFinancialYearTransection(year);

        TextView credit = findViewById(R.id.total_credit);
        TextView debit = findViewById(R.id.total_debit);
        TextView due = findViewById(R.id.total_due);

        int total_credit = 0;
        int total_debit = 0;
        for (Transection transection : transectionlist) {
            if (transection.getTransecType().equals("Credit"))
                total_credit += transection.getAmount();
            if (transection.getTransecType().equals("Debit"))
                total_debit += transection.getAmount();
        }
        int total_due = total_debit - total_credit;
        credit.setText(total_credit + "");
        debit.setText(total_debit + "");
        due.setText(total_due + "");

        adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, clinetList, transectionlist);
        listView = (ListView) findViewById(R.id.sumery_list);
        listView.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.summery_download_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(SummeryActivity.this).create();
                LayoutInflater inflater = LayoutInflater.from(SummeryActivity.this);
                View dialogeview = inflater.inflate(R.layout.summery_fab_dialoge, null);
                alertDialog.setView(dialogeview);
                alertDialog.setTitle("Choose File Type");
                String[] arr = new String[]{"Excel", "PDF"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SummeryActivity.this, android.R.layout.simple_list_item_1, Arrays.asList(arr));
                ListView listview = dialogeview.findViewById(R.id.summer_file_type_list);
                listview.setAdapter(arrayAdapter);
                listview.setOnItemClickListener((parent, view1, position, id) -> {
                    try {
                        AnnualSummeryServices services = new AnnualSummeryServices(clinetList, transectionlist);
                        File report_file = services.generateYearReport(year, Arrays.asList(arr).get(position));
                        if (position == Arrays.asList(arr).indexOf("PDF")) {
                            alertDialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri uri = FileProvider.getUriForFile(SummeryActivity.this, BuildConfig.APPLICATION_ID + ".provider", report_file);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "application/pdf");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(report_file.getAbsolutePath()), "application/pdf");
                                intent = Intent.createChooser(intent, "Open File");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        } else {
                            alertDialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri uri = FileProvider.getUriForFile(SummeryActivity.this, BuildConfig.APPLICATION_ID + ".provider", report_file);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "application/vnd.ms-excel");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(report_file.getAbsolutePath()), "application/vnd.ms-excel");
                                intent = Intent.createChooser(intent, "Open File");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(SummeryActivity.this, "Some Error Occured !!", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_summery, menu);

        MenuItem item_year2 = menu.findItem(R.id.year1);
        item_year2.setTitle(ProjectUtils.getFinancialYear(Calendar.getInstance().getTime()));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) -1);
        MenuItem item_year3 = menu.findItem(R.id.year2);
        item_year3.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));

        MenuItem item_year1 = menu.findItem(R.id.year3);
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 2);
        item_year1.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));

        MenuItem item = menu.findItem(R.id.summery_action_search);

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
                adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, filteredClientList, transectionlist);
                if(listView==null)
                    listView = (ListView) findViewById(R.id.sumery_list);
                listView.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id != R.id.summery_action_search) {
            year = item.getTitle().toString();
            Log.d(SummeryActivity.class.getSimpleName(), year);
            transectionlist = TransectionDbServices.getFinancialYearTransection(year);
            onRestart();
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getSupportActionBar().setTitle(year);
        TextView credit = findViewById(R.id.total_credit);
        TextView debit = findViewById(R.id.total_debit);
        TextView due = findViewById(R.id.total_due);

        int total_credit = 0;
        int total_debit = 0;
        for (Transection transection : transectionlist) {
            if (transection.getTransecType().equals("Credit"))
                total_credit += transection.getAmount();
            if (transection.getTransecType().equals("Debit"))
                total_debit += transection.getAmount();
        }
        int total_due = total_debit - total_credit;
        credit.setText(total_credit + "");
        debit.setText(total_debit + "");
        due.setText(total_due + "");

        adapter = new SummeryListAdapter(SummeryActivity.this, R.layout.summery_list_item_layout, clinetList, transectionlist);
        if (listView == null)
            listView = (ListView) findViewById(R.id.sumery_list);
        listView.setAdapter(adapter);
    }
}
