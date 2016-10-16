package com.kranthi.xmpptest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ChatService extends Service implements ConnectionListener {
    private static final String TAG = ChatService.class.getSimpleName();
    HashMap<String, WeakReference<ChatActivity>> activities = new HashMap<>();
    HashMap<String, Chat> chats = new HashMap<>();
    private LocalBinder mBinder = new LocalBinder();
    private XMPPTCPConnection mConnection;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: started server" + intent);
        if (intent != null) {
            Log.e(TAG, "onStartCommand: attempting connection");
            String host = intent.getStringExtra("host");
            int port = intent.getIntExtra("port", 5222);
            final String user = intent.getStringExtra("user");
            final String password = intent.getStringExtra("password");

            /* IMPORTANT If a connection is active, dont recreate the connection. It'll close the existing chats */
            if (mConnection == null) {
                // create connection and save
                XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
                config.setUsernameAndPassword(user, password);
                config.setServiceName(host);
                config.setHost(host);
                config.setPort(port);
                config.setDebuggerEnabled(false);
                SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.core.SCRAMSHA1Mechanism");
                SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism");
                SASLAuthentication.registerSASLMechanism(new SASLPlainMechanism());
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                mConnection = new XMPPTCPConnection(config.build());
                mConnection.setUseStreamManagement(true);
                mConnection.addConnectionListener(this);
                ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
                Log.e(TAG, "onStartCommand: built connection obj");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e(TAG, "run: attempting login");
                        Log.e(TAG, "run: " + user );
                        Log.e(TAG, "run: " + password );
                        mConnection.connect().login();
                    } catch (SmackException | IOException | XMPPException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: ", e);
                    }
                }
            }).start();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startChat(String jid, ChatActivity chatActivity) {
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(jid, new MessageListener());
        activities.put(jid, new WeakReference<>(chatActivity));
        chats.put(jid, chat);
    }

    public void sendMessage(String jid, String message) {
        // TODO save in the database
        Chat chat = chats.get(jid);
        try {
            chat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.e(TAG, "authenticated: sending broadcast");
        Intent intent = new Intent(Constants.Actions.AUTENTICATED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }

    public class MessageListener implements ChatMessageListener {

        @Override
        public void processMessage(Chat chat, final Message message) {
            String jid = chat.getParticipant();
            Log.e(TAG, "processMessage: " + message.getBody());
            // TODO save the message in database
            WeakReference<ChatActivity> activityWeakReference = activities.get(jid);
            if (activityWeakReference != null) {
                final ChatActivity activity = activityWeakReference.get();
                if (activity != null && activity.isVisible) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            activity.receiveMessage(message);
                        }
                    });
                } else {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(ChatService.this)
                                    .setSmallIcon(R.drawable.ic_send_white_24dp)
                                    .setContentTitle("My notification")
                                    .setContentText(message.getBody());
                    NotificationManager notificationService = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationService.notify(001, mBuilder.build());
                }
            } else {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(ChatService.this)
                                .setSmallIcon(R.drawable.ic_send_white_24dp)
                                .setContentTitle("My notification")
                                .setContentText(message.getBody());
                NotificationManager notificationService = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationService.notify(001, mBuilder.build());
            }
        }
    }

    public class LocalBinder extends Binder {
        public ChatService getServiceInstance() {
            return ChatService.this;
        }
    }
}