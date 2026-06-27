package com.example.bai1;

import java.util.List;

public class CarModel {
    private int id;
    private String name;
    private String brand;
    private String price;
    private String engine;
    private String power;
    private String acceleration;
    private String drivetrain;
    private String description;
    private String imageUrl; // URL hoặc tên resource
    private String region;   // "Nội địa" hoặc "Nước ngoài"
    private int quantity;    // tổng tồn kho
    private int cartQty;     // số lượng đang chọn trong giỏ (KHÁC với tồn kho)
    private List<ColorVariant> colors; // các biến thể màu (nhiều ảnh)

    public CarModel() {
    }

    public CarModel(int id, String name, String brand, String price, String engine, String power,
                    String acceleration, String drivetrain, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.engine = engine;
        this.power = power;
        this.acceleration = acceleration;
        this.drivetrain = drivetrain;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getPrice() { return price; }
    public String getEngine() { return engine; }
    public String getPower() { return power; }
    public String getAcceleration() { return acceleration; }
    public String getDrivetrain() { return drivetrain; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getRegion() { return region; }
    public int getQuantity() { return quantity; }
    public List<ColorVariant> getColors() { return colors; }

    /** Số lượng trong giỏ; coi 0 (chưa set) là 1. */
    public int getCartQty() { return cartQty < 1 ? 1 : cartQty; }
    public void setCartQty(int q) { this.cartQty = Math.max(1, q); }
}
