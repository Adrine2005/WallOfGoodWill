package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class InviteDto implements Serializable {

    @SerializedName("code")
    private String code;

    @SerializedName("label")
    private String label;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("maxUses")
    private int maxUses;

    @SerializedName("usedCount")
    private int usedCount;

    @SerializedName("isActive")
    private boolean isActive;

    public InviteDto() {}

    public InviteDto(String code, String label, String createdAt, String expiresAt, int maxUses, int usedCount, boolean isActive) {
        this.code = code;
        this.label = label;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.usedCount = usedCount;
        this.isActive = isActive;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

