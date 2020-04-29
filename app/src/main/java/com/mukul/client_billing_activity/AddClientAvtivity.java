package com.mukul.client_billing_activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import BeanClasses.Client;
import db_services.ClientDbServices;

public class AddClientAvtivity extends AppCompatActivity {
    EditText client_name;
    EditText address;
    EditText contact;
    EditText fee;
    String modes;
    Integer clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        String password=getIntent().getStringExtra("password");
        Bundle bundle = getIntent().getBundleExtra("data");
        if (bundle != null) {
            modes = bundle.getString("mode");
            clientId = bundle.getInt("id");
        }

        client_name = (EditText) findViewById(R.id.client_name_txt);
        address = (EditText) findViewById(R.id.client_address_txt);
        fee=(EditText) findViewById(R.id.client_fee);
        contact =(EditText) findViewById(R.id.client_contact_txt);
        Button add = (Button)findViewById(R.id.add_client);

        if (modes != null && modes.equals("Edit")) {
            Client client = ClientDbServices.getClient(clientId);
            Log.i(MainActivity.class.getSimpleName(), client.getName() + " : " + client.getAddress() + ":" + client.getContact());
            client_name.setText(client.getName());
            address.setText(client.getAddress());
            fee.setText(client.getFee()+"");
            contact.setText(client.getContact());
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (modes != null && modes.equals("Edit"))
                        ClientDbServices.updateClient(client_name.getText().toString(), address.getText().toString(), Integer.parseInt(fee.getText().toString()),contact.getText().toString(), clientId);
                    else
                        ClientDbServices.addClient(client_name.getText().toString(), address.getText().toString(), Integer.parseInt(fee.getText().toString()),contact.getText().toString());
                    Toast.makeText(AddClientAvtivity.this, "Done !!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddClientAvtivity.this,
                            MainActivity.class);
                    intent.putExtra("app_password",password);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(AddClientAvtivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return true;
    }
}
