package com.falcon.balav.eatmonster;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.falcon.balav.eatmonster.data.EatStatusContract;


import static com.falcon.balav.eatmonster.data.EatStatusContract.EatStatusEntry.CONTENT_URI;

/**
 * Implementation of App Widget functionality.
 */
public class HomeScreenWidgetProvider extends AppWidgetProvider {
    private static final String TAG = HomeScreenWidgetProvider.class.toString();
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int coins, int score, String foodItem,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews (context.getPackageName (), R.layout.home_screen_widget_provider);

        views.setTextViewText (R.id.tvCoins,context.getString(R.string.coinsLabelText)+" "+String.valueOf (coins));
        views.setTextViewText (R.id.tvScore,context.getString (R.string.scoreLabelText)+" "+String.valueOf (score));
        views.setImageViewResource (R.id.imageView,context.getResources ()
                .getIdentifier ("drawable/"+foodItem.substring (0,foodItem.lastIndexOf ('.')),"drawable",context.getPackageName ()));

    //    fillEatStatusText(context,views);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);
        PendingIntent pendingIntent=PendingIntent.getActivity (context,0,intent,0);
        views.setOnClickPendingIntent (R.id.widgetEatMonster,pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget (appWidgetId, views);
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews (context.getPackageName (), R.layout.home_screen_widget_provider);

        fillEatStatusText(context,views);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);
        PendingIntent pendingIntent=PendingIntent.getActivity (context,0,intent,0);
        views.setOnClickPendingIntent (R.id.widgetEatMonster,pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget (appWidgetId, views);
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int coins, int score, String foodItem, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, coins, score,foodItem, appWidgetId);
        }
    }
    private static void fillEatStatusText(Context context, RemoteViews views) {
        StringBuilder sb=new StringBuilder ();
        Uri EATSTATUS_URI = CONTENT_URI;
        Cursor cursor = context.getContentResolver ().query(
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
            String image=cursor.getString (imageIndex);
            sb.append (String.valueOf (cursor.getLong (idIndex)) + "." + score + " " + coins +" "+image+ "\n");
            Log.v(TAG,"[fillEatStatusText] EatStatus Text -->"+sb.toString ());
            views.setTextViewText (R.id.tvCoins,context.getString(R.string.coinsLabelText)+" "+String.valueOf (coins));
            views.setTextViewText (R.id.tvScore,context.getString (R.string.scoreLabelText)+" "+String.valueOf (score));
            views.setImageViewResource (R.id.imageView,context.getResources ()
                    .getIdentifier ("drawable/"+image.substring (0,image.lastIndexOf ('.')),"drawable",context.getPackageName ()));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget (context, appWidgetManager, appWidgetId);
        }
        Log.v(TAG,"[onUpdate]--called");
       // HomeScreenWidgetService.startActionUpdateEatStatusWidgets (context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

