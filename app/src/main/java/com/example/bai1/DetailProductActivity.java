package com.example.bai1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailProductActivity extends BaseActivity {

    private DataManager dataManager;
    private AccountManager accountManager;
    private CarModel currentCar;
    private ImageView btnFavorite;
    private List<ReviewModel> reviews = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        // Edge-to-edge: chừa khoảng cho status bar (header) và nav bar (thanh dưới)
        View header = findViewById(R.id.llHeader);
        View bottomBar = findViewById(R.id.llBottomActions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), bars.top + dp(8),
                        header.getPaddingRight(), dp(8));
            }
            if (bottomBar != null) {
                bottomBar.setPadding(bottomBar.getPaddingLeft(), dp(16),
                        bottomBar.getPaddingRight(), bars.bottom + dp(16));
            }
            return insets;
        });

        dataManager = new DataManager(this);
        accountManager = new AccountManager(this);

        // Map Views
        ImageView imgProduct = findViewById(R.id.imgProduct);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        ImageView btnShare = findViewById(R.id.btnShare);

        TextView tvBrand = findViewById(R.id.tvBrand);
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvEngine = findViewById(R.id.tvEngine);
        TextView tvPower = findViewById(R.id.tvPower);
        TextView tvAccel = findViewById(R.id.tvAcceleration);
        TextView tvDrive = findViewById(R.id.tvDrivetrain);
        TextView tvDescription = findViewById(R.id.tvDescription);
        MaterialButton btnBuy = findViewById(R.id.btnBuy);
        MaterialButton btnTestDrive = findViewById(R.id.btnTestDrive);
        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        TextView btnAddReview = findViewById(R.id.btnAddReview);

        // Get Data from Intent
        String name = getIntent().getStringExtra("CAR_NAME");
        String brand = getIntent().getStringExtra("CAR_BRAND");
        String price = getIntent().getStringExtra("CAR_PRICE");
        String engine = getIntent().getStringExtra("CAR_ENGINE");
        String power = getIntent().getStringExtra("CAR_POWER");
        String accel = getIntent().getStringExtra("CAR_ACCEL");
        String drive = getIntent().getStringExtra("CAR_DRIVE");
        String desc = getIntent().getStringExtra("CAR_DESC");
        String imgUrl = getIntent().getStringExtra("CAR_IMAGE");
        int id = getIntent().getIntExtra("CAR_ID", -1);

        // Ưu tiên lấy dữ liệu đầy đủ (gồm các màu) theo id; nếu không có thì dựng từ Intent
        currentCar = findCarById(id);
        if (currentCar == null) {
            currentCar = new CarModel(id, name, brand, price, engine, power, accel, drive, desc, imgUrl);
        }

        // Bind Data từ currentCar
        if (tvProductName != null) tvProductName.setText(currentCar.getName());
        if (tvBrand != null) tvBrand.setText(currentCar.getBrand() != null ? currentCar.getBrand().toUpperCase() : "");
        if (tvPrice != null) tvPrice.setText(currentCar.getPrice());
        if (tvEngine != null) tvEngine.setText(currentCar.getEngine());
        if (tvPower != null) tvPower.setText(currentCar.getPower());
        if (tvDescription != null) tvDescription.setText(currentCar.getDescription());
        if (tvAccel != null) tvAccel.setText(notEmpty(currentCar.getAcceleration()) ? currentCar.getAcceleration() : "—");
        if (tvDrive != null) tvDrive.setText(notEmpty(currentCar.getDrivetrain()) ? currentCar.getDrivetrain() : "—");

        updateFavoriteUI();

        if (imgProduct != null) {
            CarImages.load(this, currentCar, imgProduct);
        }

        setupColors(imgProduct);

        // Setup Reviews (Load from DataManager)
        loadReviews(id, rvReviews);

        // Actions
        btnBack.setOnClickListener(v -> finish());

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                dataManager.toggleWishlist(currentCar);
                updateFavoriteUI();
                String msg = dataManager.isFavorite(currentCar.getId()) ? "Added to Garage!" : "Removed from Garage!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareCar());
        }

        if (btnBuy != null) {
            btnBuy.setOnClickListener(v -> {
                UiUtils.playCarAcross(v); // 🏎️ xe chạy ngang trong nút cho vui
                if (dataManager.isInCart(currentCar.getId())) {
                    Toast.makeText(this, "Xe đã có trong giỏ hàng.", Toast.LENGTH_SHORT).show();
                } else {
                    dataManager.addToCart(currentCar);
                    Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTestDrive != null) {
            btnTestDrive.setOnClickListener(v -> {
                Intent i = new Intent(this, TestDriveActivity.class);
                i.putExtra("CAR_ID", currentCar.getId());
                i.putExtra("CAR_NAME", currentCar.getName());
                startActivity(i);
            });
        }

        if (btnAddReview != null) {
            btnAddReview.setOnClickListener(v -> showAddReviewDialog());
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private CarModel findCarById(int id) {
        if (id <= 0) return null;
        for (CarModel c : JsonUtils.getCars(this)) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    /** Dựng dãy ô chọn màu; bấm màu nào thì đổi ảnh hero theo màu đó. */
    private void setupColors(ImageView imgProduct) {
        View section = findViewById(R.id.sectionColors);
        LinearLayout llColors = findViewById(R.id.llColors);
        TextView tvColorName = findViewById(R.id.tvColorName);
        if (section == null || llColors == null) return;

        List<ColorVariant> colors = currentCar.getColors();
        if (colors == null || colors.isEmpty()) {
            section.setVisibility(View.GONE);
            return;
        }
        section.setVisibility(View.VISIBLE);
        llColors.removeAllViews();

        final List<View> swatches = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            ColorVariant cv = colors.get(i);
            View sw = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(40), dp(40));
            lp.setMarginEnd(dp(12));
            sw.setLayoutParams(lp);
            sw.setBackground(makeSwatch(cv.getHex(), false));
            final int idx = i;
            sw.setOnClickListener(v -> selectColor(idx, colors, swatches, imgProduct, tvColorName));
            llColors.addView(sw);
            swatches.add(sw);
        }
        selectColor(0, colors, swatches, imgProduct, tvColorName);
    }

    private void selectColor(int index, List<ColorVariant> colors, List<View> swatches,
                             ImageView imgProduct, TextView tvColorName) {
        for (int i = 0; i < swatches.size(); i++) {
            swatches.get(i).setBackground(makeSwatch(colors.get(i).getHex(), i == index));
        }
        ColorVariant cv = colors.get(index);
        if (tvColorName != null) {
            String stock = cv.getQuantity() > 0 ? "Còn " + cv.getQuantity() + " xe" : "Hết hàng";
            tvColorName.setText(cv.getName() + " · " + stock);
        }
        if (imgProduct != null) {
            CarImages.loadByName(this, cv.getImage(), currentCar.getImageUrl(), imgProduct);
        }
    }

    private GradientDrawable makeSwatch(String hex, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        int color;
        try {
            color = Color.parseColor(hex);
        } catch (Exception e) {
            color = Color.GRAY;
        }
        d.setColor(color);
        if (selected) {
            d.setStroke(dp(3), Color.parseColor("#F5333F"));
        } else {
            d.setStroke(dp(1), Color.parseColor("#66FFFFFF"));
        }
        return d;
    }

    private void loadReviews(int carId, RecyclerView rvReviews) {
        reviews.clear();
        reviews.addAll(dataManager.getReviewsForCar(carId));

        reviewAdapter = new ReviewAdapter(reviews);
        reviewAdapter.setOnReplyListener((position, review) -> showReplyDialog(position, review));
        if (rvReviews != null) {
            rvReviews.setLayoutManager(new LinearLayoutManager(this));
            rvReviews.setAdapter(reviewAdapter);
        }
        refreshReviewUi();
    }

    private void showReplyDialog(int position, ReviewModel review) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null);
        ((TextView) v.findViewById(R.id.tvReplyTitle)).setText("@" + review.getUserName());
        TextInputEditText edt = v.findViewById(R.id.edtReply);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(v).create();
        transparentBg(dialog);

        v.findViewById(R.id.btnCancelReply).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnSendReply).setOnClickListener(x -> {
            String text = edt.getText() != null ? edt.getText().toString().trim() : "";
            if (text.isEmpty()) {
                edt.setError(getString(R.string.your_comment));
                return;
            }
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            review.getReplies().add(new ReviewModel(accountManager.getUserName(), 0f, text, date));
            dataManager.saveReviewsForCar(currentCar.getId(), reviews);
            reviewAdapter.notifyItemChanged(position);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void transparentBg(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void refreshReviewUi() {
        TextView tvSummary = findViewById(R.id.tvRatingSummary);
        TextView tvNoReviews = findViewById(R.id.tvNoReviews);
        RecyclerView rvReviews = findViewById(R.id.rvReviews);

        if (reviews.isEmpty()) {
            if (tvNoReviews != null) tvNoReviews.setVisibility(View.VISIBLE);
            if (rvReviews != null) rvReviews.setVisibility(View.GONE);
            if (tvSummary != null) tvSummary.setText("Chưa có đánh giá");
            return;
        }
        if (tvNoReviews != null) tvNoReviews.setVisibility(View.GONE);
        if (rvReviews != null) rvReviews.setVisibility(View.VISIBLE);

        float sum = 0f;
        for (ReviewModel r : reviews) sum += r.getRating();
        float avg = sum / reviews.size();
        if (tvSummary != null) {
            tvSummary.setText(String.format(Locale.US, "⭐ %.1f / 5.0  •  %d đánh giá", avg, reviews.size()));
        }
    }

    private void showAddReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRating);
        TextInputEditText edtComment = dialogView.findViewById(R.id.edtReviewComment);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        transparentBg(dialog);

        dialogView.findViewById(R.id.btnCancelReview).setOnClickListener(x -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSubmitReview).setOnClickListener(x -> {
            float rating = ratingBar.getRating();
            String comment = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";
            if (rating <= 0f) {
                Toast.makeText(this, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                edtComment.setError(getString(R.string.your_comment));
                return;
            }

            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            ReviewModel newReview = new ReviewModel(accountManager.getUserName(), rating, comment, date);

            dataManager.addReview(currentCar.getId(), newReview);
            reviews.add(0, newReview);
            reviewAdapter.notifyItemInserted(0);
            refreshReviewUi();
            dialog.dismiss();
            Toast.makeText(this, "Đã gửi đánh giá. Cảm ơn bạn!", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void shareCar() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this " + currentCar.getName() + " on JDM Legends Garage! Price: " + currentCar.getPrice());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void updateFavoriteUI() {
        if (btnFavorite != null && currentCar != null) {
            btnFavorite.setImageResource(R.drawable.ic_heart);
            int color = dataManager.isFavorite(currentCar.getId())
                    ? getResources().getColor(R.color.jdm_red)
                    : getResources().getColor(R.color.white);
            btnFavorite.setColorFilter(color);
        }
    }
}