package com.myfitnesspal.assignment.newsapp.models;

/**
 * Created by saip92 on 8/2/2017.
 */

public class NewsStories {

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


}
