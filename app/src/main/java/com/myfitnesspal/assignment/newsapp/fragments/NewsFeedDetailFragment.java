package com.myfitnesspal.assignment.newsapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myfitnesspal.assignment.newsapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedDetailFragment extends Fragment {


    public NewsFeedDetailFragment() {
        // Required empty public constructor
    }


    public static NewsFeedDetailFragment newInstance(){
        return new NewsFeedDetailFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_feed_detail, container, false);
    }

}
