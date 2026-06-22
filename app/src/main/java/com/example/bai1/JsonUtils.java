package com.example.bai1;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    // Server đã deploy lên cloud (Render) -> app chạy MỌI NƠI, mọi mạng (Wi-Fi/4G), không cần laptop.
    // Khi cần chạy server cục bộ để debug: đổi tạm thành "http://10.0.2.2:8000" (máy ảo)
    // hoặc "http://<IP-LAN-máy>:8000" (điện thoại thật cùng Wi-Fi).
    public static final String BASE_URL = "https://jdm-shop.onrender.com";

    // Cache trong bộ nhớ: được nạp từ API (warm ở SplashActivity / HomeActivity.onResume).
    private static List<CarModel> cache;

    // JWT của người dùng — set sau khi đăng nhập / khi khởi động app (từ AccountManager).
    private static String authToken;

    public static void setAuthToken(String token) { authToken = token; }
    public static String getAuthToken() { return authToken; }

    private static void applyAuth(HttpURLConnection conn) {
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
    }

    /** Kết quả HTTP gồm mã trạng thái + body (dùng cho login/register). code=0 nghĩa là lỗi mạng. */
    public static class Resp {
        public int code;
        public String body;
    }

    /** Gọi HTTP có kèm token, trả về cả code lẫn body (đọc cả errorStream khi lỗi). */
    public static Resp requestJson(String method, String path, String jsonBody) {
        HttpURLConnection conn = null;
        Resp r = new Resp();
        try {
            conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("Accept", "application/json");
            applyAuth(conn);
            if (jsonBody != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            r.code = conn.getResponseCode();
            InputStream is = (r.code >= 200 && r.code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            if (is != null) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                r.body = sb.toString();
            }
        } catch (Exception e) {
            r.code = 0;
            r.body = null;
        } finally {
            if (conn != null) conn.disconnect();
        }
        return r;
    }

    /** Trả về danh sách xe: ưu tiên cache từ server, nếu chưa có thì đọc cars.json đóng gói. */
    public static List<CarModel> getCars(Context context) {
        if (cache != null && !cache.isEmpty()) return new ArrayList<>(cache);
        return loadLocal(context);
    }

    /** Tải xe từ API jdm-shop (gọi trên luồng nền). Thành công -> cập nhật cache. */
    public static boolean refreshFromApi() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/api/cars");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) return false;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            Type type = new TypeToken<ArrayList<CarModel>>() {}.getType();
            List<CarModel> list = new Gson().fromJson(sb.toString(), type);
            if (list != null && !list.isEmpty()) {
                cache = list;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    /** Gửi POST JSON tới API (gọi trên luồng nền). Trả về true nếu thành công. */
    public static boolean postJson(String path, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            applyAuth(conn);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** GET trả về chuỗi body (gọi trên luồng nền). null nếu lỗi. */
    public static String httpGet(String path) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("Accept", "application/json");
            applyAuth(conn);
            if (conn.getResponseCode() != 200) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static List<CarModel> loadLocal(Context context) {
        String json = loadJSONFromAsset(context, "cars.json");
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<CarModel>>() {}.getType();
        List<CarModel> cars = new Gson().fromJson(json, type);
        return cars != null ? cars : new ArrayList<>();
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
