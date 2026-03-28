package com.omar.acer.musicalstructure;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class NowPlaying extends AppCompatActivity {

    private SharedPreferences pref;
    private boolean seekBarTouch = false;
    private MediaPlaybackService mediaPlaybackService;
    private int position = -1;

    private BroadcastReceiver receiverElapsedTime;
    private BroadcastReceiver receiverCompleted;
    private BroadcastReceiver receiverNewSong;

    private AppCompatSeekBar elapsedTimeSeekBar;
    private TextView elapsedTimeTextView;
    private TextView durationTextView;
    private TextView albumTv;
    private TextView songTv;
    private ImageView iv;
    private ImageButton play_pause;

    private Uri globalUri;
    private List<Uri> playingMusicList = new ArrayList<>();
    private final dialog loading = new dialog(this);
    private boolean isSingle = false;
    private int pauseorplay = 0; // 0 for playing, 1 for paused/stopped

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlaybackService = ((MediaPlaybackService.IDBinder) service).getService();
            showMusic();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlaybackService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        musicinfo.issongopen = true;
        pref = getSharedPreferences("MyPref", 0);
        initViews();
        setupReceivers();

        loading.Loading();

        Intent serviceIntent = new Intent(this, MediaPlaybackService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mediaPlaybackService != null) {
            showMusic();
        }
    }

    private void initViews() {
        iv = findViewById(R.id.viewedimage);
        songTv = findViewById(R.id.songname);
        albumTv = findViewById(R.id.albumname);
        elapsedTimeTextView = findViewById(R.id.textViewElapsedTime);
        durationTextView = findViewById(R.id.textViewDuration);
        elapsedTimeSeekBar = findViewById(R.id.seekBar);
        play_pause = findViewById(R.id.play_pause);

        seekBar();
        setupBottomTab();
    }

    private void setupBottomTab() {
        findViewById(R.id.toHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NowPlaying.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        findViewById(R.id.tomusic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicinfo.navigation(NowPlaying.this, 1, pref, loading);
            }
        });

        findViewById(R.id.toalbums).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicinfo.navigation(NowPlaying.this, 2, pref, loading);
            }
        });

        findViewById(R.id.rewind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextOrprev(false, false);
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService != null) {
                    mediaPlaybackService.stop();
                }
                elapsedTimeSeekBar.setProgress(0);
                elapsedTimeSeekBar.animate().alpha(0.1f).setDuration(800).start();
                elapsedTimeTextView.setText("00:00");
                play_pause.setImageResource(R.drawable.play);
                pauseorplay = 1;
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService == null) return;
                if (pauseorplay == 0 && mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    play_pause.setImageResource(R.drawable.play);
                    pauseorplay = 1;
                } else {
                    if (mediaPlaybackService.getFile() == null) {
                        if (globalUri != null) {
                            mediaPlaybackService.init(globalUri);
                        } else {
                             Toast.makeText(NowPlaying.this, "No song to play", Toast.LENGTH_SHORT).show();
                             return;
                        }
                    }
                    mediaPlaybackService.play();
                    play_pause.setImageResource(R.drawable.pause);
                    pauseorplay = 0;
                    elapsedTimeSeekBar.animate().alpha(1f).setDuration(800).start();
                }
            }
        });

        findViewById(R.id.fastforward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextOrprev(true, false);
            }
        });
    }

    public void NewSongPath(View view) {
        musicinfo.initializeIntent(this);
    }

    private void setupReceivers() {
        receiverElapsedTime = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateElapsedTime(intent.getIntExtra(MediaPlaybackService.MPS_MESSAGE, 0));
            }
        };

        receiverCompleted = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("completed", false)) {
                    elapsedTimeSeekBar.animate().alpha(0.1f).setDuration(800).start();
                    play_pause.setImageResource(R.drawable.play);
                    pauseorplay = 1;
                    elapsedTimeTextView.setText("00:00");
                    elapsedTimeSeekBar.setProgress(0);
                }
            }
        };

        receiverNewSong = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Uri uri = intent.getParcelableExtra("uri");
                if (uri != null) {
                    globalUri = uri;
                    refreshSongUI(uri);
                }
            }
        };
    }

    private void refreshSongUI(final Uri uri) {
        if (uri == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final musicinfo.SongMetadata metadata = musicinfo.getMetadata(NowPlaying.this, uri);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing()) return;
                        updateSongUI(uri, metadata.title, metadata.album, metadata.duration);
                        if (metadata.image != null) {
                            iv.setImageBitmap(metadata.image);
                        } else {
                            iv.setImageResource(R.drawable.iconmain);
                        }
                    }
                });
            }
        }).start();

        if (playingMusicList != null) {
            for (int i = 0; i < playingMusicList.size(); i++) {
                if (playingMusicList.get(i).equals(uri)) {
                    position = i;
                    break;
                }
            }
        }
    }

    private void showMusic() {
        if (mediaPlaybackService == null) return;

        // 1. If the service is already playing something, let's sync to THAT first.
        Uri currentlyPlaying = mediaPlaybackService.getFile();
        if (currentlyPlaying != null) {
            globalUri = currentlyPlaying;
            refreshSongUI(currentlyPlaying);
            
            // Sync the playlist if available in service
            if (mediaPlaybackService.getUris() != null) {
                playingMusicList = mediaPlaybackService.getUris();
                position = mediaPlaybackService.getPosition();
            }
            
            // If the intent explicitly asked for a DIFFERENT song, then we override.
            // Otherwise, we just stay with what's playing.
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("songUri")) {
                Uri songUri = extras.getParcelable("songUri");
                if (songUri != null && !songUri.equals(currentlyPlaying)) {
                    startNewSongFromIntent(extras);
                } else {
                    loading.dismiss();
                }
            } else {
                loading.dismiss();
            }
            return;
        }

        // 2. Service is NOT playing. Check intent extras.
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("songUri")) {
            startNewSongFromIntent(extras);
        } else {
            // 3. Service NOT playing, NO intent extras. Use Preferences.
            loadFromPreferences();
        }
    }

    private void startNewSongFromIntent(Bundle extras) {
        Uri songUri = extras.getParcelable("songUri");
        String songName = extras.getString("songname", "Unknown");
        String albumName = extras.getString("albumname", "Unknown");
        int duration = extras.getInt("getDuration", 0);
        position = extras.getInt("urlposition", 0);

        mediaPlaybackService.init(songUri);
        mediaPlaybackService.play();

        globalUri = songUri;
        if (musicinfo.musicUris != null && !musicinfo.musicUris.isEmpty()) {
            playingMusicList = new ArrayList<>(musicinfo.musicUris);
        } else {
            playingMusicList = new ArrayList<>();
            if (songUri != null) playingMusicList.add(songUri);
        }
        mediaPlaybackService.setUris(playingMusicList);
        mediaPlaybackService.setPosition(position);

        updateSongUI(songUri, songName, albumName, duration);
        refreshSongUI(songUri); // Ensure art is loaded
        loading.dismiss();
    }

    private void loadFromPreferences() {
        String songUriStr = pref.getString("gotsong", null);
        if (songUriStr == null) {
            Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show();
            loading.dismiss();
            finish();
            return;
        }

        Uri songUri = Uri.parse(songUriStr);
        String parentStr = pref.getString("gotparentSongFolderUri", null);
        Uri parentfolderUri = parentStr != null ? Uri.parse(parentStr) : null;

        if (parentfolderUri != null) {
            isSingle = false;
            getSongListAndPlay(parentfolderUri, songUri);
        } else {
            isSingle = true;
            globalUri = songUri;

            if (mediaPlaybackService.isPlaying() && songUri.equals(mediaPlaybackService.getFile())) {
               refreshSongUI(songUri);
               loading.dismiss();
            } else {
                mediaPlaybackService.init(songUri);
                mediaPlaybackService.play();
                refreshSongUI(songUri);
                loading.dismiss();
            }
        }

        if (getIntent().hasExtra("next")) {
            nextOrprev(getIntent().getBooleanExtra("next", false), false);
        }
    }

    private void updateSongUI(Uri uri, String name, String albumName, int songDuration) {
        if (isFinishing()) return;
        songTv.setText(name);
        albumTv.setText(albumName);

        durationTextView.setText(secondsToString(songDuration));
        elapsedTimeSeekBar.setMax(songDuration);
        elapsedTimeSeekBar.animate().alpha(1f).setDuration(800).start();

        if (mediaPlaybackService != null && mediaPlaybackService.isPlaying()) {
            play_pause.setImageResource(R.drawable.pause);
            pauseorplay = 0;
        }
    }

    private String secondsToString(int duration) {
        int minutes = (duration / 1000) / 60;
        int seconds = (duration / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void updateElapsedTime(int elapsed) {
        if (!seekBarTouch) {
            elapsedTimeSeekBar.setProgress(elapsed);
        }
        elapsedTimeTextView.setText(secondsToString(elapsed));
    }

    private void seekBar() {
        elapsedTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateElapsedTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarTouch = false;
                if (mediaPlaybackService != null) {
                    mediaPlaybackService.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiverElapsedTime, new IntentFilter(MediaPlaybackService.MPS_RESULT));
        lbm.registerReceiver(receiverCompleted, new IntentFilter(MediaPlaybackService.MPS_COMPLETED));
        lbm.registerReceiver(receiverNewSong, new IntentFilter(MediaPlaybackService.MPS_NEW_SONG));
        if (mediaPlaybackService != null) {
            showMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiverElapsedTime);
        lbm.unregisterReceiver(receiverCompleted);
        lbm.unregisterReceiver(receiverNewSong);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        musicinfo.issongopen = false;
    }

    private void nextOrprev(boolean next, boolean automatic) {
        if (mediaPlaybackService == null) return;
        
        if (isSingle) {
             mediaPlaybackService.seekTo(0);
             mediaPlaybackService.play();
             return;
        }

        if (next) {
            position++;
            if (position >= playingMusicList.size()) position = 0;
        } else {
            position--;
            if (position < 0) position = playingMusicList.size() - 1;
        }

        Uri nextUri = playingMusicList.get(position);
        globalUri = nextUri;
        mediaPlaybackService.init(nextUri);
        mediaPlaybackService.play();
        mediaPlaybackService.setPosition(position);
        refreshSongUI(nextUri);
    }

    private void getSongListAndPlay(final Uri folderUri, final Uri selectedSong) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DocumentFile df = DocumentFile.fromTreeUri(NowPlaying.this, folderUri);
                final List<Uri> uris = musicinfo.getFiles(df, 1, NowPlaying.this, null);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playingMusicList = uris;
                        mediaPlaybackService.setUris(playingMusicList);
                        
                        for (int i = 0; i < playingMusicList.size(); i++) {
                            if (playingMusicList.get(i).equals(selectedSong)) {
                                position = i;
                                break;
                            }
                        }
                        
                        globalUri = selectedSong;
                        mediaPlaybackService.init(selectedSong);
                        mediaPlaybackService.play();
                        mediaPlaybackService.setPosition(position);
                        refreshSongUI(selectedSong);
                        loading.dismiss();
                    }
                });
            }
        }).start();
    }
}
