package com.omar.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class music extends AppCompatActivity {
    private SharedPreferences pref;
    private final dialog loading = new dialog(music.this);
    private List<Uri> uris;
    private ListView musicListView;
    private List<String> musicNames = new ArrayList<>();
    private List<String> albumNames = new ArrayList<>();
    private List<Bitmap> images = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        loading.Loading();

        Button tohome = findViewById(R.id.toHome);
        Button toalbum = findViewById(R.id.toalbums);
        Button tonowplaying = findViewById(R.id.toplayingsong);

        pref = this.getSharedPreferences("MyPref", 0);

        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(music.this, 2, pref);
            }
        });

        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(music.this, NowPlaying.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(music.this, MainActivity.class);
                tohome.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(tohome);
                finish();
            }
        });

        if (getIntent().hasExtra("almumname")) {
            final ImageButton favoritAlbum = findViewById(R.id.favalbum);
            if (favoritAlbum != null) {
                favoritAlbum.setVisibility(View.VISIBLE);
                favoritAlbum.animate()
                        .alpha(1.0f)
                        .setDuration(1200)
                        .start();
                favoritAlbum.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setfavalbum();
                        favoritAlbum.animate().alpha(0).setDuration(1200).translationYBy(-18).start();
                        Toast.makeText(music.this, "favorite playlist set", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        musicListView = findViewById(R.id.musiclist);
        uris = (musicinfo.musicUris != null) ? musicinfo.musicUris : new ArrayList<Uri>();
        
        adapter = new Adapter(musicNames, images, -1, null, this, albumNames);
        musicListView.setAdapter(adapter);

        loadMetadata();

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= uris.size()) return;

                Uri selected = uris.get(position);
                Intent tosong = new Intent(music.this, NowPlaying.class);
                tosong.putExtra("songUri", selected);
                tosong.putExtra("songname", position < musicNames.size() ? musicNames.get(position) : "Unknown");
                tosong.putExtra("albumname", position < albumNames.size() ? albumNames.get(position) : "Unknown");
                tosong.putExtra("urlposition", position);
                tosong.putExtra("getDuration", musicinfo.getDuration(music.this, selected));

                musicinfo.setpref(music.this, pref, 3, selected);

                startActivity(tosong);
                finish();
            }
        });
    }

    private void loadMetadata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> names = new ArrayList<>();
                final List<String> albums = new ArrayList<>();
                final List<Bitmap> bmps = new ArrayList<>();
                Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.iconmain);

                for (Uri uri : uris) {
                    musicinfo.SongMetadata meta = musicinfo.getMetadata(music.this, uri);
                    names.add(meta.title);
                    albums.add(meta.album);
                    bmps.add(meta.image != null ? meta.image : placeholder);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        musicNames.clear();
                        musicNames.addAll(names);
                        albumNames.clear();
                        albumNames.addAll(albums);
                        images.clear();
                        images.addAll(bmps);
                        adapter.notifyDataSetChanged();
                        loading.dismiss();
                    }
                });
            }
        }).start();
    }

    private void setfavalbum() {
        String albumName = getIntent().getStringExtra("almumname");
        String albumTree = getIntent().getStringExtra("albumtree");
        if (albumName != null && albumTree != null) {
            pref.edit()
                .putString("favoritalbum", albumName)
                .putString("gotmusic", albumTree)
                .apply();
        }
    }

    public void NewMusicPath(View view) {
        musicinfo.initializeIntent(music.this);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            pref.edit().remove("favoritalbum").apply();
            musicinfo.getUris(this, data.getData(), pref, requestCode);
            finish();
        } else {
            loading.dismiss();
        }
    }
}
