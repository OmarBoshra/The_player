package com.omar.acer.musicalstructure;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NowPlaying extends AppCompatActivity {

    private SharedPreferences pref;

    private String treeUri;

    private boolean seekBarTouch = false;

    private MediaPlaybackService mediaPlaybackService;
    private int currentUrlPosition = 0;

    private BroadcastReceiver receiverElapsedTime;
    private BroadcastReceiver receiverCompleted;
    private AppCompatSeekBar elapsedTimeSeekBar;

    private TextView elapsedTimeTextView;
    private TextView durationTextView;
    private int duration = 0;
    private int position = -1;

    private Uri globalUri;

    private TextView album;
    private TextView song;
    private ImageView iv;

    private List<Uri> playingmsic;

    private dialog loading = new dialog(NowPlaying.this);


    private boolean isSingle = false;

    private ImageButton play_pause;

    private DocumentFile musicfile;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlaybackService = ((MediaPlaybackService.IDBinder) service).getService();

            showMusic();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private int pauseorplay = 0;

    private void showMusic() {

        iv = findViewById(R.id.viewedimage);
        song = findViewById(R.id.songname);
        album = findViewById(R.id.albumname);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("songname")) {


            Uri songUri = getIntent().getParcelableExtra("songUri");

            mediaPlaybackService.setcompletestarted(false);
            mediaPlaybackService.init(songUri);
            mediaPlaybackService.play();

            song.setText(getIntent().getStringExtra("songname"));
            album.setText(getIntent().getStringExtra("albumname"));

            byte[] byteArray = getIntent().getByteArrayExtra("albumpicture");
            iv.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));

            duration = getIntent().getIntExtra("getDuration", 0);

            durationTextView.setText(secondsToString(duration));

            elapsedTimeSeekBar.setMax(duration);

            globalUri = songUri;

            position = getIntent().getIntExtra("urlposition", 0);
            mediaPlaybackService.setPosition(position);

            loading.dismiss();
        } else {




            musicinfo.musicUris = new ArrayList<>();//remove uris

            Uri songUri = Uri.parse(pref.getString("gotsong", null));


            Uri legitSongUri = null;

            if (pref.contains("gotparentSongFolderUri")) {


                Uri parentfolderUri = Uri.parse(pref.getString("gotparentSongFolderUri", null));


                legitSongUri = getSongList(parentfolderUri, songUri);


            } else {
                isSingle = true;
                legitSongUri = songUri;
            }
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("next"))
                nextOrprev(getIntent().getBooleanExtra("next", false), false);
            else if (legitSongUri != null) {

                mediaPlaybackService.init(legitSongUri);
                mediaPlaybackService.play();

                globalUri = legitSongUri;

                song.setText(pref.getString("gotsongname", null));
                album.setText(pref.getString("gotsongalbum", null));


                byte[] imageAsBytes = Base64.decode(pref.getString("gotsongimage", null).getBytes(), Base64.DEFAULT);
                iv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

                duration = (pref.getInt("gotsongduration", 0));
                durationTextView.setText(secondsToString(duration));
                elapsedTimeSeekBar.setMax(duration);


                loading.dismiss();
            } else
                Toast.makeText(mediaPlaybackService, "No song playing", Toast.LENGTH_SHORT).show();

        }


    }

    private Uri getSongList(Uri parentUri, Uri songUri) {

        musicfile = DocumentFile.fromTreeUri(this, parentUri);

        Uri legitSongUri = null;
        DocumentFile[] files = musicfile.listFiles();

        for (DocumentFile file : files) {

            if (file.getName().endsWith("mp3") || file.getName().endsWith("WAV") || file.getName().endsWith("MP4") || file.getName().endsWith("FLAC") || file.getName().endsWith("M4A")) {

                musicinfo.musicUris.add(file.getUri());

                if (musicinfo.getMusicNames(NowPlaying.this, Arrays.asList(file.getUri())).get(0).equals(musicinfo.getMusicNames(NowPlaying.this, Arrays.asList(songUri)).get(0))) {
                    legitSongUri = file.getUri();
                    position = musicinfo.musicUris.size() - 1;
                    mediaPlaybackService.setPosition(position);

                }
            }


        }
        playingmsic= musicinfo.musicUris;
        mediaPlaybackService.setUris(playingmsic);
        return legitSongUri;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        loading.Loading();

        pref = this.getSharedPreferences("MyPref", 0);


        receiverElapsedTime = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                updateElapsedTime(intent.getIntExtra(MediaPlaybackService.MPS_MESSAGE, 0));
            }
        };

        receiverCompleted = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getExtras().getBoolean("completed")==false) {
                    switch (pref.getInt("settings", 0)) {

                        case 1:
                            nextOrprev(true, true);
                            break;
                        case 2:

                            if (!seekBarTouch) {
                                play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                            }


                            break;
                        case 3:
                            elapsedTimeSeekBar.animate().alpha(0.1f).setDuration(800).start();

                            play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                            mediaPlaybackService.didStop=false;
                            break;


                    }
                }



            }
        };


        elapsedTimeTextView = findViewById(R.id.textViewElapsedTime);
        durationTextView = findViewById(R.id.textViewDuration);


        seekBar();
        buttons();


    }

    private void seekBar() {

        elapsedTimeSeekBar = findViewById(R.id.seekBar);


        elapsedTimeSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // Construct a rect of the view's bounds
                    seekBarTouch = true;
                    mediaPlaybackService.getTouchStatus(seekBarTouch);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    seekBarTouch = false;
                    mediaPlaybackService.getTouchStatus(seekBarTouch);

                }
                return false;
            }
        });

        elapsedTimeSeekBar.setProgress(0);
        elapsedTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (seekBarTouch)
                    mediaPlaybackService.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!mediaPlaybackService.isPlaying()){
                    mediaPlaybackService.seekTo(seekBar.getProgress());
                    mediaPlaybackService.play();
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                }else

                    mediaPlaybackService.seekTo(seekBar.getProgress());
            }
        });

    }


    private void buttons() {

        //basic buttons
        Button tohome = findViewById(R.id.toHome);
        Button gomusic = findViewById(R.id.tomusic);
        Button toalbum = findViewById(R.id.toalbums);

        final Handler handler = new Handler();

        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(NowPlaying.this, MainActivity.class);
                startActivity(tohome);

            }
        });

        gomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(NowPlaying.this, 1, pref);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                    }
                },100);


            }
        });
        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(NowPlaying.this, 2, pref);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                    }
                },100);

            }
        });

        ImageButton rewind = findViewById(R.id.rewind);
        ImageButton stop = findViewById(R.id.stop);
        play_pause = findViewById(R.id.play_pause);
        ImageButton fastforward = findViewById(R.id.fastforward);

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nextOrprev(false,false);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                elapsedTimeSeekBar.setProgress(0);
                mediaPlaybackService.stop();
                elapsedTimeSeekBar.animate().alpha(0.1f).setDuration(800).start();
                elapsedTimeTextView.setText("");
                durationTextView.setText("");
                play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                pauseorplay = 1;

            }
        });
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pauseorplay == 0) {

                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                    pauseorplay = 1;


                    mediaPlaybackService.pause();

                } else {

                    if (mediaPlaybackService.mMediaPlayer == null) {
                        mediaPlaybackService.init(globalUri);
                        durationTextView.setText(secondsToString(duration));

                        elapsedTimeSeekBar.animate().alpha(1f).setDuration(800).start();

                    }


                    mediaPlaybackService.play();
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                    pauseorplay = 0;
                }
            }
        });
        fastforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nextOrprev(true,false);

            }
        });
    }

    private void intent(boolean isnext) {
        Intent folderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderIntent.createChooser(folderIntent, "Choose The playlist folder");
        startActivityForResult(folderIntent, isnext ? 5 : 6);
        Toast.makeText(mediaPlaybackService, "Please confirm the playList location", Toast.LENGTH_LONG).show();
    }

    private void nextOrprev(boolean isnext,boolean fromsettings) {

        if (isSingle) {
            intent(isnext);
        } else {

            if (playingmsic.size() > 1) {

                Uri nextSongUri = null;

            if(fromsettings) {
                if (mediaPlaybackService.position > 0) {//got the position from the playlist after user left app
                    position = mediaPlaybackService.position;
                    nextSongUri = playingmsic.get(position);

                }
            }else{
                    if (isnext) {
                        if (position == playingmsic.size() - 1)
                            position = 0;

                        nextSongUri = playingmsic.get(++position);
                    } else {
                        if (position == 0)
                            position = playingmsic.size() - 1;

                        nextSongUri = playingmsic.get(--position);

                    }
                    mediaPlaybackService.setPosition(position);//send to the service


                    mediaPlaybackService.setcompletestarted(false);
                    mediaPlaybackService.init(nextSongUri);
                    mediaPlaybackService.play();
                }
                song.setText(musicinfo.getMusicNames(NowPlaying.this, Arrays.asList(nextSongUri)).get(0));
                album.setText(musicinfo.getAlbumNames(NowPlaying.this, Arrays.asList(nextSongUri)).get(0));
                iv.setImageBitmap(musicinfo.getImages(NowPlaying.this, Arrays.asList(nextSongUri)).get(0));

                play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                durationTextView.setText(secondsToString(duration = musicinfo.getDuration(NowPlaying.this, nextSongUri)));
                globalUri = nextSongUri;

                elapsedTimeSeekBar.setProgress(0);

                elapsedTimeSeekBar.setMax(duration);

                loading.dismiss();

            } else
                Toast.makeText(mediaPlaybackService, "Only 1 song in playlist", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {

        musicinfo.issongopen = true;

        if(mediaPlaybackService!=null&&mediaPlaybackService.getFile()!=null) {

            if(mediaPlaybackService.position>-1)//got the position from the playlist after user left app
                position=mediaPlaybackService.position;


            globalUri = mediaPlaybackService.getFile();

            song.setText(musicinfo.getMusicNames(NowPlaying.this, Arrays.asList(globalUri)).get(0));
            album.setText(musicinfo.getAlbumNames(NowPlaying.this, Arrays.asList(globalUri)).get(0));
            iv.setImageBitmap(musicinfo.getImages(NowPlaying.this, Arrays.asList(globalUri)).get(0));

            play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

            durationTextView .setText(secondsToString(duration = musicinfo.getDuration(NowPlaying.this, globalUri)));

            elapsedTimeSeekBar.setProgress(0);

            elapsedTimeSeekBar.setMax(duration);


            if(mediaPlaybackService.didStop){//when playing stops
                elapsedTimeSeekBar.animate().alpha(0.1f).setDuration(800).start();
                play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                mediaPlaybackService.didStop=false;
            }
        }


        getApplicationContext().bindService(new Intent(getApplicationContext(),
                MediaPlaybackService.class), connection, BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverElapsedTime,
                new IntentFilter(MediaPlaybackService.MPS_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverCompleted,
                new IntentFilter(MediaPlaybackService.MPS_COMPLETED)
        );


        super.onResume();
    }

    @Override
    protected void onPause() {
        // Désenregistrement des BroadcastReceiver à la mise en pause de l'activité
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverElapsedTime);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCompleted);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String secondsToString(int time) {
        time = time / 1000;
        return String.format("%2d:%02d", time / 60, time % 60);
    }

    private void updateElapsedTime(int elapsedTime) {
        elapsedTimeSeekBar.setProgress(elapsedTime);
        elapsedTimeTextView.setText(secondsToString(elapsedTime));


    }


    public void NewSongPath(View view) {

        musicinfo.initializeIntent(NowPlaying.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode >4) {

                if (getSongList(data.getData(), globalUri) == null) {
                    musicinfo.musicUris = new ArrayList<>();//remove uris
                    playingmsic = new ArrayList<>();//remove uris
                    Toast.makeText(mediaPlaybackService, "Song isn't in this folder", Toast.LENGTH_LONG).show();
//clear extra data
                    return;
                } else {
                    mediaPlaybackService.stop();

                    pref.edit().putString("gotparentSongFolderUri", data.getData().toString()).apply();

                }

                finish();

            }


            if (requestCode ==3)
                finish();

            musicinfo.setUris(this, data.getData(), pref, requestCode);




        }else
            loading.dismiss();
    }


}
