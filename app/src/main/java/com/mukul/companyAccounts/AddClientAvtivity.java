package com.mukul.companyAccounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import modals.Client;
import dao.DbHandler;
import dbServices.ClientDbServices;

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
                    Toast.makeText(AddClientAvtivity.this, "Client Added Successfully !!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddClientAvtivity.this,
                            MainDrawerActivity.class);
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
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }
}
