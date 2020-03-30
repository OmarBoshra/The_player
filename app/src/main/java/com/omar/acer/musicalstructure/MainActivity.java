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


            this.alertDialog();

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        final Button toalbum = this.findViewById(R.id.toalbums);
        Button tomusic = this.findViewById(R.id.tomusic);
        final Button tonowplaying = this.findViewById(R.id.toplayingsong);


        this.pref = getSharedPreferences("MyPref", 0);

//to go to the fav music by default
        if (this.getIntent().getAction()!=null&& this.pref.contains("favorite") && this.pref.getInt("favorite", 0) == 1) {

            musicinfo.intents(this, 3);
            this.finish();
        }
        toalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.loading.Loading();
                musicinfo.navigation(MainActivity.this, 2, MainActivity.this.pref);


            }
        });
        tonowplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (musicinfo.issongopen)
                    MainActivity.this.onBackPressed();
                else{
                    musicinfo.navigation(MainActivity.this, 3, MainActivity.this.pref);



                }
            }
        });
        tomusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity.this.loading.Loading();
                musicinfo.navigation(MainActivity.this, 1, MainActivity.this.pref);


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!this.isdialogopen)
            this.permissions();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {


        if (data != null) {
            musicinfo.getUris(this, data.getData(), this.pref, requestCode);

            grantUriPermission(getPackageName(), data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
            int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);// Check for the freshest data.
            //noinspection WrongConstant
            getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);

            this.finish();


        }else
            this.loading.dismiss();


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
                MainActivity.this.openAppSettings();
                d.dismiss();
            }
        });

        d.show();

        this.isdialogopen = true;
    }

    private void openAppSettings() {

        final Uri packageUri = Uri.fromParts("package", this.getApplicationContext().getPackageName(), null);

        final Intent applicationDetailsSettingsIntent = new Intent();

        applicationDetailsSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        applicationDetailsSettingsIntent.setData(packageUri);
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.startActivity(applicationDetailsSettingsIntent);

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

                if (MainActivity.this.settings > 0) {
                    MainActivity.this.settings = 0;
                    restart.setEnabled(true);
                    stopsong.setEnabled(true);
                } else
                    MainActivity.this.settings = 1;
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                next.setEnabled(false);
                stopsong.setEnabled(false);

                if (MainActivity.this.settings > 0) {
                    MainActivity.this.settings = 0;
                    next.setEnabled(true);
                    stopsong.setEnabled(true);
                } else
                    MainActivity.this.settings = 2;

            }
        });

        stopsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                restart.setEnabled(false);
                next.setEnabled(false);

                if (MainActivity.this.settings > 0) {
                    MainActivity.this.settings = 0;
                    restart.setEnabled(true);
                    next.setEnabled(true);
                } else
                    MainActivity.this.settings = 3;

            }
        });

        if (this.pref.contains("settings")) {

            switch (this.pref.getInt("settings", 0)) {

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

                if (MainActivity.this.favoritmusic > 0) {
                    MainActivity.this.favoritmusic = 0;
                } else
                    MainActivity.this.favoritmusic = 1;

            }
        });

        if (this.pref.contains("favorite")) {
            if (this.pref.getInt("favorite", 0) == 1)
                songfav.performClick();

        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(MainActivity.this, "SAVED", Toast.LENGTH_SHORT).show();
                MainActivity.this.pref.edit().putInt("settings", MainActivity.this.settings).apply();
                MainActivity.this.pref.edit().putInt("favorite", MainActivity.this.favoritmusic).apply();
                MainActivity.this.settings = 0;
                MainActivity.this.favoritmusic = 0;

                d.dismiss();
            }
        });


        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                MainActivity.this.settings = 0;
                MainActivity.this.favoritmusic = 0;

            }
        });

        d.show();


    }

}