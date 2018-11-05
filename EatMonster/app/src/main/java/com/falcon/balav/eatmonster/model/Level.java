package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Level implements Parcelable{
    int id;
    String foodItem;

    public int getId() {
        return id;
    }

    public void setLevel(int level) {
        this.id = level;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    @Override
    public String toString() {
        return "Level{"+
                "id="+id+
                ", foodItem='"+foodItem+'\''+
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt (this.id);
        dest.writeString (this.foodItem);
    }

    public Level() {
    }

    protected Level(Parcel in) {
        this.id = in.readInt ();
        this.foodItem = in.readString ();
    }

    public static final Creator<Level> CREATOR = new Creator<Level> () {
        @Override
        public Level createFromParcel(Parcel source) {
            return new Level (source);
        }

        @Override
        public Level[] newArray(int size) {
            return new Level[size];
        }
    };
}
