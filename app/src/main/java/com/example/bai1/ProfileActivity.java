package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class ProfileActivity extends BaseActivity {
    private AccountManager accountManager;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UiUtils.applySystemBarInsets(this);

        accountManager = new AccountManager(this);
        dataManager = new DataManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout menuEdit = findViewById(R.id.menuEditProfile);
        LinearLayout menuNotif = findViewById(R.id.menuNotifications);
        LinearLayout menuOrders = findViewById(R.id.menuOrders);
        LinearLayout menuSettings = findViewById(R.id.menuSettings);
        LinearLayout menuWishlist = findViewById(R.id.menuWishlist);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);
        TextView tvCarsInCart = findViewById(R.id.tvCarsInCart);
        TextView tvCarsInWishlist = findViewById(R.id.tvCarsInWishlist);

        // Hiển thị thông tin
        if (tvProfileName != null) tvProfileName.setText(accountManager.getUserName());
        if (tvProfileEmail != null) tvProfileEmail.setText(accountManager.getUserEmail());
        bindExtraInfo();
        loadAvatar();
        updateCounts(tvCarsInCart, tvCarsInWishlist);

        btnBack.setOnClickListener(v -> finish());

        if (menuEdit != null) menuEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        if (menuNotif != null) menuNotif.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        if (menuOrders != null) menuOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderHistoryActivity.class)));
        if (menuSettings != null) menuSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        if (menuWishlist != null) menuWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)));

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn thoát khỏi ứng dụng?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    accountManager.logout();
                    JsonUtils.setAuthToken(null);
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void bindExtraInfo() {
        TextView tvPhone = findViewById(R.id.tvProfilePhone);
        TextView tvBio = findViewById(R.id.tvProfileBio);
        String phone = accountManager.getPhone();
        String bio = accountManager.getBio();
        if (tvPhone != null) {
            if (phone != null && !phone.isEmpty()) {
                tvPhone.setText("📞 " + phone);
                tvPhone.setVisibility(View.VISIBLE);
            } else {
                tvPhone.setVisibility(View.GONE);
            }
        }
        if (tvBio != null) {
            if (bio != null && !bio.isEmpty()) {
                tvBio.setText("“" + bio + "”");
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }
        }
    }

    private void loadAvatar() {
        ImageView img = findViewById(R.id.imgProfileAvatar);
        if (img == null) return;
        String path = accountManager.getAvatarPath();
        if (path != null && new File(path).exists()) {
            Glide.with(this)
                    .load(new File(path))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(img);
        } else {
            img.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    private void updateCounts(TextView cart, TextView wishlist) {
        if (cart != null) cart.setText(String.valueOf(dataManager.getCart().size()));
        if (wishlist != null) wishlist.setText(String.valueOf(dataManager.getWishlist().size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvCarsInCart = findViewById(R.id.tvCarsInCart);
        TextView tvCarsInWishlist = findViewById(R.id.tvCarsInWishlist);
        
        if (tvProfileName != null) tvProfileName.setText(accountManager.getUserName());
        bindExtraInfo();
        loadAvatar();
        updateCounts(tvCarsInCart, tvCarsInWishlist);
    }
}