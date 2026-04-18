package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. TẠO HÀM CHUYỂN TRANG CHUNG
        View.OnClickListener onProductClick = v -> {
            Intent intent = new Intent(HomeActivity.this, DetailProductActivity.class);
            startActivity(intent);
        };

        // 2. GẮN SỰ KIỆN CHO TẤT CẢ 12 SẢN PHẨM TRÊN MÀN HÌNH

        // Nhóm Sản phẩm Hot
        findViewById(R.id.imgHot1).setOnClickListener(onProductClick);
        findViewById(R.id.imgHot2).setOnClickListener(onProductClick);
        findViewById(R.id.imgHot3).setOnClickListener(onProductClick);
        findViewById(R.id.imgHot4).setOnClickListener(onProductClick);

        // Nhóm Khuyến mãi
        findViewById(R.id.imgPromo1).setOnClickListener(onProductClick);
        findViewById(R.id.imgPromo2).setOnClickListener(onProductClick);
        findViewById(R.id.imgPromo3).setOnClickListener(onProductClick);
        findViewById(R.id.imgPromo4).setOnClickListener(onProductClick);

        // Nhóm Danh sách lưới
        findViewById(R.id.imgGrid1).setOnClickListener(onProductClick);
        findViewById(R.id.imgGrid2).setOnClickListener(onProductClick);
        findViewById(R.id.imgGrid3).setOnClickListener(onProductClick);
        findViewById(R.id.imgGrid4).setOnClickListener(onProductClick);
    }
}