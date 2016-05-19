package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SystemIdentity;
import de.xavaro.android.common.WebLib;

public class PreferencesComm
{
    private static final String LOGTAG = PreferencesComm.class.getSimpleName();

    //region Phone preferences

    public static class PhoneFragment extends ContactsFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Telefon";
            header.iconRes = GlobalConfigs.IconResPhoneApp;
            header.fragment = PhoneFragment.class.getName();

            return header;
        }

        public PhoneFragment()
        {
            super();

            isPhone = true;
            iconres = GlobalConfigs.IconResPhoneApp;
            keyprefix = "phone";
            masterenable = "Telefon freischalten";
        }
    }

    //endregion Phone preferences

    //region Skype preferences

    public static class SkypeFragment extends ContactsFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Skype";
            header.iconRes = GlobalConfigs.IconResSkype;
            header.fragment = SkypeFragment.class.getName();

            return header;
        }

        public SkypeFragment()
        {
            super();

            isSkype = true;
            iconres = GlobalConfigs.IconResSkype;
            keyprefix = "skype";
            masterenable = "Skype freischalten";
            installtext = "Skype";
            installpack = CommonConfigs.packageSkype;
        }
    }

    //endregion Skype preferences

    //region WhatsApp preferences

    public static class WhatsAppFragment extends ContactsFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "WhatsApp";
            header.iconRes = GlobalConfigs.IconResWhatsApp;
            header.fragment = WhatsAppFragment.class.getName();

            return header;
        }

        public WhatsAppFragment()
        {
            super();

            isWhatsApp = true;
            iconres = GlobalConfigs.IconResWhatsApp;
            keyprefix = "whatsapp";
            masterenable = "WhatsApp freischalten";
            installtext = "WhatsApp";
            installpack = CommonConfigs.packageWhatsApp;
        }
    }

    //endregion WhatsApp preferences

    //region Contacts preferences stub

    public static class ContactsFragmentStub extends PreferenceFragments.EnableFragmentStub
    {
        protected boolean isPhone;
        protected boolean isSkype;
        protected boolean isWhatsApp;
        protected String installtext;
        protected String installpack;

        protected final CharSequence[] destText = {
                "Nicht aktiviert",
                "Home-Bildschirm",
                "App-Verzeichnis",
                "Kontakte-Verzeichnis"};

        protected final CharSequence[] destVals = {
                "inact",
                "home",
                "appdir",
                "comdir" };

        protected final CharSequence[] installText = {
                "Installieren",
                "Bereit"};

        protected final CharSequence[] installVals = {
                "notinst",
                "ready"};

        private void putNumberType(JSONObject numbers, String type, String orignumber)
        {
            if (orignumber == null) return;

            String nospacenum = orignumber.replace(" ", "");

            if (nospacenum.endsWith("@s.whatsapp.net"))
            {
                nospacenum = "+" + nospacenum.replace("@s.whatsapp.net", "");
            }

            JSONObject number;

            if (! numbers.has(nospacenum))
            {
                number = new JSONObject();
                Json.put(numbers, nospacenum, number);
            }
            else
            {
                number = Json.getObject(numbers, nospacenum);
            }

            Json.put(number, type, nospacenum);
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference nc;
            NicedPreferences.NiceScorePreference sp;
            NicedPreferences.NiceListPreference lp;

            boolean enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");

            if (installpack != null)
            {
                boolean installed = Simple.isAppInstalled(installpack);

                JSONObject config = WebLib.getLocaleConfig("appstore");
                config = Json.getObject(config, "essential");
                config = Json.getObject(config, installpack);

                if (config != null)
                {
                    nc = new NicedPreferences.NiceCategoryPreference(context);
                    nc.setTitle(Json.getString(config, "label") + " – " + "Anwendung");
                    nc.setEnabled(enabled);

                    preferences.add(nc);

                    //
                    // Score preference.
                    //

                    sp = new NicedPreferences.NiceScorePreference(context);

                    sp.setKey(keyprefix + ".installed");
                    sp.setEntries(installText);
                    sp.setEntryValues(installVals);
                    sp.setTitle(Json.getString(config, "label"));
                    sp.setSummary(Json.getString(config, "summary"));
                    sp.setEnabled(enabled);

                    String score = Json.getString(config, "score");
                    sp.setScore((score == null) ? -1 : Integer.parseInt(score));
                    sp.setAPKName(installpack);

                    Simple.setSharedPrefString(sp.getKey(), installed ? "ready" : "notinst");

                    preferences.add(sp);
                    activekeys.add(sp.getKey());
                }
                else
                {
                    nc = new NicedPreferences.NiceCategoryPreference(context);
                    nc.setTitle(installtext + " – " + "Anwendung");
                    nc.setEnabled(enabled);

                    preferences.add(nc);

                    //
                    // Legacy preference.
                    //

                    lp = new NicedPreferences.NiceListPreference(context);

                    lp.setEntries(installText);
                    lp.setEntryValues(installVals);
                    lp.setKey(keyprefix + ".installed");
                    lp.setTitle(installtext);
                    lp.setEnabled(enabled);

                    //
                    // This is nice about Java. No clue how it is done!
                    //

                    final String installName = installpack;

                    lp.setOnclick(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Simple.installAppFromPlaystore(installName);
                        }
                    });

                    Simple.setSharedPrefString(lp.getKey(), installed ? "ready" : "notinst");

                    preferences.add(lp);
                    activekeys.add(lp.getKey());
                }
            }

            JSONObject contacts = ContactsHandler.getJSONData(context);
            Iterator<String> keysIterator = contacts.keys();

            while (keysIterator.hasNext())
            {
                //
                // We are in a contact.
                //

                String cid = keysIterator.next();
                JSONArray items = Json.getArray(contacts, cid);
                if (items == null) continue;

                String name = null;
                JSONObject numbers = new JSONObject();

                for (int inx = 0; inx < items.length(); inx++)
                {
                    JSONObject item = Json.getObject(items, inx);
                    if ((item == null) || ! item.has("KIND")) continue;
                    String kind = Json.getString(item, "KIND");

                    if (Simple.equals(kind, "StructuredName"))
                    {
                        //
                        // Workaround for Skype which puts
                        // nickname as display name and
                        // duplicates it into given name.
                        //

                        String disp = Json.getString(item, "DISPLAY_NAME");
                        String gina = Json.getString(item, "GIVEN_NAME");

                        if ((name == null) || ! Simple.equals(disp, gina)) name = disp;
                    }

                    if (Simple.equals(kind, "Phone") && ! isSkype)
                    {
                        //
                        // There are sometimes duplicate entries with
                        // number with space and w/o spaces. We prefer
                        // the numbers with spaces.
                        //

                        String isspacenum = Simple.UTF8defuck(Json.getString(item, "NUMBER"));
                        if (isspacenum == null) continue;

                        String nospacenum = isspacenum.replace(" ", "");

                        JSONObject number;

                        if (! numbers.has(nospacenum))
                        {
                            number = new JSONObject();
                            Json.put(numbers, nospacenum, number);
                        }
                        else
                        {
                            number = Json.getObject(numbers, nospacenum);
                        }

                        if (number == null) continue;

                        if (isspacenum.contains(" ") || ! number.has("nicephone"))
                        {
                            Json.put(number, "nicephone", isspacenum);
                            Json.put(number, "label", Json.getString(item, "LABEL"));
                            Json.put(number, "type", Json.getInt(item, "TYPE"));

                            int type = Json.getInt(item, "TYPE");

                            if (type != 0)
                            {
                                String tkey = "" + type;
                                String label = Simple.getTransVal(R.array.phone_labels_keys, tkey);
                                Json.put(number, "label", label);
                            }
                        }
                    }

                    if (isPhone)
                    {
                        if (Simple.equals(kind, "Phone"))
                        {
                            putNumberType(numbers, "voipphone",
                                    Simple.UTF8defuck(Json.getString(item, "NUMBER")));

                            putNumberType(numbers, "textphone",
                                    Simple.UTF8defuck(Json.getString(item, "NUMBER")));
                        }
                    }

                    if (isSkype)
                    {
                        if (Simple.equals(kind, "@com.skype.android.chat.action"))
                        {
                            putNumberType(numbers, "chatphone", Json.getString(item, "DATA1"));
                        }

                        if (Simple.equals(kind, "@com.skype.android.skypecall.action"))
                        {
                            putNumberType(numbers, "voipphone", Json.getString(item, "DATA1"));
                        }

                        if (Simple.equals(kind, "@com.skype.android.videocall.action"))
                        {
                            putNumberType(numbers, "vicaphone", Json.getString(item, "DATA1"));
                        }
                    }

                    if (isWhatsApp)
                    {
                        if (Simple.equals(kind, "@vnd.com.whatsapp.profile"))
                        {
                            putNumberType(numbers, "chatphone", Json.getString(item, "DATA1"));
                        }

                        if (Simple.equals(kind, "@vnd.com.whatsapp.voip.call"))
                        {
                            putNumberType(numbers, "voipphone", Json.getString(item, "DATA1"));
                        }
                    }
                }

                //
                // Build contacts category preference.
                //

                Iterator<String> numbersIterator = numbers.keys();

                boolean first = true;
                boolean multi = false;

                while (numbersIterator.hasNext())
                {
                    String nospacenumber = numbersIterator.next();
                    JSONObject number = Json.getObject(numbers, nospacenumber);
                    if (number == null) continue;

                    Log.d(LOGTAG, number.toString());

                    String nicephone = Json.getString(number, "nicephone");
                    String voipphone = Json.getString(number, "voipphone");
                    String textphone = Json.getString(number, "textphone");
                    String vicaphone = Json.getString(number, "vicaphone");
                    String chatphone = Json.getString(number, "chatphone");

                    if ((voipphone == null) && (textphone == null) &&
                            (vicaphone == null) && (chatphone == null)) continue;

                    if (isSkype) nicephone = vicaphone;

                    if (isPhone && (nicephone != null) && (nicephone.length() < 6))
                    {
                        //
                        // Remove all special phone numbers.
                        //

                        continue;
                    }

                    if (first)
                    {
                        multi = numbersIterator.hasNext();
                        first = false;

                        Drawable icon = ProfileImages.getProfileDrawable(nicephone, true);

                        nc = new NicedPreferences.NiceCategoryPreference(context);
                        nc.setTitle(name + (multi ? "" : " " + nicephone));
                        nc.setIcon(icon);
                        nc.setEnabled(enabled);

                        preferences.add(nc);
                    }

                    if (chatphone != null)
                    {
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setKey(keyprefix + ".chat." + chatphone);
                        lp.setTitle("Nachricht" + (multi ? " " + nicephone : ""));

                        if (multi) lp.setSummary(Json.getString(number, "label"));
                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (textphone != null)
                    {
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setKey(keyprefix + ".text." + voipphone);
                        lp.setTitle("SMS" + (multi ? " " + nicephone : ""));

                        if (multi) lp.setSummary(Json.getString(number, "label"));
                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (voipphone != null)
                    {
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setKey(keyprefix + ".voip." + voipphone);
                        lp.setTitle("Anruf" + (multi ? " " + nicephone : ""));

                        if (multi) lp.setSummary(Json.getString(number, "label"));
                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (vicaphone != null)
                    {
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setKey(keyprefix + ".vica." + vicaphone);
                        lp.setTitle("Videoanruf" + (multi ? " " + nicephone : ""));

                        if (multi) lp.setSummary(Json.getString(number, "label"));
                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }
                }
            }

            //
            // Remove disabled or obsoleted preferences.
            //

            String websiteprefix = keyprefix + ".";

            Map<String, ?> exists = Simple.getAllPreferences(websiteprefix);

            for (Map.Entry<String, ?> entry : exists.entrySet())
            {
                if (activekeys.contains(entry.getKey())) continue;

                Simple.removeSharedPref(entry.getKey());
            }
        }
    }

    //endregion Contacts preferences stub
}
