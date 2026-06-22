package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Khôi phục JWT đã lưu để các request cần đăng nhập hoạt động ngay sau khi mở app
        JsonUtils.setAuthToken(new AccountManager(this).getToken());

        // Tải danh sách xe từ server (jdm-shop) trên luồng nền để làm nóng cache
        new Thread(() -> JsonUtils.refreshFromApi()).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
