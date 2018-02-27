package com.brouken.wear.butcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LaunchActions {

    private Context mContext;
    private boolean mComboStartPrimaryButton;

    private String actionDefault;
    private String actionButton0Long;
    private String actionButton1;
    private String actionButton1Long;
    private String actionButton2;
    private String actionButton2Long;
    private String actionButton3;
    private String actionButton3Long;

    public LaunchActions(Context context, boolean comboStartPrimaryButton) {
        mContext = context;
        mComboStartPrimaryButton = comboStartPrimaryButton;

        loadConfig();
    }

    public String getAppForButton(int button, boolean longPress) {
        switch (button) {
            case -1:
                return actionDefault;
            case 0:
                return actionButton0Long;
            case 1:
                return (longPress ? actionButton1Long : actionButton1);
            case 2:
                return (longPress ? actionButton2Long : actionButton2);
            case 3:
                return (longPress ? actionButton3Long : actionButton3);
            default:
                return null;
        }
    }

    public boolean hasOnlyDefaultAction() {
        if (actionButton0Long == null && actionButton1 == null && actionButton1Long == null)
            return true;
        else
            return false;
    }

    private void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String comboStart = (mComboStartPrimaryButton ? "home" : "extra");

        actionDefault = sharedPreferences.getString(comboStart + "_default", null);
        actionButton0Long = sharedPreferences.getString(comboStart + "_button0long", null);
        actionButton1 = sharedPreferences.getString(comboStart + "_button1", null);
        actionButton1Long = sharedPreferences.getString(comboStart + "_button1long", null);
        actionButton2 = sharedPreferences.getString(comboStart + "_button2", null);
        actionButton2Long = sharedPreferences.getString(comboStart + "_button2long", null);
        actionButton3 = sharedPreferences.getString(comboStart + "_button3", null);
        actionButton3Long = sharedPreferences.getString(comboStart + "_button3long", null);
    }
}
