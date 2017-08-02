package com.myfitnesspal.assignment.newsapp.activities;

import android.support.v4.app.Fragment;

import com.myfitnesspal.assignment.newsapp.abs.SingleFragmentActivity;
import com.myfitnesspal.assignment.newsapp.fragments.NewsFeedFragment;

public class NewsFeedActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return NewsFeedFragment.newInstance();
    }
}
