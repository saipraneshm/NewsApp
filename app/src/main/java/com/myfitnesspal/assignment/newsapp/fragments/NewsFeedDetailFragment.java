package com.myfitnesspal.assignment.newsapp.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.activities.NewsFeedDetailActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedDetailFragment extends Fragment {


    private static final String ARG_URI = "photo_page_url";
    private Uri mUri;

    @BindView(R.id.web_view)
    WebView mWebView;

    @BindView(R.id.web_view_progress_bar)
    ProgressBar mProgressBar;

    public NewsFeedDetailFragment() {
        // Required empty public constructor
    }

    public boolean onBackPressed(){
       if(mWebView.canGoBack())
           mWebView.goBack();
        return mWebView.canGoBack();
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
        setHasOptionsMenu(true);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed_detail, container, false);

        ButterKnife.bind(this, view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                    if(newProgress == 100){
                        mProgressBar.setVisibility(View.GONE);
                    }else{
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressBar.setProgress(newProgress);
                    }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if(activity != null &&
                        activity instanceof NewsFeedDetailActivity
                        && activity.getSupportActionBar() != null)
                    activity.getSupportActionBar().setSubtitle(title);
            }
        });

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(mUri.toString());
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_news_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_share){
            Intent shareIntent = createShareNewsIntent();
            startActivity(shareIntent);
            return true;
        }else if(id == android.R.id.home){
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareNewsIntent(){
        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("Read the following news article on NYTimes: " + mUri)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return shareIntent;
    }


}
