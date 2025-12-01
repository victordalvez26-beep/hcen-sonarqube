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
import uy.edu.tse.hcen.notifications.NotificationItem;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final List<NotificationItem> notifications;
    private final OnDeleteNotificationListener deleteListener;

    public interface OnDeleteNotificationListener {
        void onDelete(NotificationItem item);
    }

    public NotificationsAdapter(List<NotificationItem> notifications, OnDeleteNotificationListener deleteListener) {
        this.notifications = notifications;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        holder.txtMessage.setText(item.getMessage());
        holder.txtDate.setText(item.getFormattedDate());
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(item);
                }
            });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            ImageView btnDelete;
            TextView txtMessage;
            TextView txtDate;

            ViewHolder(View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.imgIcon);
                txtMessage = itemView.findViewById(R.id.txtMessage);
                txtDate = itemView.findViewById(R.id.txtDate);
                btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
            }
    }
}
