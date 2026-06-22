package com.example.bai1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationsActivity extends BaseActivity {

    private final List<NotificationModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        UiUtils.applySystemBarInsets(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        RecyclerView rvNotif = findViewById(R.id.rvNotifications);
        View llEmpty = findViewById(R.id.llEmptyNotif);

        btnBack.setOnClickListener(v -> finish());

        list.clear();
        list.addAll(NotificationHelper.getAll(this));

        boolean empty = list.isEmpty();
        if (llEmpty != null) llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvNotif.setVisibility(empty ? View.GONE : View.VISIBLE);

        rvNotif.setLayoutManager(new LinearLayoutManager(this));
        rvNotif.setAdapter(new NotifAdapter());
    }

    private class NotifAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            NotificationModel m = list.get(position);
            ((TextView) holder.itemView.findViewById(R.id.tvNotifTitle)).setText(m.getTitle());
            ((TextView) holder.itemView.findViewById(R.id.tvNotifMsg)).setText(m.getMessage());
            ((TextView) holder.itemView.findViewById(R.id.tvNotifTime)).setText(m.getTime());
            View dot = holder.itemView.findViewById(R.id.vNotifDot);
            dot.setVisibility(m.isRead() ? View.INVISIBLE : View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                if (!m.isRead()) {
                    m.setRead(true);
                    NotificationHelper.markRead(NotificationsActivity.this, m.getId());
                    dot.setVisibility(View.INVISIBLE);
                }
                // Thông báo đơn hàng -> mở lịch sử đơn
                if (m.getId().startsWith("order_")) {
                    startActivity(new Intent(NotificationsActivity.this, OrderHistoryActivity.class));
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }
    }
}
