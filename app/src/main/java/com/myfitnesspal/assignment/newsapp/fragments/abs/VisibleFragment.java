package com.myfitnesspal.assignment.newsapp.fragments.abs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.myfitnesspal.assignment.newsapp.utils.ConnectivityBroadcastReceiver;


public abstract class VisibleFragment extends Fragment {

    private BroadcastReceiver connectivityReceiver = null;
    @Override
    public void onStart() {
        super.onStart();
        connectivityReceiver = createConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(connectivityReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(connectivityReceiver);
    }

    protected abstract BroadcastReceiver createConnectivityBroadcastReceiver();
}
