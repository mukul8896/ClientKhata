package com.mukul.companyAccounts;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mukul.companyAccounts.ui.ClientListFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import adapterClasses.BillListAdapter;
import adapterClasses.TransectionListAdapter;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;
import modals.Bill;
import modals.Client;
import modals.Transection;
import services.BillGenerationServices;
import dbServices.BillDbServices;
import utils.BillUtils;
import utils.ProjectUtils;

public class BillTabFragment extends Fragment implements BillListAdapter.ItemEventListner {
    private BillListAdapter adapter;
    private int index;
    private Dialog dialog;
    private Client client;
    private List<Bill> billList;
    private static BillTabFragment billTabFragment;
    private RecyclerView recyclerView;

    public static BillTabFragment newInstance(List<Bill> billList, Client client) {
        if(billTabFragment==null)
            billTabFragment=new BillTabFragment(billList,client);
        else{
            billTabFragment.setClient(client);
            billTabFragment.setBillList(billList);
        }
        return billTabFragment;
    }

    public BillTabFragment(List<Bill> billList, Client client) {
        this.billList=billList;
        this.client=client;
    }

    public void setClient(Client client){
        this.client=client;
    }
    public void setBillList(List<Bill> billList){
        this.billList=billList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_generated_bill, container, false);
        setHasOptionsMenu(true);
        recyclerView = rootView.findViewById(R.id.bill_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BillListAdapter(this.getContext(),  billList, BillTabFragment.this);
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);
        return rootView;
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            BillUtils utils = new BillUtils(billList.get(index));
            try {
                utils.sharePdfFile(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Some error occured: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.edit_bill) {
            Intent intent = new Intent(getActivity(),
                    BillEditActivity.class);
            Log.i(BillTabFragment.class.getSimpleName(), billList.get(index).toString());
            intent.putExtra("bill_id", billList.get(index).getBillId());
            startActivity(intent);
            Log.i(BillTabFragment.class.getSimpleName(), "Inside generated bill fragment");
        }else if (id == R.id.delete_bill) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Alert !!");
            builder.setMessage("Do you want to continue to delete bill "+billList.get(index).getBill_year()+" | Bill No-"+billList.get(index).getBill_no()+" ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(BillDbServices.deleteBill(billList.get(index).getBillId())) {
                        BillUtils utils = new BillUtils(billList.get(index));
                        utils.deleteBill();
                        billList.remove(index);
                        adapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(BillTabFragment.this.getContext(), "Clould not delete this bill...!", Toast.LENGTH_SHORT).show();
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
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.bill_context_menu, menu);
    }

    @Override
    public void onClick(View view, int position) {
        BillUtils utils = new BillUtils(billList.get(position));
        Bill bill=billList.get(position);
        File file = utils.getFile(bill.getBill_year());
        if(!file.exists()) {
            BillrestoreTask task = new BillrestoreTask(bill);
            task.execute();
        }
        utils.openPdfFile(getActivity());
    }

    @Override
    public boolean onLongClick(View view, int position) {
        index = position;
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.client_fragment_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.client_addtransection_action){
            dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.download_bill_dialoge);

            Bill bill = new Bill();
            EditText fromDateEditText = (EditText) dialog.findViewById(R.id.from_date);
            EditText todateEditText = (EditText) dialog.findViewById(R.id.to_date);
            EditText billDateEditText = (EditText) dialog.findViewById(R.id.bill_date);
            EditText billNumber = (EditText) dialog.findViewById(R.id.bill_number);

            fromDateEditText.setShowSoftInputOnFocus(false);
            fromDateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
                    Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    DatePickerDialog picker = new DatePickerDialog(BillTabFragment.this.getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //date_edit_txt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            Date date = new Date(year - 1900, month, dayOfMonth);
                            bill.setFrom_date(date);
                            fromDateEditText.setText(fmt.format(date));
                        }
                    }, year, month, day);
                    picker.show();
                }
            });

            todateEditText.setShowSoftInputOnFocus(false);
            todateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
                    Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    DatePickerDialog picker = new DatePickerDialog(BillTabFragment.this.getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //date_edit_txt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            Date date = new Date(year - 1900, month, dayOfMonth);
                            bill.setTo_date(date);
                            todateEditText.setText(fmt.format(date));
                        }
                    }, year, month, day);
                    picker.show();
                }
            });

            billDateEditText.setShowSoftInputOnFocus(false);
            billDateEditText.setText(ProjectUtils.getFormatedDate());
            bill.setGenerationDate(ProjectUtils.getFormatedDate());
            billDateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    DatePickerDialog picker = new DatePickerDialog(BillTabFragment.this.getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //date_edit_txt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            Date date = new Date(year - 1900, month, dayOfMonth);
                            bill.setGenerationDate(ProjectUtils.parseDateToString(date,"MMMM dd, yyyy"));
                            billDateEditText.setText(ProjectUtils.parseDateToString(date,"MMMM dd, yyyy"));
                        }
                    }, year, month, day);
                    picker.show();
                }
            });

            String financial_year = ProjectUtils.getFinancialYear(new Date());
            bill.setBill_year(financial_year);

            int nextBillNo = BillDbServices.getMaxBillNo(financial_year) + 1;
            billNumber.setText(String.valueOf(nextBillNo));

            bill.setClient_id(client.getId());

            TextView submit = (TextView) dialog.findViewById(R.id.bill_doigole_submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        bill.setBill_no(Integer.parseInt(billNumber.getText().toString()));
                        if(BillDbServices.isBillExist(bill.getBill_year(),bill.getBill_no()))
                            throw new Exception("Bill with number "+bill.getBill_no()+" already created");
                        if(bill.getBill_no() > nextBillNo)
                            throw new Exception("Bill number "+bill.getBill_no()+" do not match the series");
                        if(bill.getFrom_date()==null || bill.getTo_date()==null)
                            throw new Exception("From and To date cannot be empty");
                        if(bill.getFrom_date().after(bill.getTo_date()))
                            throw new Exception("From date cannot be after To date");
                        Log.i(BillTabFragment.class.getSimpleName(), bill.toString());
                        PDFGenerationTask task = new PDFGenerationTask(bill);
                        task.execute();
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            TextView cancel = (TextView) dialog.findViewById(R.id.bill_dialoge_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private class PDFGenerationTask extends AsyncTask<String, String, String> {
        private Bill bill;

        public PDFGenerationTask(Bill bill){
            this.bill=bill;
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected String doInBackground(String... strings) {
            try {
                BillGenerationServices services = new BillGenerationServices();
                services.generateBill(bill,BillTabFragment.this.getContext());
                BillDbServices.addBill(bill);
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
                billList.add(bill);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Bill generated successfully!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Some error while bill generation !!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class BillrestoreTask extends AsyncTask<String, String, String> {
        private Bill bill;
        public BillrestoreTask(Bill bill){
            this.bill=bill;
        }
        @Override
        protected void onPreExecute() { }

        @Override
        protected String doInBackground(String... strings) {
            try {
                BillGenerationServices services = new BillGenerationServices();
                services.updateBill(bill,BillTabFragment.this.getContext());
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(BillTabFragment.class.getSimpleName(), e.getMessage() + "Error while bill generation");
                return "not success";
            }
        }
        @Override
        protected void onPostExecute(String s) {}
    }
}
