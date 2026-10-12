package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("role")
    private String role;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserDto user;

    public AuthResponse() {}

    public AuthResponse(String token, boolean verified, String role, String message, UserDto user) {
        this.token = token;
        this.verified = verified;
        this.role = role;
        this.message = message;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public UserDto getUser() {
        return user;
    }
}
