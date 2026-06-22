package com.example.bai1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS = "USER_PREFS";
    private static final String KEY_NOTIF = "pref_notifications";

    private final String[] langCodes = {"vi", "en", "ja"};
    private final String[] langNames = {"Tiếng Việt", "English", "日本語"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        UiUtils.applySystemBarInsets(this);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBack);
        SwitchMaterial switchNotif = findViewById(R.id.switchNotif);
        TextView tvPrivacy = findViewById(R.id.tvPrivacy);
        TextView tvTerms = findViewById(R.id.tvTerms);
        TextView tvVersion = findViewById(R.id.tvVersion);
        View rowLanguage = findViewById(R.id.rowLanguage);
        TextView tvCurrentLang = findViewById(R.id.tvCurrentLang);

        if (tvVersion != null) tvVersion.setText(getString(R.string.version_prefix) + " " + BuildConfig.VERSION_NAME);
        if (tvCurrentLang != null) tvCurrentLang.setText(currentLangName());

        btnBack.setOnClickListener(v -> finish());

        switchNotif.setChecked(prefs.getBoolean(KEY_NOTIF, true));
        switchNotif.setOnCheckedChangeListener((b, isChecked) ->
                prefs.edit().putBoolean(KEY_NOTIF, isChecked).apply());

        if (rowLanguage != null) rowLanguage.setOnClickListener(v -> showLanguageDialog());

        tvPrivacy.setOnClickListener(v ->
                openWeb(getString(R.string.privacy_policy), "https://www.termsfeed.com/live/privacy-policy-generic"));
        tvTerms.setOnClickListener(v ->
                openWeb(getString(R.string.terms_of_service), "https://www.termsfeed.com/live/terms-conditions-generic"));
    }

    private String currentLangName() {
        String cur = LocaleHelper.getLanguage(this);
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(cur)) return langNames[i];
        }
        return langNames[0];
    }

    private void showLanguageDialog() {
        String cur = LocaleHelper.getLanguage(this);
        int checked = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(cur)) checked = i;
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_language))
                .setSingleChoiceItems(langNames, checked, (dialog, which) -> {
                    LocaleHelper.setLanguage(this, langCodes[which]);
                    dialog.dismiss();
                    restartApp();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Khởi động lại app để áp ngôn ngữ mới cho toàn bộ màn hình. */
    private void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void openWeb(String title, String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
}
