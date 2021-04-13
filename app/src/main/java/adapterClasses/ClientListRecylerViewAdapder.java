package adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mukul.companyAccounts.R;

import java.util.List;

import modals.Client;

public class ClientListRecylerViewAdapder extends RecyclerView.Adapter<ClientListRecylerViewAdapder.ViewHolder> {

    private Context context;
    private List<Client> clientList;
    private ItemEventListner eventListner;

    public ClientListRecylerViewAdapder(Context context, List<Client> clientList, ItemEventListner eventListner) {
        this.context = context;
        this.clientList = clientList;
        this.eventListner = eventListner;
    }

    // Where to get the single card as viewholder Object
    @Override
    public ClientListRecylerViewAdapder.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list_item, parent, false);
        return new ViewHolder(view);
    }

    // What will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(ClientListRecylerViewAdapder.ViewHolder holder, int position) {
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


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        public TextView clientName_txt;
        public TextView clientBalance_txt;
        public TextView balanceTag;
        public TextView fee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            clientName_txt = itemView.findViewById(R.id.client_name);
            clientBalance_txt = itemView.findViewById(R.id.client_balance);
            balanceTag = itemView.findViewById(R.id.balance_tag);
            fee = itemView.findViewById(R.id.client_fee);
        }

        @Override
        public void onClick(View v) {
            eventListner.onClick(v,getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return eventListner.onLongClick(v,getAdapterPosition());
        }
    }

    public interface ItemEventListner{
        void onClick(View view, int position);
        boolean onLongClick(View view, int position);
    }
}


