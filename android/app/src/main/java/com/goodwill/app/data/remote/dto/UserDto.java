package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UserDto {

    @SerializedName("uid")
    private String uid;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("hostelBlock")
    private String hostelBlock;

    @SerializedName("roomNo")
    private String roomNo;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("verifiedViaCode")
    private String verifiedViaCode;

    public UserDto() {}

    public UserDto(String uid, String name, String email, String hostelBlock, String roomNo, String phone, String role, boolean verified, String verifiedViaCode) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.hostelBlock = hostelBlock;
        this.roomNo = roomNo;
        this.phone = phone;
        this.role = role;
        this.verified = verified;
        this.verifiedViaCode = verifiedViaCode;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getHostelBlock() {
        return hostelBlock;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getVerifiedViaCode() {
        return verifiedViaCode;
    }
}
