package com.mukul.clientbilling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import BeanClasses.Client;
import db_services.DBServices;

public class AddClientAvtivity extends AppCompatActivity {
    EditText client_name;
    EditText address;
    EditText contact;
    String modes;
    Integer clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        Bundle bundle=getIntent().getBundleExtra("data");
        if(bundle!=null){
            modes  = bundle.getString("mode");
            clientId = bundle.getInt("id");
        }


        client_name= findViewById(R.id.client_name_txt);
        address= findViewById(R.id.client_address_txt);
        contact= findViewById(R.id.client_contact_txt);
        Button add= findViewById(R.id.add_client);

        if(modes!=null && modes.equals("Edit")){
            Client client=DBServices.getClient(clientId);
            Log.i(MainActivity.class.getSimpleName(),client.getName() +" : "+client.getAddress()+":"+client.getContact());
            client_name.setText(client.getName());
            address.setText(client.getAddress());
            contact.setText(client.getContact());
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(modes!=null && modes.equals("Edit"))
                        DBServices.updateClient(client_name.getText().toString(),address.getText().toString(),contact.getText().toString(),clientId);
                    else
                        DBServices.addClient(client_name.getText().toString(),address.getText().toString(),contact.getText().toString());
                    Toast.makeText(AddClientAvtivity.this, "Done !!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddClientAvtivity.this,
                            MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }catch (Exception e){
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
