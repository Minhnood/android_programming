package com.example.bai1;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Tiện ích parse JSON an toàn. Tách riêng khỏi Android để có thể unit test trên JVM.
 * Nếu chuỗi JSON null/rỗng/hỏng thì trả về giá trị mặc định thay vì làm văng app.
 */
public final class JsonSafe {

    private JsonSafe() {
    }

    /**
     * Parse {@code json} thành kiểu {@code type}. Trả về {@code fallback} nếu:
     * chuỗi null/rỗng, Gson ném lỗi (JSON hỏng), hoặc kết quả parse ra null.
     */
    public static <T> T parseOr(Gson gson, String json, Type type, T fallback) {
        if (json == null || json.trim().isEmpty()) return fallback;
        try {
            T value = gson.fromJson(json, type);
            return value != null ? value : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
