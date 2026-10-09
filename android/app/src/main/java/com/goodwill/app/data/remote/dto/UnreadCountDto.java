package com.goodwill.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UnreadCountDto implements Serializable {

    @SerializedName("count")
    private int count;

    public UnreadCountDto() {}

    public UnreadCountDto(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
