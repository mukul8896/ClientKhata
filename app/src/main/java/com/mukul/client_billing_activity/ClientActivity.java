package com.mukul.client_billing_activity;

import android.support.annotation.NonNull;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import AdapterClasses.PageAdapter;
import BeanClasses.Client;
import db_services.DBServices;

public class ClientActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabItem transectionTab,generatedBillTab;
    private PageAdapter pageAdapter;
    private Integer client_id;
    private ActionBar toolbar;
    private TextView contact_txt;
    private TextView address_txt;
    private TextView name_txt;
    private Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);
        client_id=getIntent().getIntExtra("id",0);
        client= DBServices.getClient(client_id);
        toolbar = getSupportActionBar();
        toolbar.setTitle(getIntent().getStringExtra("Client Name"));
        toolbar.setDisplayHomeAsUpEnabled(false);

        name_txt=(TextView)findViewById(R.id.client_name_data);
        contact_txt=(TextView)findViewById(R.id.client_contact_data);
        address_txt=(TextView)findViewById(R.id.client_address_data);
        contact_txt.setText(client.getContact());
        address_txt.setText(client.getAddress());
        name_txt.setText(client.getName());

        tabLayout=(TabLayout)findViewById(R.id.tablayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        transectionTab=(TabItem)findViewById(R.id.transection_tab);
        generatedBillTab=(TabItem)findViewById(R.id.generated_bill_tab);

        pageAdapter=new PageAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),client_id);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),true);
                if(tab.getPosition()==0){
                    pageAdapter.notifyDataSetChanged();
                }
                else if (tab.getPosition()==1) {
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
