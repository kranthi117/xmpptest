package com.kranthi.xmpptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    ContactsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // TODO start service if not connected
            Log.e(TAG, "onCreate: starting service ");
            Intent serviceIntent = new Intent(this, ChatService.class);
            serviceIntent.putExtra("host", "13.78.120.174");
            serviceIntent.putExtra("port", 443);
            serviceIntent.putExtra("user", "sai@13.78.120.174");
            serviceIntent.putExtra("password", "sai");
            startService(serviceIntent);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.contacts_list);
        mAdapter = new ContactsAdapter();
        rv.setAdapter(mAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: got broadcast updating data");
            mAdapter.initData();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Constants.Actions.AUTENTICATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }
}

