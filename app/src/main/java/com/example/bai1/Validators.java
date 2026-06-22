package com.example.bai1;

import java.util.regex.Pattern;

/**
 * Các hàm kiểm tra dữ liệu nhập (email, mật khẩu, số điện thoại).
 * Dùng regex Java thuần (không phụ thuộc Android) để có thể unit test trên JVM
 * và để các màn hình dùng chung một logic, tránh lặp code.
 */
public final class Validators {

    // Regex email cơ bản, đủ dùng cho form đăng nhập/đăng ký.
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Số điện thoại VN: 10 chữ số bắt đầu bằng 0.
    private static final Pattern PHONE = Pattern.compile("0\\d{9}");

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /** Mật khẩu tối thiểu 6 ký tự. */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }
}
