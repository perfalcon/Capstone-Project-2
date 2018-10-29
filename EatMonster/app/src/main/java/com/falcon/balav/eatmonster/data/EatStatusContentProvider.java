package com.falcon.balav.eatmonster.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class EatStatusContentProvider extends ContentProvider {
    public static final int EATSTATUS =100;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = EatStatusContentProvider.class.getName();
    private EatStatusDbHelper mEatStatusDbHelper;
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches
        uriMatcher.addURI(EatStatusContract.AUTHORITY,
                EatStatusContract.PATH_EATSTATUS, EATSTATUS);
        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        Context context = getContext ();
        mEatStatusDbHelper=new EatStatusDbHelper (context);
        Log.v(TAG,"in constructor -->"+mEatStatusDbHelper);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase = mEatStatusDbHelper.getReadableDatabase ();
        int match = sUriMatcher.match (uri);
        Cursor retCursor;
        switch (match){
            case EATSTATUS:
                retCursor = sqLiteDatabase.query (EatStatusContract.EatStatusEntry.TABLE_NAME,
                        projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException ("Unkonw uri "+uri);
        }
        retCursor.setNotificationUri (getContext ().getContentResolver (),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {       return null;    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase sqLiteDatabase = mEatStatusDbHelper.getWritableDatabase ();
        int match = sUriMatcher.match (uri);
        Uri returnUri;
        switch (match){
            case EATSTATUS:
                long id = sqLiteDatabase.insert (
                        EatStatusContract.EatStatusEntry.TABLE_NAME,null,contentValues);
                if(id>0){
                    returnUri= ContentUris.withAppendedId (EatStatusContract.EatStatusEntry.CONTENT_URI,id);
                }
                else{
                    throw new android.database.SQLException ("Failed to insert row into"+uri);
                }
                Log.v(TAG,"Number of Records inserted -->"+id);
                break;
            default:
                throw new UnsupportedOperationException ("Unkown uri "+uri);
        }
        getContext ().getContentResolver ().notifyChange (uri,null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase sqLiteDatabase = mEatStatusDbHelper.getWritableDatabase ();
        int match = sUriMatcher.match (uri);
        int rows_deleted = 0;
        switch (match) {
            case EATSTATUS:
                rows_deleted = sqLiteDatabase.delete (EatStatusContract.EatStatusEntry.TABLE_NAME, null, null);
                break;
            default:
                throw new UnsupportedOperationException ("Unknown uri: " + uri);
        }
        if (rows_deleted != 0) {
            getContext ().getContentResolver ().notifyChange (uri, null);
        }
        return rows_deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
