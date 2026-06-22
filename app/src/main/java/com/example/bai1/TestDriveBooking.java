package com.example.bai1;

/** Một lượt đăng ký lái thử xe. POJO thuần để Gson serialize/deserialize. */
public class TestDriveBooking {
    private int carId;
    private String carName;
    private String customerName;
    private String phone;
    private String date;   // ngày mong muốn, dd/MM/yyyy
    private String time;   // giờ mong muốn, HH:mm
    private String note;
    private String createdAt;

    public TestDriveBooking() {
    }

    public TestDriveBooking(int carId, String carName, String customerName, String phone,
                            String date, String time, String note, String createdAt) {
        this.carId = carId;
        this.carName = carName;
        this.customerName = customerName;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getCarId() { return carId; }
    public String getCarName() { return carName; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
}
