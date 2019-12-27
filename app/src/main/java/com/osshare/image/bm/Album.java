package com.osshare.image.bm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author imkk
 * @date 2018/10/8.
 */

public class Album implements Parcelable {
    private String id;
    private String name;
    private int count;
    private String cover;
    private String recent;
    private boolean isChecked;

    public Album(String id) {
        this.id = id;
    }

    public Album(String id, String name, int count, String cover) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.cover = cover;
    }

    public Album(String id, String name, int count, String cover, String recent, boolean isChecked) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.cover = cover;
        this.recent = recent;
        this.isChecked = isChecked;
    }

    protected Album(Parcel in) {
        id = in.readString();
        name = in.readString();
        count = in.readInt();
        cover = in.readString();
        recent = in.readString();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(count);
        dest.writeString(cover);
        dest.writeString(recent);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increaseCount() {
        count++;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getRecent() {
        return recent;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


}

