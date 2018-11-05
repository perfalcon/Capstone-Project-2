package com.falcon.balav.eatmonster.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItems  implements Parcelable {
    int level;
    String foodItem;//image name
    int weight;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "FoodItems{" +
                "level=" + level +
                ", foodItem='" + foodItem + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt (this.level);
        dest.writeString (this.foodItem);
        dest.writeInt (this.weight);
    }

    public FoodItems() {
    }

    protected FoodItems(Parcel in) {
        this.level = in.readInt ();
        this.foodItem = in.readString ();
        this.weight = in.readInt ();
    }

    public static final Creator<FoodItems> CREATOR = new Creator<FoodItems> () {
        @Override
        public FoodItems createFromParcel(Parcel source) {
            return new FoodItems (source);
        }

        @Override
        public FoodItems[] newArray(int size) {
            return new FoodItems[size];
        }
    };
}
