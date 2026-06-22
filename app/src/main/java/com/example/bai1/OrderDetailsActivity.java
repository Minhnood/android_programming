package com.example.bai1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

public class OrderDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        UiUtils.applySystemBarInsets(this);

        // Nhận dữ liệu Order từ Intent (thông qua JSON String để đơn giản)
        String orderJson = getIntent().getStringExtra("ORDER_DATA");
        OrderModel order = new Gson().fromJson(orderJson, OrderModel.class);

        if (order == null) {
            finish();
            return;
        }

        // Ánh xạ View
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvOrderTitle = findViewById(R.id.tvOrderTitle);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvTotal = findViewById(R.id.tvDetailTotal);
        RecyclerView rvItems = findViewById(R.id.rvOrderItems);
        MaterialButton btnSupport = findViewById(R.id.btnSupport);

        // Gán dữ liệu
        tvOrderTitle.setText(getString(R.string.order_number_prefix) + order.getOrderId());
        tvStatus.setText(order.getStatus().toUpperCase());
        String dateLine = "Đặt lúc: " + order.getDate();
        if (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty()) {
            dateLine += "\nThanh toán: " + order.getPaymentMethod();
        }
        tvDate.setText(dateLine);
        tvTotal.setText(order.getTotalAmount());

        btnBack.setOnClickListener(v -> finish());

        renderTracking(order.getStatus());

        // Lấy trạng thái mới nhất từ server (admin có thể đã đổi bước)
        final String oid = order.getOrderId();
        new Thread(() -> {
            String json = JsonUtils.httpGet("/api/orders/" + oid);
            if (json == null) return;
            try {
                OrderModel fresh = new Gson().fromJson(json, OrderModel.class);
                if (fresh != null && fresh.getStatus() != null) {
                    runOnUiThread(() -> {
                        tvStatus.setText(fresh.getStatus().toUpperCase());
                        renderTracking(fresh.getStatus());
                    });
                }
            } catch (Exception ignored) {}
        }).start();

        // Setup list items trong đơn hàng (Dùng chung CartAdapter nhưng không có nút xóa)
        CartAdapter itemsAdapter = new CartAdapter(this, order.getItems(), null);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemsAdapter);

        btnSupport.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@jdmgarage.com"));
            email.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ đơn hàng #" + order.getOrderId());
            email.putExtra(Intent.EXTRA_TEXT,
                    "Xin chào JDM Garage,\n\nTôi cần hỗ trợ về đơn hàng #" + order.getOrderId()
                            + " (" + order.getTotalAmount() + ").\n\n");
            try {
                startActivity(Intent.createChooser(email, "Liên hệ hỗ trợ"));
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final String[] STAGES = {"Đang xử lý", "Đang giao", "Đã tới", "Hoàn thành"};

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    /** Vẽ timeline 4 bước theo trạng thái đơn. */
    private void renderTracking(String status) {
        LinearLayout container = findViewById(R.id.llTracking);
        if (container == null) return;
        container.removeAllViews();

        int current = 0;
        for (int i = 0; i < STAGES.length; i++) if (STAGES[i].equals(status)) current = i;

        int red = Color.parseColor("#F5333F");
        int grey = Color.parseColor("#3A3A42");
        int muted = Color.parseColor("#A0A4AD");

        for (int i = 0; i < STAGES.length; i++) {
            boolean done = i < current, active = i == current, reached = i <= current;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            // Cột trái: vòng tròn + đường nối
            LinearLayout left = new LinearLayout(this);
            left.setOrientation(LinearLayout.VERTICAL);
            left.setGravity(Gravity.CENTER_HORIZONTAL);
            left.setLayoutParams(new LinearLayout.LayoutParams(dp(34), LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView dot = new TextView(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams(dp(28), dp(28)));
            dot.setGravity(Gravity.CENTER);
            dot.setTextColor(Color.WHITE);
            dot.setTextSize(13);
            dot.setText(done ? "✓" : (active ? "●" : ""));
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(reached ? red : grey);
            dot.setBackground(circle);
            left.addView(dot);

            if (i < STAGES.length - 1) {
                android.view.View ln = new android.view.View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(2), dp(30));
                ln.setLayoutParams(lp);
                ln.setBackgroundColor(done ? red : grey);
                left.addView(ln);
            }
            row.addView(left);

            // Cột phải: tên bước + mô tả
            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            rlp.leftMargin = dp(12); rlp.topMargin = dp(3);
            right.setLayoutParams(rlp);

            TextView title = new TextView(this);
            title.setText(STAGES[i]);
            title.setTextSize(15);
            title.setTextColor(reached ? Color.WHITE : muted);
            if (reached) title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            right.addView(title);

            TextView sub = new TextView(this);
            sub.setText(done ? "Đã hoàn tất" : (active ? "Đang ở bước này" : "Đang chờ"));
            sub.setTextSize(12);
            sub.setTextColor(active ? red : muted);
            right.addView(sub);

            row.addView(right);
            container.addView(row);
        }
    }
}