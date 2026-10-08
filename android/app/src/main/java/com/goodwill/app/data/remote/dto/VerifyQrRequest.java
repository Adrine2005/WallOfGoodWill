package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyQrRequest {

    @SerializedName("code")
    private String code;

    public VerifyQrRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
