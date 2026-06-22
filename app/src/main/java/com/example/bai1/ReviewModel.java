package com.example.bai1;

import java.util.ArrayList;
import java.util.List;

public class ReviewModel {
    private String userName;
    private float rating;
    private String comment;
    private String date;
    private List<ReviewModel> replies; // cấp 2: các câu trả lời

    public ReviewModel(String userName, float rating, String comment, String date) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public String getUserName() { return userName; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDate() { return date; }

    public List<ReviewModel> getReplies() {
        if (replies == null) replies = new ArrayList<>();
        return replies;
    }
}
