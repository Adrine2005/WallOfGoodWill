package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateWishlistRequest {

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

    public CreateWishlistRequest() {}

    public CreateWishlistRequest(String title, String description, String category, boolean isDonationPreferred, double maxBudget) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.isDonationPreferred = isDonationPreferred;
        this.maxBudget = maxBudget;
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
}
