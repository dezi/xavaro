package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.FileObserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupMedia extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupMedia.class.getSimpleName();

    protected String mediadir;

    public LaunchGroupMedia(Context context)
    {
        super(context);
    }

    public LaunchGroupMedia(Context context, String mediadir)
    {
        super(context);

        this.mediadir = mediadir;
    }
}
