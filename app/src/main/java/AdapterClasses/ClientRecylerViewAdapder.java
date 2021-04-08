package AdapterClasses;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mukul.client_billing_activity.R;

import java.util.List;

import BeanClasses.Client;

public class ClientRecylerViewAdapder extends RecyclerView.Adapter<ClientRecylerViewAdapder.ViewHolder> {

    private Context context;
    private List<Client> clientList;

    public ClientRecylerViewAdapder(Context context, List<Client> clientList) {
        this.context = context;
        this.clientList = clientList;
    }

    // Where to get the single card as viewholder Object
    @NonNull
    @Override
    public ClientRecylerViewAdapder.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list_item, parent, false);
        return new ViewHolder(view);
    }

    // What will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ClientRecylerViewAdapder.ViewHolder holder, int position) {
        Client client = clientList.get(position);

        holder.clientName_txt.setText(client.getName());
        holder.fee.setText(client.getFee()+"");

        int client_balance = client.getBalance();
        if (client_balance < 0) {
            client_balance = client_balance * -1;
            holder.clientBalance_txt.setText("(" + client_balance + ")");
        } else {
            holder.clientBalance_txt.setText(client_balance+"");
        }
    }

    // How many items?
    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView clientName_txt;
        public TextView clientBalance_txt;
        public TextView balanceTag;
        public TextView fee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientName_txt = itemView.findViewById(R.id.client_name);
            clientBalance_txt = itemView.findViewById(R.id.client_balance);
            balanceTag = itemView.findViewById(R.id.balance_tag);
            fee = itemView.findViewById(R.id.client_fee);
        }
    }
}


