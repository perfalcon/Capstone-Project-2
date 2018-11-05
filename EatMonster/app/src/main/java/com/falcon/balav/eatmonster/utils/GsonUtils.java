package com.falcon.balav.eatmonster.utils;

import android.util.Log;

import com.falcon.balav.eatmonster.model.FoodItems;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.util.List;

public class GsonUtils {
    private static final String TAG = GsonUtils.class.getSimpleName();

    public GsonUtils(){
        //Nothing to do
    }

    public List<FoodItems> populateFoodItems(String strJson){

        Gson gson = new Gson ();
        List<FoodItems> mFoodItems= gson.fromJson (strJson, new TypeToken<List<FoodItems>>(){}.getType ());
            for (FoodItems mfoodItem : mFoodItems) {
            Log.v(TAG,"FoodItems -->"+mfoodItem.toString ());
            }
            return mFoodItems;
    }
}
