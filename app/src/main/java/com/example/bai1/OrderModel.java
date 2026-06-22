package com.example.bai1;

import java.util.List;

public class OrderModel {
    private String orderId;
    private String date;
    private String totalAmount;
    private List<CarModel> items;
    private String status; // e.g., "Processing", "Delivered"
    private String paymentMethod;

    public OrderModel(String orderId, String date, String totalAmount, List<CarModel> items, String status) {
        this(orderId, date, totalAmount, items, status, "");
    }

    public OrderModel(String orderId, String date, String totalAmount, List<CarModel> items,
                      String status, String paymentMethod) {
        this.orderId = orderId;
        this.date = date;
        this.totalAmount = totalAmount;
        this.items = items;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() { return orderId; }
    public String getDate() { return date; }
    public String getTotalAmount() { return totalAmount; }
    public List<CarModel> getItems() { return items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
}