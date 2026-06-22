package com.example.bai1;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

/** Activity cơ sở: áp ngôn ngữ đã chọn cho mọi màn hình. */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}
