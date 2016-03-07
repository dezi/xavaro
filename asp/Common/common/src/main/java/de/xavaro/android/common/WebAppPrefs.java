package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class WebAppPrefs
{
    private static final String LOGTAG = WebAppPrefs.class.getSimpleName();

    private final String keyprefix;
    private final String webappname;

    public WebAppPrefs(String webappname)
    {
        this.webappname = webappname;

        keyprefix = "webapps.pref." + webappname + ".";
    }

    @JavascriptInterface
    public String getPrefString(String key)
    {
        return Simple.getSharedPrefString(keyprefix + key);
    }

    @JavascriptInterface
    public void setPrefString(String key, String value)
    {
        Simple.setSharedPrefString(keyprefix + key, value);
    }

    @JavascriptInterface
    public int getPrefInt(String key)
    {
        return Simple.getSharedPrefInt(keyprefix + key);
    }

    @JavascriptInterface
    public void setPrefInt(String key, int value)
    {
        Simple.setSharedPrefInt(keyprefix + key, value);
    }

    @JavascriptInterface
    public boolean getPrefBoolean(String key)
    {
        return Simple.getSharedPrefBoolean(keyprefix + key);
    }

    @JavascriptInterface
    public void setPrefBoolean(String key, boolean value)
    {
        Simple.setSharedPrefBoolean(keyprefix + key, value);
    }

    @JavascriptInterface
    public String getAllPrefs()
    {
        return getAllPrefs(null);
    }

    @JavascriptInterface
    public String getAllPrefs(String prefix)
    {
        String preprefix = keyprefix + ((prefix == null) ? "" : prefix);
        Map<String, Object> prefs = Simple.getAllPreferences(preprefix);

        JSONObject jprefs = new JSONObject();

        for (String key : prefs.keySet())
        {
            Object pref = prefs.get(key);

            if (pref instanceof Set)
            {
                JSONArray joset = new JSONArray();

                for (Object obj : (Set) pref)
                {
                    Json.put(joset, obj);
                }

                Json.put(jprefs, key.substring(keyprefix.length()), joset);
            }
            else
            {
                Json.put(jprefs, key.substring(keyprefix.length()), pref);
            }
        }

        return jprefs.toString();
    }

    @JavascriptInterface
    public void removePref(String key)
    {
        Simple.removeSharedPref(keyprefix + key);
    }

    @JavascriptInterface
    public void removeAllPrefs(String prefix)
    {
        String preprefix = keyprefix + ((prefix == null) ? "" : prefix);
        Map<String, Object> prefs = Simple.getAllPreferences(preprefix);

        JSONObject jprefs = new JSONObject();

        for (String key : prefs.keySet())
        {
            Simple.removeSharedPref(key);
        }
    }
}
