package com.example.bai1;

/** Một biến thể màu của xe: tên màu, mã màu hiển thị (swatch) và ảnh tương ứng. */
public class ColorVariant {
    private String name;
    private String hex;   // ví dụ "#0A2472"
    private String image; // tên drawable, ví dụ "car_5"
    private int quantity; // tồn kho của màu này

    public ColorVariant() {}

    public ColorVariant(String name, String hex, String image) {
        this.name = name;
        this.hex = hex;
        this.image = image;
    }

    public String getName() { return name; }
    public String getHex() { return hex; }
    public String getImage() { return image; }
    public int getQuantity() { return quantity; }
}
