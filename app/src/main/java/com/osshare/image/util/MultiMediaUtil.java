package com.osshare.image.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


import com.osshare.image.bm.Album;
import com.osshare.image.bm.MediaItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author imkk
 * @date 2018/6/3
 */

public class MultiMediaUtil {
    public static final int MEDIA_TYPE_IMAGES = 1;
    public static final int MEDIA_TYPE_AUDIO = 1 << 1;
    public static final int MEDIA_TYPE_VIDEO = 1 << 2;


    public static List<MediaItem> getLocalMultiMedia(ContentResolver resolver, int mediaType, String sortOrder) {
        List<MediaItem> mediaItems = new ArrayList<>();
        if ((mediaType & MEDIA_TYPE_IMAGES) == MEDIA_TYPE_IMAGES) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sortOrder));
        }
        if ((mediaType & MEDIA_TYPE_AUDIO) == MEDIA_TYPE_AUDIO) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, sortOrder));
        }
        if ((mediaType & MEDIA_TYPE_VIDEO) == MEDIA_TYPE_VIDEO) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sortOrder));
        }
        return mediaItems;
    }

    public static List<MediaItem> getLocalAlbumMedias(ContentResolver resolver, int mediaType, String sortOrder, String albumId) {

        List<MediaItem> mediaItems = new ArrayList<>();
        if ((mediaType & MEDIA_TYPE_IMAGES) == MEDIA_TYPE_IMAGES) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sortOrder, MediaStore.Images.ImageColumns.BUCKET_ID, albumId));
        }
        if ((mediaType & MEDIA_TYPE_AUDIO) == MEDIA_TYPE_AUDIO) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, sortOrder, MediaStore.Audio.AudioColumns.ALBUM_ID, albumId));
        }
        if ((mediaType & MEDIA_TYPE_VIDEO) == MEDIA_TYPE_VIDEO) {
            if (sortOrder == null) {
                sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            }
            mediaItems.addAll(convertMediaItem(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sortOrder, MediaStore.Video.VideoColumns.BUCKET_ID, albumId));
        }
        return mediaItems;
    }


    public static List<Album> getMediaAlbums(ContentResolver resolver, int mediaType, String sortOrder) {
        List<Album> mediaAlbums = new ArrayList<>();
        Uri uri = null;
        String albumIdColumnName = null;
        String albumNameColumnName = null;


        if ((mediaType & MEDIA_TYPE_IMAGES) == MEDIA_TYPE_IMAGES) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            albumIdColumnName = MediaStore.Images.Media.BUCKET_ID;
            albumNameColumnName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
            if (sortOrder == null) {
                sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER;
            }
        }
        if ((mediaType & MEDIA_TYPE_AUDIO) == MEDIA_TYPE_AUDIO) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            albumIdColumnName = MediaStore.Audio.Media.ALBUM_ID;
            albumNameColumnName = MediaStore.Audio.Media.ALBUM;
            if (sortOrder == null) {
                sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
            }
        }
        if ((mediaType & MEDIA_TYPE_VIDEO) == MEDIA_TYPE_VIDEO) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            albumIdColumnName = MediaStore.Video.Media.BUCKET_ID;
            albumNameColumnName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
            if (sortOrder == null) {
                sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            }
        }

        if (uri == null) return mediaAlbums;
        StringBuilder selectionBuilder = null;
        if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
            selectionBuilder = new StringBuilder();
            selectionBuilder.append(MediaStore.MediaColumns.SIZE + " > 0 AND ( "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.png' OR "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.jpeg' OR "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.jpg' ) ");
        }
        Cursor cursor = resolver.query(uri, null, selectionBuilder == null ? null : selectionBuilder.toString(), null, sortOrder);
        if (cursor != null) {
            try {
                Map<String, Album> map = new HashMap<>();
                while ((cursor.moveToNext())) {
                    String id = cursor.getString(cursor.getColumnIndex(albumIdColumnName));
                    if (map.containsKey(id)) {
                        map.get(id).increaseCount();
                    } else {
                        Album album = new Album(id, cursor.getString(cursor.getColumnIndex(albumNameColumnName)),
                                1, cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
                        map.put(id, album);
                        mediaAlbums.add(album);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return mediaAlbums;
    }

    public static MediaItem getLocalMedia(ContentResolver resolver, Uri uri) {
        MediaItem mediaItem = null;
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            boolean has = cursor.moveToFirst();
            if (has) {
                mediaItem = new MediaItem();
                String[] columnNames = cursor.getColumnNames();
                for (String columnName : columnNames) {
                    if (MediaStore.MediaColumns.TITLE.equals(columnName)) {
                        mediaItem.setTitle(cursor.getString(cursor.getColumnIndex(columnName)));
                    } else if (MediaStore.MediaColumns.DATA.equals(columnName)) {
                        mediaItem.setPath(cursor.getString(cursor.getColumnIndex(columnName)));
                    } else if (MediaStore.MediaColumns.SIZE.equals(columnName)) {
                        mediaItem.setSize(cursor.getLong(cursor.getColumnIndex(columnName)));
                    } else if (MediaStore.Audio.AudioColumns.ARTIST.equals(columnName)
                            || MediaStore.Video.VideoColumns.ARTIST.equals(columnName)) {
                        mediaItem.setArtist(cursor.getString(cursor.getColumnIndex(columnName)));
                    } else if (MediaStore.Audio.AudioColumns.DURATION.equals(columnName)
                            || MediaStore.Video.VideoColumns.DURATION.equals(columnName)) {
                        mediaItem.setDuration(cursor.getLong(cursor.getColumnIndex(columnName)));
                    }
                }
            }
            cursor.close();
        }
        return mediaItem;
    }

    private static List<MediaItem> convertMediaItem(ContentResolver resolver, Uri uri, String sortOrder) {
        List<MediaItem> mediaItems = new ArrayList<>();
        Cursor cursor = resolver.query(uri, null, null, null, sortOrder);
        if (cursor != null) {
            try {
                String[] columnNames = cursor.getColumnNames();
                while (cursor.moveToNext()) {
                    MediaItem mediaItem = new MediaItem();
                    for (String columnName : columnNames) {
                        if (MediaStore.MediaColumns.TITLE.equals(columnName)) {
                            mediaItem.setTitle(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.MediaColumns.DATA.equals(columnName)) {
                            mediaItem.setPath(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.MediaColumns.SIZE.equals(columnName)) {
                            mediaItem.setSize(cursor.getLong(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.Audio.AudioColumns.ARTIST.equals(columnName)
                                || MediaStore.Video.VideoColumns.ARTIST.equals(columnName)) {
                            mediaItem.setArtist(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.Audio.AudioColumns.ARTIST.equals(columnName)
                                || MediaStore.Video.VideoColumns.ARTIST.equals(columnName)) {
                            mediaItem.setDuration(cursor.getLong(cursor.getColumnIndex(columnName)));
                        }
                    }
                    mediaItems.add(mediaItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return mediaItems;
    }

    private static List<MediaItem> convertMediaItem(ContentResolver resolver, Uri uri, String sortOrder, String albumIdColumnName, String albumId) {
        List<MediaItem> mediaItems = new ArrayList<>();

        StringBuilder selectionBuilder = new StringBuilder(isEmpty(albumId) ? "" : albumIdColumnName + " = ? ");
        if (selectionBuilder.length() > 0) {
            selectionBuilder.append(" AND ");
        }
        if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
            selectionBuilder.append(MediaStore.MediaColumns.SIZE + " > 0 AND ( "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.png' OR "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.jpeg' OR "
                    + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE '%.jpg' ) ");
        }
        Cursor cursor = resolver.query(uri, null, selectionBuilder.toString(), isEmpty(albumId) ? null : new String[]{albumId}, sortOrder);
        if (cursor != null) {
            try {
                String[] columnNames = cursor.getColumnNames();
                while (cursor.moveToNext()) {
                    MediaItem mediaItem = new MediaItem();
                    for (String columnName : columnNames) {
                        if (MediaStore.MediaColumns.TITLE.equals(columnName)) {
                            mediaItem.setTitle(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.MediaColumns.DATA.equals(columnName)) {
                            mediaItem.setPath(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.MediaColumns.SIZE.equals(columnName)) {
                            mediaItem.setSize(cursor.getLong(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.Audio.AudioColumns.ARTIST.equals(columnName)
                                || MediaStore.Video.VideoColumns.ARTIST.equals(columnName)) {
                            mediaItem.setArtist(cursor.getString(cursor.getColumnIndex(columnName)));
                        } else if (MediaStore.Audio.AudioColumns.ARTIST.equals(columnName)
                                || MediaStore.Video.VideoColumns.ARTIST.equals(columnName)) {
                            mediaItem.setDuration(cursor.getLong(cursor.getColumnIndex(columnName)));
                        }
                    }
                    mediaItems.add(mediaItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return mediaItems;
    }


    public static class Image {
        public void scale() {
            Context context;
            LoaderManager loaderManager = null;
            MediaLoaderCallbacks loaderCallback = null;
            Loader<Cursor> loader = loaderManager.initLoader(1, null, loaderCallback);
            loader.registerListener(1, new Loader.OnLoadCompleteListener<Cursor>() {
                @Override
                public void onLoadComplete(@NonNull Loader<Cursor> loader, @Nullable Cursor data) {

                }
            });
        }

        public void rotate() {

        }

        public void crop() {

        }

        public void skew() {

        }
    }

    public static class MediaLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        Context context;
        public static final String[] PROJECTION = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE};

        public Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        public MediaLoaderCallbacks(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            return new CursorLoader(context, uri, PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (data == null) return;

            while (data.moveToNext()) {

            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    }


    public static boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text) || TextUtils.getTrimmedLength(text) == 0;
    }

}
