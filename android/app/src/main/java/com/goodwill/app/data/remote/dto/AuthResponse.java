package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserDto user;

    public AuthResponse() {}

    public AuthResponse(boolean verified, String message, UserDto user) {
        this.verified = verified;
        this.message = message;
        this.user = user;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getMessage() {
        return message;
    }

    public UserDto getUser() {
        return user;
    }
}
