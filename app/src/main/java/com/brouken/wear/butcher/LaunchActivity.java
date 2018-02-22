package com.brouken.wear.butcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
    private int progress = 3000;

    private int mProgressStatus = 3000;
    private ProgressBarAsync mProgressbarAsync;

    //private static AsyncTask<Void, Void, Void> countdownTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        //mTextView = (TextView) findViewById(R.id.text);
        mImageView = findViewById(R.id.imageView);
        mProgressBar = findViewById(R.id.progressBar);

        // Enables Always-on
        //setAmbientEnabled();

        try {
            Drawable icon = getPackageManager().getApplicationIcon("com.brouken.wear.payenabler");
            mImageView.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }



        int count = WearableButtons.getButtonCount(this);

        Log.d("TEST", "count = " + count);

        WearableButtons.ButtonInfo buttonInfo =
                WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);

        //Log.d("TEST", "count = " + buttonInfo.toString());

        Intent intent = getIntent();

        ComponentName componentName = getCallingActivity();

        Uri ref = ActivityCompat.getReferrer(LaunchActivity.this);
        // launcher: android-app://com.google.android.wearable.app
        // home long press/assist: android-app://android
        // configurable button: android-app://com.google.android.apps.wearable.settings, because intent action is null


        Log.d("TEST", "intent action = " + getIntent().getAction());

        mProgressbarAsync = new ProgressBarAsync();
        mProgressbarAsync.execute();

        vibrate();
    }

    private class ProgressBarAsync extends AsyncTask<Void, Integer, Void>{

        private boolean mRunning = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mRunning = true;
        }

        @Override
        protected Void doInBackground(Void...params) {
            while(mProgressStatus>0){
                try {
                    if(!mRunning)
                        break;
                    mProgressStatus -= 50;

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

        /*
        if (keyCode == KeyEvent.KEYCODE_STEM_2) {
            Log.d("TEST", "KEYCODE_STEM_2");
            return true;
        }
        */

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        Log.d("TEST", "onKeyLongPress");
        toast("long");

        /*
        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            Log.d("TEST", "KEYCODE_STEM_1");
            return true;
        }
        */

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d("TEST", "onKeyUp");
        toast("short");

        return super.onKeyUp(keyCode, event);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100};
        vibrator.vibrate(pattern, -1);
    }

    private void vibrate2() {
        //getactivi
        //performHapticFeedback();
    }
}
