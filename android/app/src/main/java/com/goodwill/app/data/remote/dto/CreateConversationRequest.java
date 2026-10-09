package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateConversationRequest {

    @SerializedName("listingId")
    private String listingId;

    public CreateConversationRequest() {}

    public CreateConversationRequest(String listingId) {
        this.listingId = listingId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
}
