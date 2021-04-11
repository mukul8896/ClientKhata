package com.mukul.companyAccounts;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dao.DbHandler;
import modals.Transection;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;

public class AddTransecActivity extends AppCompatActivity {
    private EditText date_edit_txt;
    private EditText amt_edit_txt;
    private EditText desc_edit_txt;
    private DatePickerDialog picker;
    private Integer client_id;
    private Spinner spinner;
    private String modes;
    private Integer transectionId;
    private String parentActivity;
    private ClientDbServices clientDbServices;
    private TransectionDbServices transectionDbServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transec);

        client_id = getIntent().getIntExtra("id", 0);

        DbHandler handler=new DbHandler(this);
        clientDbServices=new ClientDbServices(handler);
        transectionDbServices=new TransectionDbServices(handler);


        Bundle bundle = getIntent().getBundleExtra("data");
        if (bundle != null) {
            modes = bundle.getString("mode");
            transectionId = bundle.getInt("transecid");
            client_id = bundle.getInt("clientid");
            parentActivity = bundle.getString("parentActivity");
        }

        spinner = (Spinner) findViewById(R.id.transec_type);
        amt_edit_txt = (EditText) findViewById(R.id.transec_amount);
        desc_edit_txt = (EditText) findViewById(R.id.transec_desc);
        date_edit_txt = (EditText) findViewById(R.id.transec_date);

        String[] spinner_list = new String[]{"Credit", "Debit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinner_list);
        spinner.setAdapter(adapter);

        if (modes != null && modes.equals("Edit")) {
            Transection transection = transectionDbServices.getTransection(transectionId);
            amt_edit_txt.setText(transection.getAmount() + "");
            desc_edit_txt.setText(transection.getDesc());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            date_edit_txt.setText(simpleDateFormat.format(transection.getDate()));
            spinner.setSelection(adapter.getPosition(transection.getTransecType()));
        }

        date_edit_txt.setShowSoftInputOnFocus(false);
        date_edit_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
                Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                picker = new DatePickerDialog(AddTransecActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        //date_edit_txt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        Date date = new Date(year - 1900, month, dayOfMonth);
                        date_edit_txt.setText(fmt.format(date));
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Log.i(MainActivity.class.getSimpleName(), "mukul 8");
        Button add_transec = (Button) findViewById(R.id.add_transec_submit_btn);
        add_transec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!amt_edit_txt.getText().toString().matches("[0-9]+"))
                        throw new Exception("Invalid amount number !!");
                    if (desc_edit_txt.getText() != null && desc_edit_txt.getText().toString().isEmpty())
                        throw new Exception("Provide desription !!");
                    if (desc_edit_txt.getText() != null && desc_edit_txt.getText().toString().isEmpty())
                        throw new Exception("Provide desription !!");
                    if (date_edit_txt.getText() != null && date_edit_txt.getText().toString().isEmpty())
                        throw new Exception("Provide transection date !!");
                    Integer amount = Integer.parseInt(amt_edit_txt.getText().toString());
                    String desc = desc_edit_txt.getText().toString();
                    String date = date_edit_txt.getText().toString();
                    String type = spinner.getSelectedItem().toString();

                    if (modes != null && modes.equals("Edit"))
                        transectionDbServices.updateTransection(amount, transectionId, desc, date, type, client_id);
                    else
                        transectionDbServices.addTransectioin(amount, client_id, desc, date, type);
                    Intent intent = null;
                    if (parentActivity != null && parentActivity.contains(BillEditActivity.class.getSimpleName())) {
                        intent = new Intent(AddTransecActivity.this, BillEditActivity.class);
                        intent.putExtra("bill_id", Integer.parseInt(parentActivity.split("-")[1]));
                    } else {
                        intent = new Intent(AddTransecActivity.this, ClientActivity.class);
                    }
                    intent.putExtra("id", client_id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(AddTransecActivity.this, "Done !!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
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
            Intent intent = null;
            if (parentActivity != null && parentActivity.contains(BillEditActivity.class.getSimpleName())) {
                intent = new Intent(AddTransecActivity.this, BillEditActivity.class);
                intent.putExtra("bill_id", Integer.parseInt(parentActivity.split("-")[1]));
            } else {
                intent = new Intent(AddTransecActivity.this, ClientActivity.class);
            }
            intent.putExtra("id", client_id);
            intent.putExtra("Client Name", clientDbServices.getClient(client_id).getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }
}
