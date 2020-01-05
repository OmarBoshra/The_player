package com.omar.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class music extends AppCompatActivity {
    private Intent tosong;
    private SharedPreferences pref;

    private dialog loading = new dialog(music.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        loading.Loading();

        Button tohome = findViewById(R.id.toHome);
        Button toalbum = findViewById(R.id.toalbums);
        Button tonowplaying = findViewById(R.id.toplayingsong);


        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(music.this, 2, pref);

            }
        });

        pref = this.getSharedPreferences("MyPref", 0);

        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (musicinfo.issongopen)
                    onBackPressed();
                else{
                    loading.Loading();
                    musicinfo.navigation(music.this, 3, pref);

                }
            }
        });
        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(music.this, MainActivity.class);
                startActivity(tohome);
                finish();
            }
        });

        musicinfo.albumUris = null;//remove extra uris


        if (getIntent().getExtras() != null) {// in case of albums
            if (getIntent().getExtras().containsKey("almumname")) {//c

                final ImageButton favoritAlbum = findViewById(R.id.favalbum);


                if (pref.contains("gotmusic"))
                    favoritAlbum.animate().alpha(1).setDuration(1800).start();
                else {
                    favoritAlbum.setVisibility(View.GONE);//to prevent clicking by mistake
                    setfavalbum(pref);

                }
                favoritAlbum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        setfavalbum(pref);

                        favoritAlbum.animate().alpha(0).setDuration(1200).translationYBy(-18).start();
                        Toast.makeText(music.this, "favorite playlist set", Toast.LENGTH_SHORT).show();

                    }
                });


            }
        }


        final ListView music = findViewById(R.id.musiclist);

// showing these
        final List<String> musicNames = musicinfo.getMusicNames(this, musicinfo.musicUris);

        final List<String> AlbumNames = musicinfo.getAlbumNames(this, musicinfo.musicUris);

        final List<Bitmap> Images = musicinfo.getImages(this, musicinfo.musicUris);
        //


        Adapter a = new Adapter(musicNames, Images, -1, null, this, AlbumNames);
        music.setAdapter(a);

        tosong = new Intent(music.this, NowPlaying.class);


        loading.dismiss();

        music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String selectedMusic = musicNames.get(position);
                String selectedAlbumName = AlbumNames.get(position);
                Bitmap selectedImage = Images.get(position);

                //convert image to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Uri selected = musicinfo.musicUris.get(position);
                tosong.putExtra("songUri", selected);
                tosong.putExtra("songname", selectedMusic);
                tosong.putExtra("albumpicture", byteArray);
                tosong.putExtra("albumname", selectedAlbumName);
                tosong.putExtra("urlposition", position);
                tosong.putExtra("getDuration", musicinfo.getDuration(music.this, selected));

                if (!pref.contains("gotsong")) {// if no fav music set this one
                    musicinfo.setpref(music.this, pref, 3, selected);
                }

                finish();

                startActivity(tosong);
            }
        });
    }

    private void setfavalbum(SharedPreferences pref) {
        SharedPreferences.Editor editor = pref.edit();

        editor = pref.edit();
        editor.putString("favoritalbum", getIntent().getStringExtra("almumname"));
        editor.putString("gotmusic", getIntent().getStringExtra("albumtree"));
        editor.apply();
    }

    public void NewMusicPath(View view) {

        musicinfo.initializeIntent(music.this);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            musicinfo.setUris(this, data.getData(), pref, requestCode);
            pref.edit().remove("favoritalbum").apply();
            loading.dismiss();
            finish();

        }


    }


}
