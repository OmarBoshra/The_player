package com.omar.acer.musicalstructure;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.omar.acer.musicalstructure.app.CHANNEL_ID;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String MPS_MESSAGE = "com.example.acer.musicalstructure.MediaPlaynackService.MESSAGE";
    public static final String MPS_RESULT = "com.example.acer.musicalstructure.MediaPlaynackService.RESULT";
    public static final String MPS_COMPLETED = "com.example.acer.musicalstructure.MediaPlaynackService.COMPLETED";

    private static final String SHUTDOWN = "com.naman14.timber.shutdown";

    IDBinder idBinder = new IDBinder();

    boolean completestarted = false;
    MediaPlayer mMediaPlayer = null;
    Uri file;
    int position = -1;
    boolean didStop = false;
    boolean didStart = false;
    LocalBroadcastManager broadcastManager;

    boolean seekBarTouch = false;

    Runnable sendUpdates = new Runnable() {
        @Override
        public void run() {
            while (mMediaPlayer != null) {
                sendElapsedTime();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private List<Uri> playingmsic;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(didStart==false) {

            didStart=true;


        }

        return START_NOT_STICKY;
    }


/*    public void onTaskRemoved(Intent intent){
        super.onTaskRemoved(intent);
        Intent intent=new Intent(this,this.getClass());
        startService(intet);
    }*/


    public void getTouchStatus(boolean seekBarTouch) {
        this.seekBarTouch = seekBarTouch;
    }

    public void setUris(List<Uri> playingmsic) {
        this.playingmsic = playingmsic;
    }


    @Override
    public void onCreate() {

            startServiceWithNotification();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

            broadcastManager = LocalBroadcastManager.getInstance(this);
            super.onCreate();

    }

    @Override
    public void onDestroy() {
//        stop();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return idBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Si le service est débinder, arrêter la lecture
        stop();
        return super.onUnbind(intent);
    }


    public void setPosition(int position) {
        this.position = position;
    }

    public void init(Uri file) {
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

        } catch (IOException e) {
            e.printStackTrace();
            stop();
        }
    }

    void startServiceWithNotification() {

// TO OPEN ACTIVITY FROM THE NOTIFICATION
     /*   Intent notificationIntent = new Intent(getApplicationContext(), NowPlaying.class);
        notificationIntent.setAction("start");  // A string containing the action name

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
*/
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.play);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.albums))
                .setSmallIcon(R.drawable.play)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
//                .setDeleteIntent(contentPendingIntent)  // if needed
                .build();
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(1, notification);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Le lecteur est prêt, on commence la lecture
        mp.start();

        // Création et lancement du Thread de mise à jour de l'UI
        Thread updateThread = new Thread(sendUpdates);
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

    public void seekTo(@NonNull int msec) {
        if (mMediaPlayer != null)
            mMediaPlayer.seekTo(msec);

    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public Uri getFile() {
        return file;
    }


    public void setcompletestarted(boolean completestarted) {
        this.completestarted = completestarted;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Utilisation du BroadcastReceiver local pour indiquer à l'activité que la lecture est terminée

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                return true;
            }
        });

        if (completestarted && !seekBarTouch) {
            SharedPreferences pref = this.getSharedPreferences("MyPref", 0);

            if (pref.contains("settings")) {
                switch (pref.getInt("settings", 0)) {

                    case 1:

                        if (playingmsic != null && playingmsic.size() > 1) {
                            if (position == playingmsic.size() - 1)
                                position = 0;

                            file = playingmsic.get(++position);
                            init(file);
                            play();
                        }


                        break;
                    case 2:
                        init(file);
                        play();

                        break;
                    case 3:


                        stop();
                        didStop = true;
                        break;

                }
                completestarted = false;
            }
        } else
            completestarted = true;

        Intent intent = new Intent(MPS_COMPLETED);
        intent.putExtra("completed", completestarted);
        broadcastManager.sendBroadcast(intent);
    }

    private void sendElapsedTime() {
        // Utilisation du BroadcastReceiver local pour envoyer la durée passée
        Intent intent = new Intent(MPS_RESULT);
        if (mMediaPlayer != null)
            try {
                intent.putExtra(MPS_MESSAGE, mMediaPlayer.getCurrentPosition());
                broadcastManager.sendBroadcast(intent);
            } catch (IllegalStateException e) {

            }
    }

    public class IDBinder extends Binder {

        MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

}
