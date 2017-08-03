package com.myfitnesspal.assignment.newsapp.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.myfitnesspal.assignment.newsapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedDetailFragment extends Fragment {


    private static final String ARG_URI = "photo_page_url";
    private Uri mUri;

    @BindView(R.id.web_view)
    private WebView mWebView;

    public NewsFeedDetailFragment() {
        // Required empty public constructor
    }

    public static NewsFeedDetailFragment newInstance(Uri uri){
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        NewsFeedDetailFragment fragment = new NewsFeedDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed_detail, container, false);

        ButterKnife.bind(this, view);


        // Inflate the layout for this fragment
        return view;
    }

}
