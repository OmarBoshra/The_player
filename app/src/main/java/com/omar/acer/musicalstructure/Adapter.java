package com.omar.acer.musicalstructure;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class Adapter extends BaseAdapter {

    private final List<String> music;
    private final List<String> albums;
    private final List<Bitmap> rimg;
    private final Context a;
    private final int singleimage;
    private final String singlealbum;

    public Adapter(final List<String> music, final List<Bitmap> rimg, final int singleimage, final String singlealbum, final Context a, final List<String> albums) {
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
            return music.size();
        } else
            return albums.size();
    }

    @Override
    public Object getItem(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        View vie = convertView;

        if (vie == null) {
            vie = LayoutInflater.from(a).inflate(
                    R.layout.listviewtemplate, parent, false);
        }

        final TextView album = vie.findViewById(R.id.albumname);
        final TextView songs = vie.findViewById(R.id.song);

        if (music == null) {//hide song when viewing album
            songs.setVisibility(View.GONE);
        } else {
            songs.setText(music.get(position));
        }
        final ImageView imge = vie.findViewById(R.id.albumimage);

        if (singlealbum != null) {// for music


            album.setText(singlealbum);
            imge.setImageResource(singleimage);
        }

        if (singleimage == 0) {// in albums
            imge.setImageBitmap(rimg.get(position));
            album.setText(albums.get(position));


        } else if (singleimage == -1) {//for all music

            imge.setImageBitmap(rimg.get(position));
            album.setText(albums.get(position));
        }

        return vie;
    }
}
