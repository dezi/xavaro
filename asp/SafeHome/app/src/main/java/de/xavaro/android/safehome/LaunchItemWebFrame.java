package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;

public class LaunchItemWebFrame extends LaunchItem
{
    private final static String LOGTAG = LaunchItemWebFrame.class.getSimpleName();

    public LaunchItemWebFrame(Context context)
    {
        super(context);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("ioc")) launchWebFrame();
    }

    private void launchWebFrame()
    {
        if (config.has("name"))
        {
            String url = Json.getString(config, "url");
            String name = Json.getString(config, "name");

            LaunchFrameWebFrame webframe = new LaunchFrameWebFrame(context, this);
            webframe.setLoadURL(config, name, url);

            ((HomeActivity) context).addViewToBackStack(webframe);
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupWebStream(context, this);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            if (config.has("name"))
            {
                launchWebFrame();
            }

            return true;
        }

        return false;
    }
}
