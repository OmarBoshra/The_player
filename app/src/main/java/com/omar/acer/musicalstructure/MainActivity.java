package com.omar.acer.musicalstructure;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private final dialog loading = new dialog(this);

    SharedPreferences pref;
    boolean isdialogopen;
    private int settings;
    private int favoritmusic;

    private void permissions() {
        final List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }
            
            if (!allGranted) {
                alertDialog();
            }
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

        if (getIntent().getAction() != null && pref.contains("favorite") && pref.getInt("favorite", 0) == 1) {
            String songUriStr = pref.getString("gotsong", null);
            Uri songUri = songUriStr != null ? Uri.parse(songUriStr) : null;
            musicinfo.intents(this, 3, songUri);
            finish();
            return;
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
                Intent intent = new Intent(MainActivity.this, NowPlaying.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
        if (!isdialogopen) {
            permissions();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            musicinfo.getUris(this, data.getData(), pref, requestCode);
            finish();
        } else {
            loading.dismiss();
        }
    }

    private void alertDialog() {
        if (isdialogopen) return;

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialogue);
        final Button ok = d.findViewById(R.id.ok);
        final TextView tv = d.findViewById(R.id.textView);
        if (tv != null) tv.setText("App requires storage access permission");

        final LinearLayout checkboxes = d.findViewById(R.id.checkboxes);
        if (checkboxes != null) checkboxes.setVisibility(View.GONE);

        d.setCancelable(false);
        if (d.getWindow() != null) {
            d.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(MainActivity.this, "Click on permissions", Toast.LENGTH_SHORT).show();
                openAppSettings();
                d.dismiss();
                isdialogopen = false;
            }
        });

        d.show();
        isdialogopen = true;
    }

    private void openAppSettings() {
        final Uri packageUri = Uri.fromParts("package", getPackageName(), null);
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void settings(final View view) {
        view.animate().alpha(0.9f).setDuration(200).start();
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialogue);
        final Button ok = d.findViewById(R.id.ok);
        final TextView tv = d.findViewById(R.id.textView);
        if (tv != null) tv.setText("Settings");

        final CheckBox restart = d.findViewById(R.id.restart);
        final CheckBox next = d.findViewById(R.id.next);
        final CheckBox stopsong = d.findViewById(R.id.stopsong);
        final CheckBox songfav = d.findViewById(R.id.fav);

        if (d.getWindow() != null) {
            d.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (next.isChecked()) {
                    settings = 1;
                    restart.setChecked(false);
                    stopsong.setChecked(false);
                } else {
                    settings = 0;
                }
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (restart.isChecked()) {
                    settings = 2;
                    next.setChecked(false);
                    stopsong.setChecked(false);
                } else {
                    settings = 0;
                }
            }
        });

        stopsong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (stopsong.isChecked()) {
                    settings = 3;
                    next.setChecked(false);
                    restart.setChecked(false);
                } else {
                    settings = 0;
                }
            }
        });

        if (pref.contains("settings")) {
            int savedSettings = pref.getInt("settings", 0);
            settings = savedSettings;
            switch (savedSettings) {
                case 1: next.setChecked(true); break;
                case 2: restart.setChecked(true); break;
                case 3: stopsong.setChecked(true); break;
            }
        }

        songfav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                favoritmusic = songfav.isChecked() ? 1 : 0;
            }
        });

        if (pref.contains("favorite")) {
            favoritmusic = pref.getInt("favorite", 0);
            songfav.setChecked(favoritmusic == 1);
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(MainActivity.this, "SAVED", Toast.LENGTH_SHORT).show();
                pref.edit().putInt("settings", settings).apply();
                pref.edit().putInt("favorite", favoritmusic).apply();
                d.dismiss();
            }
        });

        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                // Keep existing settings on cancel
            }
        });

        d.show();
    }
}
