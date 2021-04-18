package com.mukul.companyAccounts;

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
    private String financialYear;

    private List<Transection> transectionList;
    private List<Bill> billList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);

        client_id = getIntent().getIntExtra("id", 0);
        client = ClientDbServices.getClient(client_id);
        financialYear = ProjectUtils.getFinancialYear(Calendar.getInstance().getTime());

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
        transectionList = TransectionDbServices.getClientsTransections(client_id,financialYear);
        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),transectionList,billList,client);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
                ClientActivity.this.getSupportActionBar().setSubtitle(ProjectUtils.getFinancialYear(Calendar.getInstance().getTime()));
                if (tab.getPosition() == 0) {
                    pageAdapter.notifyDataSetChanged();
                } else if (tab.getPosition() == 1) {
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
}
