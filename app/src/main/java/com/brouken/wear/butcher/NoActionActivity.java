package com.brouken.wear.butcher;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class NoActionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
    }
}
