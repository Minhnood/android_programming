package com.example.bai1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

/** Tải ảnh xe: ưu tiên ảnh nhúng trong app (drawable/car_<id>), nếu không có thì dùng URL. */
public class CarImages {

    public static void load(Context context, CarModel car, ImageView target) {
        if (context == null || car == null || target == null) return;

        int resId = context.getResources()
                .getIdentifier("car_" + car.getId(), "drawable", context.getPackageName());

        RequestBuilder<Drawable> builder = (resId != 0)
                ? Glide.with(context).load(resId)
                : Glide.with(context).load(car.getImageUrl());

        builder.placeholder(R.drawable.ic_car_placeholder)
                .error(R.drawable.ic_car_placeholder)
                .centerCrop()
                .into(target);
    }

    /** Tải ảnh theo tên drawable (vd "car_5"); nếu không có thì dùng fallbackUrl. */
    public static void loadByName(Context context, String drawableName, String fallbackUrl, ImageView target) {
        if (context == null || target == null) return;

        int resId = (drawableName == null) ? 0
                : context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());

        RequestBuilder<Drawable> builder = (resId != 0)
                ? Glide.with(context).load(resId)
                : Glide.with(context).load(fallbackUrl);

        builder.placeholder(R.drawable.ic_car_placeholder)
                .error(R.drawable.ic_car_placeholder)
                .centerCrop()
                .into(target);
    }
}
