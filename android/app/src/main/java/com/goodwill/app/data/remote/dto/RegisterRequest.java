package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

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

    @SerializedName("inviteCode")
    private String inviteCode;

    public RegisterRequest(String uid, String name, String email, String hostelBlock, String roomNo, String phone, String inviteCode) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.hostelBlock = hostelBlock;
        this.roomNo = roomNo;
        this.phone = phone;
        this.inviteCode = inviteCode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getInviteCode() {
        return inviteCode;
    }
}
