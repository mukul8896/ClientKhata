package com.mukul.companyAccounts;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.mukul.companyAccounts.ui.SummeryFragment;

import java.util.Calendar;
import java.util.List;

import adapterClasses.PageAdapter;
import adapterClasses.SummeryListAdapter;
import adapterClasses.TransectionListAdapter;
import dao.DbHandler;
import dbServices.BillDbServices;
import dbServices.TransectionDbServices;
import modals.Bill;
import modals.Client;
import dbServices.ClientDbServices;
import modals.Transection;
import utils.ProjectUtils;

public class ClientActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private Integer client_id;
    private Client client;
    private String financialYear = "All";
    private int currentPage = 0;
    private List<Transection> transectionList;
    private List<Bill> billList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);

        client_id = getIntent().getIntExtra("id", 0);
        client = ClientDbServices.getClient(client_id);

        Toolbar toolbar = (Toolbar)findViewById(R.id.clientToolbar);
        toolbar.setTitle(client.getName());
        toolbar.setSubtitle(financialYear);
        setSupportActionBar(toolbar);

        TextView fee_txt = (TextView) findViewById(R.id.client_fee_data);
        TextView address_txt = (TextView) findViewById(R.id.client_address_data);
        address_txt.setText(client.getAddress());
        fee_txt.setText(client.getFee()+"");

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabItem transectionTab = (TabItem) findViewById(R.id.transection_tab);
        TabItem generatedBillTab = (TabItem) findViewById(R.id.generated_bill_tab);

        billList = BillDbServices.getBillList(client_id,financialYear);
        billList.forEach(bill-> System.out.println(bill.toString()));
        transectionList = TransectionDbServices.getClientsTransections(client_id,financialYear);
        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),transectionList,billList,client);
        viewPager.setAdapter(pageAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 0) {
                    currentPage=tab.getPosition();
                    pageAdapter.notifyDataSetChanged();
                } else if (tab.getPosition() == 1) {
                    currentPage=tab.getPosition();
                    pageAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_options_menu, menu);

        MenuItem item_year2 = menu.findItem(R.id.client_year1);
        item_year2.setTitle(ProjectUtils.getFinancialYear(Calendar.getInstance().getTime()));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) -1);
        MenuItem item_year3 = menu.findItem(R.id.client_year2);
        item_year3.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));

        MenuItem item_year1 = menu.findItem(R.id.client_year3);
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 2);
        item_year1.setTitle(ProjectUtils.getFinancialYear(cal.getTime()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id!=R.id.client_addtransection_action){
            String financialYear = item.getTitle().toString();
            billList = BillDbServices.getBillList(client_id,financialYear);
            transectionList = TransectionDbServices.getClientsTransections(client_id,financialYear);
            pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),transectionList,billList,client);
            viewPager.setAdapter(pageAdapter);
            viewPager.setCurrentItem(currentPage);
            getSupportActionBar().setSubtitle(financialYear);
        }
        return super.onOptionsItemSelected(item);
    }
}
