package com.myfitnesspal.assignment.newsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsStory implements Parcelable {

    private String mHeadline;
    private String mPubDate;
    private String mWebUrl;
    private String mThumbnailUrl;
    private String mByline;
    private String mId;
    private int mHits;

    public String getHeadline() {
        return mHeadline;
    }

    public void setHeadline(String headline) {
        mHeadline = headline;
    }

    public String getPubDate() {
        return mPubDate;
    }

    public void setPubDate(String pubDate) {
        mPubDate = pubDate;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public void setWebUrl(String webUrl) {
        mWebUrl = webUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public String getByline() {
        return mByline;
    }

    public void setByline(String byline) {
        mByline = byline;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }


    public int getHits() {
        return mHits;
    }

    public void setHits(int hits) {
        mHits = hits;
    }

    @Override
    public String toString() {
        return "Headline " +
                mHeadline +
                ", mPubDate " +
                mPubDate +
                ", mPubDate " + mPubDate +
                ", mWebUrl " + mWebUrl +
                ", mThumbnailUrl " + mThumbnailUrl +
                ", mByline " + mByline;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mHeadline);
        dest.writeString(this.mPubDate);
        dest.writeString(this.mWebUrl);
        dest.writeString(this.mThumbnailUrl);
        dest.writeString(this.mByline);
        dest.writeString(this.mId);
        dest.writeInt(this.mHits);
    }

    public NewsStory() {
    }

    protected NewsStory(Parcel in) {
        this.mHeadline = in.readString();
        this.mPubDate = in.readString();
        this.mWebUrl = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mByline = in.readString();
        this.mId = in.readString();
        this.mHits = in.readInt();
    }

    public static final Parcelable.Creator<NewsStory> CREATOR = new Parcelable.Creator<NewsStory>() {
        @Override
        public NewsStory createFromParcel(Parcel source) {
            return new NewsStory(source);
        }

        @Override
        public NewsStory[] newArray(int size) {
            return new NewsStory[size];
        }
    };
}
