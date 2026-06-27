package com.example.bai1;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileActivity extends BaseActivity {
    private AccountManager accountManager;
    private ShapeableImageView imgAvatar;
    private String avatarPath; // đường dẫn ảnh hiện tại (có thể null)

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    String saved = copyImageToInternal(uri);
                    if (saved != null) {
                        avatarPath = saved;
                        showAvatar();
                    } else {
                        Toast.makeText(this, "Không thể đọc ảnh, thử lại.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        UiUtils.applySystemBarInsets(this);

        accountManager = new AccountManager(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnChangePhoto = findViewById(R.id.btnChangePhoto);
        imgAvatar = findViewById(R.id.imgEditAvatar);
        TextInputEditText edtName = findViewById(R.id.edtEditName);
        TextInputEditText edtPhone = findViewById(R.id.edtEditPhone);
        TextInputEditText edtBio = findViewById(R.id.edtEditBio);
        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);

        // Nạp dữ liệu hiện có
        edtName.setText(accountManager.getUserName());
        edtPhone.setText(accountManager.getPhone());
        edtBio.setText(accountManager.getBio());
        avatarPath = accountManager.getAvatarPath();
        showAvatar();

        btnBack.setOnClickListener(v -> finish());

        // Chọn ảnh từ thư viện (PhotoPicker - không cần xin quyền)
        Runnable openPicker = () -> pickImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        imgAvatar.setOnClickListener(v -> openPicker.run());
        btnChangePhoto.setOnClickListener(v -> openPicker.run());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
            String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
            String bio = edtBio.getText() != null ? edtBio.getText().toString().trim() : "";

            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập tên");
                edtName.requestFocus();
                return;
            }

            accountManager.updateProfile(name, phone, bio);
            if (avatarPath != null) {
                accountManager.setAvatarPath(avatarPath);
            }
            Toast.makeText(this, "Đã cập nhật hồ sơ!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showAvatar() {
        if (avatarPath != null && new File(avatarPath).exists()) {
            Glide.with(this)
                    .load(new File(avatarPath))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    /** Sao chép ảnh người dùng chọn vào bộ nhớ trong để giữ lại lâu dài. */
    private String copyImageToInternal(Uri uri) {
        // Đặt tên file theo email để mỗi tài khoản có ảnh riêng, không ghi đè nhau
        String email = accountManager.getUserEmail();
        String safe = (email == null || email.isEmpty())
                ? "guest" : email.replaceAll("[^a-zA-Z0-9]", "_");
        File out = new File(getFilesDir(), "avatar_" + safe + ".jpg");
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(out)) {
            if (in == null) return null;
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
