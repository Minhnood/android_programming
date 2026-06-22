package com.example.bai1;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/** Quản lý ngôn ngữ ứng dụng (vi / en / ja), lưu vào SharedPreferences. */
public class LocaleHelper {
    private static final String PREFS = "USER_PREFS";
    private static final String KEY_LANG = "app_lang";

    public static String getLanguage(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "vi");
    }

    public static void setLanguage(Context c, String lang) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANG, lang).apply();
    }

    /** Bọc context với ngôn ngữ đã chọn. Gọi trong attachBaseContext của Activity. */
    public static Context wrap(Context c) {
        String lang = getLanguage(c);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(c.getResources().getConfiguration());
        config.setLocale(locale);
        return c.createConfigurationContext(config);
    }
}
