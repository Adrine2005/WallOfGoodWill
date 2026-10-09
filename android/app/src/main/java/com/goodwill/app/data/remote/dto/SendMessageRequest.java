package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {

    @SerializedName("text")
    private String text;

    public SendMessageRequest() {}

    public SendMessageRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
