package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EditListingRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("price")
    private double price;

    @SerializedName("condition")
    private String condition;

    @SerializedName("photos")
    private List<String> photos;

    public EditListingRequest() {
    }

    public EditListingRequest(String title, String description, String category, double price, String condition) {
        this(title, description, category, price, condition, null);
    }

    public EditListingRequest(String title, String description, String category, double price, String condition, List<String> photos) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.condition = condition;
        this.photos = photos;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
}
