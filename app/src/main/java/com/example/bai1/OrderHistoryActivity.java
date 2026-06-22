package com.example.bai1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderHistoryActivity extends BaseActivity {

    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;
    private DataManager dataManager;
    private LinearLayout llEmptyOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        UiUtils.applySystemBarInsets(this);

        dataManager = new DataManager(this);
        initViews();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        llEmptyOrders = findViewById(R.id.llEmptyOrders);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private List<OrderModel> orders;

    private void loadOrders() {
        orders = dataManager.getOrders();
        if (orders.isEmpty()) {
            llEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            return;
        }
        llEmptyOrders.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);

        adapter = new OrderHistoryAdapter(this, orders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        // Đồng bộ trạng thái mới nhất từ server (admin có thể đã đổi bước)
        syncStatusesFromServer();
    }

    private void syncStatusesFromServer() {
        final List<OrderModel> snapshot = orders;
        new Thread(() -> {
            boolean changed = false;
            for (OrderModel o : snapshot) {
                if (o == null || o.getOrderId() == null) continue;
                String json = JsonUtils.httpGet("/api/orders/" + o.getOrderId());
                if (json == null) continue;
                try {
                    OrderModel fresh = new com.google.gson.Gson().fromJson(json, OrderModel.class);
                    if (fresh != null && fresh.getStatus() != null
                            && !fresh.getStatus().equals(o.getStatus())) {
                        o.setStatus(fresh.getStatus());
                        changed = true;
                    }
                } catch (Exception ignored) {}
            }
            if (changed) {
                dataManager.updateOrders(snapshot);
                runOnUiThread(() -> {
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }
}