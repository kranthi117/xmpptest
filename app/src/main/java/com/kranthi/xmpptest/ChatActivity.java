package com.kranthi.xmpptest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import org.jivesoftware.smack.packet.Message;

import co.devcenter.android.ChatView;
import co.devcenter.android.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private ChatView chatView;
    ChatService mService;
    public boolean mBound = false;
    public boolean isVisible = false;
    String to;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
            mService = binder.getServiceInstance(); //Get instance of your service!
            mService.startChat(to, ChatActivity.this); //Activity register in the service as client for callabcks!
            mBound = true;
            // TODO load the previous chat history from the database
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        to = intent.getStringExtra("to");

        chatView = (ChatView) findViewById(R.id.chat_view);

        chatView.setOnSentMessageListener(new SendMessageListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent chatIntent = new Intent(this, ChatService.class);
        bindService(chatIntent, mConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    public void receiveMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage(message.getBody(), System.currentTimeMillis(), ChatMessage.Type.RECEIVED);
        chatView.addMessage(chatMessage);
    }

    private class SendMessageListener implements ChatView.OnSentMessageListener {
        @Override
        public boolean sendMessage(ChatMessage chatMessage) {
            if (mBound) {
                mService.sendMessage(to, chatMessage.getMessage());
                return true;
            }
            return false;
        }
    }
}