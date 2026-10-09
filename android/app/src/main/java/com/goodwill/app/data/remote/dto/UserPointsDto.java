package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPointsDto implements Serializable {

    @SerializedName("userId")
    private String userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("hostelBlock")
    private String hostelBlock;

    @SerializedName("points")
    private int points;

    @SerializedName("donationCount")
    private int donationCount;

    @SerializedName("tierName")
    private String tierName;

    @SerializedName("nextTierPoints")
    private int nextTierPoints;

    @SerializedName("progressPercent")
    private int progressPercent;

    @SerializedName("badges")
    private List<BadgeDto> badges = new ArrayList<>();

    @SerializedName("updatedAt")
    private String updatedAt;

    public UserPointsDto() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHostelBlock() {
        return hostelBlock;
    }

    public void setHostelBlock(String hostelBlock) {
        this.hostelBlock = hostelBlock;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getDonationCount() {
        return donationCount;
    }

    public void setDonationCount(int donationCount) {
        this.donationCount = donationCount;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public int getNextTierPoints() {
        return nextTierPoints;
    }

    public void setNextTierPoints(int nextTierPoints) {
        this.nextTierPoints = nextTierPoints;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public List<BadgeDto> getBadges() {
        return badges != null ? badges : new ArrayList<>();
    }

    public void setBadges(List<BadgeDto> badges) {
        this.badges = badges;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
