package com.kranthi.xmpptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kranthi on 17/10/16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, ChatService.class);
        serviceIntent.putExtra("host", "13.78.120.174");
        serviceIntent.putExtra("port", 443);
        serviceIntent.putExtra("user", "sai@13.78.120.174");
        serviceIntent.putExtra("password", "sai");
        context.startService(serviceIntent);
    }
}
