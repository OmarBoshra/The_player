package com.example.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toalbum = findViewById(R.id.toalbums);
        final Button tomusic = findViewById(R.id.tomusic);
        Button tonowplaying = findViewById(R.id.toplayingsong);

        final SharedPreferences pref = this.getSharedPreferences("MyPref", 0);

        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toalbums = new Intent(MainActivity.this, Albums.class);
                startActivity(toalbums);

            }
        });
        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.contains("songname")) {
                    Intent toNowPlaying = new Intent(MainActivity.this, NowPlaying.class);
                    toNowPlaying.putExtra("frommainpage", 0);
                    startActivity(toNowPlaying);
                } else
                    Toast.makeText(MainActivity.this, "No playing songs", Toast.LENGTH_SHORT).show();


            }
        });
        tomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tomusic = new Intent(MainActivity.this, music.class);
                tomusic.putExtra("viewmusic", 0);
                startActivity(tomusic);
            }
        });


    }
}
