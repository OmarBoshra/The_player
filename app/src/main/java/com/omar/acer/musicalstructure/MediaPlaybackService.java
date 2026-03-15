package com.omar.acer.musicalstructure;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import static com.omar.acer.musicalstructure.app.CHANNEL_ID;

public class MediaPlaybackService extends Service implements 
        MediaPlayer.OnPreparedListener, 
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String MPS_MESSAGE = "com.example.acer.musicalstructure.MediaPlaynackService.MESSAGE";
    public static final String MPS_RESULT = "com.example.acer.musicalstructure.MediaPlaynackService.RESULT";
    public static final String MPS_COMPLETED = "com.example.acer.musicalstructure.MediaPlaynackService.COMPLETED";
    public static final String MPS_NEW_SONG = "com.example.acer.musicalstructure.MediaPlaynackService.NEW_SONG";
    
    public static final String ACTION_STOP = "com.omar.acer.musicalstructure.ACTION_STOP";
    public static final String ACTION_NEXT = "com.omar.acer.musicalstructure.ACTION_NEXT";
    public static final String ACTION_PREV = "com.omar.acer.musicalstructure.ACTION_PREV";
    public static final String ACTION_PAUSE_RESUME = "com.omar.acer.musicalstructure.ACTION_PAUSE_RESUME";

    private final MediaPlaybackService.IDBinder idBinder = new MediaPlaybackService.IDBinder();
    public MediaPlayer mMediaPlayer;
    private Uri file;
    private String currentSongName = "Unknown Title";
    private String currentAlbumName = "Unknown Album";
    
    private int position = -1;
    private LocalBroadcastManager broadcastManager;
    private boolean seekBarTouch;
    private List<Uri> playingmsic = new ArrayList<>();
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private boolean isPreparing = false;

    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        sendElapsedTime();
                    }
                } catch (IllegalStateException e) {
                    // Ignore
                }
            }
            updateHandler.postDelayed(this, 1000);
        }
    };

    public void getTouchStatus(final boolean seekBarTouch) {
        this.seekBarTouch = seekBarTouch;
    }

    public void setUris(final List<Uri> playingmsic) {
        if (playingmsic != null) {
            this.playingmsic = new ArrayList<>(playingmsic);
        }
    }

    public List<Uri> getUris() {
        return playingmsic;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateHandler.post(updateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_STOP:
                    stop();
                    stopForeground(true);
                    final Intent stoppedIntent = new Intent(MPS_COMPLETED);
                    stoppedIntent.putExtra("completed", true);
                    broadcastManager.sendBroadcast(stoppedIntent);
                    stopSelf();
                    return START_NOT_STICKY;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREV:
                    playPrevious();
                    break;
                case ACTION_PAUSE_RESUME:
                    if (isPlaying()) pause(); else play();
                    break;
            }
        }
        startServiceWithNotification();
        return START_STICKY;
    }

    private void playNext() {
        if (playingmsic != null && !playingmsic.isEmpty()) {
            position = (position < playingmsic.size() - 1) ? position + 1 : 0;
            init(playingmsic.get(position));
        }
    }

    private void playPrevious() {
        if (playingmsic != null && !playingmsic.isEmpty()) {
            position = (position > 0) ? position - 1 : playingmsic.size() - 1;
            init(playingmsic.get(position));
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!isPlaying()) {
            stopSelf();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        updateHandler.removeCallbacks(updateRunnable);
        abandonAudioFocus();
        stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return idBinder;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public void init(final Uri file) {
        if (file == null) return;
        this.file = file;
        
        musicinfo.SongMetadata meta = musicinfo.getMetadata(this, file);
        currentSongName = meta.title;
        currentAlbumName = meta.album;

        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
            } catch (Exception e) {
                mMediaPlayer.release();
                mMediaPlayer = createMediaPlayer();
            }
        } else {
            mMediaPlayer = createMediaPlayer();
        }

        isPreparing = true;
        try {
           mMediaPlayer.setDataSource(getApplicationContext(), file);
           mMediaPlayer.prepareAsync();
        } catch (final Exception e) {
            isPreparing = false;
            Log.e("MediaPlaybackService", "Error setting data source", e);
        }
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        return mp;
    }

    private void startServiceWithNotification() {
        Intent notificationIntent = new Intent(this, NowPlaying.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags);

        // Actions
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 1, new Intent(this, MediaPlaybackService.class).setAction(ACTION_PREV), pendingIntentFlags);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 2, new Intent(this, MediaPlaybackService.class).setAction(ACTION_PAUSE_RESUME), pendingIntentFlags);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, new Intent(this, MediaPlaybackService.class).setAction(ACTION_NEXT), pendingIntentFlags);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 4, new Intent(this, MediaPlaybackService.class).setAction(ACTION_STOP), pendingIntentFlags);

        int pauseIcon = isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSongName)
                .setContentText(currentAlbumName)
                .setSmallIcon(R.drawable.iconmain)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(pauseIcon, "Pause/Resume", pausePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .addAction(R.drawable.stop, "Stop", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying());

        try {
            androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3);
            builder.setStyle(mediaStyle);
        } catch (Exception ignored) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, builder.build(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, builder.build());
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        isPreparing = false;
        if (requestAudioFocus()) {
            mp.start();
            startServiceWithNotification();
            Intent intent = new Intent(MPS_NEW_SONG);
            intent.putExtra("uri", file);
            broadcastManager.sendBroadcast(intent);
        }
    }

    public void pause() {
        if (mMediaPlayer != null && !isPreparing) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    startServiceWithNotification();
                }
            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }

    public void play() {
        if (mMediaPlayer != null) {
            if (isPreparing) return;
            try {
                if (!mMediaPlayer.isPlaying()) {
                    if (requestAudioFocus()) {
                        mMediaPlayer.start();
                        startServiceWithNotification();
                    }
                }
            } catch (IllegalStateException e) {
                if (file != null) init(file);
            }
        } else if (file != null) {
            init(file);
        }
    }

    public void stop() {
        abandonAudioFocus();
        isPreparing = false;
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e) {
                // Ignore
            } finally {
                mMediaPlayer = null;
            }
        }
    }

    public void seekTo(final int msec) {
        if (mMediaPlayer != null && !isPreparing) {
            try {
                mMediaPlayer.seekTo(msec);
            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }

    public boolean isPlaying() {
        if (isPreparing) return true;
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public Uri getFile() {
        return file;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        if (seekBarTouch) return;
        final SharedPreferences pref = getSharedPreferences("MyPref", 0);
        int settings = pref.getInt("settings", 0);

        if (settings == 1) { // Next
            playNext();
        } else if (settings == 2) { // Restart
            if (file != null) init(file);
        } else {
            stop();
            stopForeground(true);
            final Intent intent = new Intent(MPS_COMPLETED);
            intent.putExtra("completed", true);
            broadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MediaPlaybackService", "MediaPlayer error: " + what + ", " + extra);
        isPreparing = false;
        try {
            mp.reset();
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private void sendElapsedTime() {
        if (mMediaPlayer != null && !isPreparing) {
            try {
                int currentPos = mMediaPlayer.getCurrentPosition();
                final Intent intent = new Intent(MediaPlaybackService.MPS_RESULT);
                intent.putExtra(MPS_MESSAGE, currentPos);
                broadcastManager.sendBroadcast(intent);
            } catch (final Exception ignored) {}
        }
    }

    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusRequest != null) {
                audioManager.abandonAudioFocusRequest(focusRequest);
                focusRequest = null;
            }
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.setVolume(0.3f, 0.3f);
                    } catch (Exception e) {}
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                        play();
                    } catch (Exception e) {}
                }
                break;
        }
    }

    public class IDBinder extends Binder {
        MediaPlaybackService getService() { return MediaPlaybackService.this; }
    }
}
