package com.myfitnesspal.assignment.newsapp.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.myfitnesspal.assignment.newsapp.R;
import com.myfitnesspal.assignment.newsapp.activities.NewsFeedDetailActivity;
import com.myfitnesspal.assignment.newsapp.fragments.abs.VisibleFragment;
import com.myfitnesspal.assignment.newsapp.utils.AppUtils;
import com.myfitnesspal.assignment.newsapp.utils.ConnectivityBroadcastReceiver;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Fragment responsible to render WebViews to the  user
 */
public class NewsFeedDetailFragment extends VisibleFragment {


    private static final String ARG_URI = "photo_page_url";
    private Uri mUri;
    private String url;

    @BindView(R.id.web_view)
    WebView mWebView;

    @BindView(R.id.web_view_progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.news_detail_fragment_cl)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.error_message)
    FrameLayout mErrorMessageFL;

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
        url = mUri != null ? mUri.toString() : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed_detail, container, false);

        ButterKnife.bind(this, view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new ManagePostWebviewInterface());
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





        if(AppUtils.isNetworkAvailableAndConnected(getActivity())){
            boolean isHttp = URLUtil.isHttpUrl(mUri.toString());
            boolean isHttps = URLUtil.isHttpsUrl(mUri.toString());
            if(isHttp || isHttps)
                mWebView.loadUrl(mUri.toString());
            else{
                Intent i = new Intent(Intent.ACTION_VIEW, mUri);
                startActivity(i);
            }
        }
        else{
            mWebView.setVisibility(View.GONE);
            mErrorMessageFL.setVisibility(View.VISIBLE);
        }

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
          //  Intent shareIntent = createShareNewsIntent();
            startActivity(createShareNewsIntent());
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


    @Override
    protected BroadcastReceiver createConnectivityBroadcastReceiver() {
        return new ConnectivityBroadcastReceiver(mConstraintLayout);
    }

    public class ManagePostWebviewInterface extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        public void onPageFinished(WebView view, String link) {
            mProgressBar.setVisibility(View.GONE);
            url = link; // save final url so user can open in default browser
            super.onPageFinished(view, url);
        }
    }
}
