package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class HealthData
{
    private static final String LOGTAG = HealthData.class.getSimpleName();

    public static File getDataDir()
    {
        File datadir = new File(Simple.getExternalFilesDir(), "health");

        if ((! datadir.exists()) && (! datadir.mkdirs()))
        {
            Log.d(LOGTAG, "getDataDir: cannot create dir=" + datadir);
        }

        return datadir;
    }

    public static void setLastReadDate(String datatype)
    {
        JSONObject status = getStatus(datatype);
        Json.put(status, "lastReadDate", Simple.nowAsISO());
        putStatus(datatype, status);
    }

    public static JSONObject getStatus(String datatype)
    {
        File datafile = new File(getDataDir(), datatype + ".status.json");
        JSONObject status = Simple.getFileJSONObject(datafile);
        return (status != null) ? status : new JSONObject();
    }

    public static void putStatus(String datatype, JSONObject status)
    {
        File datafile = new File(getDataDir(), datatype + ".status.json");
        Simple.putFileJSON(datafile, status);
    }

    public static void clearStatus(String datatype)
    {
        putStatus(datatype, new JSONObject());
    }

    public static void delRecord(String datatype, JSONObject record)
    {
        String dts = Json.getString(record, "dts");
        if (dts == null) return;

        if (dts.endsWith("Z") && (dts.length() == 24))
        {
            Json.put(record, "dts", dts.substring(0, dts.length() - 5) + "Z");
        }

        JSONArray records = getRecords(datatype);

        for (int inx = 0; inx < records.length(); inx++)
        {
            JSONObject oldrecord = Json.getObject(records, inx);

            dts = Json.getString(oldrecord, "dts");

            if (dts == null)
            {
                Json.remove(records, inx--);
                continue;
            }

            if (dts.endsWith("Z") && (dts.length() == 24))
            {
                Json.put(oldrecord, "dts", dts.substring(0, dts.length() - 5) + "Z");
            }

            if (Json.equals(record, "dts", oldrecord))
            {
                records.remove(inx);
                putRecords(datatype, records);

                //
                // Check for external file and delete if present.
                //

                String exf = Json.getString(oldrecord, "exf");

                if (exf != null)
                {
                    File delfile = new File(getDataDir(), exf);

                    if (delfile.exists() && delfile.delete())
                    {
                        Log.d(LOGTAG, "delRecord deleted=" + delfile);
                    }
                }

                return;
            }
        }
    }

    public static void addRecord(String datatype, JSONObject record)
    {
        String dts = Json.getString(record, "dts");
        if (dts == null) return;

        if (dts.endsWith("Z") && (dts.length() == 24))
        {
            Json.put(record, "dts", dts.substring(0, dts.length() - 5) + "Z");
        }

        JSONArray records = getRecords(datatype);

        for (int inx = 0; inx < records.length(); inx++)
        {
            JSONObject oldrecord = Json.getObject(records, inx);

            dts = Json.getString(oldrecord, "dts");

            if (dts == null)
            {
                Json.remove(records, inx--);
                continue;
            }

            if (dts.endsWith("Z") && (dts.length() == 24))
            {
                Json.put(oldrecord, "dts", dts.substring(0, dts.length() - 5) + "Z");
            }

            if (Json.equals(record, "dts", oldrecord))
            {
                Iterator<String> keysIterator = record.keys();

                while (keysIterator.hasNext())
                {
                    String property = keysIterator.next();

                    Json.put(oldrecord, property, Json.get(record, property));
                }

                putRecords(datatype, records);

                return;
            }
        }

        records.put(record);

        records = Json.sort(records, "dts", true);

        putRecords(datatype, records);
    }

    public static JSONArray getRecords(String datatype)
    {
        File datafile = new File(getDataDir(), datatype + ".records.json");
        JSONArray records = Simple.getFileJSONArray(datafile);
        return (records != null) ? records : new JSONArray();
    }

    @SuppressWarnings("WeakerAccess")
    public static void putRecords(String datatype, JSONArray records)
    {
        File datafile = new File(getDataDir(), datatype + ".records.json");
        Simple.putFileJSON(datafile, records);
    }

    public static void clearRecords(String datatype)
    {
        putRecords(datatype, new JSONArray());
    }
}
