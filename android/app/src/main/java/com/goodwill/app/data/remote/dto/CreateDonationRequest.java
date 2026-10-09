package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CreateDonationRequest implements Serializable {

    @SerializedName("message")
    private String message;

    public CreateDonationRequest() {}

    public CreateDonationRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
