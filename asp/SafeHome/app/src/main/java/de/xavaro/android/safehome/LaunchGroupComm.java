package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;

//
// Utility namespace for app launch groups.
//

public class LaunchGroupComm
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

                SharedPreferences sp = Simple.getSharedPrefs();

                Map<String, Object> xavaros = Simple.getAllPreferences("xavaro");

                for (String prefkey : xavaros.keySet())
                {
                    Log.d(LOGTAG, "===========" + prefkey + "=" + xavaros.get(prefkey));
                }

                //
                // Xavaro users.
                //

                for (String prefkey : xavaros.keySet())
                {
                    String keyprefix = "xavaro.remote.users.chat.";

                    if (! prefkey.startsWith(keyprefix))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String ident = prefkey.substring(keyprefix.length());
                    String label = RemoteContacts.getDisplayName(ident);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "xavaro");
                    whatsentry.put("subtype", "chat");
                    whatsentry.put("chattype", "user");
                    whatsentry.put("identity", ident);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=chat=" + ident + "=" + label);
                }

                //
                // Xavaro groups.
                //

                for (String prefkey : xavaros.keySet())
                {
                    String keyprefix = "xavaro.remote.groups.chat.";

                    if (! prefkey.startsWith(keyprefix))
                    {
                        continue;
                    }

                    String what = sp.getString(prefkey, null);

                    if ((what == null) || what.equals("inact")) continue;

                    String ident = prefkey.substring(keyprefix.length());
                    String label = RemoteGroups.getDisplayName(ident);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "xavaro");
                    whatsentry.put("subtype", "chat");
                    whatsentry.put("chattype", "group");
                    whatsentry.put("identity", ident);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=chat=" + ident + "=" + label);
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
        }

        public static JSONArray getConfig()
        {
            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONArray cdir = new JSONArray();

            SharedPreferences sp = Simple.getSharedPrefs();
            Map<String, Object> prefs = Simple.getAllPreferences("phone");

            for (String prefkey : prefs.keySet())
            {
                if (!(prefkey.startsWith("phone.text") || prefkey.startsWith("phone.voip")))
                {
                    continue;
                }

                String what = sp.getString(prefkey, null);

                if ((what == null) || what.equals("inact")) continue;

                String phonenumber = prefkey.substring(11);
                String subtype = prefkey.substring(6, 10);
                String label = ProfileImages.getDisplayFromPhoneOrSkype(phonenumber);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "phone");
                Json.put(entry, "subtype", subtype);
                Json.put(entry, "phonenumber", phonenumber);
                Json.put(entry, "order", 600);

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);
                if (Simple.sharedPrefEquals(prefkey, "appdir")) Json.put(adir, entry);
                if (Simple.sharedPrefEquals(prefkey, "comdir")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + phonenumber + "=" + label);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "phone");
                Json.put(entry, "label", "Telefon");
                Json.put(entry, "order", 650);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "launchitems", cdir);
                Json.put(home, entry);
            }

            return home;
        }
    }

    public static class SkypeGroup extends LaunchGroup
    {
        private static final String LOGTAG = SkypeGroup.class.getSimpleName();

        public SkypeGroup(Context context)
        {
            super(context);
        }

        public static JSONArray getConfig()
        {
            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONArray cdir = new JSONArray();

            SharedPreferences sp = Simple.getSharedPrefs();
            Map<String, Object> prefs = Simple.getAllPreferences("skype");

            for (String prefkey : prefs.keySet())
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
                String label = ProfileImages.getDisplayFromPhoneOrSkype(skypename);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "skype");
                Json.put(entry, "subtype", subtype);
                Json.put(entry, "skypename", skypename);
                Json.put(entry, "order", 800);

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);
                if (Simple.sharedPrefEquals(prefkey, "appdir")) Json.put(adir, entry);
                if (Simple.sharedPrefEquals(prefkey, "comdir")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + skypename + "=" + label);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "skype");
                Json.put(entry, "label", "Skype");
                Json.put(entry, "order", 850);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "launchitems", cdir);
                Json.put(home, entry);
            }

            return home;
        }
    }

    public static class WhatsappGroup extends LaunchGroup
    {
        private static final String LOGTAG = WhatsappGroup.class.getSimpleName();

        public WhatsappGroup(Context context)
        {
            super(context);
        }

        public static JSONArray getConfig()
        {
            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONArray cdir = new JSONArray();

            SharedPreferences sp = Simple.getSharedPrefs();
            Map<String, Object> prefs = Simple.getAllPreferences("whatsapp");

            for (String prefkey : prefs.keySet())
            {
                if (!(prefkey.startsWith("whatsapp.voip") || prefkey.startsWith("whatsapp.chat")))
                {
                    continue;
                }

                String what = sp.getString(prefkey, null);

                if ((what == null) || what.equals("inact")) continue;

                String waphonenumber = prefkey.substring(14);
                String subtype = prefkey.substring(9, 13);
                String label = ProfileImages.getDisplayFromPhoneOrSkype(waphonenumber);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "whatsapp");
                Json.put(entry, "subtype", subtype);
                Json.put(entry, "waphonenumber", waphonenumber);
                Json.put(entry, "order", 750);

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);
                if (Simple.sharedPrefEquals(prefkey, "appdir")) Json.put(adir, entry);
                if (Simple.sharedPrefEquals(prefkey, "comdir")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=" + subtype + "=" + waphonenumber + "=" + label);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "whatsapp");
                Json.put(entry, "label", "WhatsApp");
                Json.put(entry, "order", 750);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "launchitems", cdir);
                Json.put(home, entry);
            }

            return home;
        }
    }
}