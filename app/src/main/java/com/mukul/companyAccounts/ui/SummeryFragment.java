package com.mukul.companyAccounts.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mukul.companyAccounts.BuildConfig;
import com.mukul.companyAccounts.MainDrawerActivity;
import com.mukul.companyAccounts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import adapterClasses.SummeryListAdapter;
import dbServices.TransectionDbServices;
import modals.Client;
import modals.Transection;
import services.AnnualSummeryServices;
import utils.ProjectUtils;

public class SummeryFragment extends Fragment {
    private List<Client> clinetList;
    private List<Transection> transectionlist;
    private ListView listView;
    private SummeryListAdapter adapter;
    private String year;
    private TextView credit;
    private TextView debit;
    private TextView due;

    private static SummeryFragment summeryFragment;

    public SummeryFragment(List<Client> clientList,List<Transection> transectionlist,String year) {
        this.clinetList=clientList;
        this.transectionlist=transectionlist;
        this.year=year;
    }

    public static SummeryFragment newInstance(List<Client> clientList,List<Transection> transectionlist,String year) {
        if(summeryFragment==null)
            summeryFragment =  new SummeryFragment(clientList,transectionlist,year);
        return summeryFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summery, container, false);
        setHasOptionsMenu(true);

        credit = view.findViewById(R.id.total_credit);
        debit = view.findViewById(R.id.total_debit);
        due = view.findViewById(R.id.total_due);

        updateHeader();

        adapter = new SummeryListAdapter(this.getContext(), R.layout.summery_list_item_layout, clinetList, transectionlist);
        listView = (ListView) view.findViewById(R.id.sumery_list);
        listView.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.summery_download_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(SummeryFragment.this.getContext()).create();
                LayoutInflater inflater = LayoutInflater.from(SummeryFragment.this.getContext());
                View dialogeview = inflater.inflate(R.layout.summery_fab_dialoge, null);
                alertDialog.setView(dialogeview);
                alertDialog.setTitle("Choose File Type");
                String[] arr = new String[]{"Excel", "PDF"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SummeryFragment.this.getContext(), android.R.layout.simple_list_item_1, Arrays.asList(arr));
                ListView listview = dialogeview.findViewById(R.id.summer_file_type_list);
                listview.setAdapter(arrayAdapter);
                listview.setOnItemClickListener((parent, view1, position, id) -> {
                    try {
                        AnnualSummeryServices services = new AnnualSummeryServices(clinetList, transectionlist);
                        File report_file = services.generateYearReport(year, Arrays.asList(arr).get(position));
                        if (position == Arrays.asList(arr).indexOf("PDF")) {
                            alertDialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri uri = FileProvider.getUriForFile(SummeryFragment.this.getContext(), BuildConfig.APPLICATION_ID + ".provider", report_file);
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
                                Uri uri = FileProvider.getUriForFile(SummeryFragment.this.getContext(), BuildConfig.APPLICATION_ID + ".provider", report_file);
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
                        Toast.makeText(SummeryFragment.this.getContext(), "Some Error Occured !!", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_summery, menu);

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
                 List<Client> filteredClientList = new ArrayList<>();
                for (Client client : clinetList) {
                    if (client.getName().toLowerCase().startsWith(newText.toLowerCase()))
                        filteredClientList.add(client);
                }
                adapter = new SummeryListAdapter(SummeryFragment.this.getContext(), R.layout.summery_list_item_layout, filteredClientList, transectionlist);
                listView.setAdapter(adapter);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id != R.id.summery_action_search) {
            year = item.getTitle().toString();
            ((MainDrawerActivity)getActivity()).getSupportActionBar().setTitle("Summery | "+year);
            Log.d(SummeryFragment.class.getSimpleName(), year);
            transectionlist = TransectionDbServices.getFinancialYearTransection(year);
            updateHeader();
            adapter = new SummeryListAdapter(SummeryFragment.this.getContext(), R.layout.summery_list_item_layout, clinetList, transectionlist);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateHeader(){
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
    }
}