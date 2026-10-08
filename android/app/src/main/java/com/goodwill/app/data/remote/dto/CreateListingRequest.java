package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreateListingRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("type")
    private String type;

    @SerializedName("price")
    private double price;

    @SerializedName("condition")
    private String condition;

    @SerializedName("photos")
    private List<String> photos;

    public CreateListingRequest(String title, String description, String category,
                                String type, double price, String condition, List<String> photos) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.price = price;
        this.condition = condition;
        this.photos = photos;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public List<String> getPhotos() { return photos; }
}
