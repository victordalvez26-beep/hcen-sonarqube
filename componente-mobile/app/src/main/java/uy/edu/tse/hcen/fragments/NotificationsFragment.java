package uy.edu.tse.hcen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.adapters.NotificationsAdapter;
import uy.edu.tse.hcen.notifications.NotificationItem;
import uy.edu.tse.hcen.notifications.NotificationStorage;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new NotificationsAdapter(notifications, item -> {
                NotificationStorage.deleteNotification(requireContext(), item.getTimestamp());
                updateNotifications();
            });
            recyclerView.setAdapter(adapter);

        // Botón para borrar todas las notificaciones
        View btnDeleteAll = view.findViewById(R.id.btnDeleteAllNotifications);
        btnDeleteAll.setOnClickListener(v -> {
            NotificationStorage.clear(requireContext());
            updateNotifications();
        });

        updateNotifications();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNotifications();
    }

    private void updateNotifications() {
        List<NotificationItem> notifs = NotificationStorage.getNotifications(requireContext());

        // Ordenar por timestamp descendente
        Collections.sort(notifs, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

        notifications.clear();
        notifications.addAll(notifs);
        adapter.notifyDataSetChanged();

        // Mostrar u ocultar vista vacía
        if (notifications.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
