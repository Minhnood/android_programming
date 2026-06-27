package com.example.bai1;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountManager {
    private static final String PREF_NAME = "USER_PREFS";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASS = "user_pass";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_BIO = "user_bio";
    private static final String KEY_AVATAR = "user_avatar";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROLE = "user_role";

    private SharedPreferences prefs;

    public AccountManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean register(String name, String email, String pass) {
        if (prefs.contains(email)) return false; // Đã tồn tại
        prefs.edit()
                .putString(email, pass)
                .putString(email + "_name", name)
                .apply();
        return true;
    }

    /** Kiểm tra email đã đăng ký chưa (dùng cho quên mật khẩu). */
    public boolean accountExists(String email) {
        return email != null && prefs.contains(email);
    }

    public boolean login(String email, String pass) {
        String savedPass = prefs.getString(email, null);
        if (savedPass != null && savedPass.equals(pass)) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
            prefs.edit().putString(KEY_EMAIL, email).apply();
            prefs.edit().putString(KEY_NAME, prefs.getString(email + "_name", "JDM Fan")).apply();
            return true;
        }
        return false;
    }

    /** Lưu phiên đăng nhập lấy từ server (JWT). */
    public void setServerSession(String token, String name, String email, String role) {
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(KEY_IS_LOGGED_IN, true);
        e.putString(KEY_TOKEN, token);
        e.putString(KEY_EMAIL, email);
        if (name != null && !name.isEmpty()) {
            e.putString(KEY_NAME, name);
            e.putString(email + "_name", name);
        }
        e.putString(KEY_ROLE, role != null ? role : "customer");
        e.apply();
    }

    /** Lưu thông tin đăng nhập cục bộ để có thể đăng nhập offline lần sau. */
    public void cacheLocalCredential(String email, String pass, String name) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(email, pass);
        if (name != null && !name.isEmpty()) e.putString(email + "_name", name);
        e.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "customer");
    }

    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_TOKEN)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "JDM Fan");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        // Ưu tiên dữ liệu theo từng tài khoản; fallback key cũ để tương thích
        String email = getUserEmail();
        if (email != null && !email.isEmpty() && prefs.contains(email + "_phone")) {
            return prefs.getString(email + "_phone", "");
        }
        return prefs.getString(KEY_PHONE, "");
    }

    public String getBio() {
        String email = getUserEmail();
        if (email != null && !email.isEmpty() && prefs.contains(email + "_bio")) {
            return prefs.getString(email + "_bio", "");
        }
        return prefs.getString(KEY_BIO, "");
    }

    /** Đường dẫn ảnh đại diện của tài khoản hiện tại (null nếu chưa có). */
    public String getAvatarPath() {
        String email = getUserEmail();
        if (email != null && !email.isEmpty()) {
            // Chỉ trả ảnh nếu tài khoản này đã từng lưu ảnh riêng
            return prefs.getString(email + "_avatar", null);
        }
        return null;
    }

    public void setAvatarPath(String path) {
        String email = getUserEmail();
        SharedPreferences.Editor e = prefs.edit();
        if (email != null && !email.isEmpty()) {
            e.putString(email + "_avatar", path);
        }
        e.apply();
    }

    /** Cập nhật hồ sơ: tên, số điện thoại, giới thiệu (lưu theo từng tài khoản). */
    public void updateProfile(String name, String phone, String bio) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(KEY_NAME, name);
        // Lưu theo email để mỗi tài khoản giữ dữ liệu riêng
        String email = getUserEmail();
        if (email != null && !email.isEmpty()) {
            e.putString(email + "_name", name);
            e.putString(email + "_phone", phone);
            e.putString(email + "_bio", bio);
        }
        e.apply();
    }
}