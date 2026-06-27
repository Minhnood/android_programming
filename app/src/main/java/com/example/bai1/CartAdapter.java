package com.example.bai1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CarModel> cartList;
    private Context context;
    private OnItemRemoveListener removeListener;
    private OnQuantityChangeListener qtyListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    /** Báo số lượng mới khi người dùng bấm − / + trong giỏ. */
    public interface OnQuantityChangeListener {
        void onQuantityChange(int position, int newQty);
    }

    /** Chế độ chỉ-đọc (vd tóm tắt đơn hàng): không có nút xoá, không sửa số lượng. */
    public CartAdapter(Context context, List<CarModel> cartList, OnItemRemoveListener listener) {
        this(context, cartList, listener, null);
    }

    /** Chế độ giỏ hàng: có thể xoá và đổi số lượng. */
    public CartAdapter(Context context, List<CarModel> cartList,
                       OnItemRemoveListener removeListener, OnQuantityChangeListener qtyListener) {
        this.context = context;
        this.cartList = cartList;
        this.removeListener = removeListener;
        this.qtyListener = qtyListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CarModel car = cartList.get(position);
        holder.tvName.setText(car.getName());

        long unit = DataManager.parsePrice(car.getPrice());
        int qty = car.getCartQty();
        long subtotal = unit * qty;

        CarImages.load(context, car, holder.imgCar);

        boolean editable = qtyListener != null;
        holder.llQty.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.btnRemove.setVisibility(removeListener != null ? View.VISIBLE : View.GONE);

        if (editable) {
            // Trong giỏ: hiện thành tiền của dòng + bộ chọn số lượng
            holder.tvPrice.setText(String.format(Locale.US, "$%,d", subtotal));
            holder.tvQty.setText(String.valueOf(qty));
            holder.btnQtyMinus.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                int cur = cartList.get(pos).getCartQty(); // đọc SL mới nhất, tránh bấm nhanh bị trượt
                if (cur > 1) qtyListener.onQuantityChange(pos, cur - 1);
            });
            holder.btnQtyPlus.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                int cur = cartList.get(pos).getCartQty();
                qtyListener.onQuantityChange(pos, cur + 1);
            });
        } else {
            // Tóm tắt đơn: thành tiền + "(x N)" nếu mua nhiều
            holder.tvPrice.setText(qty > 1
                    ? String.format(Locale.US, "$%,d  (x%d)", subtotal, qty)
                    : String.format(Locale.US, "$%,d", subtotal));
        }

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (removeListener != null && pos != RecyclerView.NO_POSITION) {
                removeListener.onRemove(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar, btnRemove;
        TextView tvName, tvPrice, tvQty, btnQtyMinus, btnQtyPlus;
        LinearLayout llQty;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCartItem);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
            llQty = itemView.findViewById(R.id.llQty);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnQtyMinus = itemView.findViewById(R.id.btnQtyMinus);
            btnQtyPlus = itemView.findViewById(R.id.btnQtyPlus);
        }
    }
}
