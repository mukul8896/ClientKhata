package adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mukul.companyAccounts.R;

import java.util.List;

import modals.Bill;
import utils.ProjectUtils;

public class BillListAdapter extends RecyclerView.Adapter<BillListAdapter.ViewHolder> {

    private Context context;
    private List<Bill> billList;
    private BillListAdapter.ItemEventListner eventListner;

    public BillListAdapter(Context context, List<Bill> billlist, BillListAdapter.ItemEventListner eventListner){
        this.context = context;
        this.billList = billlist;
        this.eventListner = eventListner;
    }

    @Override
    public BillListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_list_item, parent, false);
        return new BillListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BillListAdapter.ViewHolder holder, int position) {
        Bill bill = billList.get(position);

        holder.bill_no.setText(bill.getBill_year() + " | Bill No- " + bill.getBill_no());

        holder.bill_from_date.setText(ProjectUtils.parseDateToString(bill.getFrom_date(),"MMMM dd, yyyy"));

        holder.bill_to_date.setText(ProjectUtils.parseDateToString(bill.getTo_date(),"MMMM dd, yyyy"));
    }


    @Override
    public int getItemCount() {
        return billList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        public TextView bill_no;
        public TextView bill_from_date;
        public TextView bill_to_date;
        public ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            bill_no = itemView.findViewById(R.id.bill_no);
            bill_from_date = itemView.findViewById(R.id.from_date_text);
            bill_to_date = itemView.findViewById(R.id.to_date_text);
            img = itemView.findViewById(R.id.is_bill_shared);
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
