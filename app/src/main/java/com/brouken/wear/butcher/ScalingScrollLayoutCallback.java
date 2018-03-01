package com.brouken.wear.butcher;

import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.view.View;

public class ScalingScrollLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {

    // Max we scale the child View.
    private static final float MAX_CHILD_SCALE = 0.65f;

    private float mProgressToCenter;

    /*
     * Scales the item's icons and text the farther away they are from center allowing the main
     * item to be more readable to the user on small devices like Wear.
     */
    @Override
    public void onLayoutFinished(View child, RecyclerView parent) {

        // Figure out % progress from top to bottom.
        float centerOffset = ((float) child.getHeight() / 2.0f) /  (float) parent.getHeight();
        float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

        // Normalizes for center.
        mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);

        // Adjusts to the maximum scale.
        mProgressToCenter = Math.min(mProgressToCenter, MAX_CHILD_SCALE);

        child.setScaleX(1 - mProgressToCenter);
        child.setScaleY(1 - mProgressToCenter);
    }
}
