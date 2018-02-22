package com.brouken.wear.butcher;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        int count = WearableButtons.getButtonCount(this);

        Log.d("TEST", "count = " + count);

        WearableButtons.ButtonInfo buttonInfo =
                WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);

        //Log.d("TEST", "count = " + buttonInfo.toString());

        Intent intent = getIntent();

        ComponentName componentName = getCallingActivity();

        Uri ref = ActivityCompat.getReferrer(MainActivity.this);
        // launcher: android-app://com.google.android.wearable.app
        // home long press/assist: android-app://android
        // configurable button: android-app://com.google.android.apps.wearable.settings, because intent action is null


        Log.d("TEST", "intent action = " + getIntent().getAction());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d("TEST", "keyCode = " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            Log.d("TEST", "KEYCODE_STEM_1");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_STEM_2) {
            Log.d("TEST", "KEYCODE_STEM_2");
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
