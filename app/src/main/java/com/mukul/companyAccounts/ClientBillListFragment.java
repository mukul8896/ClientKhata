package com.mukul.companyAccounts;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

import adapterClasses.BillListRecyclerViewAdapter;
import modals.Bill;
import modals.Client;
import services.BillGenerationServices;
import dbServices.BillDbServices;
import utils.BillUtils;
import utils.ProjectUtils;

public class ClientBillListFragment extends Fragment implements BillListRecyclerViewAdapter.ItemEventListner {
    private BillListRecyclerViewAdapter adapter;
    private int index;
    private Dialog dialog;
    private Client client;
    private List<Bill> billList;
    private static ClientBillListFragment clientBillListFragment;

    public static ClientBillListFragment newInstance(List<Bill> billList,Client client) {
        return new ClientBillListFragment(billList,client);
    }

    public ClientBillListFragment(List<Bill> billList,Client client) {
        this.billList=billList;
        this.client=client;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_generated_bill, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.bill_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BillListRecyclerViewAdapter(this.getContext(),  billList, ClientBillListFragment.this);
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);

        Button generate_bill_btn = (Button) rootView.findViewById(R.id.generate_bill_btn);
        generate_bill_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.download_bill_dialoge);
                TextView submit = (TextView) dialog.findViewById(R.id.bill_doigole_submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            PDFGenerationTask task = new PDFGenerationTask();
                            task.execute();
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        });
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
            intent.putExtra("bill_id", billList.get(index).getBillId());
            startActivity(intent);
            Log.i(ClientBillListFragment.class.getSimpleName(), "Inside generated bill fragment");
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.bill_list_context_menu, menu);
    }

    @Override
    public void onClick(View view, int position) {
        BillUtils utils = new BillUtils(billList.get(position));
        utils.openPdfFile(getActivity());
    }

    @Override
    public boolean onLongClick(View view, int position) {
        index = position;
        return false;
    }

    private class PDFGenerationTask extends AsyncTask<String, String, String> {
        private Bill bill;

        @Override
        protected void onPreExecute() {
            bill = new Bill();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                final DatePicker from_date_picer = (DatePicker) dialog.findViewById(R.id.from_date);
                final DatePicker to_date_picer = (DatePicker) dialog.findViewById(R.id.to_date);

                Date from_date = new Date(from_date_picer.getYear() - 1900, from_date_picer.getMonth(), from_date_picer.getDayOfMonth());
                bill.setFrom_date(from_date);
                Date to_date = new Date(to_date_picer.getYear() - 1900, to_date_picer.getMonth(), to_date_picer.getDayOfMonth());
                bill.setTo_date(to_date);

                bill.setClient_id(client.getId());

                String financial_year = ProjectUtils.getFinancialYear(to_date);
                bill.setBill_year(financial_year);
                bill.setBill_no(BillDbServices.getMaxBillNo(financial_year) + 1);
                bill.setGenerationDate(ProjectUtils.getFormatedDate());

                BillGenerationServices services = new BillGenerationServices();
                services.generateBill(bill);
                BillDbServices.addBill(bill);
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(ClientBillListFragment.class.getSimpleName(), e.getMessage() + "Error while bill generation");
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
}
