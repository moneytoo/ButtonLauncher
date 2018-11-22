package com.brouken.wear.butcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import static com.brouken.wear.butcher.Utils.log;

public class LaunchActivity extends WearableActivity {

    //private ImageView mImageView;
    private ProgressBar mProgressBar;

    private ObjectAnimator animator;

    private boolean launchedViaAssist = false;
    private boolean launchedViaCustom = false;
    private boolean launchedViaLauncher = false;

    private boolean vibrate = true;
    private int timeout = 3000;
    private int timer = 3;

    private boolean longPressed = false;

    private LaunchActions mLaunchActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        log("onCreate()");
        loadConfig();

        mProgressBar = findViewById(R.id.progressBar);

        animator = ObjectAnimator.ofInt(mProgressBar, "progress", timeout);
        mProgressBar.setMax(timeout);

        // Enables Always-on
        //setAmbientEnabled();

        //Uri ref = ActivityCompat.getReferrer(LaunchActivity.this);
        // launcher: android-app://com.google.android.wearable.app
        // home long press/assist: android-app://android
        // configurable button: android-app://com.google.android.apps.wearable.settings, because intent action is null

        handleStart(getIntent());

        mLaunchActions = new LaunchActions(this, launchedViaAssist);

        if (launchedViaLauncher ||
                mLaunchActions.hasOnlyDefaultAction() && !mLaunchActions.hasDefaultAction()) {
            Intent config = new Intent(this, ConfigActivity.class);
            startActivity(config);
            finish();
        }

        if (!isFinishing()) {
            setupCircles();
            loadConfig();
        }
    }

    private void setupCircles() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        RingDrawable ring = new RingDrawable(10, width/3/2, 2.3f, 0);
        ring.setColor(Color.parseColor("#002333"));

        mProgressBar.setRotation(270);
        mProgressBar.setProgressDrawable(ring);

        circle(R.drawable.circle, false, width);
        circle(R.drawable.circle_center, true, width);

        loadIcons(-1, false, width);
        loadIcons(0, true, width);

        int buttonCount = WearableButtons.getButtonCount(this);

        if (buttonCount >= 2) {
            loadIcons(1, false, width);
            loadIcons(1, true, width);
        }
        if (buttonCount >= 3) {
            loadIcons(2, false, width);
            loadIcons(2, true, width);
        }
        if (buttonCount >= 4) {
            loadIcons(3, false, width);
            loadIcons(3, true, width);
        }
    }

    private void circle(int res, boolean inner, int side) {
        FrameLayout frameLayout = findViewById(R.id.frameLayout);
        ImageView img = new ImageView(this);
        img.setBackgroundResource(res);

        int circleWidth = side / 3 * (inner ? 1 : 2);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(circleWidth, circleWidth);
        layoutParams.leftMargin = side / 2 - layoutParams.width / 2;
        layoutParams.topMargin  = side / 2 - layoutParams.height / 2;
        frameLayout.addView(img, layoutParams);
    }

    private void loadIcons(int buttonIndex, boolean longPressed, int side) {
        float buttonX = 0;
        float buttonY = 0;

        if (buttonIndex >= 0) {
            WearableButtons.ButtonInfo buttonInfo = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_PRIMARY + buttonIndex);

            if (buttonInfo == null) {
                buttonX = side;
                buttonY = side / 2;
            } else {
                buttonX = buttonInfo.getX();
                buttonY = buttonInfo.getY();
            }
        }

        FrameLayout frameLayout = findViewById(R.id.frameLayout);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getDrawableForButton(buttonIndex, longPressed));

        int center = side / 2;

        float distance = (longPressed ? 0.825f : 0.5f);

        float x = center + (buttonX - center) * distance;
        float y = center + (buttonY - center) * distance;

        if (buttonIndex == -1) {
            x = center;
            y = center;
        }

        int size = (longPressed ? side/10 : side/8);

        if (buttonIndex == -1)
            size = side / 4;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        layoutParams.leftMargin = (int) x - layoutParams.width / 2;
        layoutParams.topMargin  = (int) y - layoutParams.height / 2;
        frameLayout.addView(imageView, layoutParams);

        if (buttonIndex == -1) {
            final String app = mLaunchActions.getAppForButton(buttonIndex, longPressed);

            if (app == null)
                return;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchApp(app, true);
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("onNewIntent()");

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_ASSIST) || action.equals("android.intent.action.VOICE_ASSIST")) {
                final String app = mLaunchActions.getAppForButton(0, true);
                launchApp(app, false);
            }
        }
    }

    private void handleStart(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_ASSIST) || action.equals("android.intent.action.VOICE_ASSIST"))
                launchedViaAssist = true;
            else if (action.equals(Intent.ACTION_MAIN))
                launchedViaLauncher = true;
        } else
            launchedViaCustom = true;
    }

    private Drawable getDrawableForButton(int button, boolean longPressed) {
        try {
            final String app = mLaunchActions.getAppForButton(button, longPressed);

            if (app == null)
                return null;

            String[] appParts = app.split("/");

            ComponentName componentName = new ComponentName(appParts[0], appParts[1]);
            return getPackageManager().getActivityIcon(componentName);
        } catch (Exception e) {}
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");

        if (mLaunchActions.hasOnlyDefaultAction()) {
            String app = mLaunchActions.getAppForButton(-1, false);
            launchApp(app, !launchedViaAssist);
        } else {
            if (!launchedViaAssist || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrate();
            if (mLaunchActions.hasDefaultAction())
                startCountdown();
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
        animator.setDuration(timeout);
        animator.setInterpolator(new LinearInterpolator());

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                log("onAnimationEnd()");

                String app = mLaunchActions.getAppForButton(-1, false);
                launchApp(app, true);
            }
        });

        animator.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        log("onKeyDown");

        if (keyCode >= KeyEvent.KEYCODE_STEM_1 && keyCode <= KeyEvent.KEYCODE_STEM_3) {
            event.startTracking();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        log("onKeyLongPress");

        if (keyCode >= KeyEvent.KEYCODE_STEM_1 && keyCode <= KeyEvent.KEYCODE_STEM_3) {
            longPressed = true;

            String app = mLaunchActions.getAppForButton(keyCode - KeyEvent.KEYCODE_STEM_PRIMARY, true);
            launchApp(app, true);

            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        log("onKeyUp");

        if (keyCode >= KeyEvent.KEYCODE_STEM_1 && keyCode <= KeyEvent.KEYCODE_STEM_3) {
            if (!longPressed) {
                String app = mLaunchActions.getAppForButton(keyCode - KeyEvent.KEYCODE_STEM_PRIMARY, false);
                launchApp(app, true);
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void vibrate() {
        if (!vibrate)
            return;

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 20};
        if (vibrator != null)
            vibrator.vibrate(pattern, -1);
    }

    private void launchApp(String app, boolean vibrate) {
        if (app == null)
            return;

        String[] parts = app.split("/");

        String pkg = parts[0];
        String cls = parts[1];
        String action = Intent.ACTION_MAIN;
        String category = Intent.CATEGORY_LAUNCHER;

        if (parts.length > 2)
            action = parts[2];
        if (parts.length > 3)
            category = parts[3];

        launchApp(pkg, cls, action, category, vibrate);
    }

    private void launchApp(String pkg, String cls, String action, String category, boolean vibrate) {
        if (animator != null)
            animator.pause();

        if (isFinishing())
            return;

        ComponentName componentName = new ComponentName(pkg, cls);
        Intent intent=new Intent(action);
        intent.addCategory(category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);

        try {
            startActivity(intent);

            if (vibrate || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrate();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Launch not allowed", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        vibrate = sharedPreferences.getBoolean("vibrate", vibrate);
        timeout = Integer.parseInt(sharedPreferences.getString("timeout", Integer.toString(timeout)));
        timer = sharedPreferences.getInt("autoTimer", timer);
    }
}
