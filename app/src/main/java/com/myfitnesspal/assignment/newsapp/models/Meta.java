package com.myfitnesspal.assignment.newsapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by saip92 on 8/2/2017.
 */

public class Meta {
    @SerializedName("hits")
    @Expose
    private Integer hits;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("offset")
    @Expose
    private Integer offset;

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
