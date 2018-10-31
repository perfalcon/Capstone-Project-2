package com.falcon.balav.eatmonster;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends AppCompatActivity {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";
    private static final String TAG = MainActivity.class.toString();

    @BindView (R.id.tvCoins)    TextView tvCoins;
    @BindView (R.id.tvScore) TextView tvScore;
    @BindView (R.id.tvCurrentLevel) TextView tvCurrentLevel;
    @BindView (R.id.tvNextLevel)    TextView tvNextLevel;
    @BindView (R.id.imageFood)    ImageView ivFood;
    @BindView (R.id.imageMore) ImageView ivMore;
    @BindView (R.id.imageSettings) ImageView ivSettings;
    PopupWindow popupWindow;
    ConstraintLayout constraintLayout;

    int foodTapCounter=0;
    int coins=0;
    int score=0;
    boolean bOptionsScreen=false;
    boolean bSettingsScreen=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        ButterKnife.bind (this);
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById (R.id.adView);
        AdRequest adRequest = new AdRequest.Builder ()
                .setRequestAgent ("android_studio:ad_template").build ();
        adView.loadAd (adRequest);
        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText (this, TOAST_TEXT, Toast.LENGTH_LONG).show ();
      //  @OnTouch({R.id.imageFood})
    }
    @OnClick(R.id.imageFood)
    public void foodTapped(View view){
        Log.v(TAG,"Food Tapped, foodTapCounter-->"+foodTapCounter);
        if(foodTapCounter>3){
            foodTapCounter=0;
            incrementCoins ();
        }
        else{
            foodTapCounter++;
        }
        incrementScore ();
    }
    private void incrementCoins() {
        Log.v(TAG,"[incrementCoins]:"+coins);
        coins++;
        tvCoins.setText ("Coins: "+String.valueOf (coins));
    }
    private void incrementScore(){
        Log.v(TAG,"[incrementScore]:"+score);
        score++;
        tvScore.setText ("Score: "+String.valueOf (score));
    }

    @OnClick({R.id.imageMore,R.id.imageSettings})
    public  void actionsTapped(View view){
        Log.v(TAG,"[actionTapped]:"+view.getId ());
        constraintLayout = (ConstraintLayout) findViewById(R.id.cstlTop);

        switch (view.getId ()){
            case R.id.imageMore:
                if(bOptionsScreen){
                    popupWindow.dismiss ();
                    bOptionsScreen=false;
                }else if(bSettingsScreen){
                    popupWindow.dismiss ();
                    bSettingsScreen=false;
                    displayOptions();
                }else{
                    displayOptions();
                }


                break;
            case R.id.imageSettings:
                if(bSettingsScreen){
                    popupWindow.dismiss ();
                    bSettingsScreen=false;
                }else if(bOptionsScreen){
                    popupWindow.dismiss ();
                    bOptionsScreen=false;
                    displaySettings();
                }else{
                    displaySettings();
                }
                break;
        }
    }
    private void displaySettings(){
        LayoutInflater layoutInflaterSettings = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewSettings = layoutInflaterSettings.inflate(R.layout.activity_settings,null);
        //instantiate popup window
        popupWindow = new PopupWindow (viewSettings, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //display the popup window
        popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 50, -550);
        bSettingsScreen=true;
    }
    private void displayOptions(){
        LayoutInflater layoutInflaterOptions = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewOptions = layoutInflaterOptions.inflate(R.layout.activity_options,null);
        //instantiate popup window
        popupWindow = new PopupWindow (viewOptions, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //display the popup window
        // popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 100, -500);
        popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 50, -550);
        bOptionsScreen=true;
    }

    private void UpdateWidget() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName (getApplication(), HomeScreenWidgetProvider.class));
        HomeScreenWidgetProvider myWidget = new HomeScreenWidgetProvider ();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);
    }
}
