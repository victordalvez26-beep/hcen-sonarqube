package uy.edu.tse.hcen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.summary.SummaryItem;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
    private final List<SummaryItem> items;
    private final OnSummaryTypeClickListener listener;

    public interface OnSummaryTypeClickListener {
        void onTypeClick(SummaryItem item);
    }

    public SummaryAdapter(List<SummaryItem> items, OnSummaryTypeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summary_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SummaryItem item = items.get(position);
        holder.txtTypeTitle.setText(item.title);
        holder.imgTypeIcon.setImageResource(item.iconResId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTypeClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTypeTitle;
        ImageView imgTypeIcon;

        ViewHolder(View itemView) {
            super(itemView);
            txtTypeTitle = itemView.findViewById(R.id.txtTypeTitle);
            imgTypeIcon = itemView.findViewById(R.id.imgTypeIcon);
        }
    }
}