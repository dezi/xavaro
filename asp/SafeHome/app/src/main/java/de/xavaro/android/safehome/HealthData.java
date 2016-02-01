package de.xavaro.android.safehome;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class HealthData
{
    public static JSONObject getStatus(String datatype)
    {
        JSONObject status = new JSONObject();

        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".status.json";
        String content = Simple.readDatadirFile(filename);
        if (content != null) status = Json.fromStringObject(content);

        return status;
    }

    public static void putStatus(String datatype, JSONObject protocoll)
    {
        String filename = Simple.getPackageName() + ".healthdata." + datatype + ".status.json";
        Simple.writeDatadirFile(filename, Json.toPretty(protocoll));
    }

    public static void addRecord(String datatype, JSONObject record)
    {
        JSONArray records = getRecords(datatype);

        for (int inx = 0; inx < records.length(); inx++)
        {
            JSONObject oldrecord = Json.getObject(records, inx);

            if (Json.equals(record, "utc", oldrecord)) return;
        }

        records.put(record);

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
}
