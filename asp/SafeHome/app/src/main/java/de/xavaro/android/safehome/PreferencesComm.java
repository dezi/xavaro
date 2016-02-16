package de.xavaro.android.safehome;

import android.content.Context;
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
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SystemIdentity;

public class PreferencesComm
{
    private static final String LOGTAG = PreferencesComm.class.getSimpleName();

    //region Xavaro communication preferences

    public static class XavaroFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Xavaro";
            header.iconRes = GlobalConfigs.IconResXavaro;
            header.fragment = XavaroFragment.class.getName();

            return header;
        }

        protected final CharSequence[] sendtoText= {
                "Nicht aktiviert",
                "Home-Bildschirm",
                "App-Verzeichnis",
                "Kontakte-Verzeichnis"};

        protected final CharSequence[] sendtoVals = {
                "inact",
                "home",
                "appdir",
                "comdir" };

        public XavaroFragment()
        {
            super();

            iconres = GlobalConfigs.IconResXavaro;
            keyprefix = "xavaro";
            masterenable = "Xavaro Kommunikation freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            //
            // Confirmed connects.
            //

            registerRemotes(context, true);
        }

        private final ArrayList<String> remoteContacts = new ArrayList<>();

        private void registerRemotes(Context context, boolean initial)
        {
            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference cb;

            activekeys.clear();

            //
            // Remote user contacts.
            //

            String usersxpath = "RemoteContacts/identities";
            JSONObject rcs = PersistManager.getXpathJSONObject(usersxpath);

            if (rcs != null)
            {
                Iterator<String> keysIterator = rcs.keys();

                while (keysIterator.hasNext())
                {
                    String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String prefkey = keyprefix + ".remote.users";

                    String name = RemoteContacts.getDisplayName(ident);

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(name);

                    preferences.add(pc);
                    if (!initial) getPreferenceScreen().addPreference(pc);

                    cb = new NicedPreferences.NiceListPreference(context);
                    cb.setKey(prefkey + ".chat." + ident);
                    cb.setEntries(sendtoText);
                    cb.setEntryValues(sendtoVals);
                    cb.setDefaultValue("inact");
                    cb.setTitle("Nachricht");

                    preferences.add(cb);
                    activekeys.add(cb.getKey());

                    if (!initial) getPreferenceScreen().addPreference(cb);
                }
            }

            //
            // Remote group contacts.
            //

            String groupsxpath = "RemoteGroups/groupidentities";
            JSONObject rgs = PersistManager.getXpathJSONObject(groupsxpath);

            if (rgs != null)
            {
                Iterator<String> keysIterator = rgs.keys();

                while (keysIterator.hasNext())
                {
                    String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String grouptype = RemoteGroups.getGroupType(ident);
                    String groupowner = RemoteGroups.getGroupOwner(ident);

                    if (Simple.equals(grouptype, "alertcall") &&
                            Simple.equals(groupowner, SystemIdentity.getIdentity()))
                    {
                        //
                        // This alertgroup is owned by the owner. The chat dialog
                        // will always be available under the alerter icon.
                        //

                        continue;
                    }

                    String prefkey = keyprefix + ".remote.groups";

                    String name = RemoteGroups.getDisplayName(ident);

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(name);

                    preferences.add(pc);
                    if (!initial) getPreferenceScreen().addPreference(pc);

                    cb = new NicedPreferences.NiceListPreference(context);
                    cb.setKey(prefkey + ".chat." + ident);
                    cb.setEntries(sendtoText);
                    cb.setEntryValues(sendtoVals);
                    cb.setDefaultValue("inact");
                    cb.setTitle("Nachricht");

                    preferences.add(cb);
                    activekeys.add(cb.getKey());

                    if (!initial) getPreferenceScreen().addPreference(cb);
                }
            }

            //
            // Remove disabled or obsoleted preferences.
            //

            String websiteprefix = keyprefix + ".";

            Map<String, ?> exists = Simple.getAllPreferences(keyprefix + ".remote.");

            for (Map.Entry<String, ?> entry : exists.entrySet())
            {
                if (activekeys.contains(entry.getKey())) continue;

                Simple.removeSharedPref(entry.getKey());
            }

        }
    }

    //endregion Xavaro remote preferences

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
            installtext = "Skype App";
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
            installtext = "WhatsApp App";
            installpack = CommonConfigs.packageWhatsApp;
        }
    }

    //endregion WhatsApp preferences

    //region Contacts preferences stub

    public static class ContactsFragmentStub extends SettingsFragments.EnablePreferenceFragment
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
            NicedPreferences.NiceListPreference lp;

            boolean enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");

            if (installpack != null)
            {
                lp = new NicedPreferences.NiceListPreference(context);

                boolean installed = Simple.isAppInstalled(installpack);

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

                        String isspacenum = Json.getString(item, "NUMBER");
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
                        }
                    }

                    if (isPhone)
                    {
                        if (Simple.equals(kind, "Phone"))
                        {
                            putNumberType(numbers, "voipphone", Json.getString(item, "NUMBER"));
                            putNumberType(numbers, "textphone", Json.getString(item, "NUMBER"));
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

                    if (first)
                    {
                        multi = numbersIterator.hasNext();
                        first = false;

                        nc = new NicedPreferences.NiceCategoryPreference(context);
                        nc.setTitle(name + (multi ? "" : " " + nicephone));
                        nc.setEnabled(enabled);
                        preferences.add(nc);
                    }

                    if (chatphone != null)
                    {
                        String key = keyprefix + ".chat." + chatphone;
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setKey(key);
                        lp.setTitle("Nachricht" + (multi ? " " + nicephone : ""));
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (textphone != null)
                    {
                        String key = keyprefix + ".text." + voipphone;
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setKey(key);
                        lp.setTitle("SMS" + (multi ? " " + nicephone : ""));
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (voipphone != null)
                    {
                        String key = keyprefix + ".voip." + voipphone;
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setKey(key);
                        lp.setTitle("Anruf" + (multi ? " " + nicephone : ""));
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (vicaphone != null)
                    {
                        String key = keyprefix + ".vica." + vicaphone;
                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setEntries(destText);
                        lp.setEntryValues(destVals);
                        lp.setDefaultValue("inact");
                        lp.setKey(key);
                        lp.setTitle("Videoanruf" + (multi ? " " + nicephone : ""));
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
