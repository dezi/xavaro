package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class SimpleStorage
{
    private static final String LOGTAG = SimpleStorage.class.getSimpleName();

    private static final Map<String, JSONObject> storages = new HashMap<>();

    private static File storageDir;

    public static void remove(String name, String property)
    {
        Simple.removePost(flusher);

        JSONObject container = getContainer(name);

        synchronized (storages)
        {
            if (! Json.has(container, "data")) Json.put(container, "data", new JSONObject());

            JSONObject data = Json.getObject(container, "data");
            Json.remove(data, property);

            Json.put(container, "updated", Simple.nowAsISO());
            Json.put(container, "dirty", true);
        }

        Log.d(LOGTAG, "remove: " + name + "=" + property);

        Simple.makePost(flusher, 1000);
    }

    public static void put(String name, String property, Object value)
    {
        Simple.removePost(flusher);

        JSONObject container = getContainer(name);

        synchronized (storages)
        {
            if (! Json.has(container, "data")) Json.put(container, "data", new JSONObject());

            JSONObject data = Json.getObject(container, "data");
            Json.put(data, property, value);

            Json.put(container, "updated", Simple.nowAsISO());
            Json.put(container, "dirty", true);
        }

        Log.d(LOGTAG, "put: " + name + "=" + property);

        Simple.makePost(flusher, 1000);
    }

    @Nullable
    public static Object get(String name, String property)
    {
        Object jobj = Json.get(getStorage(name), property);

        if (jobj instanceof JSONArray) return Json.clone((JSONArray) jobj);
        if (jobj instanceof JSONObject) return Json.clone((JSONObject) jobj);

        return jobj;
    }

    @Nullable
    public static String getString(String name, String property)
    {
        return Json.getString(getStorage(name), property);
    }

    public static int getInt(String name, String property)
    {
        return Json.getInt(getStorage(name), property);
    }

    public static boolean getBool(String name, String property)
    {
        return Json.getBoolean(getStorage(name), property);
    }

    @Nullable
    public static JSONObject getJSONObject(String name, String property)
    {
        return Json.clone(Json.getObject(getStorage(name), property));
    }

    @Nullable
    public static JSONArray getJSONArray(String name, String property)
    {
        return Json.clone(Json.getArray(getStorage(name), property));
    }

    public static void addInt(String name, String property, int value)
    {
        if (value != 0) put(name, property, getInt(name, property) + value);
    }

    public static void addArray(String name, String property, String value)
    {
        if (value != null)
        {
            JSONArray array = getJSONArray(name, property);
            if (array == null) array = new JSONArray();
            Json.put(array, value);
            put(name, property, array);
        }
    }

    private static File getStorageDir()
    {
        if (storageDir == null)
        {
            storageDir = new File(Simple.getExternalFilesDir(), "storage");

            if ((!storageDir.exists()) && storageDir.mkdirs())
            {
                Log.d(LOGTAG, "getStorage: created storage dir:" + storageDir);
            }
        }

        return storageDir;
    }

    public static JSONObject getStorage(String name)
    {
        return Json.getObject(getContainer(name), "data");
    }

    private static JSONObject getContainer(String name)
    {
        JSONObject container;

        synchronized (storages)
        {
            if (storages.containsKey(name))
            {
                container = storages.get(name);
            }
            else
            {
                File storageDir = getStorageDir();
                File storageFile = new File(storageDir, name + ".act.json");

                if (!storageFile.exists())
                {
                    storageFile = new File(storageDir, name + ".bak.json");
                }

                if (storageFile.exists())
                {
                    container = Simple.getFileJSONObject(storageFile);
                }
                else
                {
                    container = new JSONObject();
                }

                storages.put(name, container);
            }

            if (! Json.has(container, "data")) Json.put(container, "data", new JSONObject());

            Json.put(container, "accessed", Simple.nowAsISO());
        }

        return container;
    }

    private static final Runnable flusher = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (storages)
            {
                for (String name : storages.keySet())
                {
                    JSONObject container = storages.get(name);
                    if (! Json.getBoolean(container, "dirty")) continue;
                    Json.remove(container, "dirty");

                    File storageDir = getStorageDir();
                    File storageTmpFile = new File(storageDir, name + ".tmp.json");

                    if (Simple.putFileJSON(storageTmpFile, container))
                    {
                        File storageBakFile = new File(storageDir, name + ".bak.json");
                        File storageActFile = new File(storageDir, name + ".act.json");

                        if (storageBakFile.exists() && ! storageBakFile.delete())
                        {
                            Log.d(LOGTAG, "flusher: delete failed:" + storageBakFile);
                        }

                        if (storageActFile.exists() && ! storageActFile.renameTo(storageBakFile))
                        {
                            Log.d(LOGTAG, "flusher: rename failed:" + storageActFile);
                        }

                        if (! storageTmpFile.renameTo(storageActFile))
                        {
                            Log.d(LOGTAG, "flusher: rename failed:" + storageTmpFile);
                        }
                    }
                }
            }
        }
    };
}
