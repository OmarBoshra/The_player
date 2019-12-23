package com.example.acer.musicalstructure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class music extends AppCompatActivity {
    private Intent tosong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);


        Button tohome = findViewById(R.id.toHome);
        Button toalbum = findViewById(R.id.toalbums);
        Button tonowplaying = findViewById(R.id.toplayingsong);


        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toalbums = new Intent(music.this, Albums.class);
                startActivity(toalbums);

            }
        });

        final SharedPreferences pref = this.getSharedPreferences("MyPref", 0);

        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.contains("songname")) {
                    Intent toNowPlaying = new Intent(music.this, NowPlaying.class);
                    toNowPlaying.putExtra("frommainpage", 0);
                    startActivity(toNowPlaying);
                } else
                    Toast.makeText(music.this, "No playing songs", Toast.LENGTH_SHORT).show();


            }
        });
        tohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tohome = new Intent(music.this, MainActivity.class);
                tohome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(tohome);
            }
        });


        ListView music = findViewById(R.id.musiclist);
        int[] images = new musicinfo(this).Images();
        String[][] musicnames = new musicinfo(this).Musicnames();
        if (getIntent().getExtras() != null) {

            Adapter a;
            if (getIntent().getExtras().containsKey("fromalbum")) {
                String albumname = getIntent().getStringExtra("album");

                int musicnumber = getIntent().getIntExtra("fromalbum", -1);

                a = new Adapter(musicnames[musicnumber], null, images[musicnumber], albumname, this, null);
                music.setAdapter(a);


            } else {

                String[] albumnames = new musicinfo(this).Albumnames();
                a = new Adapter(getResources().getStringArray(R.array.combination), images, -1, null, this, albumnames);
                music.setAdapter(a);

            }


        }
        tosong = new Intent(music.this, NowPlaying.class);

        music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                TextView albumview = view.findViewById(R.id.albumname);
                TextView songview = view.findViewById(R.id.song);
                ImageView albumimage = view.findViewById(R.id.albumimage);

                tosong.putExtra("songname", songview.getText().toString());

                tosong.putExtra("albumpicture", (Integer) albumimage.getTag());
                tosong.putExtra("albumname", albumview.getText().toString());

                startActivity(tosong);
            }
        });
    }
}
