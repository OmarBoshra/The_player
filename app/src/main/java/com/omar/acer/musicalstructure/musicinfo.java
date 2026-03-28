package com.omar.acer.musicalstructure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class musicinfo {

    static List<Uri> musicUris = new ArrayList<>();
    static List<Uri> albumUris = new ArrayList<>();
    static boolean issongopen;

    static class SongMetadata {
        String title;
        String album;
        int duration;
        Bitmap image;

        SongMetadata() {
            title = "Unknown Title";
            album = "Unknown Album";
            duration = 0;
        }
    }

    static SongMetadata getMetadata(Context context, Uri uri) {
        SongMetadata metadata = new SongMetadata();
        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        try {
            mData.setDataSource(context, uri);
            metadata.title = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (metadata.title == null || metadata.title.isEmpty()) {
                DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                metadata.title = (file != null) ? file.getName() : "Unknown Title";
            }
            metadata.album = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (metadata.album == null || metadata.album.isEmpty()) {
                metadata.album = "Unknown Album";
            }
            String durationStr = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                metadata.duration = Integer.parseInt(durationStr);
            }
            byte[] art = mData.getEmbeddedPicture();
            if (art != null) {
                metadata.image = BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            Log.e("musicinfo", "Error getting metadata for " + uri, e);
        } finally {
            try {
                mData.release();
            } catch (Exception ignored) {}
        }
        return metadata;
    }

    static List<String> getMusicNames(final Context context, final List<Uri> Uris) {
        final List<String> musicNames = new ArrayList<>();
        if (Uris == null) return musicNames;
        for (final Uri uri : Uris) {
            musicNames.add(getSingleMusicName(context, uri));
        }
        return musicNames;
    }

    static String getSingleMusicName(Context context, Uri uri) {
        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        try {
            mData.setDataSource(context, uri);
            String title = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title == null || title.isEmpty()) {
                DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                title = (file != null) ? file.getName() : "Unknown Title";
            }
            return title;
        } catch (Exception e) {
            return "Unknown Title";
        } finally {
            try {
                mData.release();
            } catch (Exception ignored) {}
        }
    }

    static List<String> getAlbumNames(final Context context, final List<Uri> Uris) {
        final List<String> AlbumNames = new ArrayList<>();
        if (Uris == null) return AlbumNames;
        for (final Uri uri : Uris) {
            AlbumNames.add(getSingleAlbumName(context, uri));
        }
        return AlbumNames;
    }

    static String getSingleAlbumName(Context context, Uri uri) {
        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        try {
            mData.setDataSource(context, uri);
            String album = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            return album != null ? album : "Unknown Album";
        } catch (Exception e) {
            return "Unknown Album";
        } finally {
            try {
                mData.release();
            } catch (Exception ignored) {}
        }
    }

    static int getDuration(final Context context, final Uri uri) {
        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        try {
            mData.setDataSource(context, uri);
            final String durationStr = mData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return durationStr != null ? Integer.parseInt(durationStr) : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            try {
                mData.release();
            } catch (Exception ignored) {}
        }
    }

    static Bitmap getSingleImage(Context context, Uri uri) {
        final MediaMetadataRetriever mData = new MediaMetadataRetriever();
        try {
            mData.setDataSource(context, uri);
            final byte[] art = mData.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (final Exception ignored) {
        } finally {
            try {
                mData.release();
            } catch (Exception ignored) {}
        }
        return null;
    }

    static List<Bitmap> getImages(final Context context, final List<Uri> Uris) {
        final List<Bitmap> Images = new ArrayList<>();
        if (Uris == null) return Images;
        for (final Uri uri : Uris) {
            Bitmap bitmap = getSingleImage(context, uri);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iconmain);
            }
            Images.add(bitmap);
        }
        return Images;
    }

    static void setpref(final Context context, final SharedPreferences pref, final int requestcode, final Uri uri) {
        SharedPreferences.Editor editor = pref.edit();
        if (requestcode == 3) {
            editor.putString("gotsong", uri.toString());
            if (pref.contains("gotparentSongFolderUri"))
                editor.remove("gotparentSongFolderUri");

            SongMetadata metadata = getMetadata(context, uri);
            editor.putString("gotsongname", metadata.title);
            editor.putString("gotsongalbum", metadata.album);
            editor.putInt("gotsongduration", metadata.duration);

            try {
                Bitmap bitmap = metadata.image;
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iconmain);
                }
                
                if (bitmap.getByteCount() > 100000) {
                     float scale = (float) Math.sqrt(100000.0 / bitmap.getByteCount());
                     bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*scale), (int)(bitmap.getHeight()*scale), true);
                }
                
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
                final byte[] b = baos.toByteArray();
                final String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                editor.putString("gotsongimage", encoded);
            } catch (Exception e) {
                Log.e("musicinfo", "Error saving image to pref", e);
            }
            editor.apply();
        } else {
            editor.putString(requestcode == 2 ? "gotAlbums" : "gotmusic", uri.toString());
            editor.apply();
        }
    }

    static void getUris(Context context, final Uri uri, final SharedPreferences pref, final int requestCode) {
        try {
            context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            Log.e("musicinfo", "Failed to take persistable permission", e);
        }

        switch (requestCode) {
            case 3:
                setpref(context, pref, requestCode, uri);
                intents(context, requestCode, uri);
                break;
            default:
                DocumentFile musicfile = DocumentFile.fromTreeUri(context, uri);
                if (musicfile == null) {
                    Toast.makeText(context, "Could not open folder", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (requestCode == 2) {
                    albumUris = getFiles(musicfile, 2, context, null);
                } else if (pref.contains("favoritalbum")) {
                    musicUris = getFiles(musicfile, 4, context, pref.getString("favoritalbum", null));
                } else {
                    musicUris = getFiles(musicfile, 1, context, null);
                }

                if ((requestCode == 2 && (albumUris == null || albumUris.isEmpty())) || 
                    (requestCode != 2 && (musicUris == null || musicUris.isEmpty()))) {
                    Toast.makeText(context, "Please choose a folder with music in it", Toast.LENGTH_LONG).show();
                } else {
                    if (requestCode < 5)
                       setpref(context, pref, requestCode, uri);
                    intents(context, requestCode, null);
                }
        }
    }

    static void intents(final Context context, final int requestcode, Uri selectedUri) {
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
                if (requestcode == 3 && selectedUri != null) {
                    tomusic.putExtra("songUri", selectedUri);
                    SongMetadata meta = getMetadata(context, selectedUri);
                    tomusic.putExtra("songname", meta.title);
                    tomusic.putExtra("albumname", meta.album);
                    tomusic.putExtra("getDuration", meta.duration);
                }
                if (requestcode == 5)
                    tomusic.putExtra("next", true);
                else if (requestcode == 6)
                    tomusic.putExtra("next", false);
        }
        tomusic.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(tomusic);
    }

    static void initializeIntent(final Activity activity) {
        String className = activity.getComponentName().getClassName();
        if ("com.omar.acer.musicalstructure.NowPlaying".equals(className)) {
            Toast.makeText(activity, "Choose a song to play", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            activity.startActivityForResult(Intent.createChooser(intent, "Choose Track"), 3);
        } else {
            final Intent folderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            String message = className.equals("com.omar.acer.musicalstructure.music") ? "Choose The playlist folder" : "Choose The albums folder";
            int requestCode = className.equals("com.omar.acer.musicalstructure.music") ? 1 : 2;
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            activity.startActivityForResult(Intent.createChooser(folderIntent, message), requestCode);
        }
    }

    static void navigation(final Activity activity, final int requestcode, final SharedPreferences pref, dialog loading) {
        switch (requestcode) {
            case 3:
                if (pref.contains("gotmusic"))
                    pref.edit().putString("gotparentSongFolderUri", pref.getString("gotmusic", "")).apply();
                else if (pref.contains("gotAlbums"))
                    pref.edit().putString("gotparentSongFolderUri", pref.getString("gotAlbums", "")).apply();

                if (pref.contains("gotsong")) {
                    intents(activity, 3, Uri.parse(pref.getString("gotsong", "")));
                    if (!(activity instanceof NowPlaying)) activity.finish();
                } else {
                    Toast.makeText(activity, "Choose a song to play", Toast.LENGTH_SHORT).show();
                    final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("audio/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    activity.startActivityForResult(Intent.createChooser(intent, "Choose Track"), 3);
                }
                break;
            case 2:
                if (albumUris != null && !albumUris.isEmpty()) {
                    intents(activity, 2, null);
                } else {
                    String albumsUriStr = pref.getString("gotAlbums", pref.getString("gotmusic", pref.getString("gotparentSongFolderUri", null)));
                    if (albumsUriStr != null) {
                        if (loading != null) loading.Loading();
                        getUris(activity, Uri.parse(albumsUriStr), pref, 2);
                    } else {
                        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Toast.makeText(activity, "Choose The Albums folder", Toast.LENGTH_LONG).show();
                        activity.startActivityForResult(Intent.createChooser(intent, "Choose The Albums folder"), 2);
                        return;
                    }
                }
                if (!(activity instanceof NowPlaying)) activity.finish();
                break;
            case 1:
                if (musicUris != null && !musicUris.isEmpty()) {
                    intents(activity, 1, null);
                } else {
                    String musicUriStr = pref.getString("gotmusic", pref.getString("gotAlbums", pref.getString("gotparentSongFolderUri", null)));
                    if (musicUriStr != null) {
                        if (loading != null) loading.Loading();
                        getUris(activity, Uri.parse(musicUriStr), pref, 1);
                    } else {
                        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Toast.makeText(activity, "Choose The playlist folder", Toast.LENGTH_LONG).show();
                        activity.startActivityForResult(Intent.createChooser(intent, "Choose The playlist folder"), 1);
                        return;
                    }
                }
                if (!(activity instanceof NowPlaying)) activity.finish();
                break;
        }
    }

    static List<Uri> getFiles(DocumentFile df, int requestcode, Context context, String albumname) {
        final List<Uri> fileUris = new ArrayList<>();
        if (df == null) return fileUris;
        final DocumentFile[] files = df.listFiles();
        if (files == null) return fileUris;

        Set<String> uniqueAlbums = new HashSet<>();

        for (final DocumentFile file : files) {
            String name = file.getName();
            if (name != null && isAudioFile(name)) {
                if (requestcode == 2) { 
                    String currentAlbum = getSingleAlbumName(context, file.getUri());
                    if (!uniqueAlbums.contains(currentAlbum)) {
                        uniqueAlbums.add(currentAlbum);
                        fileUris.add(file.getUri());
                    }
                } else if (requestcode == 4) {
                    String currentAlbum = getSingleAlbumName(context, file.getUri());
                    if (albumname != null && albumname.equals(currentAlbum)) {
                        fileUris.add(file.getUri());
                    }
                } else {
                    fileUris.add(file.getUri());
                }
            }
        }
        return fileUris;
    }



    private static boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".mp4") || 
               lower.endsWith(".flac") || lower.endsWith(".m4a");
    }
}
