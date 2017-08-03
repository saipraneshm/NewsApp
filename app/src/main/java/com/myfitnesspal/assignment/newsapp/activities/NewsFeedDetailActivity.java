package com.myfitnesspal.assignment.newsapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.myfitnesspal.assignment.newsapp.abs.SingleFragmentActivity;
import com.myfitnesspal.assignment.newsapp.fragments.NewsFeedDetailFragment;

public class NewsFeedDetailActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, Uri uri){
        Intent intent = new Intent(context, NewsFeedDetailActivity.class);
        intent.setData(uri);
        return intent;
    }

    NewsFeedDetailFragment mNewsFeedDetailFragment;

    @Override
    protected Fragment createFragment() {
       mNewsFeedDetailFragment = NewsFeedDetailFragment.newInstance(getIntent().getData());
        return mNewsFeedDetailFragment;
    }

    @Override
    public void onBackPressed() {
        if(mNewsFeedDetailFragment.onBackPressed()){
            return;
        }
        super.onBackPressed();

    }
}
