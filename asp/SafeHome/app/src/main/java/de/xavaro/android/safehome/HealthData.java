package de.xavaro.android.safehome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class HealthData
{
    public static void setLastReadDate(String datatype)
    {
        JSONObject status = getStatus(datatype);
        Json.put(status, "lastReadDate", Simple.nowAsISO());
        putStatus(datatype, status);
    }

    public static JSONObject getStatus(String datatype)
    {
        JSONObject status = new JSONObject();

        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".status.json";
        String content = Simple.readDatadirFile(filename);
        if (content != null) status = Json.fromStringObject(content);

        return status;
    }

    public static void putStatus(String datatype, JSONObject status)
    {
        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".status.json";
        Simple.writeDatadirFile(filename, Json.toPretty(status));
    }

    public static void clearStatus(String datatype)
    {
        putStatus(datatype, new JSONObject());
    }

    public static void addRecord(String datatype, JSONObject record)
    {
        JSONArray records = getRecords(datatype);

        for (int inx = 0; inx < records.length(); inx++)
        {
            JSONObject oldrecord = Json.getObject(records, inx);

            if (Json.equals(record, "utc", oldrecord))
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

        if (record.has("utc")) records = Json.sort(records, "utc", true);

        putRecords(datatype, records);
    }

    public static JSONArray getRecords(String datatype)
    {
        JSONArray records = new JSONArray();

        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".records.json";
        String content = Simple.readDatadirFile(filename);
        if (content != null) records = Json.fromStringArray(content);

        return records;
    }

    public static void putRecords(String datatype, JSONArray records)
    {
        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".records.json";
        Simple.writeDatadirFile(filename, Json.toPretty(records));
    }

    public static void clearRecords(String datatype)
    {
        putRecords(datatype, new JSONArray());
    }
}
