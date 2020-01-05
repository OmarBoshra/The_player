package com.omar.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class Albums extends AppCompatActivity {
    private Intent tomusic;
    private String[] albumnames;
    private SharedPreferences pref;
    private dialog loading = new dialog(Albums.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        loading.Loading();

        Button tohome = findViewById(R.id.toHome);
        Button gomusic = findViewById(R.id.tomusic);
        Button tonowplaying = findViewById(R.id.toplayingsong);


        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(Albums.this, MainActivity.class);
                startActivity(tohome);
                finish();

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
                    musicinfo.navigation(Albums.this, 3, pref);

                }


            }
        });
        gomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.Loading();
                musicinfo.navigation(Albums.this, 1, pref);
            }
        });

        ListView albums = findViewById(R.id.albumslist);

//showing albums

        final List<String> AlbumNames = musicinfo.getAlbumNames(this, musicinfo.albumUris);

        final List<Bitmap> Images = musicinfo.getImages(this, musicinfo.albumUris);

        Adapter a = new Adapter(null, Images, 0, null, this, AlbumNames);

        albums.setAdapter(a);


        tomusic = new Intent(Albums.this, music.class);

        loading.dismiss();

        albums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Uri albumUri = musicinfo.albumUris.get(position);

                String albumname = musicinfo.getAlbumNames(Albums.this, Arrays.asList(albumUri)).get(0);

                DocumentFile albumstree = DocumentFile.fromTreeUri(Albums.this, Uri.parse(pref.getString("gotAlbums", null)));

                List<Uri> relatedMusic = musicinfo.getFiles(albumstree, 4, Albums.this, albumname);

                musicinfo.musicUris = relatedMusic;

                tomusic.putExtra("almumname", albumname);// music will show opnly album music
                tomusic.putExtra("albumtree", pref.getString("gotAlbums", null));// music will show opnly album music

                finish();

                startActivity(tomusic);
            }
        });

    }

    public void NewAlbumsPath(View view) {

        musicinfo.initializeIntent(Albums.this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            musicinfo.setUris(this, data.getData(), pref, requestCode);

            finish();
            loading.dismiss();
        }


    }

}
