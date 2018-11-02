package com.falcon.balav.eatmonster;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import com.falcon.balav.eatmonster.utils.FoodCut;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends AppCompatActivity
        implements RewardedVideoAdListener {
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
    ImageView iv50Coins;
    ImageView iv100Coins;
    ImageView iv150Coins;
    PopupWindow popupWindow;
    ConstraintLayout constraintLayout;

    int foodTapCounter=0;
    int coins=0;
    int score=0;
    int coinsToAdd=50;
    boolean bOptionsScreen=false;
    boolean bSettingsScreen=false;

    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private RewardedVideoAd mRewardedVideoAd;
    Bitmap origialBitmap;

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

        Log.v(TAG, "Before Ads");
        MobileAds.initialize(this, APP_ID);
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        origialBitmap =  ((BitmapDrawable)ivFood.getDrawable()).getBitmap();
    }
    @OnClick(R.id.imageFood)
    public void foodTapped(View view){
        Log.v(TAG,"Food Tapped, foodTapCounter-->"+foodTapCounter);
        updateFoodImage(foodTapCounter);
        if(foodTapCounter>=3){
            foodTapCounter=0;
            incrementCoins ();
            updateFoodImage (-1);
        }
        else{
            foodTapCounter++;
        }
        incrementScore ();

    }

    private void updateFoodImage(int foodTapCounter) {
        Bitmap newBitmap = FoodCut.EatFood (origialBitmap,foodTapCounter);
        ivFood.setImageBitmap (newBitmap);
    }


    private void incrementCoins() {
        Log.v(TAG,"[incrementCoins]:"+coins);
        coins++;
        updateCoins (coins);
    }
    private void incrementScore(){
        Log.v(TAG,"[incrementScore]:"+score);
        score++;
        updateScore (score);
    }
    private void updateScore(int score){
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
        popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 50, -350);
        bSettingsScreen=true;
    }
    private void displayOptions(){
        LayoutInflater layoutInflaterOptions = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewOptions = layoutInflaterOptions.inflate(R.layout.activity_options,null);
        popupWindow = new PopupWindow (viewOptions, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 50, -350);
        bOptionsScreen=true;
        iv50Coins = (ImageView)viewOptions.findViewById (R.id.imageView50Coins);
        iv100Coins = (ImageView)viewOptions.findViewById (R.id.imageView100Coins);
        iv150Coins = (ImageView)viewOptions.findViewById (R.id.imageView150Coins);
        iv50Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG,"[onClick-50 coins clicked]:"+v.getId ());
                showRewardedVideo();
                coinsToAdd=50;
            }
        });
        iv100Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG,"[onClick-100 coins clicked]:"+v.getId ());
                showRewardedVideo();
                coinsToAdd=100;
            }
        });
        iv150Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG,"[onClick-150 coins clicked]:"+v.getId ());
                showRewardedVideo();
                coinsToAdd=150;
            }
        });
    }


    private void showRewardedVideo() {
       // showVideoButton.setVisibility(View.INVISIBLE);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    private void addCoins(int rewardCoins) {
        coins += coinsToAdd;
        updateCoins (coins);
    }
    private void updateCoins(int coins)
    {
        tvCoins.setText ("Coins: "+String.valueOf (coins));
    }
    private void UpdateWidget() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName (getApplication(), HomeScreenWidgetProvider.class));
        HomeScreenWidgetProvider myWidget = new HomeScreenWidgetProvider ();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);
    }

    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(AD_UNIT_ID,
                    new AdRequest.Builder().build());
        }

    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.v(TAG,"Ad Loaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.v(TAG,"Ad opened");
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.v(TAG,"Ad started");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.v(TAG,"Ad closed");
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.v(TAG,"Ad - Reward Item"+rewardItem.getAmount ());
        Log.v(TAG, "Ad- Reward -- Reward Type -->"+rewardItem.getType());
        addCoins(rewardItem.getAmount ());

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.v(TAG,"Ad left");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.v(TAG,"Ad failed loading"+i);
    }

    @Override
    public void onRewardedVideoCompleted() {
        Log.v(TAG,"Ad completed");
    }
}
