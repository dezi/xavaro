package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

@SuppressWarnings("unused")
public class ActivityManager
{
    private static final String LOGTAG = ActivityManager.class.getSimpleName();

    //region Static singleton methods.

    private static ActivityManager instance;

    public static ActivityManager getInstance()
    {
        if (instance == null) instance = new ActivityManager();

        return instance;
    }

    //endregion Static singleton methods.
}
