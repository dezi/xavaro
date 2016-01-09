package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.IdentityManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.SettingsManager;

//
// Utility namespace for app launch groups.
//

public class AppsGroup
{
    public static class XavaroGroup extends LaunchGroup
    {
        private static final String LOGTAG = XavaroGroup.class.getSimpleName();

        public XavaroGroup(Context context)
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

                Map<String, Object> xavaros = DitUndDat.SharedPrefs.getPrefix("xavaro");

                for (String prefkey : xavaros.keySet())
                {
                    Log.d(LOGTAG, "===========" + prefkey + "=" + xavaros.get(prefkey));
                }

                SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

                for (String prefkey : xavaros.keySet())
                {
                    if (! prefkey.startsWith("xavaro.remote.chat"))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String ident = prefkey.substring(19);
                    String subtype = prefkey.substring(14, 18);
                    String label = RemoteContacts.getDisplayName(ident);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "xavaro");
                    whatsentry.put("subtype", subtype);
                    whatsentry.put("ident", ident);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + ident + "=" + label);
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

    public static class PhoneGroup extends LaunchGroup
    {
        private static final String LOGTAG = PhoneGroup.class.getSimpleName();

        public PhoneGroup(Context context)
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

                Map<String, Object> skypes = DitUndDat.SharedPrefs.getPrefix("phone");

                SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

                for (String prefkey : skypes.keySet())
                {
                    if (!(prefkey.startsWith("phone.text") || prefkey.startsWith("phone.voip")))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String phonenumber = prefkey.substring(11);
                    String subtype = prefkey.substring(6, 10);
                    String label = ProfileImages.getDisplayFromPhoneOrSkype(context, phonenumber);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "phone");
                    whatsentry.put("subtype", subtype);
                    whatsentry.put("phonenumber", phonenumber);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + phonenumber + "=" + label);
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

    public static class SkypeGroup extends LaunchGroup
    {
        private static final String LOGTAG = SkypeGroup.class.getSimpleName();

        public SkypeGroup(Context context)
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

                Map<String, Object> skypes = DitUndDat.SharedPrefs.getPrefix("skype");

                SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

                for (String prefkey : skypes.keySet())
                {
                    if (!(prefkey.startsWith("skype.voip")
                            || prefkey.startsWith("skype.chat")
                            || prefkey.startsWith("skype.vica")))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String skypename = prefkey.substring(11);
                    String subtype = prefkey.substring(6, 10);
                    String label = ProfileImages.getDisplayFromPhoneOrSkype(context, skypename);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "skype");
                    whatsentry.put("subtype", subtype);
                    whatsentry.put("skypename", skypename);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + skypename + "=" + label);
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

    public static class WhatsappGroup extends LaunchGroup
    {
        private static final String LOGTAG = WhatsappGroup.class.getSimpleName();

        public WhatsappGroup(Context context)
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
                    if (!(prefkey.startsWith("whatsapp.voip") || prefkey.startsWith("whatsapp.chat")))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String waphonenumber = prefkey.substring(14);
                    String subtype = prefkey.substring(9, 13);
                    String label = ProfileImages.getDisplayFromPhoneOrSkype(context, waphonenumber);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "whatsapp");
                    whatsentry.put("subtype", subtype);
                    whatsentry.put("waphonenumber", waphonenumber);

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
}