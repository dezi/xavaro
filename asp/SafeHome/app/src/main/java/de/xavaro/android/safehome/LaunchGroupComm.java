package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;

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
        }

        public static JSONArray getConfig()
        {
            if (! Simple.getSharedPrefBoolean("xavaro.enable")) return null;

            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONArray cdir = new JSONArray();

            SharedPreferences sp = Simple.getSharedPrefs();
            Map<String, Object> prefs = Simple.getAllPreferences("xavaro");

            //
            // Xavaro users.
            //

            for (String prefkey : prefs.keySet())
            {
                String keyprefix = "xavaro.remote.users.chat.";

                if (!prefkey.startsWith(keyprefix)) continue;

                String what = sp.getString(prefkey, null);

                if ((what == null) || what.equals("inact")) continue;

                String ident = prefkey.substring(keyprefix.length());
                String label = RemoteContacts.getDisplayName(ident);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "xavaro");
                Json.put(entry, "subtype", "chat");
                Json.put(entry, "chattype", "user");
                Json.put(entry, "identity", ident);
                Json.put(entry, "order", 500);

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);
                if (Simple.sharedPrefEquals(prefkey, "appdir")) Json.put(adir, entry);
                if (Simple.sharedPrefEquals(prefkey, "comdir")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=chat=" + ident + "=" + label);
            }

            //
            // Xavaro groups.
            //

            for (String prefkey : prefs.keySet())
            {
                String keyprefix = "xavaro.remote.groups.chat.";

                if (! prefkey.startsWith(keyprefix)) continue;

                String what = sp.getString(prefkey, null);

                if ((what == null) || what.equals("inact")) continue;

                String ident = prefkey.substring(keyprefix.length());
                String label = RemoteGroups.getDisplayName(ident);
                String gtype = RemoteGroups.getGroupType(ident);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "xavaro");
                Json.put(entry, "subtype", "chat");
                Json.put(entry, "chattype", "group");
                Json.put(entry, "grouptype", gtype);
                Json.put(entry, "identity", ident);

                Json.put(entry, "order", Simple.equals(gtype, "alertcall") ? 200 : 550);

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);
                if (Simple.sharedPrefEquals(prefkey, "appdir")) Json.put(adir, entry);
                if (Simple.sharedPrefEquals(prefkey, "comdir")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + prefkey + "=chat=" + ident + "=" + label);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "xavaro");
                Json.put(entry, "label", "Chats");
                Json.put(entry, "order", 550);

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

    public static class PhoneGroup extends LaunchGroup
    {
        private static final String LOGTAG = PhoneGroup.class.getSimpleName();

        public PhoneGroup(Context context)
        {
            super(context);
        }

        public static JSONArray getConfig()
        {
            if (! Simple.getSharedPrefBoolean("phone.enable")) return null;

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

                JSONObject intent = VoiceIntent.getIntent("phone." + subtype);

                if (intent != null)
                {
                    VoiceIntent.prepareIconRes(intent, "phone", "voip", GlobalConfigs.IconResPhoneAppCall);
                    VoiceIntent.prepareIconRes(intent, "phone", "text", GlobalConfigs.IconResPhoneAppText);

                    VoiceIntent.prepareLabel(intent, label);

                    Json.put(entry, "intent", intent);
                }

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);

                Json.put(adir, entry);
                Json.put(cdir, entry);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "phone");
                Json.put(entry, "label", "Telefonbuch");
                Json.put(entry, "order", 650);

                JSONObject intent = VoiceIntent.getIntent("phone.register");

                if (intent != null)
                {
                    VoiceIntent.prepareIconRes(intent, "phone", "voip", GlobalConfigs.IconResPhoneAppCall);
                    VoiceIntent.prepareIconRes(intent, "phone", "text", GlobalConfigs.IconResPhoneAppText);
                    Json.put(entry, "intent", intent);
                }

                JSONArray intents = VoiceIntent.getIntents("phone.register");

                if (intents != null)
                {
                    VoiceIntent.prepareIconRes(intents, "phone", "voip", GlobalConfigs.IconResPhoneAppCall);
                    VoiceIntent.prepareIconRes(intents, "phone", "text", GlobalConfigs.IconResPhoneAppText);
                    Json.put(entry, "intents", intents);
                }

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "iconres", GlobalConfigs.IconResContacts);

                JSONObject intent = VoiceIntent.getIntent("contacts.register");
                if (intent != null) Json.put(entry, "intent", intent);

                JSONArray intents = VoiceIntent.getIntents("contacts.register");
                if (intents != null) Json.put(entry, "intents", intents);

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
            if (! Simple.getSharedPrefBoolean("skype.enable")) return null;

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

                JSONObject intent = VoiceIntent.getIntent("skype." + subtype);

                if (intent != null)
                {
                    VoiceIntent.prepareIconRes(intent, "skype", "voip", GlobalConfigs.IconResSkypeVoip);
                    VoiceIntent.prepareIconRes(intent, "skype", "chat", GlobalConfigs.IconResSkypeChat);
                    VoiceIntent.prepareIconRes(intent, "skype", "vica", GlobalConfigs.IconResSkypeVica);

                    VoiceIntent.prepareLabel(intent, label);

                    Json.put(entry, "intent", intent);
                }

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);

                Json.put(adir, entry);
                Json.put(cdir, entry);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "skype");
                Json.put(entry, "label", "Skype");
                Json.put(entry, "order", 850);

                Json.put(entry, "iconres", GlobalConfigs.IconResSkype);

                JSONObject intent = VoiceIntent.getIntent("skype.register");
                if (intent != null) Json.put(entry, "intent", intent);

                JSONArray intents = VoiceIntent.getIntents("skype.register");
                if (intents != null)  Json.put(entry, "intents", intents);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "iconres", GlobalConfigs.IconResContacts);

                JSONObject intent = VoiceIntent.getIntent("contacts.register");
                if (intent != null) Json.put(entry, "intent", intent);

                JSONArray intents = VoiceIntent.getIntents("contacts.register");
                if (intents != null) Json.put(entry, "intents", intents);

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
            if (! Simple.getSharedPrefBoolean("whatsapp.enable")) return null;

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

                JSONObject intent = VoiceIntent.getIntent("whatsapp." + subtype);

                if (intent != null)
                {
                    VoiceIntent.prepareIconRes(intent, "whatsapp", "voip", GlobalConfigs.IconResWhatsAppVoip);
                    VoiceIntent.prepareIconRes(intent, "whatsapp", "chat", GlobalConfigs.IconResWhatsAppChat);

                    VoiceIntent.prepareLabel(intent, label);

                    Json.put(entry, "intent", intent);
                }

                if (Simple.sharedPrefEquals(prefkey, "home")) Json.put(home, entry);

                Json.put(adir, entry);
                Json.put(cdir, entry);
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "whatsapp");
                Json.put(entry, "label", "WhatsApp");
                Json.put(entry, "order", 750);

                Json.put(entry, "iconres", GlobalConfigs.IconResWhatsApp);

                JSONObject intent = VoiceIntent.getIntent("whatsapp.register");
                if (intent != null) Json.put(entry, "intent", intent);

                JSONArray intents = VoiceIntent.getIntents("whatsapp.register");
                if (intents != null)  Json.put(entry, "intents", intents);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "iconres", GlobalConfigs.IconResContacts);

                JSONObject intent = VoiceIntent.getIntent("contacts.register");
                if (intent != null) Json.put(entry, "intent", intent);

                JSONArray intents = VoiceIntent.getIntents("contacts.register");
                if (intents != null) Json.put(entry, "intents", intents);

                Json.put(entry, "launchitems", cdir);
                Json.put(home, entry);
            }

            return home;
        }
    }
}