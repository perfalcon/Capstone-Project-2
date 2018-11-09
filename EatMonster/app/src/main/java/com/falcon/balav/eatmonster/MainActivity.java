package com.falcon.balav.eatmonster;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import com.falcon.balav.eatmonster.utils.NetworkUtils;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.falcon.balav.eatmonster.data.EatStatusContract.EatStatusEntry.CONTENT_URI;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity   implements RewardedVideoAdListener, LoaderManager.LoaderCallbacks<Cursor> {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Test ads are being shown. "
            + "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";
    private static final String TAG = MainActivity.class.toString();

    private static final int EATSTATUS_LOADER_ID = 0;
    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;


    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;
    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;
    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
   private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

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
    boolean bLevelChanged=false;


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
    private static final String EATSTATUS_STATE="eatstatus";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

      if(savedInstanceState!=null){
          if(savedInstanceState.containsKey(EATSTATUS_STATE)){
              mEatSatus=savedInstanceState.getParcelable (EATSTATUS_STATE);
              populateUI();
          }
      }else{
          mEatSatus=new EatStatus ();
          mSettings = new Settings ();
          mLevel = new Level ();
          mEatSatus.setSettings (new Settings ());
          mEatSatus.setLevel (new Level ());
      }


        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        ButterKnife.bind (this);


        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById (R.id.adView);
        AdRequest adRequest = new AdRequest.Builder ()
                .setRequestAgent ("android_studio:ad_template").build ();
        adView.loadAd (adRequest);

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

        getFoodItems (this);
        activateLoader();
        addAnalyticsTracking("Main");
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState (outState);
        outState.putParcelable (EATSTATUS_STATE,mEatSatus);
    }

    private void saveDataDatabase( EatStatus mEatStatus){
        if(!bDataStatus) return;
        deleteEatStatus ();//delete the current record and add a record with new details ... ( have to remove after the update query is done)
        insertEatStatus (mEatStatus);
        updateGamePlayServices(mEatStatus.getScore (),mEatStatus.getCoins ());
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
                bLevelChanged=true;
                mEatSatus.getLevel ().setLevel (mfoodItem.getLevel ());
                mEatSatus.getLevel ().setFoodItem (mfoodItem.getFoodItem ());
                ivCurrentLevel.setImageResource (getImageID (mfoodItem.getFoodItem ()));
                ivFood.setImageResource (getImageID(mfoodItem.getFoodItem ()));

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
                if(NetworkUtils.isConnected (MainActivity.this)){
                    if(isChecked){
                        Log.d(TAG, "Sign-in button clicked");
                        startSignInIntent();
                    }else{
                        Log.d(TAG, "Sign-out button clicked");
                        signOut();
                    }
                }else{
                    Log.d(TAG,"No Network Connections");
                    Toast.makeText (MainActivity.this,getString(R.string.NetworkConnectionError),Toast.LENGTH_LONG).show ();
                }
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

    //Game Play Services

    public void updateGamePlayServices(int score, int coins){
        if(!NetworkUtils.isConnected (this)){
            Log.d(TAG,"No Network Connections");
           // Toast.makeText (this,getString(R.string.NetworkConnectionError),Toast.LENGTH_LONG).show ();
            return;
        }
        Log.v(TAG,"[updateGamePlayServices]--called");
        checkForAchievements (score,coins);
        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();


    }

    // Checks if n is prime. We don't consider 0 and 1 to be prime.
    private boolean isPrime(int n) {
        int i;
        if (n == 0 || n == 1) {
            return false;
        }
        for (i = 2; i <= n / 2; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param score the score the user got.
     * @param coins the coins the user got.
     */
    private void checkForAchievements(int score, int coins) {
        Log.v(TAG,"[checkForAchievements]--called");
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (isPrime(score)) {
            mOutbox.mPrimeAchievement = true;
            achievementToast(getString(R.string.achievement_prime_toast_text));
        }
        if(bLevelChanged){
            Log.v(TAG,"[checkForAchievements]--Level Changed");
            int resId=getResources ().getIdentifier ("string/"+"Level_"+String.valueOf (mEatSatus.getLevel ().getId ()),"string",getPackageName ());
            Log.v (TAG,"[checkForAchievements]resId-->"+resId);
          //  achievementToast (getString (resId));
            bLevelChanged=false;
            switch(mEatSatus.getLevel ().getId ()){
                case 1:
                    mOutbox.mLevel1Achievement=true; break;
                case 2:
                    mOutbox.mLevel2Achievement=true; break;
                case 3:
                    mOutbox.mLevel3Achievement=true; break;
                case 4:
                    mOutbox.mLevel4Achievement=true; break;
                case 5:
                    mOutbox.mLevel5Achievement=true; break;
            }
        }
        mOutbox.mBoredSteps++;
    }

    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void pushAccomplishments() {
        Log.v(TAG,"[pushAccomplishments]--called");
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if (mOutbox.mPrimeAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_prime));
            mOutbox.mPrimeAchievement = false;
        }
        if(mOutbox.mLevel1Achievement){
            mAchievementsClient.unlock (getString(R.string.achievement_reached_level_1));
        }
        if(mOutbox.mLevel2Achievement){
            mAchievementsClient.unlock (getString(R.string.achievement_reached_level_2));
        }
        if(mOutbox.mLevel3Achievement){
            mAchievementsClient.unlock (getString(R.string.achievement_reached_level_3));
        }
        if(mOutbox.mLevel4Achievement){
            mAchievementsClient.unlock (getString(R.string.achievement_reached_level_4));
        }
        if(mOutbox.mLevel5Achievement){
            mAchievementsClient.unlock (getString(R.string.achievement_reached_level_5));
        }

    }
    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
    private void signInSilently() {
        Log.d(TAG, "signInSilently()");
    if(!NetworkUtils.isConnected (this)){
            Log.d(TAG,"No Network Connections");
            Toast.makeText (this,getString(R.string.NetworkConnectionError),Toast.LENGTH_LONG).show ();
            return;
        }

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount> () {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }
    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }
    @Override
    protected void onResume() {
        super.onResume();
        activateLoader();
        signInSilently();
    }
    private void signOut() {
        Log.d(TAG, "signOut()");

        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    }
                });
    }

    public void onShowAchievementsRequested() {
        mAchievementsClient.getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent> () {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener () {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }
    private void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = getString(R.string.status_exception_error, details, status, e);

        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }


    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);


        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            handleException(e, getString(R.string.players_exception));
                            displayName = "???";
                        }
                        //mMainMenuFragment.setGreeting("Hello, " + displayName);
                    }
                });


        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }

        loadAndPrintEvents();
    }
    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
    }

    private class AccomplishmentsOutbox {
        boolean mPrimeAchievement = false;
        boolean mLevel1Achievement=false;
        boolean mLevel2Achievement=false;
        boolean mLevel3Achievement=false;
        boolean mLevel4Achievement=false;
        boolean mLevel5Achievement=false;

        int mBoredSteps = 0;
        boolean isEmpty() {
            return !mPrimeAchievement && !mLevel1Achievement && !mLevel2Achievement && !mLevel3Achievement
            && !mLevel4Achievement && !mLevel5Achievement && mBoredSteps == 0 ;
        }
    }

    private void loadAndPrintEvents() {
        final MainActivity mainActivity = this;
        mEventsClient.load(true)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<EventBuffer>>() {
                    @Override
                    public void onSuccess(AnnotatedData<EventBuffer> eventBufferAnnotatedData) {
                        EventBuffer eventBuffer = eventBufferAnnotatedData.get();

                        int count = 0;
                        if (eventBuffer != null) {
                            count = eventBuffer.getCount();
                        }

                        Log.i(TAG, "number of events: " + count);

                        for (int i = 0; i < count; i++) {
                            Event event = eventBuffer.get(i);
                            Log.i(TAG, "event: "
                                    + event.getName()
                                    + " -> "
                                    + event.getValue());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }
}
