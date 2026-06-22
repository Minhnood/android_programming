package com.example.bai1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class CartActivity extends BaseActivity {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private DataManager dataManager;
    private TextView tvTotalPrice;
    private View llEmptyCart;
    private View cvCheckout;
    private List<CarModel> cartItems;
    private static final int CHECKOUT_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        UiUtils.applySystemBarInsets(this);

        dataManager = new DataManager(this);
        initViews();
        loadCart();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        llEmptyCart = findViewById(R.id.llEmptyCart);
        cvCheckout = findViewById(R.id.cvCheckout);
        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);

        btnBack.setOnClickListener(v -> finish());
        
        btnCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("TOTAL_AMOUNT", tvTotalPrice.getText().toString());
                startActivityForResult(intent, CHECKOUT_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        cartItems = dataManager.getCart();

        boolean empty = cartItems.isEmpty();
        llEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        cvCheckout.setVisibility(empty ? View.GONE : View.VISIBLE);

        adapter = new CartAdapter(this, cartItems, position -> {
            dataManager.removeFromCart(position);
            loadCart();
        });
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (CarModel car : cartItems) {
            try {
                String priceStr = car.getPrice().replace("$", "").replace(",", "");
                total += Integer.parseInt(priceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tvTotalPrice.setText(String.format(Locale.US, "$%,d", total));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECKOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Đã đặt hàng thành công, làm mới giỏ hàng (sẽ trống)
            loadCart();
            finish(); // Đóng luôn trang giỏ hàng
        }
    }
}