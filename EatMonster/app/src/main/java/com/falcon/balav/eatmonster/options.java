package com.falcon.balav.eatmonster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class options extends AppCompatActivity{
        //implements RewardedVideoAdListener {
    private static final String TAG = options.class.getSimpleName();
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private RewardedVideoAd mRewardedVideoAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_options);
Log.v(TAG, "Before Ads");
        /*MobileAds.initialize(this, APP_ID);

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();*/
    }
   /* private void loadRewardedVideoAd() {
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
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.v(TAG,"Ad - Reward Item");
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
    }*/
}
