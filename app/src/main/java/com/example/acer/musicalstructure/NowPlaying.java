package com.example.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NowPlaying extends AppCompatActivity {

    private int pauseorplay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);


        SharedPreferences pref = this.getSharedPreferences("MyPref", 0);


        Button tohome = findViewById(R.id.toHome);
        Button gomusic = findViewById(R.id.tomusic);
        Button toalbum = findViewById(R.id.toalbums);


        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(NowPlaying.this, MainActivity.class);
                tohome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(tohome);

            }
        });


        gomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gomusic = new Intent(NowPlaying.this, music.class);
                gomusic.putExtra("viewmusic", 0);
                startActivity(gomusic);
            }
        });
        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toalbum = new Intent(NowPlaying.this, Albums.class);
                startActivity(toalbum);
            }
        });


        ImageView iv = findViewById(R.id.viewedimage);
        TextView song = findViewById(R.id.songname);
        TextView album = findViewById(R.id.albumname);

        ImageButton rewind = findViewById(R.id.rewind);
        ImageButton stop = findViewById(R.id.stop);
        final ImageButton play_pause = findViewById(R.id.play_pause);
        ImageButton fastforward = findViewById(R.id.fastforward);

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NowPlaying.this, "Rewind", Toast.LENGTH_SHORT).show();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NowPlaying.this, "Stop", Toast.LENGTH_SHORT).show();

            }
        });
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pauseorplay == 0) {
                    Toast.makeText(NowPlaying.this, "Pause", Toast.LENGTH_SHORT).show();
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                    pauseorplay = 1;
                } else {
                    play_pause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                    Toast.makeText(NowPlaying.this, "Play", Toast.LENGTH_SHORT).show();
                    pauseorplay = 0;
                }
            }
        });
        fastforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NowPlaying.this, "Fast forward", Toast.LENGTH_SHORT).show();

            }
        });
        if (getIntent().getExtras() != null) {

            if (getIntent().getExtras().containsKey("songname")) {
                song.setText(getIntent().getStringExtra("songname"));
                album.setText(getIntent().getStringExtra("albumname"));
                iv.setImageResource(getIntent().getIntExtra("albumpicture", -1));


                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.putInt("albumpicture", getIntent().getIntExtra("albumpicture", -1));
                editor.putString("songname", getIntent().getStringExtra("songname"));
                editor.putString("albumname", getIntent().getStringExtra("albumname"));
                editor.apply();
            } else {
                song.setText(pref.getString("songname", ""));
                album.setText(pref.getString("albumname", ""));
                iv.setImageResource(pref.getInt("albumpicture", 0));


            }


        }
    }
}
