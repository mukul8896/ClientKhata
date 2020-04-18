package AdapterClasses;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mukul.client_billing_activity.GeneratedBillFragment;
import com.mukul.client_billing_activity.TransectionFragment;

public class PageAdapter extends FragmentPagerAdapter {
    private int tabCount;
    private Integer client_id;

    public PageAdapter(FragmentManager fm, int tabCount, Integer client_id) {
        super(fm);
        this.tabCount = tabCount;
        this.client_id = client_id;
    }

    @Override
    public Fragment getItem(int tabNum) {
        Fragment fragment = null;
        switch (tabNum) {
            case 0:
                return TransectionFragment.newInstance(client_id);
            case 1:
                return GeneratedBillFragment.newInstance(client_id);
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
