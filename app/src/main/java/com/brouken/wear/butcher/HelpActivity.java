package com.brouken.wear.butcher;

import android.graphics.Point;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        TextView textView = findViewById(R.id.textView);

        if (getResources().getConfiguration().isScreenRound()) {
            ScrollView scrollView = findViewById(R.id.scrollView);
            LinearLayout layoutTop = findViewById(R.id.layoutTop);
            LinearLayout layoutBottom = findViewById(R.id.layoutBottom);

            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int padding = size.x / 10;

            scrollView.setPadding(padding, 0, padding, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(padding * 2.5f));
            layoutTop.setLayoutParams(params);
            layoutBottom.setLayoutParams(params);
        }


        String labelPrimary = "";
        String labelSecondary = "";
        int buttonCount = WearableButtons.getButtonCount(this);

        if (buttonCount >= 1) {
            labelPrimary = "(" + WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_PRIMARY).toString().toLowerCase() + ") ";

            ArrayList<String> labels = new ArrayList<>();
            for (int i = 1; i < buttonCount; i++) {
                String label = WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_PRIMARY + i).toString().toLowerCase();
                labels.add(label);
            }

            labelSecondary = "(" + TextUtils.join(", ", labels) + ") ";
        }
        
        textView.setText("Start Button Launcher from your launcher to open this configuration screen.\n" +
                "\n" +
                "Long press your watch primary button " + labelPrimary + "to use the first set of shortcut combos.\n" +
                "\n" +
                "Map your other watch button " + labelSecondary + "in system settings to Button Launcher to use the other set of shortcut combos. Unfortunately it is not possible to detect what specific custom button was used for launch.\n" +
                "\n" +
                "The Default action is what app will be launched if no secondary button is pressed (short or long) before time for launch runs out.\n" +
                "\n" +
                "Auto Timer is an additional action that can be executed. Upon launch, it will always start a timer with the configured time (without any confirmation).");
    }
}
