package com.mukul.companyAccounts.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mukul.companyAccounts.AddClientAvtivity;
import com.mukul.companyAccounts.ClientActivity;
import com.mukul.companyAccounts.BillTabFragment;
import com.mukul.companyAccounts.R;

import java.util.ArrayList;
import java.util.List;

import adapterClasses.ClientListAdapder;
import dao.Migration;
import dbServices.ClientDbServices;
import modals.Client;

public class ClientListFragment extends Fragment implements ClientListAdapder.ItemEventListner {

    private static ClientListFragment fragment;
    private List<Client> clientList;
    private RecyclerView recyclerlistView;
    private ClientListAdapder adapter;
    private int index;
    private TextView total_balance;
    private  TextView total_fee;
    private static String interval = "all";
    private static String filter_type = "Balance";
    private TextView total_value_tag;

    public ClientListFragment(List<Client> clientList) {
        this.clientList=clientList;
    }

    public void setList(List<Client> clientList){
        this.clientList=clientList;
    }

    public List<Client> getClientList() {
        return clientList;
    }

    public static ClientListFragment newInstance() {
        return fragment;
    }

    public static ClientListFragment newInstance(List<Client> clientList) {
        if(fragment==null)
            fragment = new ClientListFragment(clientList);
        else
            fragment.setList(clientList);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_list, container, false);
        setHasOptionsMenu(true);

        total_balance = (TextView) view.findViewById(R.id.total_balance);
        total_fee=(TextView) view.findViewById(R.id.total_fee);
        total_value_tag=(TextView) view.findViewById(R.id.total_value_id);
        total_value_tag.setText("Total "+filter_type);
        total_balance.setText(getTotalBalance(clientList));
        total_fee.setText(getTotalFee(clientList));

        recyclerlistView = (RecyclerView) view.findViewById(R.id.client_list_view);
        recyclerlistView.setHasFixedSize(true);
        recyclerlistView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        adapter = new ClientListAdapder(getActivity().getApplicationContext(), clientList, this);
        recyclerlistView.setAdapter(adapter);
        registerForContextMenu(recyclerlistView);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        AddClientAvtivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.transection_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Alert !!");
            builder.setMessage("All data related to this client will be deleted.\n" +
                    "Do you want to continue ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        if(ClientDbServices.deleteClient(clientList.get(index).getId())) {
                            clientList.remove(index);
                            adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(ClientListFragment.this.getContext(), "Clould not delete this client...!", Toast.LENGTH_SHORT).show();
                        }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else if (id == R.id.edit) {
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    AddClientAvtivity.class);
            Bundle data = new Bundle();
            data.putString("mode", "Edit");
            data.putInt("id", clientList.get(index).getId());
            intent.putExtra("data", data);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_options_menu, menu);

        MenuItem item = menu.findItem(R.id.main_action_search);
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
                List<Client> fullList = ClientDbServices.getClientsList(interval, filter_type);
                for (Client client : fullList) {
                    if (client.getName().toLowerCase().startsWith(newText.toLowerCase()))
                        filteredClientList.add(client);
                }
                total_balance.setText(getTotalBalance(filteredClientList));
                total_fee.setText(getTotalFee(filteredClientList));
                clientList = filteredClientList;
                adapter = new ClientListAdapder(getActivity().getApplicationContext(), clientList, ClientListFragment.this);
                recyclerlistView.setAdapter(adapter);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.main_action_filter){
            Dialog dialog = new Dialog(this.getContext());
            dialog.setContentView(R.layout.client_filter_dialoge);

            Spinner monSpinner = (Spinner) dialog.findViewById(R.id.month_of_filter);
            String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            ArrayAdapter<String> months_adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, monthNames);
            monSpinner.setAdapter(months_adapter);
            for (String str : monthNames) {
                if (str.equals(interval)) {
                    monSpinner.setSelection(months_adapter.getPosition(str));
                }
            }
            Spinner yearSpinner = (Spinner) dialog.findViewById(R.id.year_of_filter);
            String[] years = {"2020", "2021", "2022"};
            ArrayAdapter<String> year_adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, years);
            yearSpinner.setAdapter(year_adapter);


            Spinner typeSpinner = (Spinner) dialog.findViewById(R.id.type_of_filter);
            String[] type_list = new String[]{"Credit", "Debit", "Balance"};
            ArrayAdapter<String> type_adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, type_list);
            typeSpinner.setAdapter(type_adapter);
            for (String str : type_list) {
                if (str.equals(filter_type)) {
                    typeSpinner.setSelection(type_adapter.getPosition(str));
                }
            }

            TextView submit = (TextView) dialog.findViewById(R.id.apply_filter);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        interval = monSpinner.getSelectedItem().toString();
                        filter_type = typeSpinner.getSelectedItem().toString();
                        FilterAsyncTask task = new FilterAsyncTask();
                        task.execute();
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ClientListFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            TextView cancel = (TextView) dialog.findViewById(R.id.remove_filter);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interval = "all";
                    filter_type = "Balance";
                    clientList=ClientDbServices.getClientsList(interval,filter_type);
                    total_balance.setText(getTotalBalance(clientList));
                    total_fee.setText(getTotalFee(clientList));
                    adapter = new ClientListAdapder(getActivity().getApplicationContext(), clientList, ClientListFragment.this);
                    recyclerlistView.setAdapter(adapter);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, int position) {
        Log.d("mk_logs","Clicked on:"+clientList.get(position));
        Intent intent = new Intent(getActivity().getApplicationContext(),
                ClientActivity.class);
        intent.putExtra("id", clientList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view, int position) {
        Log.d("mk_logs","Long Clicked on:"+clientList.get(position));
        index = position;
        return false;
    }

    private String getTotalBalance(List<Client> clientList) {
        int total = 0;
        for (Client client : clientList) {
            total += client.getBalance();
        }
        if (total < 0) {
            total = total * -1;
            return "("+total +")";
        } else
            return total+"";
    }

    private String getTotalFee(List<Client> clientList){
        int total = 0;
        for (Client client : clientList) {
            total += client.getFee();
        }
        return total+"";
    }


    private class FilterAsyncTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDoalog;
        @Override
        protected void onPreExecute() {
            progressDoalog = new ProgressDialog(ClientListFragment.this.getContext());
            progressDoalog.setMax(100);
            progressDoalog.setMessage("Its loading....");
            progressDoalog.setTitle("Applying filter...");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                clientList = ClientDbServices.getClientsList(interval, filter_type);
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
                total_value_tag.setText("Total "+filter_type);
                total_balance.setText(getTotalBalance(clientList));
                if (recyclerlistView == null)
                    recyclerlistView = getView().findViewById(R.id.client_list_view);
                adapter = new ClientListAdapder(ClientListFragment.this.getContext(), clientList, ClientListFragment.this);
                recyclerlistView.setAdapter(adapter);
                progressDoalog.dismiss();
            } else {
                progressDoalog.dismiss();
                Toast.makeText(ClientListFragment.this.getContext(), "Some error while filtering data !!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        clientList=ClientDbServices.getClientsList("all","Balance");
        total_balance.setText(getTotalBalance(clientList));
        total_fee.setText(getTotalFee(clientList));
        adapter = new ClientListAdapder(getActivity().getApplicationContext(), clientList, ClientListFragment.this);
        recyclerlistView.setAdapter(adapter);
    }
}