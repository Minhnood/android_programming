package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private RecyclerView rvHotCars, rvAllCars;
    private CarAdapter hotAdapter, allAdapter;
    private List<CarModel> carList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Edge-to-edge: chừa khoảng status bar (header) và nav bar (thanh dưới)
        View header = findViewById(R.id.llHeader);
        View bottomNav = findViewById(R.id.bottomNav);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), bars.top + header.getPaddingBottom(),
                        header.getPaddingRight(), header.getPaddingBottom());
            }
            if (bottomNav != null) {
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(), bars.bottom + bottomNav.getPaddingTop());
            }
            return insets;
        });

        initViews();
        loadCarData();
        setupRecyclerViews();
        setupClickListeners();
    }

    private void initViews() {
        rvHotCars = findViewById(R.id.rvHotCars);
        rvAllCars = findViewById(R.id.rvAllCars);
    }

    private void loadCarData() {
        carList.clear();
        carList.addAll(JsonUtils.getCars(this));
    }

    private void setupRecyclerViews() {
        // Hot Releases (Horizontal)
        hotAdapter = new CarAdapter(this, carList.subList(0, Math.min(carList.size(), 3)), true);
        rvHotCars.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHotCars.setAdapter(hotAdapter);

        // Explore Garage (Grid)
        allAdapter = new CarAdapter(this, carList, false);
        rvAllCars.setLayoutManager(new GridLayoutManager(this, 2));
        rvAllCars.setAdapter(allAdapter);
        rvAllCars.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        // News Card
        CardView cardNews = findViewById(R.id.cardNews);
        cardNews.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NewsActivity.class)));

        // Search & Profile
        findViewById(R.id.btnSearch).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        findViewById(R.id.btnNotifications).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationsActivity.class)));

        // Khu vực: Nội địa / Nước ngoài
        findViewById(R.id.cardDomestic).setOnClickListener(v -> openRegion("Nội địa"));
        findViewById(R.id.cardForeign).setOnClickListener(v -> openRegion("Nước ngoài"));

        // Brands
        View.OnClickListener onBrandClick = v -> {
            TextView tv = (TextView) v;
            Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
            intent.putExtra("CATEGORY_NAME", tv.getText().toString());
            startActivity(intent);
        };
        findViewById(R.id.brandNissan).setOnClickListener(onBrandClick);
        findViewById(R.id.brandToyota).setOnClickListener(onBrandClick);
        findViewById(R.id.brandMazda).setOnClickListener(onBrandClick);
        findViewById(R.id.brandHonda).setOnClickListener(onBrandClick);
        findViewById(R.id.brandMitsubishi).setOnClickListener(onBrandClick);
        findViewById(R.id.brandSubaru).setOnClickListener(onBrandClick);

        // Bottom Nav
        findViewById(R.id.navWishlist).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, WishlistActivity.class)));
        findViewById(R.id.navCart).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));
        
        TextView btnSeeAll = findViewById(R.id.btnSeeAll);
        btnSeeAll.setOnClickListener(v -> {
             Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
             intent.putExtra("CATEGORY_NAME", "All JDM");
             startActivity(intent);
        });
    }

    private void openRegion(String region) {
        Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
        intent.putExtra("REGION", region);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHeaderAvatar();
        updateNotifBadge();
        refreshCarsFromApi();
    }

    /** Làm mới danh sách xe từ server; nếu có dữ liệu mới thì dựng lại RecyclerView. */
    private void refreshCarsFromApi() {
        new Thread(() -> {
            boolean ok = JsonUtils.refreshFromApi();
            if (ok) {
                runOnUiThread(() -> {
                    loadCarData();
                    setupRecyclerViews();
                });
            }
        }).start();
    }

    private void updateNotifBadge() {
        TextView badge = findViewById(R.id.tvNotifBadge);
        if (badge == null) return;
        int unread = NotificationHelper.unreadCount(this);
        if (unread > 0) {
            badge.setText(unread > 9 ? "9+" : String.valueOf(unread));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void loadHeaderAvatar() {
        ImageView btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile == null) return;
        String path = new AccountManager(this).getAvatarPath();
        if (path != null && new File(path).exists()) {
            Glide.with(this)
                    .load(new File(path))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(btnProfile);
        } else {
            btnProfile.setImageResource(R.drawable.ic_car_placeholder);
        }
    }
}