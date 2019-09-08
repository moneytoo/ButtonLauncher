package com.brouken.wear.butcher;

import android.graphics.drawable.GradientDrawable;

import java.lang.reflect.Field;

public class RingDrawable extends GradientDrawable {

    private Class<?> mGradientState;

    public RingDrawable() {
        this(Orientation.TOP_BOTTOM, null);
    }

    public RingDrawable(int innerRadius, int thickness, float innerRadiusRatio, float thicknessRatio) {
        this(Orientation.TOP_BOTTOM, null, innerRadius, thickness, innerRadiusRatio, thicknessRatio);
    }

    public RingDrawable(GradientDrawable.Orientation orientation, int[] colors) {
        super(orientation, colors);
        setShape(RING);
    }

    public RingDrawable(GradientDrawable.Orientation orientation, int[] colors, int innerRadius, int thickness, float innerRadiusRatio, float thicknessRatio) {
        this(orientation, colors);
        try {
            setRingThickness(thickness);
            setUseLevel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRingThickness(int thicknessValue) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (mGradientState == null) mGradientState = resolveGradientState();
        Field thickness = resolveField(mGradientState, "mThickness");
        thickness.setInt(getConstantState(), thicknessValue);
    }

    public void setUseLevel(boolean level) throws SecurityException, /*NoSuchFieldException,*/ IllegalArgumentException/*, IllegalAccessException*/ {
        try {
            if (mGradientState == null) mGradientState = resolveGradientState();
            Field useLevel = resolveField(mGradientState, "mUseLevel");
            useLevel.setBoolean(getConstantState(), level);
        } catch (Exception e) {}
    }

    private Class<?> resolveGradientState() {
        Class<?>[] classes = GradientDrawable.class.getDeclaredClasses();
        for (Class<?> singleClass : classes) {
            if (singleClass.getSimpleName().equals("GradientState")) return singleClass;
        }
        throw new RuntimeException("GradientState could not be found in current GradientDrawable implementation");
    }

    private Field resolveField(Class<?> source, String fieldName) throws SecurityException, NoSuchFieldException {
        Field field = source.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

}
