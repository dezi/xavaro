package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class WhatsappGroup extends LaunchGroup
{
    private static final String LOGTAG = WhatsappGroup.class.getSimpleName();

    public WhatsappGroup(Context context)
    {
        super(context);

        this.config = getConfig(context);
    }

    public WhatsappGroup(Context context, String webtype)
    {
        super(context);

        this.config = getConfig(context);
    }

    public WhatsappGroup(Context context, String webtype, String subtype)
    {
        super(context);

        this.config = getConfig(context);
    }

    private JSONObject getConfig(Context context)
    {
        try
        {
            JSONObject launchgroup = new JSONObject();
            JSONArray launchitems = new JSONArray();

            Map<String, Object> whatapps = DitUndDat.SharedPrefs.getPrefix("whatsapp");

            SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

            for (String prefkey : whatapps.keySet())
            {
                if (! (prefkey.startsWith("whatsapp.voip") || prefkey.startsWith("whatsapp.chat")))
                {
                    continue;
                }

                String what = sp.getString(prefkey, null);

                if (what.equals("inact")) continue;

                String waphonenumber = prefkey.substring(14);
                String subtype = prefkey.substring(9,13);
                String label = ProfileImages.getDisplayFromPhone(context, waphonenumber);

                JSONObject whatsentry = new JSONObject();

                whatsentry.put("label",label);
                whatsentry.put("type","whatsapp");
                whatsentry.put("subtype",subtype);
                whatsentry.put("waphonenumber",waphonenumber);

                launchitems.put(whatsentry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + waphonenumber + "=" + label);
            }

            launchgroup.put("launchitems", launchitems);

            return launchgroup;
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        return new JSONObject();
    }
}
