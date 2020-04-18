package com.mukul.client_billing_activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import AdapterClasses.ClientListAdapter;
import BeanClasses.Client;
import db_services.ClientDbServices;
import utils.ProjectUtil;

public class MainActivity extends AppCompatActivity {
    private List<Client> clientsList;
    private ListView listView;
    private int index;
    private ClientListAdapter adapter;
    private TextView total_balance;
    private static final int storage_request_code = 1;
    private int total_bal;
    private static String interval = "all";
    private static String filter_type = "Balance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* requesting permission for application */
        requestPermission();

        total_balance = (TextView) findViewById(R.id.total_balance);
        listView = findViewById(R.id.client_list);
        try {
            clientsList = ClientDbServices.getClientsList("all", "Balance");
            Log.i(MainActivity.class.getSimpleName(), clientsList.toString());
            total_bal = getTotalBalance(clientsList);
            if (total_bal < 0) {
                total_bal = total_bal * -1;
                total_balance.setText("(" + total_bal + " Rs)");
            } else
                total_balance.setText(total_bal + " Rs");
            adapter = new ClientListAdapter(this, R.layout.client_list_item, clientsList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,
                        ClientActivity.class);
                intent.putExtra("id", clientsList.get(position).getId());
                Log.i(MainActivity.class.getSimpleName(), "Client clicked");
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                index = position;
                return false;
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        AddClientAvtivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                List<Client> searchclientsList = new ArrayList<>();
                for (Client client : clientsList) {
                    if (client.getName().toLowerCase().startsWith(newText.toLowerCase()))
                        searchclientsList.add(client);
                }
                adapter = new ClientListAdapter(MainActivity.this, R.layout.client_list_item, searchclientsList);
                listView.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.client_filter_dialoge);

            Spinner monSpinner = (Spinner) dialog.findViewById(R.id.month_of_filter);
            String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, monthNames);
            monSpinner.setAdapter(adapter);
            for (String str : monthNames) {
                if (str.equals(interval)) {
                    monSpinner.setSelection(adapter.getPosition(str));
                }
            }
            Spinner yearSpinner = (Spinner) dialog.findViewById(R.id.year_of_filter);
            String[] years = {"2020", "2021", "2022"};
            ArrayAdapter<String> year_adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, years);
            yearSpinner.setAdapter(year_adapter);


            Spinner typeSpinner = (Spinner) dialog.findViewById(R.id.type_of_filter);
            String[] type_list = new String[]{"Credit", "Debit", "Balance"};
            ArrayAdapter<String> type_adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, type_list);
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            TextView cancel = (TextView) dialog.findViewById(R.id.remove_filter);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interval = "all";
                    filter_type = "Balance";
                    onRestart();
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        }
        return true;
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
            Log.i(MainActivity.class.getSimpleName(), "Delete button clicked");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert !!");
            builder.setMessage("All data related to this client will be deleted.\n" +
                    "Do you want to continue ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Log.i(MainActivity.class.getSimpleName(), "delete alert submited");
                        ClientDbServices.deleteClient(clientsList.get(index).getId());
                        //int client_bal=clientsList.get(index).getBalance();
                        clientsList.remove(index);
                        adapter.notifyDataSetChanged();
                        Log.i(MainActivity.class.getSimpleName(), "list notified");
                        //clientsList=DBServices.getClientsList();
                        Log.i(MainActivity.class.getSimpleName(), clientsList.toString());
                        total_bal = getTotalBalance(clientsList);
                        if (total_bal < 0) {
                            total_bal = total_bal * -1;
                            total_balance.setText("(" + total_bal + " Rs)");
                        } else
                            total_balance.setText(total_bal + " Rs");
                    } catch (Exception e) {
                        e.printStackTrace();
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
            Intent intent = new Intent(MainActivity.this,
                    AddClientAvtivity.class);
            Bundle data = new Bundle();
            data.putString("mode", "Edit");
            data.putInt("id", clientsList.get(index).getId());
            intent.putExtra("data", data);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(MainActivity.class.getSimpleName(), "In restart");
        try {
            clientsList = ClientDbServices.getClientsList(interval, filter_type);
            int total_bal = getTotalBalance(clientsList);
            if (total_bal < 0) {
                total_bal = total_bal * -1;
                total_balance.setText("(" + total_bal + " Rs)");
            } else
                total_balance.setText(total_bal + " Rs");
            if (listView == null)
                listView = findViewById(R.id.client_list);
            adapter = new ClientListAdapter(MainActivity.this, R.layout.client_list_item, clientsList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!isHavePermission(Manifest.permission.READ_EXTERNAL_STORAGE) && !isHavePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , storage_request_code);
            } else {
                ProjectUtil.createDirectoryFolder();
            }
        } else {
            //system OS < Marshmallow, call save pdf method
            ProjectUtil.createDirectoryFolder();
        }
    }

    private boolean isHavePermission(String permission) {
        return ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private int getTotalBalance(List<Client> list) {
        int total = 0;
        for (Client client : list) {
            total += client.getBalance();
        }
        return total;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case storage_request_code: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted from popup, call savepdf method
                    ProjectUtil.createDirectoryFolder();
                } else {
                    //permission was denied from popup, show error message
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class FilterAsyncTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDoalog;

        @Override
        protected void onPreExecute() {
            progressDoalog = new ProgressDialog(MainActivity.this);
            progressDoalog.setMax(100);
            progressDoalog.setMessage("Its loading....");
            progressDoalog.setTitle("Applying filter...");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                clientsList = ClientDbServices.getClientsList(interval, filter_type);
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
                int total_bal = getTotalBalance(clientsList);
                if (total_bal < 0) {
                    total_bal = total_bal * -1;
                    total_balance.setText("(" + total_bal + " Rs)");
                } else
                    total_balance.setText(total_bal + " Rs");
                if (listView == null)
                    listView = findViewById(R.id.client_list);
                adapter = new ClientListAdapter(MainActivity.this, R.layout.client_list_item, clientsList);
                listView.setAdapter(adapter);
                progressDoalog.dismiss();
            } else {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, "Some error while filtering data !!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
