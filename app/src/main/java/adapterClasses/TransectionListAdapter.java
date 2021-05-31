package adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mukul.companyAccounts.R;

import java.util.List;

import modals.Transection;
import utils.ProjectUtils;

public class TransectionListAdapter extends RecyclerView.Adapter<TransectionListAdapter.ViewHolder> {

    private Context context;
    List<Transection> transectionList;
    private TransectionListAdapter.ItemEventListner eventListner;

    public TransectionListAdapter(Context context, List<Transection>  transectionList, TransectionListAdapter.ItemEventListner eventListner) {
        this.context = context;
        this.transectionList = transectionList;
        this.eventListner = eventListner;
    }

    // Where to get the single card as viewholder Object
    @Override
    public TransectionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transection_list_item, parent, false);
        return new TransectionListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransectionListAdapter.ViewHolder holder, int position) {
        Transection transection = transectionList.get(position);

        holder.date_text.setText(ProjectUtils.parseDateToString(transection.getDate(),"MMMM dd, yyyy"));

        holder.amount_txt.setText(transection.getAmount() + "");

        holder.type_txt.setText(transection.getTransecType().equals("Credit") ? "Cr" : "Dr");
        if (transection.getTransecType().equals("Credit"))
            holder.type_txt.setTextColor(ContextCompat.getColor(context, R.color.credit));
        else
            holder.type_txt.setTextColor(ContextCompat.getColor(context, R.color.debit));

        holder.desc_txt.setText(transection.getDesc());

        if (transection.getBill_details() != null && !transection.getBill_details().isEmpty()) {
            holder.img.setBackgroundResource(R.drawable.transection_billed_icon);
        }
    }

    // How many items?
    @Override
    public int getItemCount() {
        return transectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        private TextView date_text;
        private TextView amount_txt;
        private TextView type_txt;
        private TextView desc_txt;
        private ImageView img;

        public ViewHolder( View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            date_text = itemView.findViewById(R.id.transection_date);
            amount_txt = itemView.findViewById(R.id.transection_amount);
            type_txt = itemView.findViewById(R.id.debit_credit);
            desc_txt = itemView.findViewById(R.id.transection_desc);
            img = (ImageView) itemView.findViewById(R.id.is_transection_billed);
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


