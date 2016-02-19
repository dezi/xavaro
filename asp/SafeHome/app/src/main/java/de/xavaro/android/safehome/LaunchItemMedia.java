package de.xavaro.android.safehome;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommSender;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public abstract class LaunchItemMedia extends LaunchItem
{
    private final static String LOGTAG = LaunchItemMedia.class.getSimpleName();

    public LaunchItemMedia(Context context)
    {
        super(context);
    }

    protected FileObserver observer;
    protected String obserdir;

    @Override
    protected void setConfig()
    {
        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if (mediapath != null)
            {
                if (! mediapath.exists())
                {
                    //noinspection ResultOfMethodCallIgnored
                    mediapath.mkdir();
                }

                if (mediapath.isDirectory())
                {
                    int mask = FileObserver.CREATE | FileObserver.DELETE;
                    obserdir = mediapath.toString();

                    observer = new FileObserver(obserdir, mask)
                    {
                        @Override
                        public void onEvent(int event, String path)
                        {
                            final int pevent = event;
                            final String ppath = path;

                            post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onFileObserverEvent(pevent, ppath);
                                }
                            });
                        }
                    };

                    observer.startWatching();
                }
            }
        }
    }

    protected void onFileObserverEvent(int event, String path)
    {
        if (event == FileObserver.CREATE)
        {
            Log.d(LOGTAG, "onFileObserverEvent: CREATE=" + obserdir + "/" + path);
        }

        if (event == FileObserver.DELETE)
        {
            Log.d(LOGTAG, "onFileObserverEvent: DELETE=" + obserdir + "/" + path);
        }
    }
}
