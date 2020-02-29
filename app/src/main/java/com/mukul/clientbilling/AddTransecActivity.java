package com.mukul.clientbilling;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

import DbConnect.DBServices;

public class AddTransecActivity extends AppCompatActivity {
    EditText date_edit;
    EditText amt_edit;
    EditText desc_edit;
    DatePickerDialog picker;
    Integer client_id;
    Spinner spinner;
    Button add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transec);
        client_id=getIntent().getIntExtra("id",0);

        amt_edit=(EditText)findViewById(R.id.transec_amount);
        desc_edit=(EditText)findViewById(R.id.transec_desc);

        date_edit=(EditText) findViewById(R.id.transec_date);
        date_edit.setShowSoftInputOnFocus(false);
        date_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                picker=new DatePickerDialog(AddTransecActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date_edit.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        spinner=(Spinner) findViewById(R.id.transec_type);
        String[] spinner_list=new String[]{"Credit","Debit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinner_list);
        spinner.setAdapter(adapter);


        add=(Button)findViewById(R.id.add_transec_submit_btn);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!amt_edit.getText().toString().matches("[0-9]+"))
                        throw new Exception("Invalid amount number !!");
                    Integer amount=Integer.parseInt(amt_edit.getText().toString());
                    String desc=desc_edit.getText().toString();
                    String date=date_edit.getText().toString();
                    String type=spinner.getSelectedItem().toString();
                    DBServices.addTransectioin(amount,client_id,desc,date,type);
                    Toast.makeText(AddTransecActivity.this, "Done !!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddTransecActivity.this,
                            ClientDataActivity.class);
                    intent.putExtra("id",client_id);
                    intent.putExtra("Client Name",DBServices.getClient(client_id).getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(AddTransecActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(AddTransecActivity.this,
                    ClientDataActivity.class);
            intent.putExtra("id",client_id);
            intent.putExtra("Client Name",DBServices.getClient(client_id).getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }

        return true;
    }
}
