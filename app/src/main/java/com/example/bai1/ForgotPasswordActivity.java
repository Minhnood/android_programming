package com.example.bai1;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        UiUtils.applySystemBarInsets(this);

        AccountManager accountManager = new AccountManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextInputEditText edtEmail = findViewById(R.id.edtForgotEmail);
        MaterialButton btnSend = findViewById(R.id.btnSendReset);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ");
                edtEmail.requestFocus();
                return;
            }
            if (!accountManager.accountExists(email)) {
                edtEmail.setError("Không tìm thấy tài khoản với email này");
                edtEmail.requestFocus();
                return;
            }
            Toast.makeText(this, "Đã gửi hướng dẫn đặt lại mật khẩu tới " + email, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}