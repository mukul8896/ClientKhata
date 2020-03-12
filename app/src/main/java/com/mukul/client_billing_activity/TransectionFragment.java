package com.mukul.client_billing_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mukul.client_billing_activity.R;

import java.util.Collections;
import java.util.List;

import AdapterClasses.TransectionListAdapter;
import BeanClasses.Client;
import BeanClasses.Transection;
import db_services.DBServices;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransectionFragment extends Fragment {
    private ListView transection_lstView;
    private List<Transection> transectionList;
    private Integer client_id;
    private TransectionListAdapter adapter;
    private Client client;
    private int index;

    public TransectionFragment() {}

    public static TransectionFragment newInstance(Integer clientId) {
        TransectionFragment fragment = new TransectionFragment();
        Bundle bundle2=new Bundle();
        bundle2.putInt("ClientId",clientId);
        fragment.setArguments(bundle2);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client_id=getArguments().getInt("ClientId");
        transectionList= DBServices.getClientsTransections(client_id);
        client=DBServices.getClient(client_id);
        Collections.sort(transectionList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transections, container, false);
        transection_lstView= (ListView)rootView.findViewById(R.id.transection_list);
        adapter=new TransectionListAdapter(getActivity().getApplicationContext(),R.layout.transection_list_item,transectionList);
        transection_lstView.setAdapter(adapter);

        registerForContextMenu(transection_lstView);
        transection_lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(ClientDataActivity.class.getSimpleName(),"Logn press done");
                index=position;
                return false;
            }
        });
        Button add_new_transection= (Button) rootView.findViewById(R.id.add_transection_btn);
        add_new_transection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_to_add_transec = new Intent(getActivity().getApplicationContext(),
                        AddTransecActivity.class);
                intent_to_add_transec.putExtra("id",client_id);
                Log.i(ClientDataActivity.class.getSimpleName(),"about to starrt transection add activity");
                startActivity(intent_to_add_transec);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.client_transection_list_menu,menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            try {
                DBServices.deleteTransection(client,transectionList.get(index));
                transectionList.remove(index);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity().getApplicationContext(), "Done !!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if(id == R.id.edit){
            Intent intent = new Intent(getActivity(), AddTransecActivity.class);
            Bundle data=new Bundle();
            data.putString("mode","Edit");
            data.putInt("clientid",client_id);
            data.putInt("transecid",transectionList.get(index).getTransecId());
            intent.putExtra("data",data);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }
}