package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EatStatus implements Parcelable{
    int coins;
    int score;
    Level level;
    Settings settings;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }


    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }


    @Override
    public String toString() {
        return "EatStatus{"+
                "coins="+ coins +
                ", score="+ score +
                ", level="+ level+
                ", settings="+ settings+
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt (this.coins);
        dest.writeInt (this.score);
        dest.writeParcelable (this.level, flags);
        dest.writeParcelable (this.settings,flags);
    }

    public EatStatus() {
    }
    public void incrementCoins(){
        coins++;
    }
    public void incrementScore(){
        score++;
    }

    protected EatStatus(Parcel in) {
        this.coins = in.readInt ();
        this.score = in.readInt ();
        this.level = in.readParcelable (Level.class.getClassLoader ());
        this.settings = in.readParcelable (Settings.class.getClassLoader ());
    }

    public static final Creator<EatStatus> CREATOR = new Creator<EatStatus> () {
        @Override
        public EatStatus createFromParcel(Parcel source) {
            return new EatStatus (source);
        }

        @Override
        public EatStatus[] newArray(int size) {
            return new EatStatus[size];
        }
    };


}
