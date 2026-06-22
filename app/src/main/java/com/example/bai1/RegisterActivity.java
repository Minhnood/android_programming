package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends BaseActivity {
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UiUtils.applySystemBarInsets(this);

        accountManager = new AccountManager(this);

        TextInputEditText edtName = findViewById(R.id.edtRegisterName);
        TextInputEditText edtEmail = findViewById(R.id.edtRegisterEmail);
        TextInputEditText edtPass = findViewById(R.id.edtRegisterPassword);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtPass.getText().toString();

            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập tên");
                edtName.requestFocus();
                return;
            }
            if (!Validators.isValidEmail(email)) {
                edtEmail.setError("Email không hợp lệ");
                edtEmail.requestFocus();
                return;
            }
            if (!Validators.isValidPassword(pass)) {
                edtPass.setError("Mật khẩu tối thiểu 6 ký tự");
                edtPass.requestFocus();
                return;
            }

            btnRegister.setEnabled(false);
            doServerRegister(name, email, pass, btnRegister);
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    /** Đăng ký qua server jdm-shop; offline thì fallback đăng ký cục bộ. */
    private void doServerRegister(String name, String email, String pass, MaterialButton btnRegister) {
        new Thread(() -> {
            java.util.Map<String, String> b = new java.util.HashMap<>();
            b.put("name", name);
            b.put("email", email);
            b.put("password", pass);
            JsonUtils.Resp r = JsonUtils.requestJson("POST", "/api/users/register",
                    new com.google.gson.Gson().toJson(b));
            runOnUiThread(() -> handleRegister(r, name, email, pass, btnRegister));
        }).start();
    }

    private void handleRegister(JsonUtils.Resp r, String name, String email, String pass,
                                MaterialButton btnRegister) {
        btnRegister.setEnabled(true);
        if (r.code == 0) {
            // Offline -> đăng ký cục bộ
            if (accountManager.register(name, email, pass)) {
                accountManager.login(email, pass);
                JsonUtils.setAuthToken(accountManager.getToken());
                Toast.makeText(this, "Đăng ký thành công (ngoại tuyến)!", Toast.LENGTH_SHORT).show();
                goHome();
            } else {
                Toast.makeText(this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (r.code >= 200 && r.code < 300) {
            try {
                org.json.JSONObject o = new org.json.JSONObject(r.body);
                String token = o.optString("token", null);
                org.json.JSONObject u = o.optJSONObject("user");
                String uname = u != null ? u.optString("name", name) : name;
                String role = u != null ? u.optString("role", "customer") : "customer";
                accountManager.setServerSession(token, uname, email, role);
                accountManager.cacheLocalCredential(email, pass, uname);
                JsonUtils.setAuthToken(token);
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                goHome();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý phản hồi máy chủ.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        // 400 -> email tồn tại / thiếu trường
        String msg;
        try {
            msg = new org.json.JSONObject(r.body).optString("message", "Đăng ký thất bại.");
        } catch (Exception e) {
            msg = "Đăng ký thất bại.";
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}