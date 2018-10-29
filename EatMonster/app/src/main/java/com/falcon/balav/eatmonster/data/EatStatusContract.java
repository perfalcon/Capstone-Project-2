package com.falcon.balav.eatmonster.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class EatStatusContract {
    public static final String AUTHORITY="com.falcon.balav.eatmonster";
    public static final Uri BASE_CONTENT_URI= Uri.parse ("content://"+AUTHORITY);
    public static final String PATH_EATSTATUS = "status";

    //public static final long INVALID_LEVEL_ID = -1;

    public static final class EatStatusEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon ().appendPath (PATH_EATSTATUS).build ();

        // EATSTATUS table and column names
        public static final String TABLE_NAME = "eatstatus";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final  String SCORE="score";
        public static final  String COINS="coins";
        public static final  String LEVELID="level_id";
        public static final  String IMAGE="level_image";
    }
}
