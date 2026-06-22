package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends BaseActivity {
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        accountManager = new AccountManager(this);
        if (accountManager.isLoggedIn()) {
            JsonUtils.setAuthToken(accountManager.getToken());
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        UiUtils.applySystemBarInsets(this);

        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtPass = findViewById(R.id.edtPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPass.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Validators.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                edtEmail.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);
            doServerLogin(email, pass, btnLogin);
        });

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });
        }
    }

    /** Đăng nhập qua server jdm-shop; nếu không có mạng thì fallback đăng nhập cục bộ. */
    private void doServerLogin(String email, String pass, MaterialButton btnLogin) {
        new Thread(() -> {
            java.util.Map<String, String> b = new java.util.HashMap<>();
            b.put("email", email);
            b.put("password", pass);
            JsonUtils.Resp r = JsonUtils.requestJson("POST", "/api/users/login",
                    new com.google.gson.Gson().toJson(b));
            runOnUiThread(() -> handleAuthResult(r, email, pass, btnLogin, "Sai email hoặc mật khẩu!"));
        }).start();
    }

    private void handleAuthResult(JsonUtils.Resp r, String email, String pass,
                                  MaterialButton btnLogin, String failMsg) {
        btnLogin.setEnabled(true);
        if (r.code == 0) {
            // Mất kết nối server -> thử đăng nhập offline bằng dữ liệu đã lưu
            if (accountManager.login(email, pass)) {
                JsonUtils.setAuthToken(accountManager.getToken());
                goHome();
            } else {
                Toast.makeText(this, "Không kết nối được máy chủ. Hãy kiểm tra mạng/server.",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (r.code >= 200 && r.code < 300) {
            try {
                org.json.JSONObject o = new org.json.JSONObject(r.body);
                String token = o.optString("token", null);
                org.json.JSONObject u = o.optJSONObject("user");
                String name = u != null ? u.optString("name", "") : "";
                String role = u != null ? u.optString("role", "customer") : "customer";
                accountManager.setServerSession(token, name, email, role);
                accountManager.cacheLocalCredential(email, pass, name); // cho phép offline lần sau
                JsonUtils.setAuthToken(token);
                goHome();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý phản hồi máy chủ.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        // 400/401/403 -> hiển thị thông báo từ server nếu có
        Toast.makeText(this, serverMessage(r.body, failMsg), Toast.LENGTH_SHORT).show();
    }

    private String serverMessage(String body, String fallback) {
        try {
            return new org.json.JSONObject(body).optString("message", fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void goHome() {
        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}