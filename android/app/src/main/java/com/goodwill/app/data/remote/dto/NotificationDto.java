package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NotificationDto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type; // REQUEST_RECEIVED, REQUEST_ACCEPTED, REQUEST_DECLINED

    @SerializedName("targetId")
    private String targetId;

    @SerializedName("isRead")
    private boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    public NotificationDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRequestReceived() {
        return "REQUEST_RECEIVED".equalsIgnoreCase(type);
    }

    public boolean isRequestAccepted() {
        return "REQUEST_ACCEPTED".equalsIgnoreCase(type);
    }

    public boolean isRequestDeclined() {
        return "REQUEST_DECLINED".equalsIgnoreCase(type);
    }
}
