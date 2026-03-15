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
        } else if (albums != null) {
            return albums.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View vie = convertView;
        if (vie == null) {
            vie = LayoutInflater.from(a).inflate(R.layout.listviewtemplate, parent, false);
        }

        final TextView albumTv = vie.findViewById(R.id.albumname);
        final TextView songsTv = vie.findViewById(R.id.song);
        final ImageView imge = vie.findViewById(R.id.albumimage);

        if (music == null) {
            if (songsTv != null) songsTv.setVisibility(View.GONE);
        } else {
            if (songsTv != null) {
                songsTv.setVisibility(View.VISIBLE);
                if (position < music.size()) {
                    songsTv.setText(music.get(position));
                }
            }
        }

        if (singlealbum != null) {
            if (albumTv != null) albumTv.setText(singlealbum);
            if (imge != null) imge.setImageResource(singleimage);
        } else if (singleimage == 0 || singleimage == -1) {
            if (albums != null && position < albums.size()) {
                if (albumTv != null) albumTv.setText(albums.get(position));
            }
            if (rimg != null && position < rimg.size()) {
                if (imge != null) imge.setImageBitmap(rimg.get(position));
            } else if (imge != null) {
                imge.setImageResource(R.drawable.iconmain);
            }
        }

        return vie;
    }
}
