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
    public static final int EATSTATUS_ID =101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = EatStatusContentProvider.class.getName();
    private EatStatusDbHelper mEatStatusDbHelper;
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches
        uriMatcher.addURI(EatStatusContract.AUTHORITY,  EatStatusContract.PATH_EATSTATUS, EATSTATUS);
    //    uriMatcher.addURI(EatStatusContract.AUTHORITY, EatStatusContract.PATH_EATSTATUS + "/#", EATSTATUS_ID);
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
        Cursor retCursor = null;
        switch (match){
            case EATSTATUS:
                if(!isTableExists (EatStatusContract.EatStatusEntry.TABLE_NAME)){
                    return retCursor;
                }
                retCursor = sqLiteDatabase.query (EatStatusContract.EatStatusEntry.TABLE_NAME,
                        projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException ("Unkonw uri "+uri);
        }

        if(retCursor.getCount ()>0){
            retCursor.setNotificationUri (getContext ().getContentResolver (),uri);
        }else
            {
            retCursor =null;
        }

        return retCursor;
    }


    public boolean isTableExists(String tableName) {
        SQLiteDatabase sqLiteDatabase = mEatStatusDbHelper.getReadableDatabase ();

        Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
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
                if(!isTableExists (EatStatusContract.EatStatusEntry.TABLE_NAME)){
                    return 0;
                }
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
        final SQLiteDatabase sqLiteDatabase = mEatStatusDbHelper.getWritableDatabase ();
        //Keep track of if an update occurs
        int tasksUpdated;

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case EATSTATUS_ID:
                //update a single task by getting the id
                String id = uri.getPathSegments().get(1);
                //using selections
                tasksUpdated = sqLiteDatabase.update(EatStatusContract.EatStatusEntry.TABLE_NAME, contentValues, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (tasksUpdated != 0) {
            //set notifications if a task was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of tasks updated
        return tasksUpdated;

    }
}

    /*public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
        // Here is where you'll implement swipe to delete

        // TODO (1) Construct the URI for the item to delete
        //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
        int id = (int)viewHolder.itemView.getTag ();
        String stringId = Integer.toString (id);
        Uri uri = TaskContract.TaskEntry.CONTENT_URI;
        uri = uri.buildUpon ().appendPath (stringId).build ();

        // TODO (2) Delete a single row of data using a ContentResolver
        getContentResolver ().delete (uri,null,null);

        // TODO (3) Restart the loader to re-query for all tasks after a deletion
        getSupportLoaderManager ().restartLoader (TASK_LOADER_ID,null,MainActivity.this);

    }*/
