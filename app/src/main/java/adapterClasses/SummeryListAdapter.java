package adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mukul.companyAccounts.R;

import java.util.List;

import modals.Client;
import modals.Transection;

public class SummeryListAdapter extends ArrayAdapter<Client> {
    List<Client> clientsList;
    List<Transection> transectionList;
    Context context;
    int resource;

    public SummeryListAdapter(@NonNull Context context, int resource, List<Client> clientsList, List<Transection> transections) {
        super(context, resource, clientsList);
        this.clientsList = clientsList;
        this.transectionList=transections;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, null);
        Client client = clientsList.get(position);

        TextView clientName_txt = view.findViewById(R.id.client_name_summery_list);
        clientName_txt.setText(client.getName());

        TextView debit=view.findViewById(R.id.client_debit);
        String total_debit=getSumOf("Debit",clientsList.get(position).getId());
        debit.setText(total_debit);

        TextView credit=view.findViewById(R.id.client_credit);
        String total_credit=getSumOf("Credit",clientsList.get(position).getId());
        credit.setText(total_credit);

        TextView due=view.findViewById(R.id.client_due);
        due.setText((Integer.parseInt(total_debit)-Integer.parseInt(total_credit))+"");

        return view;
    }

    private String getSumOf(String value,Integer id){
        int total=0;
        for(Transection transection:transectionList){
            if(transection.getTransecType().equals(value) && transection.getClientId().equals(id))
                total+=transection.getAmount();
        }
        return total+"";
    }
}
