package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Settings implements Parcelable {
    boolean saveSettings;
    String skin;
    public boolean isSaveSettings() {
        return saveSettings;
    }

    public void setSaveSettings(boolean saveSettings) {
        this.saveSettings = saveSettings;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte (this.saveSettings ? (byte) 1 : (byte) 0);
        dest.writeString (this.skin);
    }

    public Settings() {
    }

    protected Settings(Parcel in) {
        this.saveSettings = in.readByte () != 0;
        this.skin = in.readString ();
    }

    public static final Creator<Settings> CREATOR = new Creator<Settings> () {
        @Override
        public Settings createFromParcel(Parcel source) {
            return new Settings (source);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
}
