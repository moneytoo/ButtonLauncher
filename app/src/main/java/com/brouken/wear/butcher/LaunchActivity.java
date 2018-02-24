package com.brouken.wear.butcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LaunchActivity extends WearableActivity {

    //private TextView mTextView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Handler mHandler = new Handler();
    private long countdownStart;

    private int mProgressStatus = 3000;
    private ProgressBarAsync mProgressbarAsync;

    private String actionDefault;
    private String actionButton1;
    private String actionButton1Long;

    boolean longPressed = false;

    //private static AsyncTask<Void, Void, Void> countdownTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        loadConfig();

        //mTextView = (TextView) findViewById(R.id.text);
        mImageView = findViewById(R.id.imageView);
        mProgressBar = findViewById(R.id.progressBar);

        // Enables Always-on
        //setAmbientEnabled();

        try {
            String pkg = actionDefault.split("/")[0];
            String cls = actionDefault.split("/")[1];

            ComponentName componentName = new ComponentName(pkg, cls);

            //TODO: not an activity icon!
            //getPackageManager().getActivityIcon(componentName)

            Drawable icon = getPackageManager().getActivityIcon(componentName);
            mImageView.setImageDrawable(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }



        int count = WearableButtons.getButtonCount(this);

        Log.d("TEST", "count = " + count);

        WearableButtons.ButtonInfo buttonInfo = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);

        //String btn = WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_1).toString();
        //mImageView.setImageDrawable(WearableButtons.getButtonIcon(this, KeyEvent.KEYCODE_STEM_1));

        //Log.d("TEST", "desc = " + btn);

        Intent intent = getIntent();

        ComponentName componentName = getCallingActivity();

        Uri ref = ActivityCompat.getReferrer(LaunchActivity.this);
        // launcher: android-app://com.google.android.wearable.app
        // home long press/assist: android-app://android
        // configurable button: android-app://com.google.android.apps.wearable.settings, because intent action is null


        Log.d("TEST", "intent action = " + getIntent().getAction());


        boolean launchedViaAssist = false;
        if (intent.getAction().equals(Intent.ACTION_ASSIST))
            launchedViaAssist = true;

        if (!launchedViaAssist)
            vibrate();

        mProgressbarAsync = new ProgressBarAsync();
        //mProgressbarAsync.execute();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mProgressbarAsync.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mProgressbarAsync.cancel(true);
    }

    private class ProgressBarAsync extends AsyncTask<Void, Integer, Void>{

        private boolean mRunning = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            countdownStart = System.currentTimeMillis();
            mRunning = true;
        }

        @Override
        protected Void doInBackground(Void...params) {
            while(mProgressStatus>0){
                try {
                    if(!mRunning)
                        break;

                    int diff = (int) (System.currentTimeMillis() - countdownStart);

                    mProgressStatus = mProgressBar.getMax() - diff;

                    publishProgress(mProgressStatus);
                    Thread.sleep(50);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mRunning = false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(mProgressStatus);
            /*if(!mTglStart.isChecked()){
                this.cancel(true);
            }*/
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //mTglStart.setChecked(false);

            if (mProgressStatus <= 0)
                launchApp(actionDefault);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d("TEST", "onKeyDown");


        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            Log.d("TEST", "KEYCODE_STEM_1");
            event.startTracking();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        Log.d("TEST", "onKeyLongPress");

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            //Log.d("TEST", "KEYCODE_STEM_1");
            longPressed = true;
            launchApp(actionButton1Long);
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d("TEST", "onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            if (!longPressed)
                launchApp(actionButton1);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        // TODO: FIX don't vibrate when launched via HOME/ASSIST
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 30};
        vibrator.vibrate(pattern, -1);
    }

    private void launchApp(String app) {
        if (app == null)
            return;

        String pkg = app.split("/")[0];
        String cls = app.split("/")[1];
        launchApp(pkg, cls);
    }

    private void launchApp(String pkg, String cls) {
        ComponentName componentName = new ComponentName(pkg, cls);
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        startActivity(intent);

        vibrate();
        finish();
    }

    private void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        actionDefault = sharedPreferences.getString("home_default", null);
        actionButton1 = sharedPreferences.getString("home_button1", null);
        actionButton1Long = sharedPreferences.getString("home_button1long", null);
    }
}
