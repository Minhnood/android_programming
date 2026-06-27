package com.example.bai1;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Sinh thông báo từ dữ liệu thật (đơn hàng + lời chào) và quản lý trạng thái đã đọc. */
public class NotificationHelper {

    private static final String PREFS = "USER_PREFS";
    private static final String KEY_READ = "read_notifications";
    private static final String WELCOME_ID = "welcome";

    public static List<NotificationModel> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> read = prefs.getStringSet(KEY_READ, new HashSet<>());

        List<NotificationModel> list = new ArrayList<>();
        for (OrderModel o : new DataManager(context).getOrders()) {
            String id = "order_" + o.getOrderId();
            int count = 0;
            if (o.getItems() != null) {
                for (CarModel c : o.getItems()) count += c.getCartQty();
            }
            list.add(new NotificationModel(
                    id,
                    "Đơn hàng #" + o.getOrderId() + " " + o.getStatus(),
                    "Đơn " + count + " xe • Tổng " + o.getTotalAmount() + ". Cảm ơn bạn đã đặt hàng!",
                    o.getDate(),
                    read.contains(id)));
        }
        list.add(new NotificationModel(
                WELCOME_ID,
                "Chào mừng đến JDM Legends Garage 🏎️",
                "Khám phá bộ sưu tập huyền thoại JDM và tạo Dream Garage của riêng bạn.",
                "Bắt đầu",
                read.contains(WELCOME_ID)));
        return list;
    }

    public static int unreadCount(Context context) {
        int n = 0;
        for (NotificationModel m : getAll(context)) {
            if (!m.isRead()) n++;
        }
        return n;
    }

    public static void markRead(Context context, String id) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> read = new HashSet<>(prefs.getStringSet(KEY_READ, new HashSet<>()));
        if (read.add(id)) {
            prefs.edit().putStringSet(KEY_READ, read).apply();
        }
    }
}
