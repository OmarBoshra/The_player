package com.example.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class Albums extends AppCompatActivity {
    private Intent tomusic;
    private String[] albumnames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);


        Button tohome = findViewById(R.id.toHome);
        Button gomusic = findViewById(R.id.tomusic);
        Button tonowplaying = findViewById(R.id.toplayingsong);


        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(Albums.this, MainActivity.class);
                tohome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(tohome);


            }
        });

        final SharedPreferences pref = this.getSharedPreferences("MyPref", 0);

        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.contains("songname")) {
                    Intent toNowPlaying = new Intent(Albums.this, NowPlaying.class);
                    toNowPlaying.putExtra("frommainpage", 0);
                    startActivity(toNowPlaying);
                } else
                    Toast.makeText(Albums.this, "No playing songs", Toast.LENGTH_SHORT).show();


            }
        });
        gomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tomusic = new Intent(Albums.this, music.class);
                tomusic.putExtra("viewmusic", 0);
                startActivity(tomusic);
            }
        });

        ListView albums = findViewById(R.id.albumslist);

        int[] images = new musicinfo(this).Images();
        albumnames = new musicinfo(this).Albumnames();
        Adapter a = new Adapter(null, images, 0, null, this, albumnames);
        albums.setAdapter(a);


        tomusic = new Intent(Albums.this, music.class);
        albums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                tomusic.putExtra("fromalbum", position);
                tomusic.putExtra("album", albumnames[position]);

                startActivity(tomusic);
            }
        });

    }
}
