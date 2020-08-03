package com.uiza.sdk.observers;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.uiza.sdk.events.ConnectEvent;
import com.uiza.sdk.utils.ConnectivityUtils;

import org.greenrobot.eventbus.EventBus;

public class ConnectivityReceiver extends BroadcastReceiver {

    private ConnectEvent connectEvent;

    // default constructor
    public ConnectivityReceiver() {
        connectEvent = new ConnectEvent();
    }

    private static boolean isConnectedOrConnecting(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isConnectedOrConnecting(context);
        boolean connectedMobile = false;
        boolean connectedWifi = false;
        boolean connectedFast = false;
        if (connected) {
            if (ConnectivityUtils.isConnectedWifi(context)) {
                connectedWifi = true;
                connectedFast = true;
            } else if (ConnectivityUtils.isConnectedMobile(context)) {
                connectedMobile = true;
                connectedFast = ConnectivityUtils.isConnectedFast(context);
            }
        }
        ConnectEvent event = new ConnectEvent(connected, connectedWifi, connectedMobile, connectedFast);
        if(!event.equals(connectEvent)){
            connectEvent = event;
            EventBus.getDefault().post(event);
        }
    }
}