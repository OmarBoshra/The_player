package com.example.acer.musicalstructure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

class Adapter extends BaseAdapter {

    private final String[] music;
    private final String[] albums;
    private final int[] rimg;
    private final Context a;
    private final int singleimage;
    private final String singlealbum;

    public Adapter(String[] music, int[] rimg, int singleimage, String singlealbum, Context a, String[] albums) {
        this.music = music;
        this.albums = albums;
        this.singlealbum = singlealbum;
        this.rimg = rimg;
        this.singleimage = singleimage;
        this.a = a;
    }

    @Override
    public int getCount() {
        if (music != null) {
            return music.length;
        } else
            return albums.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vie = inflater.inflate(R.layout.listviewtemplate, null);
        TextView album = vie.findViewById(R.id.albumname);
        TextView songs = vie.findViewById(R.id.song);

        if (music == null) {//hide song when viewing album
            songs.setVisibility(View.GONE);
        } else {
            songs.setText(music[position]);
        }
        ImageView imge = vie.findViewById(R.id.albumimage);

        if (singlealbum != null) {// for music


            album.setText(singlealbum);
            imge.setImageResource(singleimage);
            imge.setTag(singleimage);
        }

        if (singleimage == 0) {// in albums
            imge.setImageResource(rimg[position]);
            album.setText(albums[position]);


        } else if (singleimage == -1) {//for all music

            imge.setImageResource(rimg[position / 6]);
            imge.setTag((rimg[position / 6]));
            album.setText(albums[position / 6]);
        }

        return vie;
    }
}
