package com.brouken.wear.butcher;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        TextView textView = findViewById(R.id.textView);

        String labelPrimary = WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_PRIMARY).toString().toLowerCase();

        ArrayList<String> labels = new ArrayList<String>();
        int buttonCount = WearableButtons.getButtonCount(this);
        for (int i = 1; i < buttonCount; i++) {
            String label = WearableButtons.getButtonLabel(this, KeyEvent.KEYCODE_STEM_PRIMARY + i).toString().toLowerCase();
            labels.add(label);
        }

        String labelSecondary = TextUtils.join(", ", labels);

        textView.setText("Start Button Launcher from your launcher to open this configuration screen.\n" +
                "\n" +
                "Long press your watch primary button (" + labelPrimary + ") to use the first set of shortcut combos.\n" +
                "\n" +
                "Map your other watch button (" + labelSecondary + ") in system settings to Button Launcher to use the other set of shortcut combos. Unfortunately it is not possible to detect what specific custom button was used for launch.");
    }
}
