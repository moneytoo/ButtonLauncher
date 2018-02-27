package com.brouken.wear.butcher;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import static com.brouken.wear.butcher.Utils.log;

public class LaunchActivity extends WearableActivity {

    private ImageView mImageView;
    private ImageView mImageView2;
    private ImageView mImageView3;
    private ProgressBar mProgressBar;

    ObjectAnimator animator;

    private boolean launchedViaAssist = false;
    private boolean launchedViaCustom = false;

    private String actionHomeDefault;
    private String actionHomeButton0Long;
    private String actionHomeButton1;
    private String actionHomeButton1Long;

    private String actionExtraDefault;
    private String actionExtraButton0Long;
    private String actionExtraButton1;
    private String actionExtraButton1Long;

    boolean longPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch2);

        log("onCreate()");
        loadConfig();

        mImageView = findViewById(R.id.imageView);
        //mImageView2 = findViewById(R.id.imageView2);
        //mImageView3 = findViewById(R.id.imageView3);
        mProgressBar = findViewById(R.id.progressBar);

        animator = ObjectAnimator.ofInt(mProgressBar, "progress", 3000);

        // Enables Always-on
        //setAmbientEnabled();

        //Uri ref = ActivityCompat.getReferrer(LaunchActivity.this);
        // launcher: android-app://com.google.android.wearable.app
        // home long press/assist: android-app://android
        // configurable button: android-app://com.google.android.apps.wearable.settings, because intent action is null

        handleStart(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("onNewIntent()");

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_ASSIST)) {
            if (launchedViaAssist && actionHomeButton0Long != null)
                launchApp(actionHomeButton0Long, false);
            else if (launchedViaCustom && actionExtraButton0Long != null)
                launchApp(actionExtraButton0Long, false);
        }
    }

    private void handleStart(Intent intent) {
        boolean launchedViaLauncher = false;

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_ASSIST))
                launchedViaAssist = true;
            else if (action.equals(Intent.ACTION_MAIN))
                launchedViaLauncher = true;
        } else
            launchedViaCustom = true;

        if (launchedViaLauncher) {
            Intent config = new Intent(this, ConfigActivity.class);
            startActivity(config);
            finish();
        } else {
            if (!launchedViaAssist)
                vibrate();

            loadIcon();
        }
    }

    private void loadIcon() {
        try {
            final String[] app;

            if (launchedViaAssist) {
                if (actionHomeDefault == null)
                    return;
                app = actionHomeDefault.split("/");
            } else {
                if (actionExtraDefault == null)
                    return;
                app = actionExtraDefault.split("/");
            }

            ComponentName componentName = new ComponentName(app[0], app[1]);
            Drawable icon = getPackageManager().getActivityIcon(componentName);
            mImageView.setImageDrawable(icon);

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchApp(app[0], app[1], true);
                }
            });
        } catch (PackageManager.NameNotFoundException e) {}
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");

        if (launchedViaAssist && actionHomeDefault != null) {
            if (actionHomeButton1 == null && actionHomeButton1Long == null) {
                launchApp(actionHomeDefault, false);
            } else {
                startCountdown();
            }
        } else if (launchedViaCustom && actionExtraDefault != null) {
            if (actionExtraButton1 == null && actionExtraButton1Long == null) {
                launchApp(actionExtraDefault, true);
            } else {
                startCountdown();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop()");

        if (!isFinishing())
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy()");
    }

    private void startCountdown() {
        animator.setDuration(3000);
        animator.setInterpolator(new LinearInterpolator());

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                log("onAnimationEnd()");

                if (launchedViaAssist)
                    launchApp(actionHomeDefault, true);
                else
                    launchApp(actionExtraDefault, true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        animator.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        log("onKeyDown");

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            log("KEYCODE_STEM_1");
            event.startTracking();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        log("onKeyLongPress");

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            longPressed = true;
            if (launchedViaAssist)
                launchApp(actionHomeButton1Long, true);
            else
                launchApp(actionExtraButton1Long, true);
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        log("onKeyUp");

        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            if (!longPressed) {
                if (launchedViaAssist)
                    launchApp(actionHomeButton1, true);
                else
                    launchApp(actionExtraButton1, true);
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 20};
        vibrator.vibrate(pattern, -1);
    }

    private void launchApp(String app, boolean vibrate) {
        if (app == null)
            return;

        String pkg = app.split("/")[0];
        String cls = app.split("/")[1];
        launchApp(pkg, cls, vibrate);
    }

    private void launchApp(String pkg, String cls, boolean vibrate) {
        if (animator != null)
            animator.pause();

        if (isFinishing())
            return;

        ComponentName componentName = new ComponentName(pkg, cls);
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        startActivity(intent);

        if (vibrate)
            vibrate();
        finish();
    }

    private void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        actionHomeDefault = sharedPreferences.getString("home_default", null);
        actionHomeButton0Long = sharedPreferences.getString("home_button0long", null);
        actionHomeButton1 = sharedPreferences.getString("home_button1", null);
        actionHomeButton1Long = sharedPreferences.getString("home_button1long", null);

        actionExtraDefault = sharedPreferences.getString("extra_default", null);
        actionExtraButton0Long = sharedPreferences.getString("extra_button0long", null);
        actionExtraButton1 = sharedPreferences.getString("extra_button1", null);
        actionExtraButton1Long = sharedPreferences.getString("extra_button1long", null);
    }
}
