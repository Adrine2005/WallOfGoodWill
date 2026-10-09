package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ReportListingRequest implements Serializable {

    @SerializedName("reason")
    private String reason;

    @SerializedName("comment")
    private String comment;

    public ReportListingRequest() {}

    public ReportListingRequest(String reason, String comment) {
        this.reason = reason;
        this.comment = comment;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
