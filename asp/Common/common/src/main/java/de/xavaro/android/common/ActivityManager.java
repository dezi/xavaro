package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

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

    private File getDataDir()
    {
        File datadir = new File(Simple.getExternalFilesDir(), "activity");

        if ((! datadir.exists()) && (! datadir.mkdirs()))
        {
            Log.d(LOGTAG, "getDataDir: cannot create dir=" + datadir);
        }

        return datadir;
    }

    public void recordActivity(JSONObject activity)
    {
        //
        // Make sure activity date has been set.
        //

        if (! Json.has(activity, "date")) Json.put(activity, "date", Simple.nowAsISO());
        long date = Simple.getTimeStamp(Json.getString(activity, "date"));

        //
        // Relocate icon resource id to icon name.
        //

        if (Json.has(activity, "icid"))
        {
            int iconid = Json.getInt(activity, "icid");

            String iconName = Simple.getResources().getResourceEntryName(iconid);

            Json.remove(activity, "icid");
            Json.put(activity, "icon", iconName);
        }

        String fileDate = Simple.getLocalDateInternal(date).replace(".", "");
        File datafile = new File(getDataDir(), "activity." + fileDate + ".json");

        JSONArray records = Simple.getFileJSONArray(datafile);
        if (records == null) records = new JSONArray();
        Json.put(records, activity);
        Json.sort(records, "date", true);
        Simple.putFileJSON(datafile, records);
    }
}
