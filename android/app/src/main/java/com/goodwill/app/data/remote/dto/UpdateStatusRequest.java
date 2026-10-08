package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateStatusRequest {

    @SerializedName("status")
    private String status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
