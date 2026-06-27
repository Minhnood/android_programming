package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CheckoutActivity extends BaseActivity {

    private DataManager dataManager;
    private List<CarModel> cartItems;
    private String totalAmount;

    private AutoCompleteTextView edtAddress;
    private com.google.android.material.textfield.TextInputEditText edtPhone;
    private View llCardForm;
    private com.google.android.material.textfield.TextInputEditText edtCardNumber;
    private com.google.android.material.textfield.TextInputEditText edtCardHolder;
    private com.google.android.material.textfield.TextInputEditText edtCardExpiry;
    private com.google.android.material.textfield.TextInputEditText edtCardCvv;
    private android.webkit.WebView mapView;
    private ArrayAdapter<String> suggestAdapter;
    private final List<String> suggestNames = new ArrayList<>();
    private final List<double[]> suggestLatLon = new ArrayList<>(); // [lat, lon]
    private final Handler debounce = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        UiUtils.applySystemBarInsets(this);

        dataManager = new DataManager(this);
        cartItems = dataManager.getCart();
        totalAmount = getIntent().getStringExtra("TOTAL_AMOUNT");

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        RecyclerView rvSummary = findViewById(R.id.rvSummary);
        edtPhone = findViewById(R.id.edtPhone);
        // Gợi ý sẵn số điện thoại đã lưu trong hồ sơ (nếu có)
        String savedPhone = new AccountManager(this).getPhone();
        if (savedPhone != null && !savedPhone.isEmpty()) edtPhone.setText(savedPhone);
        edtAddress = findViewById(R.id.edtAddress);
        mapView = findViewById(R.id.mapView);
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.getSettings().setDomStorageEnabled(true);
        MaterialButton btnFinalize = findViewById(R.id.btnFinalize);
        RadioGroup rgPayment = findViewById(R.id.rgPayment);
        llCardForm = findViewById(R.id.llCardForm);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtCardHolder = findViewById(R.id.edtCardHolder);
        edtCardExpiry = findViewById(R.id.edtCardExpiry);
        edtCardCvv = findViewById(R.id.edtCardCvv);

        btnBack.setOnClickListener(v -> finish());

        CartAdapter adapter = new CartAdapter(this, cartItems, null);
        rvSummary.setLayoutManager(new LinearLayoutManager(this));
        rvSummary.setAdapter(adapter);

        setupAddressAutocomplete();

        // Hiện form nhập thẻ khi chọn "Thẻ / Chuyển khoản"
        rgPayment.setOnCheckedChangeListener((group, checkedId) ->
                llCardForm.setVisibility(checkedId == R.id.rbCard ? View.VISIBLE : View.GONE));

        // Tự chèn dấu "/" cho ô hạn thẻ (MM/YY)
        edtCardExpiry.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;
                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);
                String formatted = digits.length() > 2
                        ? digits.substring(0, 2) + "/" + digits.substring(2)
                        : digits;
                s.replace(0, s.length(), formatted);
                editing = false;
            }
        });

        btnFinalize.setOnClickListener(v -> {
            String address = edtAddress.getText().toString().trim();
            if (address.isEmpty()) {
                edtAddress.setError("Vui lòng nhập địa chỉ giao hàng");
                edtAddress.requestFocus();
                return;
            }
            if (address.length() < 5) {
                edtAddress.setError("Địa chỉ quá ngắn, vui lòng nhập đầy đủ");
                edtAddress.requestFocus();
                return;
            }
            String phone = edtPhone.getText() == null ? "" : edtPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                edtPhone.setError(getString(R.string.err_phone_required));
                edtPhone.requestFocus();
                return;
            }
            if (!Validators.isValidPhone(phone)) {
                edtPhone.setError(getString(R.string.err_phone_invalid));
                edtPhone.requestFocus();
                return;
            }
            boolean isCard = rgPayment != null && rgPayment.getCheckedRadioButtonId() == R.id.rbCard;
            if (isCard && !validateCard()) {
                return;
            }
            // Lưu số điện thoại vào hồ sơ để lần sau gợi ý sẵn
            AccountManager am = new AccountManager(this);
            am.updateProfile(am.getUserName(), phone, am.getBio());
            String payment;
            if (isCard) {
                String last4 = onlyDigits(text(edtCardNumber));
                last4 = last4.substring(last4.length() - 4);
                payment = "Thẻ Visa **** " + last4;
            } else {
                payment = "Thanh toán khi nhận hàng (COD)";
            }
            processOrder(address, payment, phone);
        });
    }

    // ---- Kiểm tra thông tin thẻ Visa ----
    private boolean validateCard() {
        String number = onlyDigits(text(edtCardNumber));
        if (number.length() < 13 || number.length() > 19 || !luhnValid(number)) {
            edtCardNumber.setError(getString(R.string.err_card_number));
            edtCardNumber.requestFocus();
            return false;
        }
        if (text(edtCardHolder).trim().length() < 3) {
            edtCardHolder.setError(getString(R.string.err_card_holder));
            edtCardHolder.requestFocus();
            return false;
        }
        if (!validExpiry(text(edtCardExpiry).trim())) {
            edtCardExpiry.setError(getString(R.string.err_card_expiry));
            edtCardExpiry.requestFocus();
            return false;
        }
        String cvv = onlyDigits(text(edtCardCvv));
        if (cvv.length() < 3 || cvv.length() > 4) {
            edtCardCvv.setError(getString(R.string.err_card_cvv));
            edtCardCvv.requestFocus();
            return false;
        }
        return true;
    }

    private String text(com.google.android.material.textfield.TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }

    private String onlyDigits(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    // Thuật toán Luhn xác thực số thẻ
    private boolean luhnValid(String number) {
        int sum = 0;
        boolean alt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (alt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    // Kiểm tra hạn thẻ định dạng MM/YY và chưa hết hạn
    private boolean validExpiry(String exp) {
        if (!exp.matches("\\d{2}/\\d{2}")) return false;
        int mm = Integer.parseInt(exp.substring(0, 2));
        int yy = Integer.parseInt(exp.substring(3, 5));
        if (mm < 1 || mm > 12) return false;
        java.util.Calendar c = java.util.Calendar.getInstance();
        int curYy = c.get(java.util.Calendar.YEAR) % 100;
        int curMm = c.get(java.util.Calendar.MONTH) + 1;
        return yy > curYy || (yy == curYy && mm >= curMm);
    }

    // ---- Gợi ý địa chỉ (OpenStreetMap Nominatim) + bản đồ ----
    private void setupAddressAutocomplete() {
        // Adapter có bộ lọc "mở" — luôn hiển thị đúng kết quả lấy từ server (không lọc theo dấu)
        suggestAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, suggestNames) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override protected FilterResults performFiltering(CharSequence c) {
                        FilterResults r = new FilterResults();
                        r.values = suggestNames; r.count = suggestNames.size();
                        return r;
                    }
                    @Override protected void publishResults(CharSequence c, FilterResults r) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        edtAddress.setAdapter(suggestAdapter);
        edtAddress.setThreshold(1);

        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                final String q = s.toString().trim();
                debounce.removeCallbacksAndMessages(null);
                if (q.length() < 3) return;
                debounce.postDelayed(() -> fetchSuggestions(q), 400);
            }
        });

        edtAddress.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < suggestLatLon.size()) {
                double[] ll = suggestLatLon.get(position);
                showMap(ll[0], ll[1]);
            }
        });
    }

    private void fetchSuggestions(String query) {
        new Thread(() -> {
            final List<String> names = new ArrayList<>();
            final List<double[]> coords = new ArrayList<>();
            HttpURLConnection conn = null;
            try {
                String url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=vn&q="
                        + URLEncoder.encode(query, "UTF-8");
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                conn.setRequestProperty("User-Agent", "JDMGarageApp/1.0 (demo)");
                conn.setRequestProperty("Accept-Language", "vi");
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                JSONArray arr = new JSONArray(sb.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    names.add(o.optString("display_name"));
                    coords.add(new double[]{o.optDouble("lat"), o.optDouble("lon")});
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }
            runOnUiThread(() -> {
                suggestNames.clear(); suggestNames.addAll(names);
                suggestLatLon.clear(); suggestLatLon.addAll(coords);
                suggestAdapter.notifyDataSetChanged();
                if (!names.isEmpty() && edtAddress.hasFocus()) edtAddress.showDropDown();
            });
        }).start();
    }

    private void showMap(double lat, double lon) {
        double d = 0.008;
        String url = "https://www.openstreetmap.org/export/embed.html?bbox="
                + (lon - d) + "," + (lat - d) + "," + (lon + d) + "," + (lat + d)
                + "&layer=mapnik&marker=" + lat + "," + lon;
        mapView.setVisibility(View.VISIBLE);
        mapView.loadUrl(url);
    }

    private void processOrder(String address, String paymentMethod, String phone) {
        String orderId = String.valueOf(10000 + new Random().nextInt(90000));
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        OrderModel newOrder = new OrderModel(orderId, date, totalAmount,
                new ArrayList<>(cartItems), "Đang xử lý", paymentMethod);

        dataManager.addOrder(newOrder);
        dataManager.clearCart();

        pushOrderToServer(orderId, date, paymentMethod, address, phone);

        Toast.makeText(this, "Đặt hàng thành công! Đơn #" + orderId, Toast.LENGTH_SHORT).show();

        // Mở màn theo dõi đơn hàng ngay
        Intent track = new Intent(this, OrderDetailsActivity.class);
        track.putExtra("ORDER_DATA", new com.google.gson.Gson().toJson(newOrder));
        startActivity(track);

        setResult(RESULT_OK);
        finish();
    }

    private void pushOrderToServer(String orderId, String date, String paymentMethod, String address, String phone) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("orderId", orderId);
        body.put("userEmail", new AccountManager(this).getUserEmail());
        body.put("date", date);
        body.put("totalAmount", totalAmount);
        body.put("status", "Đang xử lý");
        body.put("paymentMethod", paymentMethod);
        body.put("address", address);
        body.put("phone", phone);
        body.put("items", cartItems);
        final String json = new com.google.gson.Gson().toJson(body);
        new Thread(() -> JsonUtils.postJson("/api/orders", json)).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
