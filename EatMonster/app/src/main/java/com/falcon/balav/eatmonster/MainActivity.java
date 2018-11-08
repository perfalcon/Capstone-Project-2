package com.falcon.balav.eatmonster;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.falcon.balav.eatmonster.data.EatStatusContract;
import com.falcon.balav.eatmonster.model.EatStatus;
import com.falcon.balav.eatmonster.model.FoodItems;
import com.falcon.balav.eatmonster.model.Level;
import com.falcon.balav.eatmonster.model.Settings;
import com.falcon.balav.eatmonster.utils.FoodCut;
import com.falcon.balav.eatmonster.utils.GsonUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.falcon.balav.eatmonster.data.EatStatusContract.EatStatusEntry.CONTENT_URI;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class MainActivity extends AppCompatActivity   implements RewardedVideoAdListener, LoaderManager.LoaderCallbacks<Cursor> {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";
    private static final String TAG = MainActivity.class.toString();

    private static final int EATSTATUS_LOADER_ID = 0;

    @BindView (R.id.tvCoins)    TextView tvCoins;
    @BindView (R.id.tvScore) TextView tvScore;
    @BindView (R.id.ivCurrentLevel) ImageView ivCurrentLevel;
    @BindView (R.id.ivNextLevel) ImageView ivNextLevel;
    @BindView (R.id.imageFood)    ImageView ivFood;
    @BindView (R.id.imageMore) ImageView ivMore;
    @BindView (R.id.imageSettings) ImageView ivSettings;

    ImageView iv50Coins;
    ImageView iv100Coins;
    ImageView iv150Coins;
    PopupWindow popupWindow;
    ConstraintLayout constraintLayout;
    Switch  mSaveSwitch;
    boolean bDataStatus;

    int foodTapCounter=0;
    int coinsToAdd=50;
    boolean bOptionsScreen=false;
    boolean bSettingsScreen=false;


    EatStatus mEatSatus;
    List<FoodItems> mFoodItems;
    Settings mSettings;
    Level mLevel;


    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private RewardedVideoAd mRewardedVideoAd;
    Bitmap originalBitmap;

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

        Log.v(TAG, "Before Ads");
        MobileAds.initialize(this, APP_ID);

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();


        // [START tracker]
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // Enable Advertising Features.
        mTracker.enableAdvertisingIdCollection(true);
        // [END tracker]


        originalBitmap =  ((BitmapDrawable)ivFood.getDrawable()).getBitmap();

        mEatSatus=new EatStatus ();
        mSettings = new Settings ();
        mLevel = new Level ();
        mEatSatus.setSettings (new Settings ());
        mEatSatus.setLevel (new Level ());
        getFoodItems (this);
        activateLoader();
       // getDataDatabase(this);
         addAnalyticsTracking("Main");
    }

    private void saveDataDatabase( EatStatus mEatStatus){
        if(!bDataStatus) return;
        deleteEatStatus ();//delete the current record and add a record with new details ... ( have to remove after the update query is done)
        insertEatStatus (mEatStatus);
    }
    private void deleteEatStatus() {
        Uri uri = EatStatusContract.EatStatusEntry.CONTENT_URI;
        int rows_deleted =  getContentResolver ().delete (uri,null,null);
        Log.v(TAG,"Rows Deleted -->"+rows_deleted);
    }

    private void insertEatStatus(EatStatus mEatStatus) {
        ContentValues contentValues = new ContentValues ();
        contentValues.put(EatStatusContract.EatStatusEntry.COINS, mEatStatus.getCoins ());
        contentValues.put(EatStatusContract.EatStatusEntry.SCORE,mEatStatus.getScore ());
        contentValues.put(EatStatusContract.EatStatusEntry.LEVELID,mEatStatus.getLevel ().getId ());
        contentValues.put(EatStatusContract.EatStatusEntry.IMAGE,mEatStatus.getLevel ().getFoodItem ());
        Log.v(TAG,"image-->"+mEatStatus.getLevel ().getFoodItem ());
        contentValues.put(EatStatusContract.EatStatusEntry.SAVESETTINGS,mEatStatus.getSettings ().isSaveSettings ()?1:0);
        contentValues.put(EatStatusContract.EatStatusEntry.SKIN,mEatStatus.getSettings ().getSkin ());

        Uri uri = getContentResolver ().insert (EatStatusContract.EatStatusEntry.CONTENT_URI,contentValues);
        if(uri!=null){
           // Toast.makeText (getBaseContext (), "insertEatStatus--> EatStatus added to DB",Toast.LENGTH_LONG).show ();
        }

        Log.v(TAG, "EatStatus-->"+mEatStatus.toString ());

        UpdateWidget();
       // HomeScreenWidgetService.startActionUpdateEatStatusWidgets (this);
    }


    private void getDataDatabase(Context mContext) {
        Uri EATSTATUS_URI = CONTENT_URI;
        Cursor cursor = mContext.getContentResolver ().query(
                EATSTATUS_URI,
                null,
                null,
                null,
                null
        );
        if(cursor!=null){
            Log.v (TAG, "count-->" + cursor.getCount ());
            bDataStatus=true;
            fillEatStatus(cursor);
            populateUI ();
        }else{
            Log.v(TAG,"Opened for First Time");
            bDataStatus=false;
            populateDefaultUI();
        }
    }

    private void getDataFromCursor(Cursor cursor){
        if(cursor!=null){
            Log.v (TAG, "count-->" + cursor.getCount ());
            bDataStatus=true;
            fillEatStatus(cursor);
            populateUI ();
        }else{
            Log.v(TAG,"Opened for First Time");
            bDataStatus=false;
            populateDefaultUI();
        }
    }
    private void activateLoader(){
        Log.v(TAG,"[activateLoader]");
        if(getSupportLoaderManager ().hasRunningLoaders ()){
            getSupportLoaderManager().initLoader(EATSTATUS_LOADER_ID, null, this);
        }
        else{
            getSupportLoaderManager ().restartLoader (EATSTATUS_LOADER_ID, null, this);
        }
    }

    private void fillEatStatus(Cursor cursor) {
        StringBuilder sb=new StringBuilder ();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst ();// there will be only one record all the time.
            int idIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry._ID);
            int scoreIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.SCORE);
            int coinsIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.COINS);
            int levelIdIndex=cursor.getColumnIndex (EatStatusContract.EatStatusEntry.LEVELID);
            int imageIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.IMAGE);
            int saveSettingsIndex=cursor.getColumnIndex (EatStatusContract.EatStatusEntry.SAVESETTINGS);
            int skinIndex = cursor.getColumnIndex (EatStatusContract.EatStatusEntry.SKIN);
            int id=cursor.getInt (idIndex);
            int score = cursor.getInt (scoreIndex);
            int coins=cursor.getInt (coinsIndex);
            int levelID=cursor.getInt (levelIdIndex);
            String image=cursor.getString (imageIndex);
            boolean saveSettings=cursor.getInt(saveSettingsIndex)!=0? true:false;
            String skin = cursor.getString (skinIndex);
            sb.append (String.valueOf (cursor.getLong (idIndex)) + "." + score + " " + coins +" "+ levelID +" "+image+" "+saveSettings+" "+skin+ "\n");

            mEatSatus.setCoins (coins);
            mEatSatus.setScore (score);
            mLevel.setFoodItem (image);
            mLevel.setLevel (levelID);
            mSettings.setSkin (skin);
            mSettings.setSaveSettings (saveSettings);
            mEatSatus.setLevel (mLevel);
            mEatSatus.setSettings (mSettings);
            bDataStatus=saveSettings;

            Log.v(TAG,"EatStatus Text -->"+sb.toString ());
        }
    }

    private void populateUI() {
        String strTemp=getString (R.string.coinsLabelText)+" "+ String.valueOf (mEatSatus.getCoins ());
        tvCoins.setText (strTemp );
        strTemp = getString(R.string.scoreLabelText) +" "+ String.valueOf (mEatSatus.getScore ());
        tvScore.setText (strTemp);
        ivNextLevel.setImageResource (getImageID (mEatSatus.getLevel ().getFoodItem ()));
        ivCurrentLevel.setImageResource (getImageID (mEatSatus.getLevel ().getFoodItem ()));
        checkAndUpdateLevel();
    }
    private void checkAndUpdateLevel(){
        //Get the current Level
        for (FoodItems mfoodItem : mFoodItems) {
            if(mEatSatus.getCoins ()<mfoodItem.getWeight ()){
                Log.v(TAG,"[checkAndUpdateLevel]--> Weight matched");
                mEatSatus.getLevel ().setLevel (mfoodItem.getLevel ());
                mEatSatus.getLevel ().setFoodItem (mfoodItem.getFoodItem ());
                ivCurrentLevel.setImageResource (getImageID (mfoodItem.getFoodItem ()));
                ivFood.setImageResource (getImageID(mfoodItem.getFoodItem ()));

              /*  mEatSatus.setCoins (mEatSatus.getCoins ()-mfoodItem.getWeight ());//Reset the coin count to 0
                String strTemp = getString (R.string.coinsDefaultText);
                tvCoins.setText (strTemp);*/
                //as weight matched --- then find the next level
                for(FoodItems mfoodItemNextLevel:mFoodItems){
                    if(mEatSatus.getLevel ().getId ()<mfoodItemNextLevel.getLevel () ){
                        ivNextLevel.setImageResource (getImageID (mfoodItemNextLevel.getFoodItem ()));
                        break; //break the loop
                    }
                }
                break;
            }
            Log.v(TAG,"[checkAndUpdateLevel]--> Weight Not Matched--"+mEatSatus.getCoins ()+"=="+mfoodItem.getWeight ());
        }

    }

    private void populateDefaultUI(){
        tvCoins.setText (R.string.coinsDefaultText);
        tvScore.setText (R.string.scoreDefaultText);
        mEatSatus.getLevel ().setLevel (mFoodItems.get (0).getLevel ());
        mEatSatus.getLevel ().setFoodItem (mFoodItems.get (0).getFoodItem ());
        ivCurrentLevel.setImageResource (getImageID (mFoodItems.get (0).getFoodItem ()));
        ivNextLevel.setImageResource (getImageID (mFoodItems.get (1).getFoodItem ()));
        ivFood.setImageResource (getImageID (mFoodItems.get (0).getFoodItem ()));
        mEatSatus.getSettings ().setSkin ("default");
        mEatSatus.getSettings ().setSaveSettings (false);

    }

    private void getFoodItems(Context context){
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream=assetManager.open ("foodItems.json");
            InputStreamReader inputStreamReader = new InputStreamReader (inputStream);

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder strFoodItems = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                strFoodItems.append(line).append('\n');
            }
            Log.v(TAG,"FoodItems String-->"+strFoodItems.toString ());
            mFoodItems = new GsonUtils ().populateFoodItems (strFoodItems.toString ());

        } catch (IOException e) {
            e.printStackTrace ();
            Log.v(TAG,"Exception in reading the food items json");
        }

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
        Bitmap newBitmap = FoodCut.EatFood (((BitmapDrawable)ivFood.getDrawable()).getBitmap(),foodTapCounter);
        ivFood.setImageBitmap (newBitmap);
    }


    private void incrementCoins() {
        Log.v(TAG,"[incrementCoins]:"+mEatSatus.getCoins ());
        mEatSatus.incrementCoins ();
        updateCoins (mEatSatus.getCoins ());
    }
    private void incrementScore(){
        Log.v(TAG,"[incrementScore]:"+mEatSatus.getScore ());
        mEatSatus.incrementScore ();
        updateScore (mEatSatus.getScore ());
    }
    private void updateScore(int score){
        String strTemp = getString (R.string.scoreLabelText)+" "+String.valueOf (score);
        tvScore.setText (strTemp);
        saveDataDatabase (mEatSatus);
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

        addAnalyticsTracking("SettingsPopup");
        LayoutInflater layoutInflaterSettings = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewSettings = layoutInflaterSettings.inflate(R.layout.activity_settings,null);
//        GradientDrawable drawable = (GradientDrawable) viewSettings.getResources().getDrawable(R.drawable.popup_border);
//        viewSettings.setBackground(drawable);
        //instantiate popup window
        popupWindow = new PopupWindow (viewSettings, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //display the popup window
        if(!( MainActivity.this).isFinishing()){
            popupWindow.showAtLocation(constraintLayout, Gravity.RIGHT, 50, -350);
            bSettingsScreen=true;
            Spinner spinner = (Spinner) viewSettings.findViewById(R.id.spinnerSkin);
            spinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                   // Toast.makeText (MainActivity.this,adapterView.getSelectedItem ().toString (),Toast.LENGTH_LONG).show ();
                    UpdateColors(adapterView.getSelectedItem ().toString ());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        mSaveSwitch=(Switch) viewSettings.findViewById (R.id.switchSaveScore);
        mSaveSwitch.setChecked (mEatSatus.getSettings ().isSaveSettings ());
        mSaveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEatSatus.getSettings ().setSaveSettings (isChecked);
                bDataStatus=isChecked;
                saveDataDatabase (mEatSatus);
               }
        });
    }

    private GradientDrawable getPopupBackground(GradientDrawable drawable){
       drawable.setColor(ContextCompat.getColor(this,R.color.colorVenusPrimary));
        return drawable;
    }
    private void UpdateColors(String themeName){
        ConstraintLayout mConstraintLayout =
                (ConstraintLayout)findViewById(R.id.cstlTop);
        if(themeName.equalsIgnoreCase ("Default")){
            mConstraintLayout.setBackgroundColor (ContextCompat.getColor(this,R.color.colorMenu));
        }
        if(themeName.equalsIgnoreCase ("Venus")){
            displayPaidVersionToast();
        }
        if(themeName.equalsIgnoreCase ("Mars")){
            displayPaidVersionToast();
        }
        if(themeName.equalsIgnoreCase ("Earth")){
            displayPaidVersionToast();
        }
    }
    private void displayPaidVersionToast(){
        Toast.makeText (MainActivity.this,R.string.paidVersionText,Toast.LENGTH_LONG).show ();
    }
    private void displayOptions(){

        addAnalyticsTracking("OptionsPopup");

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
                addAnalyticsTrackingHit("50Coins");
            }
        });
        iv100Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG,"[onClick-100 coins clicked]:"+v.getId ());
                showRewardedVideo();
                coinsToAdd=100;
                addAnalyticsTrackingHit("100Coins");
            }
        });
        iv150Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG,"[onClick-150 coins clicked]:"+v.getId ());
                showRewardedVideo();
                coinsToAdd=150;
                addAnalyticsTrackingHit("150Coins");
            }
        });
    }

    private void addAnalyticsTracking(String name){
        Log.v(TAG, "Screen name: " + name);
        mTracker.setScreenName("EatMonster~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void addAnalyticsTrackingHit(String name){
        // [START custom_event]
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(name)
                .build());
        // [END custom_event]
    }
    private void showRewardedVideo() {
       // showVideoButton.setVisibility(View.INVISIBLE);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    private void addCoins(int rewardCoins) {
        mEatSatus.setCoins (mEatSatus.getCoins ()+coinsToAdd);
        updateCoins (mEatSatus.getCoins ());
    }
    private void updateCoins(int coins)
    {
        String strTemp = getString (R.string.coinsLabelText)+" "+String.valueOf (coins);
        tvCoins.setText (strTemp);
        checkAndUpdateLevel();
        saveDataDatabase (mEatSatus);

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


    private int getImageID(String imageName){
        Resources res = getResources();
        int resId=res.getIdentifier ("drawable/"+imageName.substring (0,imageName.lastIndexOf ('.')),"drawable",getPackageName ());
        Log.v(TAG,"[getImageID]-->"+imageName+"-->"+resId);
        return resId;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return  new AsyncTaskLoader<Cursor> (this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mEatStatusData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mEatStatusData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mEatStatusData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data
                Uri EATSTATUS_URI = CONTENT_URI;
                try{
                    return getContentResolver ().query (EATSTATUS_URI,
                            null,
                            null,
                            null,
                            null);

                }catch(Exception e){
                    Log.e(TAG,"failed to asynchronously load data");
                    e.printStackTrace ();
                    return null;
                }


            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mEatStatusData = data;
                super.deliverResult(mEatStatusData);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.v (TAG,"[onLoadFinished]--");
        getDataFromCursor(cursor);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
