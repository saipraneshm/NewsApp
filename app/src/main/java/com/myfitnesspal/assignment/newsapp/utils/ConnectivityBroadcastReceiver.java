package com.myfitnesspal.assignment.newsapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.myfitnesspal.assignment.newsapp.R;

/**
 * Created by saip92 on 8/3/2017.
 */

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    View mRootView;
    View mLastRootView;

    public ConnectivityBroadcastReceiver(View root){
        mRootView = root;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE );
        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        boolean isConnected = isNetworkAvailable &&
                connectivityManager.getActiveNetworkInfo().isConnected();
        if(!isConnected){
            final Snackbar snackbar = Snackbar.make(mRootView, R.string.no_internet_connection,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setActionTextColor(context.getResources().getColor(R.color.colorPrimary))
                    .setAction(R.string.dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    }).show();

            if(!isNewsFeedDetailFragment()){
                mRootView.findViewById(R.id.news_feed_fragment_recycler_view).setVisibility(View.GONE);
            }else{
                mRootView.findViewById(R.id.web_view_progress_bar).setVisibility(View.GONE);
                mRootView.findViewById(R.id.web_view).setVisibility(View.GONE);
            }
            mRootView.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
            mLastRootView = mRootView;

        }else{

            if(mLastRootView == mRootView){
                mRootView.findViewById(R.id.error_message).setVisibility(View.GONE);
                final Snackbar snackbar = Snackbar.make(mRootView, R.string.internet_connection_available,
                        Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(context.getResources().getColor(R.color.colorPrimary));
                if(isNewsFeedDetailFragment()){
                    snackbar.setAction(R.string.continue_message, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mRootView.findViewById(R.id.web_view).setVisibility(View.VISIBLE);
                                    //mRootView.findViewById(R.id.web_view_progress_bar).setVisibility(View.VISIBLE);
                                    snackbar.dismiss();
                                }
                            }).show();
                }else{
                    snackbar.setAction(R.string.continue_message, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mRootView.findViewById(R.id.news_feed_fragment_recycler_view).setVisibility(View.VISIBLE);
                                    snackbar.dismiss();
                                }
                            }).show();
                }
            }

        }

    }

    private boolean isNewsFeedDetailFragment(){
        return mRootView.findViewById(R.id.web_view) != null;
    }
}
