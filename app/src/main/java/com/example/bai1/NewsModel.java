package com.example.bai1;

public class NewsModel {
    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String imageUrl;

    public NewsModel(String title, String link, String description, String pubDate, String imageUrl) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getDescription() { return description; }
    public String getPubDate() { return pubDate; }
    public String getImageUrl() { return imageUrl; }
}