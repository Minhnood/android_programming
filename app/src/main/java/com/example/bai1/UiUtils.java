package com.example.bai1;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Tiện ích xử lý edge-to-edge: tự chừa khoảng cho status bar (trên) và
 * navigation bar (dưới) để header/nội dung không bị che.
 */
public class UiUtils {

    /** Áp inset cho root layout của Activity (chừa cả 4 phía theo system bars). */
    public static void applySystemBarInsets(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        applySystemBarInsets(content.getChildAt(0));
    }

    /** Áp inset cho một view bất kỳ, giữ nguyên padding gốc rồi cộng thêm inset. */
    public static void applySystemBarInsets(final View view) {
        if (view == null) return;
        final int pl = view.getPaddingLeft();
        final int pt = view.getPaddingTop();
        final int pr = view.getPaddingRight();
        final int pb = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(pl + bars.left, pt + bars.top, pr + bars.right, pb + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Hiệu ứng vui: một chiếc xe 🏎️ chạy ngang QUA NÚT bấm (giống animation nút
     * Subscribe/Like của YouTube). Xe xuất hiện từ mép trái nút, chạy sang mép phải
     * rồi tự biến mất. Truyền vào chính view nút vừa bấm.
     */
    public static void playCarAcross(final View button) {
        if (button == null) return;
        final View rootView = button.getRootView();
        if (!(rootView instanceof ViewGroup)) return;
        final ViewGroup root = (ViewGroup) rootView;

        final TextView car = new TextView(button.getContext());
        car.setText("🏎️");
        car.setIncludeFontPadding(false);
        car.setScaleX(-1f); // lật để đầu xe hướng sang phải (chạy trái → phải)
        root.addView(car, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        car.post(() -> {
            // Vị trí của nút so với root overlay
            int[] b = new int[2]; button.getLocationInWindow(b);
            int[] r = new int[2]; root.getLocationInWindow(r);
            float bx = b[0] - r[0];
            float by = b[1] - r[1];
            float bw = button.getWidth();
            float bh = button.getHeight();

            // Cỡ xe vừa với chiều cao nút
            car.setTextSize(TypedValue.COMPLEX_UNIT_PX, bh * 0.62f);
            car.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            float cw = car.getMeasuredWidth();
            float ch = car.getMeasuredHeight();

            car.setY(by + (bh - ch) / 2f);          // căn giữa theo chiều dọc nút
            car.setTranslationX(bx - cw);            // bắt đầu ngay sát mép trái nút
            car.animate()
                    .translationX(bx + bw)           // kết thúc ở mép phải nút
                    .setDuration(750)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> root.removeView(car))
                    .start();
        });
    }

    /**
     * Phát âm "ting" ngắn để xác nhận (vd khi bấm "Mua ngay"). Tự giải phóng
     * MediaPlayer sau khi phát xong; mọi lỗi (không loa/codec) được bỏ qua để
     * không làm gián đoạn luồng mua hàng.
     */
    public static void playDing(Context context) {
        if (context == null) return;
        try {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.ting);
            if (mp == null) return;
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        } catch (Exception ignored) {
        }
    }
}
