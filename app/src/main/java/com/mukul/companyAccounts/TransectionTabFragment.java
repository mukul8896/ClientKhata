package com.mukul.companyAccounts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import adapterClasses.PageAdapter;
import adapterClasses.TransectionListAdapter;
import dbServices.BillDbServices;
import modals.Client;
import modals.Transection;
import dbServices.TransectionDbServices;
import utils.ProjectUtils;

public class TransectionTabFragment extends Fragment implements TransectionListAdapter.ItemEventListner{
    private TransectionListAdapter adapter;
    private Client client;
    private int index;
    private List<Transection> transectionList;
    private RecyclerView recyclerView;

    private static TransectionTabFragment transectionTabFragment;

    public static TransectionTabFragment newInstance(List<Transection> transectionList, Client client) {
        if(transectionTabFragment==null)
            transectionTabFragment=new TransectionTabFragment(transectionList,client);
        else{
            transectionTabFragment.setClient(client);
            transectionTabFragment.setTransectionList(transectionList);
        }
        return transectionTabFragment;
    }

    public TransectionTabFragment(List<Transection> transectionList, Client client) {
        this.transectionList=transectionList;
        this.client=client;
    }

    public void setClient(Client client){
        this.client=client;
    }
    public void setTransectionList(List<Transection> transectionList){
        this.transectionList=transectionList;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TransectionTabFragment.class.getSimpleName(), "inside onCreate");
        Collections.sort(transectionList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TransectionTabFragment.class.getSimpleName(), "inside onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_transections, container, false);
        setHasOptionsMenu(true);

        recyclerView = rootView.findViewById(R.id.transection_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransectionListAdapter(this.getContext(),  transectionList, TransectionTabFragment.this);
        recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);
        return rootView;
    }

    @Override
    public void onClick(View view, int position) {
        if (transectionList.get(position).getBill_details() != null && !transectionList.get(position).getBill_details().isEmpty())
            Toast.makeText(getActivity().getApplicationContext(), transectionList.get(position).getBill_details(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View view, int position) {
        Log.i(TransectionTabFragment.class.getSimpleName(), "Logn press done");
        index = position;
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.transection_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (transectionList.get(index).getBill_details() != null && !transectionList.get(index).getBill_details().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "This transection is already added to bill please update bill first !!", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.delete) {
            try {
                TransectionDbServices.deleteTransection(client.getId(), transectionList.get(index));
                transectionList.remove(index);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity().getApplicationContext(), "Transection deleted successfully!!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.edit) {
            Intent intent = new Intent(getActivity(), AddTransecActivity.class);
            Bundle data = new Bundle();
            data.putString("mode", "Edit");
            data.putInt("clientid", client.getId());
            data.putInt("transecid", transectionList.get(index).getTransecId());
            intent.putExtra("data", data);
            startActivity(intent);
            return true;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.client_options_menu, menu);

        MenuItem item_year2 = menu.findItem(R.id.client_year1);
        item_year2.setTitle(ProjectUtils.getFinancialYear(Calendar.getInstance().getTime()));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) -1);
        MenuItem item_year3 = menu.findItem(R.id.client_year2);
        item_year3.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));

        MenuItem item_year1 = menu.findItem(R.id.client_year3);
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 2);
        item_year1.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.client_add_action){
            Intent intent_to_add_transec = new Intent(getActivity().getApplicationContext(),
                    AddTransecActivity.class);
            intent_to_add_transec.putExtra("id", client.getId());
            Log.i(TransectionTabFragment.class.getSimpleName(), "about to starrt transection add activity");
            startActivity(intent_to_add_transec);
        }else{
            String financialYear = item.getTitle().toString();
            transectionList = TransectionDbServices.getClientsTransections(client.getId(),financialYear);
            ((ClientActivity)getActivity()).getSupportActionBar().setSubtitle(financialYear);
            adapter = new TransectionListAdapter(this.getContext(),  transectionList, TransectionTabFragment.this);
            recyclerView.setAdapter(adapter);
        }
        return super.onOptionsItemSelected(item);
    }
}
