package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class GenerateQrRequest {

    @SerializedName("label")
    private String label;

    @SerializedName("durationSeconds")
    private long durationSeconds;

    @SerializedName("maxUses")
    private int maxUses;

    public GenerateQrRequest() {}

    public GenerateQrRequest(String label, long durationSeconds, int maxUses) {
        this.label = label;
        this.durationSeconds = durationSeconds;
        this.maxUses = maxUses;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }
}
