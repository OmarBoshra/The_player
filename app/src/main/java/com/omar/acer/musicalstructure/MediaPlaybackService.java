package com.omar.acer.musicalstructure;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String MPS_MESSAGE = "com.example.acer.musicalstructure.MediaPlaynackService.MESSAGE";
    public static final String MPS_RESULT = "com.example.acer.musicalstructure.MediaPlaynackService.RESULT";
    public static final String MPS_COMPLETED = "com.example.acer.musicalstructure.MediaPlaynackService.COMPLETED";


    boolean completestarted=false;
    MediaPlayer mMediaPlayer = null;
    Uri file;
    int position;
    IDBinder idBinder = new IDBinder();
    LocalBroadcastManager broadcastManager;

    boolean seekBarTouch =false;

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

    public void getTouchStatus(boolean seekBarTouch ){
        this.seekBarTouch=seekBarTouch;
    }


    @Override
    public void onCreate() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stop();
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


  public void setPosition(int position){
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
        if (mMediaPlayer != null)
            mMediaPlayer.start();
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();

            mMediaPlayer = null;
        }

    }

    public void seekTo(@NonNull int msec) {
        if(mMediaPlayer!=null)
        mMediaPlayer.seekTo(msec);

    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public Uri getFile() {
        return file;
    }


void setcompletestarted(boolean completestarted){
        this.completestarted=completestarted;
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
        if(completestarted) {
        SharedPreferences pref = this.getSharedPreferences("MyPref", 0);

    if(pref.contains("settings")) {
    switch (pref.getInt("settings", 0)){

        case 1:

                if (musicinfo.musicUris.size() > 1) {
                        if (position == musicinfo.musicUris.size() - 1)
                            position = 0;

                    file = musicinfo.musicUris.get(++position);
                    init(file);
                    play();
                }


            break;
        case 2:
            init(file);
            play();

            break;
        case 3:

            if (!seekBarTouch)
                stop();

            break;

    }
    completestarted = false;
}
        }else
            completestarted=true;

        Intent intent = new Intent(MPS_COMPLETED);
        intent.putExtra("completed",completestarted);
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
