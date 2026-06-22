package com.example.bai1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WishlistActivity extends BaseActivity {

    private RecyclerView rvWishlist;
    private CarAdapter adapter;
    private DataManager dataManager;
    private LinearLayout llEmptyState;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        UiUtils.applySystemBarInsets(this);

        dataManager = new DataManager(this);
        initViews();
        loadWishlist();
    }

    private void initViews() {
        rvWishlist = findViewById(R.id.rvWishlist);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvCount = findViewById(R.id.tvCount);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnClearAll = findViewById(R.id.btnClearAll);
        com.google.android.material.button.MaterialButton btnBrowse = findViewById(R.id.btnBrowse);

        btnBack.setOnClickListener(v -> finish());

        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(this, HomeActivity.class);
                i.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }
        
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                if (dataManager.getWishlist().isEmpty()) {
                    Toast.makeText(this, "Garage đang trống.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle("Xóa tất cả")
                        .setMessage("Xóa toàn bộ xe khỏi Dream Garage?")
                        .setPositiveButton("Xóa hết", (d, w) -> {
                            dataManager.clearWishlist();
                            loadWishlist();
                            Toast.makeText(this, "Đã xóa Dream Garage.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    private void loadWishlist() {
        List<CarModel> wishlist = dataManager.getWishlist();
        if (wishlist.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            tvCount.setText(getString(R.string.garage_count, 0));
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            tvCount.setText(getString(R.string.garage_count, wishlist.size()));
            
            adapter = new CarAdapter(this, wishlist, false);
            rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
            rvWishlist.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist(); // Cập nhật lại nếu có thay đổi từ trang chi tiết
    }
}