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
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class Albums extends AppCompatActivity {
    private SharedPreferences pref;
    private final dialog loading = new dialog(Albums.this);
    private List<Uri> uris;
    private ListView albumsListView;
    private List<String> albumNames = new ArrayList<>();
    private List<Bitmap> images = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        loading.Loading();

        pref = this.getSharedPreferences("MyPref", 0);

        Button tohome = findViewById(R.id.toHome);
        Button gomusic = findViewById(R.id.tomusic);
        Button tonowplaying = findViewById(R.id.toplayingsong);

        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(Albums.this, MainActivity.class);
                tohome.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(tohome);
                finish();
            }
        });

        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Albums.this, NowPlaying.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        gomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(Albums.this, 1, pref);
            }
        });

        albumsListView = findViewById(R.id.albumslist);
        uris = (musicinfo.albumUris != null) ? musicinfo.albumUris : new ArrayList<Uri>();

        adapter = new Adapter(null, images, 0, null, this, albumNames);
        albumsListView.setAdapter(adapter);

        loadMetadata();

        albumsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= uris.size()) return;

                Uri albumUri = uris.get(position);
                String albumName = position < albumNames.size() ? albumNames.get(position) : "Unknown Album";

                String albumTreeUriStr = pref.getString("gotAlbums", null);
                if (albumTreeUriStr == null) {
                    Toast.makeText(Albums.this, "Error: Album folder not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                loading.Loading();
                final String finalAlbumName = albumName;
                final String finalAlbumTreeUriStr = albumTreeUriStr;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DocumentFile albumstree = DocumentFile.fromTreeUri(Albums.this, Uri.parse(finalAlbumTreeUriStr));
                        if (albumstree == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Albums.this, "Error: Could not access folder", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }
                            });
                            return;
                        }

                        final List<Uri> relatedMusic = musicinfo.getFiles(albumstree, 4, Albums.this, finalAlbumName);
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicinfo.musicUris = relatedMusic;
                                Intent tomusic = new Intent(Albums.this, music.class);
                                tomusic.putExtra("almumname", finalAlbumName);
                                tomusic.putExtra("albumtree", finalAlbumTreeUriStr);
                                loading.dismiss();
                                startActivity(tomusic);
                                finish();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void loadMetadata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> albums = new ArrayList<>();
                final List<Bitmap> bmps = new ArrayList<>();
                Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.iconmain);

                for (Uri uri : uris) {
                    musicinfo.SongMetadata meta = musicinfo.getMetadata(Albums.this, uri);
                    albums.add(meta.album);
                    bmps.add(meta.image != null ? meta.image : placeholder);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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

    public void NewAlbumsPath(View view) {
        musicinfo.initializeIntent(Albums.this);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            musicinfo.getUris(this, data.getData(), pref, requestCode);
            finish();
        } else {
            loading.dismiss();
        }
    }
}
