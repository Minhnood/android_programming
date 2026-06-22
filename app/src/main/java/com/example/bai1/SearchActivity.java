package com.example.bai1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends BaseActivity {

    private static final String PREFS = "USER_PREFS";
    private static final String KEY_RECENT = "recent_searches";
    private static final int MAX_RECENT = 6;

    private EditText edtSearch;
    private RecyclerView rvResults;
    private ScrollView svHistory;
    private View llNoResults;
    private ChipGroup cgRecent;
    private CarAdapter adapter;
    private SharedPreferences prefs;
    private List<CarModel> allCars = new ArrayList<>();
    private List<CarModel> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        UiUtils.applySystemBarInsets(this);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        initViews();
        allCars.addAll(JsonUtils.getCars(this));
        setupSearchLogic();
        setupBrandClicks();
        renderRecent();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        rvResults = findViewById(R.id.rvResults);
        svHistory = findViewById(R.id.svHistory);
        llNoResults = findViewById(R.id.llNoResults);
        cgRecent = findViewById(R.id.cgRecent);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CarAdapter(this, filteredList, false);
        rvResults.setAdapter(adapter);
    }

    private void setupSearchLogic() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Nhấn nút tìm trên bàn phím -> lưu vào lịch sử
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = edtSearch.getText().toString().trim();
                if (!q.isEmpty()) saveRecent(q);
                return true;
            }
            return false;
        });
    }

    private void setupBrandClicks() {
        View.OnClickListener brandClick = v -> {
            if (v instanceof TextView) {
                Intent intent = new Intent(SearchActivity.this, CategoryActivity.class);
                intent.putExtra("CATEGORY_NAME", ((TextView) v).getText().toString());
                startActivity(intent);
            }
        };
        int[] brands = {R.id.brandNissan, R.id.brandToyota, R.id.brandMazda,
                R.id.brandHonda, R.id.brandMitsubishi, R.id.brandSubaru};
        for (int id : brands) {
            View b = findViewById(id);
            if (b != null) b.setOnClickListener(brandClick);
        }
    }

    private void filter(String text) {
        filteredList.clear();
        String q = text.trim().toLowerCase();
        if (q.isEmpty()) {
            svHistory.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            llNoResults.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }
        svHistory.setVisibility(View.GONE);
        for (CarModel car : allCars) {
            if (car.getName().toLowerCase().contains(q) || car.getBrand().toLowerCase().contains(q)) {
                filteredList.add(car);
            }
        }
        boolean none = filteredList.isEmpty();
        rvResults.setVisibility(none ? View.GONE : View.VISIBLE);
        llNoResults.setVisibility(none ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    // ---- Recent searches (lưu thật trong SharedPreferences) ----
    private List<String> getRecent() {
        Set<String> set = prefs.getStringSet(KEY_RECENT, new LinkedHashSet<>());
        return new ArrayList<>(set);
    }

    private void saveRecent(String query) {
        Set<String> set = new LinkedHashSet<>(getRecent());
        set.remove(query);
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(query);
        ordered.addAll(set);
        while (ordered.size() > MAX_RECENT) {
            String last = null;
            for (String s : ordered) last = s;
            ordered.remove(last);
        }
        prefs.edit().putStringSet(KEY_RECENT, ordered).apply();
        renderRecent();
    }

    private void renderRecent() {
        if (cgRecent == null) return;
        cgRecent.removeAllViews();
        List<String> recent = getRecent();
        if (recent.isEmpty()) {
            Chip hint = new Chip(this);
            hint.setText("Chưa có tìm kiếm nào");
            hint.setChipBackgroundColorResource(R.color.jdm_grey);
            hint.setTextColor(getResources().getColor(R.color.text_secondary));
            hint.setClickable(false);
            cgRecent.addView(hint);
            return;
        }
        for (String q : recent) {
            Chip chip = new Chip(this);
            chip.setText(q);
            chip.setChipBackgroundColorResource(R.color.jdm_grey);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> {
                edtSearch.setText(q);
                edtSearch.setSelection(edtSearch.getText().length());
            });
            cgRecent.addView(chip);
        }
    }
}
