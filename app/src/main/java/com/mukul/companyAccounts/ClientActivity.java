package com.mukul.companyAccounts;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import adapterClasses.PageAdapter;
import dao.DbHandler;
import modals.Client;
import dbServices.ClientDbServices;

public class ClientActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private Integer client_id;
    private ActionBar toolbar;
    private TextView address_txt;
    private TextView fee_txt;
    private Client client;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);

        client_id = getIntent().getIntExtra("id", 0);
        password = getIntent().getStringExtra("app_password");
        client = ClientDbServices.getClient(client_id);

        toolbar = getSupportActionBar();
        toolbar.setTitle(client.getName());
        toolbar.setDisplayHomeAsUpEnabled(false);
        Log.i(ClientActivity.class.getSimpleName(),client.getFee()+"");
        fee_txt = (TextView) findViewById(R.id.client_fee_data);
        address_txt = (TextView) findViewById(R.id.client_address_data);
        address_txt.setText(client.getAddress());
        fee_txt.setText(client.getFee()+"");

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabItem transectionTab = (TabItem) findViewById(R.id.transection_tab);
        TabItem generatedBillTab = (TabItem) findViewById(R.id.generated_bill_tab);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), client_id);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
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
