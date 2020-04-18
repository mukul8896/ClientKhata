package AdapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mukul.client_billing_activity.R;

import java.util.List;

import BeanClasses.Client;

public class ClientListAdapter extends ArrayAdapter<Client> {
    List<Client> clientsList;
    Context context;
    int resource;

    public ClientListAdapter(@NonNull Context context, int resource, List<Client> clientsList) {
        super(context, resource, clientsList);
        this.clientsList = clientsList;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, null);
        TextView clientName_txt = view.findViewById(R.id.client_name);
        TextView clientBalance_txt = view.findViewById(R.id.client_balance);
        TextView balanceTag = view.findViewById(R.id.balance_tag);
        Client client = clientsList.get(position);

        clientName_txt.setText(client.getName());

        int client_balance = client.getBalance();
        if (client_balance < 0) {
            client_balance = client_balance * -1;
            clientBalance_txt.setText("(" + client_balance + " Rs)");
        } else {
            clientBalance_txt.setText(client_balance + " Rs");
        }
        if (client_balance == 0) {
            clientBalance_txt.setTextColor(view.getResources().getColor(R.color.text_black));
            balanceTag.setTextColor(view.getResources().getColor(R.color.text_black));
        }

        return view;
    }
}
