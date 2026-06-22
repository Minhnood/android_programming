package com.example.bai1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonSafeTest {

    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<ArrayList<CarModel>>() {}.getType();

    @Test
    public void parsesValidJson() {
        List<CarModel> seed = new ArrayList<>();
        seed.add(new CarModel(1, "GT-R", "Nissan", "$100k", "V6", "565hp", "2.7s", "AWD", "desc", "car_1"));
        String json = gson.toJson(seed);

        List<CarModel> out = JsonSafe.parseOr(gson, json, listType, new ArrayList<>());

        assertEquals(1, out.size());
        assertEquals("GT-R", out.get(0).getName());
    }

    @Test
    public void nullReturnsFallback() {
        List<CarModel> out = JsonSafe.parseOr(gson, null, listType, new ArrayList<>());
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    public void emptyReturnsFallback() {
        assertTrue(JsonSafe.parseOr(gson, "", listType, new ArrayList<CarModel>()).isEmpty());
        assertTrue(JsonSafe.parseOr(gson, "   ", listType, new ArrayList<CarModel>()).isEmpty());
    }

    @Test
    public void malformedReturnsFallbackInsteadOfCrashing() {
        // JSON hỏng hoặc sai kiểu -> phải trả fallback, KHÔNG được ném ngoại lệ
        assertTrue(JsonSafe.parseOr(gson, "{not valid json", listType, new ArrayList<CarModel>()).isEmpty());
        assertTrue(JsonSafe.parseOr(gson, "\"a string not a list\"", listType, new ArrayList<CarModel>()).isEmpty());
    }
}
