package com.osshare.image.bm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * @author imkk
 * @date 2018/10/8.
 */

public class MediaItem implements Parcelable {
    private String title;
    private String path;
    private long size;
    private String artist;
    private long duration;

    public transient boolean isChoose;

    public MediaItem() {

    }

    public MediaItem(String path) {
        this.path = path;
    }

    public MediaItem(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public MediaItem(String title, String path, long size) {
        this.title = title;
        this.path = path;
        this.size = size;
    }

    public MediaItem(String title, String path, long size, String artist, long duration) {
        this.title = title;
        this.path = path;
        this.size = size;
        this.artist = artist;
        this.duration = duration;
    }

    protected MediaItem(Parcel in) {
        title = in.readString();
        path = in.readString();
        size = in.readLong();
        artist = in.readString();
        duration = in.readLong();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeString(artist);
        dest.writeLong(duration);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaItem mediaItem = (MediaItem) o;
        return size == mediaItem.size &&
                duration == mediaItem.duration &&
                Objects.equals(title, mediaItem.title) &&
                Objects.equals(path, mediaItem.path) &&
                Objects.equals(artist, mediaItem.artist);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, path, size, artist, duration);
    }
}
