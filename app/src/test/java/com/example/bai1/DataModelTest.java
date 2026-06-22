package com.example.bai1;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** Kiểm tra round-trip Gson cho các model dùng để lưu xuống SharedPreferences. */
public class DataModelTest {

    private final Gson gson = new Gson();

    @Test
    public void carRoundTrip() {
        CarModel car = new CarModel(7, "Supra", "Toyota", "$80k", "2JZ", "320hp", "4.6s", "RWD", "legend", "car_7");
        CarModel out = gson.fromJson(gson.toJson(car), CarModel.class);
        assertEquals(7, out.getId());
        assertEquals("Supra", out.getName());
        assertEquals("Toyota", out.getBrand());
        assertEquals("$80k", out.getPrice());
    }

    @Test
    public void orderRoundTrip() {
        List<CarModel> items = new ArrayList<>();
        items.add(new CarModel(1, "RX-7", "Mazda", "$60k", "13B", "276hp", "5.3s", "RWD", "rotary", "car_1"));
        OrderModel order = new OrderModel("12345", "01/01/2026 10:00", "$60k", items, "Đang xử lý", "COD");

        OrderModel out = gson.fromJson(gson.toJson(order), OrderModel.class);

        assertEquals("12345", out.getOrderId());
        assertEquals("Đang xử lý", out.getStatus());
        assertEquals("COD", out.getPaymentMethod());
        assertEquals(1, out.getItems().size());
        assertEquals("RX-7", out.getItems().get(0).getName());
    }

    @Test
    public void testDriveRoundTrip() {
        TestDriveBooking b = new TestDriveBooking(3, "Civic", "Minh", "0123456789",
                "20/06/2026", "14:30", "buổi chiều", "19/06/2026 09:00");

        TestDriveBooking out = gson.fromJson(gson.toJson(b), TestDriveBooking.class);

        assertEquals(3, out.getCarId());
        assertEquals("Civic", out.getCarName());
        assertEquals("Minh", out.getCustomerName());
        assertEquals("0123456789", out.getPhone());
        assertEquals("20/06/2026", out.getDate());
        assertEquals("14:30", out.getTime());
        assertEquals("buổi chiều", out.getNote());
    }
}
