package com.example.bai1;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Màn hình đăng ký lái thử một chiếc xe: nhập tên, SĐT, chọn ngày-giờ, ghi chú. */
public class TestDriveActivity extends BaseActivity {

    private DataManager dataManager;

    private TextInputEditText edtName;
    private TextInputEditText edtPhone;
    private TextInputEditText edtNote;
    private MaterialButton btnPickDate;
    private MaterialButton btnPickTime;

    private int carId;
    private String carName;
    private String selectedDate; // dd/MM/yyyy
    private String selectedTime; // HH:mm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_drive);
        UiUtils.applySystemBarInsets(this);

        dataManager = new DataManager(this);

        carId = getIntent().getIntExtra("CAR_ID", -1);
        carName = getIntent().getStringExtra("CAR_NAME");

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvCarName = findViewById(R.id.tvCarName);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtNote = findViewById(R.id.edtNote);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);

        if (tvCarName != null) {
            tvCarName.setText(carName != null ? carName : "");
        }

        // Gợi ý sẵn tên & SĐT từ hồ sơ (nếu có)
        AccountManager account = new AccountManager(this);
        String savedName = account.getUserName();
        if (savedName != null && !savedName.isEmpty()) edtName.setText(savedName);
        String savedPhone = account.getPhone();
        if (savedPhone != null && !savedPhone.isEmpty()) edtPhone.setText(savedPhone);

        btnBack.setOnClickListener(v -> finish());
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnConfirm.setOnClickListener(v -> submit());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(picked.getTime());
            btnPickDate.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        // Không cho chọn ngày trong quá khứ
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hour, minute) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            btnPickTime.setText(selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void submit() {
        String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
        String phone = edtPhone.getText() == null ? "" : edtPhone.getText().toString().trim();
        String note = edtNote.getText() == null ? "" : edtNote.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError(getString(R.string.td_err_name));
            edtName.requestFocus();
            return;
        }
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
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, getString(R.string.td_err_datetime), Toast.LENGTH_SHORT).show();
            return;
        }

        String createdAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        TestDriveBooking booking = new TestDriveBooking(
                carId, carName, name, phone, selectedDate, selectedTime, note, createdAt);
        dataManager.addTestDrive(booking);

        Toast.makeText(this, getString(R.string.td_success), Toast.LENGTH_LONG).show();
        finish();
    }
}
