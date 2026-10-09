package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class WishlistItemDto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("requesterId")
    private String requesterId;

    @SerializedName("requesterName")
    private String requesterName;

    @SerializedName("requesterHostel")
    private String requesterHostel;

    @SerializedName("requesterRoom")
    private String requesterRoom;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("isDonationPreferred")
    private boolean isDonationPreferred;

    @SerializedName("maxBudget")
    private double maxBudget;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public WishlistItemDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterHostel() {
        return requesterHostel;
    }

    public void setRequesterHostel(String requesterHostel) {
        this.requesterHostel = requesterHostel;
    }

    public String getRequesterRoom() {
        return requesterRoom;
    }

    public void setRequesterRoom(String requesterRoom) {
        this.requesterRoom = requesterRoom;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDonationPreferred() {
        return isDonationPreferred;
    }

    public void setDonationPreferred(boolean donationPreferred) {
        isDonationPreferred = donationPreferred;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
