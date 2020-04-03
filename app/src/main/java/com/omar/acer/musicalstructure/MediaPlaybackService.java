package com.omar.acer.musicalstructure;

import android.app.Notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;

import android.os.IBinder;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.List;


import static com.omar.acer.musicalstructure.app.CHANNEL_ID;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String MPS_MESSAGE = "com.example.acer.musicalstructure.MediaPlaynackService.MESSAGE";
    public static final String MPS_RESULT = "com.example.acer.musicalstructure.MediaPlaynackService.RESULT";
    public static final String MPS_COMPLETED = "com.example.acer.musicalstructure.MediaPlaynackService.COMPLETED";


    MediaPlaybackService.IDBinder idBinder = new MediaPlaybackService.IDBinder();


    boolean completestarted;
    MediaPlayer mMediaPlayer;
    Uri file;
    int position = -1;
    boolean didStop;
    boolean didStart;
    LocalBroadcastManager broadcastManager;

    boolean seekBarTouch;

    Runnable sendUpdates = new Runnable() {
        @Override
        public void run() {
            while (MediaPlaybackService.this.mMediaPlayer != null) {
                MediaPlaybackService.this.sendElapsedTime();
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private List<Uri> playingmsic;


    public void getTouchStatus(final boolean seekBarTouch) {
        this.seekBarTouch = seekBarTouch;
    }

    public void setUris(final List<Uri> playingmsic) {
        this.playingmsic = playingmsic;
    }


    @Override
    public void onCreate() {

        this.startServiceWithNotification();

        this.broadcastManager = LocalBroadcastManager.getInstance(this);
            super.onCreate();

    }


    @Override
    public void onDestroy() {


        super.onDestroy();
    }


    @Override
    public IBinder onBind(final Intent intent) {

        return this.idBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        // Si le service est débinder, arrêter la lecture
        return super.onUnbind(intent);
    }


    public void setPosition(final int position) {
        this.position = position;
    }

    public void init(final Uri file) {
        this.file = file;
        stop();
        // Si un titre est déjà en train de jouer, l'arrêter
        // Initialisation du lecteur
        try {
           mMediaPlayer = new MediaPlayer();
           mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
           mMediaPlayer.setDataSource(getApplicationContext(), file);
           mMediaPlayer.setOnPreparedListener(this);
           mMediaPlayer.setOnCompletionListener(this);
           mMediaPlayer.prepareAsync(); // prepare async to not block main thread

        } catch (final IOException e) {
            e.printStackTrace();
            stop();
        }
    }

    void startServiceWithNotification() {


        final Intent notificationIntent = new Intent(this.getApplicationContext(), NowPlaying.class);


//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


   final Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

        final NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(this);

        final Notification  notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(this.getResources().getString(R.string.app_name))
                .setTicker(this.getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.iconmain)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
        .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)

                .build();

        notificationManagerCompat.notify(1, notification);

    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        // Le lecteur est prêt, on commence la lecture
        mp.start();

        // Création et lancement du Thread de mise à jour de l'UI
        final Thread updateThread = new Thread(this.sendUpdates);
        updateThread.start();
    }

    public void pause() {
        if (mMediaPlayer != null)
            mMediaPlayer.pause();
    }

    public void play() {
        if (mMediaPlayer != null) {

            mMediaPlayer.start();

        }
    }

    public void stop() {
        if (mMediaPlayer != null) {


            mMediaPlayer.stop();
            mMediaPlayer.release();

            mMediaPlayer = null;
        }

    }

    public void seekTo(@NonNull final int msec) {
        if (this.mMediaPlayer != null)
            this.mMediaPlayer.seekTo(msec);

    }

    public boolean isPlaying() {
        return this.mMediaPlayer != null && this.mMediaPlayer.isPlaying();
    }

    public Uri getFile() {
        return this.file;
    }


    public void setcompletestarted(final boolean completestarted) {
        this.completestarted = completestarted;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        // Utilisation du BroadcastReceiver local pour indiquer à l'activité que la lecture est terminée

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(final MediaPlayer mp, final int what, final int extra) {

                return true;
            }
        });

        if (this.completestarted && !this.seekBarTouch) {
            final SharedPreferences pref = getSharedPreferences("MyPref", 0);

            if (pref.contains("settings")) {
                switch (pref.getInt("settings", 0)) {

                    case 1:

                        if (this.playingmsic != null && this.playingmsic.size() > 1) {
                            if (this.position == this.playingmsic.size() - 1)
                                this.position = 0;

                            this.file = this.playingmsic.get(++this.position);
                            this.init(this.file);
                            this.play();
                        }


                        break;
                    case 2:
                        this.init(this.file);
                        this.play();

                        break;
                    case 3:


                        this.stop();
                        this.didStop = true;
                        break;

                }
                this.completestarted = false;
            }
        } else
            this.completestarted = true;

        final Intent intent = new Intent(MediaPlaybackService.MPS_COMPLETED);
        intent.putExtra("completed", this.completestarted);
        this.broadcastManager.sendBroadcast(intent);
    }

    private void sendElapsedTime() {
        // Utilisation du BroadcastReceiver local pour envoyer la durée passée
        final Intent intent = new Intent(MediaPlaybackService.MPS_RESULT);
        if (this.mMediaPlayer != null)
            try {
                intent.putExtra(MediaPlaybackService.MPS_MESSAGE, this.mMediaPlayer.getCurrentPosition());
                this.broadcastManager.sendBroadcast(intent);
            } catch (final IllegalStateException e) {

            }
    }

    public class IDBinder extends Binder {

        MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

}
