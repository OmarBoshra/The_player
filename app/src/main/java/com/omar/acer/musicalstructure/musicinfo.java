package com.omar.acer.musicalstructure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class musicinfo {

    static List<Uri> musicUris;
    static List<Uri> albumUris;
    static boolean issongopen;


    static List<String> getMusicNames(final Context context, final List<Uri> Uris) {//get the music names


        final List<String> musicNames = new ArrayList<String>();


        for (final Uri uri : Uris) {
            final MediaMetadataRetriever mData = new MediaMetadataRetriever();
            mData.setDataSource(context, uri);
            musicNames.add(mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));


        }//filling the album names music titles and image arrays inside the selected folder
        return musicNames;
    }


    static void initializeIntent(final Activity activity) {


        switch (activity.getComponentName().getClassName()) {

            case "com.omar.acer.musicalstructure.NowPlaying":

                Toast.makeText(activity, "Choose a song to play", Toast.LENGTH_SHORT).show();

                final Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                activity.startActivityForResult(Intent.createChooser(intent, "Choose Track"), 3);

                break;

            default:

                final Intent folderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                Intent.createChooser(folderIntent, "Choose The playlist folder");

                if (activity.getComponentName().getClassName().equals("com.omar.acer.musicalstructure.music")) {
                    Toast.makeText(activity, "Choose The playlist folder", Toast.LENGTH_LONG).show();
                    activity.startActivityForResult(folderIntent, 1);

                } else {
                    Toast.makeText(activity, "Choose The albums folder", Toast.LENGTH_LONG).show();
                    activity.startActivityForResult(folderIntent, 2);

                }
                break;


        }
    }


    static List<String> getAlbumNames(final Context context, final List<Uri> Uris) {//get the AlbumNames

        final List<String> AlbumNames = new ArrayList<String>();


        for (final Uri uri : Uris) {
            final MediaMetadataRetriever mData = new MediaMetadataRetriever();
            mData.setDataSource(context, uri);
            AlbumNames.add(mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));


        }//filling the album names music titles and image arrays inside the selected folder
        return AlbumNames;
    }

    static int getDuration(final Context context, final Uri uri) {

        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        mData.setDataSource(context, uri);

        final int duration = Integer.parseInt(mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));


        return duration;
    }

    static List<Bitmap> getImages(final Context context, final List<Uri> Uris) {// get the images

        final List<Bitmap> Images = new ArrayList<Bitmap>();


        for (final Uri uri : Uris) {
            final MediaMetadataRetriever mData = new MediaMetadataRetriever();
            mData.setDataSource(context, uri);

            try {
                // Récupération et affichage de la pochette
                final byte[] art = mData.getEmbeddedPicture();
                final Bitmap image = BitmapFactory.decodeByteArray(art, 0, art.length);

                Images.add(image);


            } catch (final Exception e) {
                e.printStackTrace();

                Images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.iconmain));

            }

        }//filling the album names music titles and image arrays inside the selected folder
        return Images;
    }


    static void setpref(final Context context, final SharedPreferences pref, final int requestcode, final Uri uri) {


        SharedPreferences.Editor editor = pref.edit();

        if (requestcode == 3) {

            editor.putString("gotsong", uri.toString());// save the song uri

            if (pref.contains("gotparentSongFolderUri"))//to set new song in new path
                editor.remove("gotparentSongFolderUri");

            editor.putString("gotsongname", getMusicNames(context, Arrays.asList(uri)).get(0));// save the song name
            editor.putString("gotsongalbum", getAlbumNames(context, Arrays.asList(uri)).get(0));// save the song album

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getImages(context, Arrays.asList(uri)).get(0).compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
            final byte[] b = baos.toByteArray();

            final String encoded = Base64.encodeToString(b, Base64.DEFAULT);

            editor.putString("gotsongimage", encoded);// save the song image
            editor.putInt("gotsongduration", getDuration(context, uri));// save the song image


            editor.apply();
        } else {

            editor = pref.edit();
            editor.putString(requestcode == 2 ? "gotAlbums" : "gotmusic", uri.toString());
            editor.apply();
        }
    }

    static void getUris(Context context, final Uri uri, final SharedPreferences pref, final int requestCode) {


        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //noinspection WrongConstant
        context.getContentResolver().takePersistableUriPermission(uri, 3);

        switch (requestCode) {

            case 3:

                setpref(context, pref, requestCode, uri);
                intents(context, requestCode);
                break;
            default:
                DocumentFile musicfile = DocumentFile.fromTreeUri(context, uri);

                if (requestCode == 2) //for albums
                    albumUris = getFiles(musicfile, 2, context, null);
                else if (pref.contains("favoritalbum"))//in case its a playlist of a fav album
                    musicUris =getFiles(musicfile, 4, context, pref.getString("favoritalbum", null));
                else
                    musicUris =getFiles(musicfile, 1, context, null);

                musicfile = null;

                if (requestCode == 2 ? albumUris == null : musicUris == null) {

                    Toast.makeText(context, "Please choose a folder with music in it", Toast.LENGTH_LONG).show();
                } else {
                    if (requestCode < 5)//so request code 5 doesn't override favorites
                       setpref(context, pref, requestCode, uri);
                    intents(context, requestCode);

                }
        }

    }


    static void intents(final Context context, final int requestcode) {

        final Intent tomusic;

        switch (requestcode) {

            case 1:
                tomusic = new Intent(context, music.class);

                break;
            case 2:
                tomusic = new Intent(context, Albums.class);


                break;
            default:

                tomusic = new Intent(context, NowPlaying.class);
                if (requestcode == 5)
                    tomusic.putExtra("next", true);
                else if (requestcode == 6)
                    tomusic.putExtra("next", false);


        }


        context.startActivity(tomusic);
    }

    static void navigation(final Activity activity, final int requestcode, final SharedPreferences pref) {

        switch (requestcode) {

            case 3:

                if (pref.contains("gotmusic"))
                    pref.edit().putString("gotparentSongFolderUri", pref.getString("gotmusic", "")).apply();
                else if (pref.contains("gotAlbums"))
                    pref.edit().putString("gotparentSongFolderUri", pref.getString("gotAlbums", "")).apply();

                if (pref.contains("gotsong")) {

                    intents(activity, 3);
                    activity.finish();
                } else {

                    Toast.makeText(activity, "Choose a song to play", Toast.LENGTH_SHORT).show();
                    final Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    activity.startActivityForResult(Intent.createChooser(intent, "Choose Track"), 3);

                }
                break;
            case 2:

                if (pref.contains("gotAlbums")) {
                    getUris(activity, Uri.parse(pref.getString("gotAlbums", null)), pref, 2);
                } else if (pref.contains("gotmusic"))
                    getUris(activity, Uri.parse(pref.getString("gotmusic", null)), pref, 2);
                else if (pref.contains("gotparentSongFolderUri"))
                    getUris(activity, Uri.parse(pref.getString("gotparentSongFolderUri", null)), pref, 2);


                else {
                    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    Intent.createChooser(intent, "Choose The Albums folder");
                    Toast.makeText(activity, "Choose The Albums folder", Toast.LENGTH_LONG).show();

                    activity.startActivityForResult(intent, 2);
return;
                }
                if(!(activity instanceof NowPlaying))
                    activity.finish();
                break;
            case 1:

                if (pref.contains("gotmusic")) {
                    getUris(activity, Uri.parse(pref.getString("gotmusic", null)), pref, 1);
                } else if (pref.contains("gotAlbums"))
                    getUris(activity, Uri.parse(pref.getString("gotAlbums", null)), pref, 1);
                else if (pref.contains("gotparentSongFolderUri"))
                    getUris(activity, Uri.parse(pref.getString("gotparentSongFolderUri", null)), pref, 1);

                else {
                    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    Intent.createChooser(intent, "Choose The playlist folder");
                    Toast.makeText(activity, "Choose The playlist folder", Toast.LENGTH_LONG).show();

                    activity.startActivityForResult(intent, 1);
                    return;
                }
                if(!(activity instanceof NowPlaying))
                    activity.finish();

                break;

        }


    }


    static List<Uri> getFiles(DocumentFile df, int requestcode, Context context, String albumname) {




        final List<Uri> fileUris = new ArrayList<>();
        boolean gotmp3 = false;
        final DocumentFile[] files = df.listFiles();



        String albumName = albumname;

        if (files != null) {
            for (final DocumentFile file : files) {

                if (file.getName().endsWith("mp3") || file.getName().endsWith("WAV") || file.getName().endsWith("MP4") || file.getName().endsWith("FLAC") || file.getName().endsWith("M4A")) {

                    if (requestcode == 2) {//to get albums

                        if (albumName == null || !albumName.equals(getAlbumNames(context, Arrays.asList(file.getUri())).get(0))) {

                            albumName = getAlbumNames(context, Arrays.asList(file.getUri())).get(0);
                            if (albumName != null) {
                                fileUris.add(file.getUri());
                                gotmp3 = true;
                            }
                        }

                    } else if (requestcode == 4) {//get music related to album

                        if (albumName.equals(getAlbumNames(context, Arrays.asList(file.getUri())).get(0))) {
                            fileUris.add(file.getUri());
                            gotmp3 = true;
                        }

                    } else {//get music

                        fileUris.add(file.getUri());
                        gotmp3 = true;
                    }
                }


            }
        }
        if (gotmp3)
            return fileUris;
        else
            return null;


    }

}
