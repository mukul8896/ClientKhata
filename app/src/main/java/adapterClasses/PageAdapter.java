package adapterClasses;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mukul.companyAccounts.BillTabFragment;
import com.mukul.companyAccounts.TransectionTabFragment;

import java.util.List;

import modals.Bill;
import modals.Client;
import modals.Transection;

public class PageAdapter extends FragmentPagerAdapter {
    private int tabCount;
    private Client client;
    private String financialyear;

    private List<Transection> transectionList;
    private List<Bill> billList;

    public PageAdapter(FragmentManager fm, int tabCount, List<Transection> transectionList, List<Bill> billList, Client client) {
        super(fm);
        this.tabCount = tabCount;
        this.transectionList = transectionList;
        this.billList = billList;
        this.client = client;
    }

    @Override
    public Fragment getItem(int tabNum) {
        Fragment fragment = null;
        switch (tabNum) {
            case 0:
                return TransectionTabFragment.newInstance(transectionList,client);
            case 1:
                return BillTabFragment.newInstance(billList,client);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
