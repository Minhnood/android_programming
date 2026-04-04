package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogIn = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LogIn.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogIn.setOnClickListener(v -> {
            EditText objUser = findViewById(R.id.txtUser);
            EditText objPass = findViewById(R.id.txtPass);

            String sPhone = objUser.getText().toString();
            String sPassword = objPass.getText().toString();

            if (sPassword.equals("123") && sPhone.equals("200") ) {
                Intent it = new Intent(LogIn.this, HomeActivity.class);
                startActivity(it);

                finish();
            }
            else {
                objUser.setError("Sai tài khoản");
                objPass.setError("Sai mật khẩu");
            }
        });
    }
}