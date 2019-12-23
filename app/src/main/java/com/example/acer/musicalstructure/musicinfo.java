package com.example.acer.musicalstructure;

import android.content.Context;

public class musicinfo {

    private Context x;
    private int[] images = {R.mipmap.air, R.mipmap.bike, R.mipmap.coconut, R.mipmap.palm, R.mipmap.paradise, R.mipmap.woman};
    private String[] albumnames;
    private String[][] musicnames;

    public musicinfo(Context x) {
        this.x = x;
    }

    public int[] Images() {
        return images;
    }

    public String[] Albumnames() {
        albumnames = x.getResources().getStringArray(R.array.albumnames);
        return albumnames;
    }

    public String[][] Musicnames() {
        return musicnames = new String[][]{x.getResources().getStringArray(R.array.thesteam), x.getResources().getStringArray(R.array.midcountry), x.getResources().getStringArray(R.array.life), x.getResources().getStringArray(R.array.beachhead), x.getResources().getStringArray(R.array.paradise), x.getResources().getStringArray(R.array.mysoul)};
    }
}
