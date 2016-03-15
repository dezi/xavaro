package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

@SuppressWarnings("unused")
public class WebAppMedia
{
    private static final String LOGTAG = WebAppMedia.class.getSimpleName();

    private static final String keyprefix = "media.recorder";

    @JavascriptInterface
    public String getRecordings()
    {
        return EventManager.getComingEvents(keyprefix).toString();
    }

    @JavascriptInterface
    public void putRecordings(String events)
    {
        EventManager.putComingEvents(keyprefix, Json.fromStringArray(events));
    }

    @JavascriptInterface
    public void addRecording(String event)
    {
        EventManager.addComingEvent(keyprefix, Json.fromStringObject(event));
    }

    @JavascriptInterface
    public void removeRecording(String event)
    {
        EventManager.removeComingEvent(keyprefix, Json.fromStringObject(event));
    }

    @JavascriptInterface
    public String getLocaleDefaultChannels(String type)
    {
        JSONObject locale = WebLib.getLocaleConfig("channels");
        return Json.toJavaScript(Json.getArray(locale, "default" + "." + type));
    }

    @JavascriptInterface
    public String getLocaleInternetChannels(String type)
    {
        return Json.toJavaScript(WebLib.getLocaleConfig(type.equals("tv") ? "iptv" : "iprd"));
    }

    @JavascriptInterface
    public String getRecordedItems()
    {
        JSONArray recordings = new JSONArray();

        File mediapath = Simple.getMediaPath("recordings");

        FilenameFilter filter = new Simple.VideoFileFilter();
        JSONArray items = Simple.getDirectorySortedByAge(mediapath, filter, true);

        if (items != null)
        {
            for (int inx = 0; inx < items.length(); inx++)
            {
                JSONObject item = Json.getObject(items, inx);
                String file = Json.getString(item, "file");
                if (file == null) continue;

                Log.d(LOGTAG, "======>getRecordings:" + file);

                File mediafile = new File(file);
                File metafile = Simple.changeExtension(mediafile, ".json");
                File prevfile = Simple.changeExtension(mediafile, ".jpg");

                String metacont = Simple.getFileContent(metafile);
                JSONObject metajson = Json.fromString(metacont);
                if (metajson == null) continue;

                //
                // Rewrite meta content files because
                // video could have been moved.
                //

                Json.put(metajson, "metafile", metafile.toString());
                Json.put(metajson, "mediafile", mediafile.toString());
                Json.put(metajson, "previewfile", prevfile.exists() ? prevfile.toString() : null);

                Json.put(recordings, metajson);
            }
        }

        return Json.toPretty(recordings);
    }
}
