package adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mukul.companyAccounts.R;

import java.text.SimpleDateFormat;
import java.util.List;

import modals.Bill;

public class BillListAdapder extends ArrayAdapter<Bill> {
    List<Bill> bill_list;
    Context context;
    int resource;

    public BillListAdapder(@NonNull Context context, int resource, List<Bill> bill_list) {
        super(context, resource, bill_list);
        this.bill_list = bill_list;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, null);
        TextView bill_no = (TextView) view.findViewById(R.id.bill_no);
        TextView bill_from_date = (TextView) view.findViewById(R.id.from_date_text);
        TextView bill_to_date = (TextView) view.findViewById(R.id.to_date_text);
        ImageView img = (ImageView) view.findViewById(R.id.is_bill_shared);

        Bill bill = bill_list.get(position);

        bill_no.setText(bill.getBill_year() + " | Bill No- " + bill.getBill_no());

        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");

        bill_from_date.setText(fmt.format(bill.getFrom_date()));
        bill_to_date.setText(fmt.format(bill.getTo_date()));
        return view;
    }
}
