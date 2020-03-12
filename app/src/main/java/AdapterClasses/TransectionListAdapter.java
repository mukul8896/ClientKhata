package AdapterClasses;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mukul.client_billing_activity.R;

import java.text.SimpleDateFormat;
import java.util.List;

import BeanClasses.Transection;

public class TransectionListAdapter extends ArrayAdapter<Transection> {
    List<Transection> transectionList;
    Context context;
    int resource;
    public TransectionListAdapter(@NonNull Context context, int resource, List<Transection> transectionList) {
        super(context, resource,transectionList);
        this.transectionList=transectionList;
        this.context=context;
        this.resource=resource;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(resource,null);
        TextView date_text=view.findViewById(R.id.transection_date);
        TextView amount_txt=view.findViewById(R.id.transection_amount);
        TextView type_txt=view.findViewById(R.id.debit_credit);
        TextView desc_txt=view.findViewById(R.id.transection_desc);

        Transection transection=transectionList.get(position);
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
        date_text.setText(fmt.format(transection.getDate()));

        amount_txt.setText(transection.getAmount()+"");

        type_txt.setText(transection.getTransecType().equals("Credit")?"Cr":"Dr");
        if(transection.getTransecType().equals("Credit"))
            type_txt.setTextColor(view.getResources().getColor(R.color.credit));
        else
            type_txt.setTextColor(view.getResources().getColor(R.color.debit));

        desc_txt.setText(transection.getDesc());

        return view;
    }
}
