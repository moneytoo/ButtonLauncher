package com.brouken.wear.butcher;

import android.util.Log;

public class Utils {
    public static void log(String text){
        if (BuildConfig.DEBUG)
            Log.d("Butcher", text);
    }
}
