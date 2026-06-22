package com.example.bai1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends BaseActivity {

    private RecyclerView rvProducts;
    private View llEmpty;
    private CarAdapter adapter;
    private List<CarModel> carList = new ArrayList<>();
    private String categoryName;
    private String region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        UiUtils.applySystemBarInsets(this);

        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        region = getIntent().getStringExtra("REGION");

        initViews();
        loadFilteredData();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        llEmpty = findViewById(R.id.llEmptyCategory);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvCategoryTitle);

        if (region != null) {
            tvTitle.setText(region.toUpperCase());
        } else if (categoryName != null) {
            tvTitle.setText(categoryName.toUpperCase() + " LEGENDS");
        }

        btnBack.setOnClickListener(v -> finish());

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void loadFilteredData() {
        carList.clear();
        for (CarModel car : JsonUtils.getCars(this)) {
            if (region != null) {
                // Lọc theo khu vực (Nội địa / Nước ngoài)
                if (region.equalsIgnoreCase(car.getRegion())) {
                    carList.add(car);
                }
                continue;
            }
            boolean all = categoryName == null || categoryName.equalsIgnoreCase("All JDM");
            if (all || (car.getBrand() != null && car.getBrand().equalsIgnoreCase(categoryName))) {
                carList.add(car);
            }
        }

        adapter = new CarAdapter(this, carList, false);
        rvProducts.setAdapter(adapter);

        boolean empty = carList.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
