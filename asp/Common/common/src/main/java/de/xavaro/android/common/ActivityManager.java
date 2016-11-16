package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
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

    private File getDataDir()
    {
        File datadir = new File(Simple.getExternalFilesDir(), "activity");

        if ((! datadir.exists()) && (! datadir.mkdirs()))
        {
            Log.d(LOGTAG, "getDataDir: cannot create dir=" + datadir);
        }

        return datadir;
    }

    private final FilenameFilter filter = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String filename)
        {
            return filename.startsWith("activity.") && filename.endsWith(".json");
        }
    };

    public JSONArray loadDays()
    {
        File datadir = getDataDir();

        JSONArray dayFiles = Simple.getDirectorySortedByName(datadir, filter, true);
        if (dayFiles == null) dayFiles = new JSONArray();

        //
        // Read in all files and records and sort by local date.
        //

        JSONObject localDays = new JSONObject();

        for (int inx = 0; inx < dayFiles.length(); inx++)
        {
            JSONObject dayFile = Json.getObject(dayFiles, inx);
            if (dayFile == null) continue;

            String fileName = Json.getString(dayFile, "file");
            if (fileName == null) continue;

            Log.d(LOGTAG, "loadDays: file=" + fileName);

            JSONArray records = Simple.getFileJSONArray(new File(fileName));
            if (records == null) continue;

            //
            // Distribute records to local time days.
            //

            for (int cnt = 0; cnt < records.length(); cnt++)
            {
                JSONObject record = Json.getObject(records, cnt);
                if (record == null) continue;

                String date = Json.getString(record, "date");
                if (date == null) continue;

                String sortdate = Simple.getLocalDateInternal(Simple.getTimeStamp(date));
                if (! Json.has(localDays, sortdate)) Json.put(localDays,sortdate, new JSONArray());
                JSONArray dayRecords = Json.getArray(localDays, sortdate);

                Json.put(dayRecords, record);
            }
        }

        //
        // Convert sorted local day lists to array.
        //

        Iterator<String> keysIterator = localDays.keys();
        JSONArray days = new JSONArray();

        while (keysIterator.hasNext())
        {
            String sortDate = keysIterator.next();

            JSONObject day = new JSONObject();
            Json.put(day, "date", sortDate);
            Json.put(day, "recs", Json.getArray(localDays, sortDate));

            Json.put(days, day);
        }

        //Log.d(LOGTAG, "loadDays: days=" + Json.toPretty(Json.sort(days, "date", true)));

        return Json.sort(days, "date", true);
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
