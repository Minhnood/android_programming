package com.example.bai1;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String PREF_NAME = "JDM_PREFS";
    private static final String KEY_WISHLIST = "KEY_WISHLIST";
    private static final String KEY_CART = "KEY_CART";
    private static final String KEY_ORDERS = "KEY_ORDERS";
    private static final String KEY_REVIEWS = "KEY_REVIEWS";
    private static final String KEY_TEST_DRIVES = "KEY_TEST_DRIVES";
    
    private SharedPreferences prefs;
    private Gson gson;

    public DataManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- WISHLIST ---
    public void toggleWishlist(CarModel car) {
        List<CarModel> list = getWishlist();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == car.getId()) {
                list.remove(i);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(car);
        }
        saveWishlist(list);
    }

    public boolean isFavorite(int carId) {
        List<CarModel> list = getWishlist();
        for (CarModel c : list) {
            if (c.getId() == carId) return true;
        }
        return false;
    }

    public List<CarModel> getWishlist() {
        String json = prefs.getString(KEY_WISHLIST, null);
        Type type = new TypeToken<ArrayList<CarModel>>() {}.getType();
        return JsonSafe.parseOr(gson, json, type, new ArrayList<>());
    }

    private void saveWishlist(List<CarModel> list) {
        prefs.edit().putString(KEY_WISHLIST, gson.toJson(list)).apply();
    }

    public void clearWishlist() {
        prefs.edit().remove(KEY_WISHLIST).apply();
    }

    // --- CART ---
    public void addToCart(CarModel car) {
        List<CarModel> list = getCart();
        list.add(car);
        saveCart(list);
    }

    public boolean isInCart(int carId) {
        for (CarModel c : getCart()) {
            if (c.getId() == carId) return true;
        }
        return false;
    }

    public void removeFromCart(int position) {
        List<CarModel> list = getCart();
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            saveCart(list);
        }
    }

    public void clearCart() {
        prefs.edit().remove(KEY_CART).apply();
    }

    public List<CarModel> getCart() {
        String json = prefs.getString(KEY_CART, null);
        Type type = new TypeToken<ArrayList<CarModel>>() {}.getType();
        return JsonSafe.parseOr(gson, json, type, new ArrayList<>());
    }

    private void saveCart(List<CarModel> list) {
        prefs.edit().putString(KEY_CART, gson.toJson(list)).apply();
    }

    // --- ORDERS ---
    public void addOrder(OrderModel order) {
        List<OrderModel> list = getOrders();
        list.add(0, order);
        saveOrders(list);
    }

    public List<OrderModel> getOrders() {
        String json = prefs.getString(KEY_ORDERS, null);
        Type type = new TypeToken<ArrayList<OrderModel>>() {}.getType();
        return JsonSafe.parseOr(gson, json, type, new ArrayList<>());
    }

    private void saveOrders(List<OrderModel> list) {
        prefs.edit().putString(KEY_ORDERS, gson.toJson(list)).apply();
    }

    /** Lưu lại toàn bộ danh sách đơn (dùng sau khi đồng bộ trạng thái từ server). */
    public void updateOrders(List<OrderModel> list) {
        saveOrders(list);
    }

    // --- TEST DRIVE (đăng ký lái thử) ---
    public void addTestDrive(TestDriveBooking booking) {
        List<TestDriveBooking> list = getTestDrives();
        list.add(0, booking);
        prefs.edit().putString(KEY_TEST_DRIVES, gson.toJson(list)).apply();
    }

    public List<TestDriveBooking> getTestDrives() {
        String json = prefs.getString(KEY_TEST_DRIVES, null);
        Type type = new TypeToken<ArrayList<TestDriveBooking>>() {}.getType();
        return JsonSafe.parseOr(gson, json, type, new ArrayList<>());
    }

    // --- REVIEWS ---
    public void addReview(int carId, ReviewModel review) {
        Map<Integer, List<ReviewModel>> reviewsMap = getAllReviews();
        List<ReviewModel> carReviews = reviewsMap.get(carId);
        if (carReviews == null) carReviews = new ArrayList<>();
        carReviews.add(0, review);
        reviewsMap.put(carId, carReviews);
        saveAllReviews(reviewsMap);
    }

    /** Lưu lại toàn bộ danh sách review của 1 xe (gồm cả các câu trả lời). */
    public void saveReviewsForCar(int carId, List<ReviewModel> list) {
        Map<Integer, List<ReviewModel>> reviewsMap = getAllReviews();
        reviewsMap.put(carId, list);
        saveAllReviews(reviewsMap);
    }

    public List<ReviewModel> getReviewsForCar(int carId) {
        Map<Integer, List<ReviewModel>> reviewsMap = getAllReviews();
        return reviewsMap.getOrDefault(carId, new ArrayList<>());
    }

    private Map<Integer, List<ReviewModel>> getAllReviews() {
        String json = prefs.getString(KEY_REVIEWS, null);
        Type type = new TypeToken<Map<Integer, List<ReviewModel>>>() {}.getType();
        return JsonSafe.parseOr(gson, json, type, new HashMap<>());
    }

    private void saveAllReviews(Map<Integer, List<ReviewModel>> reviewsMap) {
        prefs.edit().putString(KEY_REVIEWS, gson.toJson(reviewsMap)).apply();
    }
}