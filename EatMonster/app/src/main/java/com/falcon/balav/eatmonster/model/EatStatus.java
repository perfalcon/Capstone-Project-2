package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EatStatus implements Parcelable{
    int coins;
    int score;
    Level level;

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
    protected EatStatus(Parcel in){
        this.coins = in.readInt ();
        this.score = in.readInt();
        this.level = (Level)in.readValue (Level.class.getClassLoader ());
}
    public static final Parcelable.Creator<EatStatus> CREATOR = new Parcelable.Creator<EatStatus> () {
        @Override
        public EatStatus createFromParcel(Parcel source) {
            return new EatStatus (source);
        }

        @Override
        public EatStatus[] newArray(int size) {
            return new EatStatus[size];
        }
    };

    @Override
    public String toString() {
        return "EatStatus{"+
                "coins="+ coins +
                ", score="+ score +
                ", level="+ level+
                '}';
    }
}
