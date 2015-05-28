package com.android.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

/**
 * {@code RecordLocationPreference} is used to keep the "store locaiton"
 * option in {@code SharedPreference}.
 */
public class SmilePhotoPreference extends IconListPreference {

    public static final String VALUE_NONE = "none";
    public static final String VALUE_ON = "on";
    public static final String VALUE_OFF = "off";

    private final ContentResolver mResolver;

    public SmilePhotoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResolver = context.getContentResolver();
    }

    @Override
    public String getValue() {
        return get(getSharedPreferences(), mResolver) ? VALUE_ON : VALUE_OFF;
    }

    public static boolean get(
            SharedPreferences pref, ContentResolver resolver) {
        String value = pref.getString(
                CameraSettings.KEY_SMILE_MODE, VALUE_NONE);
        return VALUE_ON.equals(value);
    }

    public static boolean isSet(SharedPreferences pref) {
        String value = pref.getString(
                CameraSettings.KEY_SMILE_MODE, VALUE_NONE);
        return !VALUE_NONE.equals(value);
    }
}

