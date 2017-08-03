package com.myfitnesspal.assignment.newsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saip92 on 8/2/2017.
 */

public class NewsStories implements Parcelable {

    String mHeadline;
    String mPubDate;
    String mWebUrl;
    String mThumbnailUrl;
    String mByline;
    String mId;
    boolean mIsThumbnailAvailable;
    int mHits;

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

    public boolean isThumbnailAvailable() {
        return mIsThumbnailAvailable;
    }

    public void setThumbnailAvailable(boolean thumbnailAvailable) {
        mIsThumbnailAvailable = thumbnailAvailable;
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
        dest.writeByte(this.mIsThumbnailAvailable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mHits);
    }

    public NewsStories() {
    }

    protected NewsStories(Parcel in) {
        this.mHeadline = in.readString();
        this.mPubDate = in.readString();
        this.mWebUrl = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mByline = in.readString();
        this.mId = in.readString();
        this.mIsThumbnailAvailable = in.readByte() != 0;
        this.mHits = in.readInt();
    }

    public static final Parcelable.Creator<NewsStories> CREATOR = new Parcelable.Creator<NewsStories>() {
        @Override
        public NewsStories createFromParcel(Parcel source) {
            return new NewsStories(source);
        }

        @Override
        public NewsStories[] newArray(int size) {
            return new NewsStories[size];
        }
    };
}
