package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Level implements Parcelable{
    int id;
    String image;

    @Override
    public int describeContents() {   return 0;   }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt (this.id);
    parcel.writeString (this.image);
    }

    public int getId() {
        return id;
    }

    public void setLevel(int level) {
        this.id = level;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    protected  Level(Parcel in){
        this.image=in.readString ();
        this.id=in.readInt ();
    }
    public static final Parcelable.Creator<Level> CREATOR = new Parcelable.Creator<Level> () {
        @Override
        public Level createFromParcel(Parcel source) {
            return new Level (source);
        }

        @Override
        public Level[] newArray(int size) {
            return new Level[size];
        }
    };

    @Override
    public String toString() {
        return "Level{"+
                "id="+id+
                ", image='"+image+'\''+
                '}';
    }
}
