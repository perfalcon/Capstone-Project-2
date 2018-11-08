package com.falcon.balav.eatmonster;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.falcon.balav.eatmonster.data.EatStatusContract;

import static com.falcon.balav.eatmonster.data.EatStatusContract.EatStatusEntry.CONTENT_URI;
//Not working ... not sure why .
public class HomeScreenWidgetService extends IntentService {
    public static final String ACTION_UPDATE_EATSTATUS_WIDGETS = "com.falcon.balav.eatmonster.action.update_eatstatus_widgets";
    private static final String TAG = HomeScreenWidgetService.class.toString();

    public HomeScreenWidgetService() {
        super ("HomeScreenWidgetService");
    }

    /**
     * Starts this service to perform UpdateEatMonsterWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateEatStatusWidgets(Context context) {
        Log.v(TAG,"[startActionUpdateEatStatusWidgets] ");
        Intent intent = new Intent(context, HomeScreenWidgetService.class);
        intent.setAction(ACTION_UPDATE_EATSTATUS_WIDGETS);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG,"[onHandleIntent] "+intent.getAction ());
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_EATSTATUS_WIDGETS.equals(action)) {
                handleActionUpdateEatStatusWidgets();
            }
        }
    }

    private void handleActionUpdateEatStatusWidgets() {
        Log.v(TAG,"[handleActionUpdateEatStatusWidgets] getting DB details ");
        StringBuilder sb=new StringBuilder ();
        Uri EATSTATUS_URI = CONTENT_URI;
        Cursor cursor = getContentResolver ().query(
                EATSTATUS_URI,
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst ();// there will be only one record all the time.
            int idIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry._ID);
            int scoreIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.SCORE);
            int coinsIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.COINS);
            int levelIdIndex=cursor.getColumnIndex (EatStatusContract.EatStatusEntry.LEVELID);
            int imageIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.IMAGE);
            int score = cursor.getInt (scoreIndex);
            int coins=cursor.getInt (coinsIndex);
            String foodItem=cursor.getString (imageIndex);
            sb.append (String.valueOf (cursor.getLong (idIndex)) + "." + score + " " + coins +" "+foodItem+ "\n");
            Log.v(TAG,"[fillEatStatusText] EatStatus Text -->"+sb.toString ());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName (this, HomeScreenWidgetProvider.class));
            //Now update all widgets
            HomeScreenWidgetProvider.updatePlantWidgets(this, appWidgetManager,   coins,  score,  foodItem, appWidgetIds);
        }
    }
}
