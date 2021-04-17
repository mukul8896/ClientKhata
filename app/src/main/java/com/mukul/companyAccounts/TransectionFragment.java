package com.mukul.companyAccounts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import adapterClasses.TransectionListRecyclerViewAdapter;
import dao.DbHandler;
import modals.Client;
import modals.Transection;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;

public class TransectionFragment extends Fragment implements TransectionListRecyclerViewAdapter.ItemEventListner{
    private List<Transection> transectionList;
    private Integer client_id;
    private TransectionListRecyclerViewAdapter adapter;
    private Client client;
    private int index;

    public static TransectionFragment newInstance(Integer clientId) {
        TransectionFragment fragment = new TransectionFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putInt("ClientId", clientId);
        fragment.setArguments(bundle2);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TransectionFragment.class.getSimpleName(), "inside onCreate");

        client_id = getArguments().getInt("ClientId");
        transectionList = TransectionDbServices.getClientsTransections(client_id);
        client = ClientDbServices.getClient(client_id);
        Collections.sort(transectionList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TransectionFragment.class.getSimpleName(), "inside onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_transections, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.transection_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransectionListRecyclerViewAdapter(this.getContext(),  transectionList, TransectionFragment.this);
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);


        Button add_new_transection = (Button) rootView.findViewById(R.id.add_transection_btn);
        add_new_transection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_to_add_transec = new Intent(getActivity().getApplicationContext(),
                        AddTransecActivity.class);
                intent_to_add_transec.putExtra("id", client_id);
                Log.i(TransectionFragment.class.getSimpleName(), "about to starrt transection add activity");
                startActivity(intent_to_add_transec);
            }
        });
        return rootView;
    }

    @Override
    public void onClick(View view, int position) {
        if (transectionList.get(position).getBill_details() != null && !transectionList.get(position).getBill_details().isEmpty())
            Toast.makeText(getActivity().getApplicationContext(), transectionList.get(position).getBill_details(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View view, int position) {
        Log.i(TransectionFragment.class.getSimpleName(), "Logn press done");
        index = position;
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.transection_list_context_menu, menu);
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
            data.putInt("clientid", client_id);
            data.putInt("transecid", transectionList.get(index).getTransecId());
            intent.putExtra("data", data);
            startActivity(intent);
            return true;
        }
        return true;
    }
}
