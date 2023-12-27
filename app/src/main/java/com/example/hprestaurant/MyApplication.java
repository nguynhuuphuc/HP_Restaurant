package com.example.hprestaurant;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;

import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Utils.MyWebsocketClient;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "CHANNEL 1";
    private Customer customer;
    private MyWebsocketClient websocketClient;


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

    }

    private void createNotificationChannel() {
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/"+ R.raw.sound_notification);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100,100,200,340});
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(sound,audioAttributes);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public MyWebsocketClient getWebsocketClient() {
        return websocketClient;
    }

    public void setWebsocketClient(MyWebsocketClient websocketClient) {
        this.websocketClient = websocketClient;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
