package com.omar.acer.musicalstructure;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final dialog loading = new dialog(this);

    SharedPreferences pref;
    boolean isdialogopen;
    private int settings;
    private int favoritmusic;

    private void permissions() {
        final List<String> listPermissionsNeeded = new ArrayList<>();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);


        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }

        if (!listPermissionsNeeded.isEmpty()) {


           alertDialog();

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button toalbum = findViewById(R.id.toalbums);
        Button tomusic = findViewById(R.id.tomusic);
        final Button tonowplaying = findViewById(R.id.toplayingsong);


       pref = getSharedPreferences("MyPref", 0);

//to go to the fav music by default
        if (getIntent().getAction()!=null&& pref.contains("favorite") && pref.getInt("favorite", 0) == 1) {

            musicinfo.intents(this, 3);
          finish();
        }
        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loading.Loading();
                musicinfo.navigation(MainActivity.this, 2, pref);


            }
        });
        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (musicinfo.issongopen)
                   onBackPressed();
                else{
                    musicinfo.navigation(MainActivity.this, 3, pref);


                }
            }
        });
        tomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loading.Loading();
                musicinfo.navigation(MainActivity.this, 1, pref);


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isdialogopen)
            permissions();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {


        if (data != null) {
            musicinfo.getUris(this, data.getData(), pref, requestCode);


            finish();


        }else
            loading.dismiss();


    }


    private void alertDialog() {

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialogue);
        final Button ok = d.findViewById(R.id.ok);
        final TextView tv = d.findViewById(R.id.textView);
        tv.setText("App requires storage access permission");

        final LinearLayout checkboxes = d.findViewById(R.id.checkboxes);
        checkboxes.setVisibility(View.GONE);

        d.setCancelable(false);

        d.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(MainActivity.this, "Click on permissions", Toast.LENGTH_SHORT).show();
                openAppSettings();
                d.dismiss();
            }
        });

        d.show();

        isdialogopen = true;
    }

    private void openAppSettings() {

        final Uri packageUri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);

        final Intent applicationDetailsSettingsIntent = new Intent();

        applicationDetailsSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        applicationDetailsSettingsIntent.setData(packageUri);
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(applicationDetailsSettingsIntent);

    }


    public void settings(final View view) {



        view.animate().alpha(0.9f).setDuration(200).start();
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialogue);
        final Button ok = d.findViewById(R.id.ok);
        final TextView tv = d.findViewById(R.id.textView);
        tv.setText("Settings");

        final CheckBox restart = d.findViewById(R.id.restart);
        final CheckBox next = d.findViewById(R.id.next);
        final CheckBox stopsong = d.findViewById(R.id.stopsong);
        final CheckBox songfav = d.findViewById(R.id.fav);


        d.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                restart.setEnabled(false);
                stopsong.setEnabled(false);

                if (settings > 0) {
                   settings = 0;
                    restart.setEnabled(true);
                    stopsong.setEnabled(true);
                } else
                    settings = 1;
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                next.setEnabled(false);
                stopsong.setEnabled(false);

                if (settings > 0) {
                    settings = 0;
                    next.setEnabled(true);
                    stopsong.setEnabled(true);
                } else
                    settings = 2;

            }
        });

        stopsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                restart.setEnabled(false);
                next.setEnabled(false);

                if (settings > 0) {
                    settings = 0;
                    restart.setEnabled(true);
                    next.setEnabled(true);
                } else
                    settings = 3;

            }
        });

        if (pref.contains("settings")) {

            switch (pref.getInt("settings", 0)) {

                case 1:
                    next.performClick();
                    break;
                case 2:
                    restart.performClick();

                    break;
                case 3:
                    stopsong.performClick();

                    break;
            }
        }

        songfav.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(final View v) {

                if (favoritmusic > 0) {
                    favoritmusic = 0;
                } else
                    favoritmusic = 1;

            }
        });

        if (pref.contains("favorite")) {
            if (pref.getInt("favorite", 0) == 1)
                songfav.performClick();

        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(MainActivity.this, "SAVED", Toast.LENGTH_SHORT).show();
                pref.edit().putInt("settings", settings).apply();
                pref.edit().putInt("favorite", favoritmusic).apply();
                settings = 0;
                favoritmusic = 0;

                d.dismiss();
            }
        });


        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                settings = 0;
                favoritmusic = 0;

            }
        });

        d.show();


    }

}