package com.example.bai1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private List<CarModel> carList;
    private Context context;
    private boolean isHorizontal;

    public CarAdapter(Context context, List<CarModel> carList, boolean isHorizontal) {
        this.context = context;
        this.carList = carList;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isHorizontal ? R.layout.item_car_card : R.layout.item_car_grid;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarModel car = carList.get(position);
        holder.tvName.setText(car.getName());
        holder.tvPrice.setText(car.getPrice());

        // Ưu tiên ảnh nhúng trong app, fallback URL
        CarImages.load(context, car, holder.imgCar);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivity.class);
            intent.putExtra("CAR_ID", car.getId());
            intent.putExtra("CAR_NAME", car.getName());
            intent.putExtra("CAR_BRAND", car.getBrand());
            intent.putExtra("CAR_PRICE", car.getPrice());
            intent.putExtra("CAR_ENGINE", car.getEngine());
            intent.putExtra("CAR_POWER", car.getPower());
            intent.putExtra("CAR_ACCEL", car.getAcceleration());
            intent.putExtra("CAR_DRIVE", car.getDrivetrain());
            intent.putExtra("CAR_DESC", car.getDescription());
            intent.putExtra("CAR_IMAGE", car.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView tvName, tvPrice;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}