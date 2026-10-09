package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DonationRequestDto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("listingId")
    private String listingId;

    @SerializedName("listingTitle")
    private String listingTitle;

    @SerializedName("donorId")
    private String donorId;

    @SerializedName("donorName")
    private String donorName;

    @SerializedName("requesterId")
    private String requesterId;

    @SerializedName("requesterName")
    private String requesterName;

    @SerializedName("requesterHostel")
    private String requesterHostel;

    @SerializedName("requesterRoom")
    private String requesterRoom;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status; // PENDING, ACCEPTED, DECLINED, CANCELLED

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public DonationRequestDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equalsIgnoreCase(status);
    }

    public boolean isDeclined() {
        return "DECLINED".equalsIgnoreCase(status);
    }
}
