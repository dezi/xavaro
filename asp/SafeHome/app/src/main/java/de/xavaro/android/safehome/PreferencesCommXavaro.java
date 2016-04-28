package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.SystemIdentity;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;

public class PreferencesCommXavaro extends PreferenceFragments.EnableFragmentStub
{
    private static final String LOGTAG = PreferencesCommXavaro.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.title = "Xavaro";
        header.iconRes = GlobalConfigs.IconResXavaro;
        header.fragment = PreferencesCommXavaro.class.getName();

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

    public PreferencesCommXavaro()
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

        registerRemotes(true);
    }

    private final ArrayList<String> remoteContacts = new ArrayList<>();

    private void registerRemotes(boolean initial)
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

                pc = new NicedPreferences.NiceCategoryPreference(Simple.getActContext());
                pc.setIcon(ProfileImages.getProfileDrawable(ident, true));
                pc.setTitle(name);
                pc.setEnabled(enabled);

                preferences.add(pc);
                if (!initial) getPreferenceScreen().addPreference(pc);

                cb = new NicedPreferences.NiceListPreference(Simple.getActContext());
                cb.setKey(prefkey + ".chat." + ident);
                cb.setEntries(sendtoText);
                cb.setEntryValues(sendtoVals);
                cb.setDefaultValue("inact");
                cb.setTitle("Nachricht");
                cb.setEnabled(enabled);

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

                //
                // Get our group member record.
                //

                JSONObject member = RemoteGroups.getGroupMember(ident, SystemIdentity.getIdentity());
                if ((member == null) || ! Json.equals(member, "groupstatus", "invited"))
                {
                    //
                    // Todo: Implement a remove message and delete group.
                    //

                    continue;
                }

                boolean isprepaidadmin = Json.getBoolean(member, "prepaidadmin");
                boolean isskypecallback = Json.getBoolean(member, "skypeenable");

                String summary = isprepaidadmin ? "Prepaid-Abfragen" : "";

                if (isskypecallback)
                {
                    if (! summary.isEmpty()) summary += " " + "sowie" + " ";

                    summary += "Skype-Rückruf";
                }

                String prefkey = keyprefix + ".remote.groups";

                String name = RemoteGroups.getDisplayName(ident);

                pc = new NicedPreferences.NiceCategoryPreference(Simple.getActContext());
                pc.setIcon(ProfileImages.getProfileDrawable(groupowner, true));
                pc.setTitle(name);
                pc.setSummary(summary);

                preferences.add(pc);
                if (!initial) getPreferenceScreen().addPreference(pc);

                //
                // Chat icon location.
                //

                cb = new NicedPreferences.NiceListPreference(Simple.getActContext());
                cb.setKey(prefkey + ".chat." + ident);
                cb.setEntries(sendtoText);
                cb.setEntryValues(sendtoVals);
                cb.setDefaultValue("inact");
                cb.setTitle("Nachricht");

                preferences.add(cb);
                activekeys.add(cb.getKey());
                if (!initial) getPreferenceScreen().addPreference(cb);

                //
                // Prepaid admin icon location.
                //

                if (isprepaidadmin)
                {
                    cb = new NicedPreferences.NiceListPreference(Simple.getActContext());
                    cb.setKey(prefkey + ".padm." + ident);
                    cb.setEntries(sendtoText);
                    cb.setEntryValues(sendtoVals);
                    cb.setDefaultValue("inact");
                    cb.setTitle("Prepaid-Abfrage");

                    preferences.add(cb);
                    activekeys.add(cb.getKey());
                    if (!initial) getPreferenceScreen().addPreference(cb);
                }
            }
        }

        //
        // Remove disabled or obsoleted preferences.
        //

        Map<String, ?> exists = Simple.getAllPreferences(keyprefix + ".remote.");

        for (Map.Entry<String, ?> entry : exists.entrySet())
        {
            if (activekeys.contains(entry.getKey())) continue;

            Simple.removeSharedPref(entry.getKey());
        }
    }
}
