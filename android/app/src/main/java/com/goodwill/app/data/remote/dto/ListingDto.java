package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ListingDto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("sellerId")
    private String sellerId;

    @SerializedName("sellerName")
    private String sellerName;

    @SerializedName("hostelBlock")
    private String hostelBlock;

    @SerializedName("roomNo")
    private String roomNo;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("type")
    private String type; // DONATE or SELL

    @SerializedName("price")
    private double price;

    @SerializedName("condition")
    private String condition;

    @SerializedName("photos")
    private List<String> photos;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    public ListingDto() {}

    public ListingDto(String id, String sellerId, String sellerName, String hostelBlock,
                      String roomNo, String title, String description, String category,
                      String type, double price, String condition, List<String> photos,
                      String status, String createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.hostelBlock = hostelBlock;
        this.roomNo = roomNo;
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.price = price;
        this.condition = condition;
        this.photos = photos;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getHostelBlock() { return hostelBlock; }
    public String getRoomNo() { return roomNo; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public List<String> getPhotos() { return photos; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public boolean isDonation() {
        return "DONATE".equalsIgnoreCase(type);
    }
}
