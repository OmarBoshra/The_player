package com.omar.acer.musicalstructure;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class app extends Application {
    static final String CHANNEL_ID = "servicechannel";

    @Override
    public void onCreate() {
        super.onCreate();

        notificationChannel();
    }

    private void notificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,"mainservicechannel",
                    NotificationManager.IMPORTANCE_DEFAULT);


            NotificationManager manager =getSystemService(NotificationManager.class);

            manager.createNotificationChannel(serviceChannel);

        }

    }
}
