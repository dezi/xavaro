package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.TextView;
import android.view.View;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class PrefFragments
{
    private static final String LOGTAG = PrefFragments.class.getSimpleName();

    private static SharedPreferences sharedPrefs;

    public static void initialize(Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region Firewall safety preferences

    public static class SafetyFragment extends PreferenceFragment
    {
        private final static ArrayList<Preference> preferences = new ArrayList<>();

        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Datenschutz und Sicherheit";
            header.fragment = PrefFragments.SafetyFragment.class.getName();

            return header;
        }

        public static void registerAll(Context context)
        {
            preferences.clear();

            SafetyCBPreference cb;

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.respect.privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.respect.privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.stay.onsite");
            cb.setTitle("Beim Surfen das Verlassen der Web-Seite unterbinden");

            cb.setSummaryOn("Der Anwender ist vor unbeabsichtigtem Verlassen der Web-Seite geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unqualifizierte Verlinkungen gefährdet wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.popups");
            cb.setTitle("Popups blockieren");

            cb.setSummaryOn("Der Anwender ist vor Popups geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch plötzlich auftauchende Popups belästigt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.premium");
            cb.setTitle("Kostenpflichtige Premium-Angebote blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Premium-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender keine Abonnements oder kostenpflichtige Premium-Angebote "
                    + "bestellen kannn.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.shops");
            cb.setTitle("Zugang zu Website-Shops blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Shop-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht versehentlich Shop-Angebote wahrnimmt.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.inlinead");
            cb.setTitle("Werbe-Verknüpfungen im Lesetext blockieren");

            cb.setSummaryOn("Der Anwender ist vor Werbelinks im redaktionellen Text geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch irreführenden Werbelinks im redaktionellen. "
                    + "Text einer Webseite auf Anzeigen geführt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.ads");
            cb.setTitle("Anzeigenblöcke ausblenden");

            cb.setSummaryOn("Anzeigenblöcke werden nach Möglichkeit ausgeblendet.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unerwünschte Werbung behindert, belästig oder "
                    + "das Datenvolumen unnötig belastet wird.");

            preferences.add(cb);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region SafetyCBPreference implementation.

        private static class SafetyCBPreference extends CheckBoxPreference
        {
            public SafetyCBPreference(Context context)
            {
                super(context);
            }

            @Override
            protected void onBindView(View view)
            {
                super.onBindView(view);

                //
                // Stupid limitation on size of description.
                //

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                summary.setMaxLines(50);
            }

            @Override
            public void setKey(String text)
            {
                super.setKey(text);

                if ((sharedPrefs != null) && ! sharedPrefs.contains(text))
                {
                    //
                    // Initially commit shared preference.
                    //

                    sharedPrefs.edit().putBoolean(text,false).apply();
                }
            }

            public void setSummaryOff(String text)
            {
                text = "\n" + text + "\n\n" + "Zurzeit ist der Anwender gefährdet.";

                super.setSummaryOff(text);
            }
        }

        //endregion SafetyCBPreference implementation.
    }

    //endregion Firewall safety preferences

    //region Firewall domains preferences

    public static class DomainsFragment extends PreferenceFragment
    {
        private static final ArrayList<Preference> preferences = new ArrayList<>();

        private static JSONObject globalConfig;

        private static void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, R.raw.default_firewall);

                    if (ctemp != null)
                    {
                        globalConfig = ctemp.getJSONObject("firewall");
                    }
                }
                catch (NullPointerException | JSONException ex)
                {
                    Log.e(LOGTAG, "loadGlobalConfig: Cannot read default firewall config.");
                }
            }
        }

        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Domänen Freischaltung";
            header.fragment = PrefFragments.DomainsFragment.class.getName();

            return header;
        }

        public static void registerAll(Context context)
        {
            preferences.clear();

            loadGlobalConfig(context);
            if (globalConfig == null) return;

            try
            {
                JSONArray domains = globalConfig.getJSONArray("domains");

                DomainsCBPreference cb;

                for (int inx = 0; inx < domains.length(); inx++)
                {
                    JSONObject domain = domains.getJSONObject(inx);

                    // @formatter:off
                    String name   = domain.has("domain") ? domain.getString("domain") : null;
                    String type   = domain.has("type")   ? domain.getString("type")   : null;
                    String source = domain.has("source") ? domain.getString("source") : null;
                    // @formatter:on

                    JSONArray desc = domain.has("description") ? domain.getJSONArray("description") : null;
                    JSONArray faults = domain.has("faults") ? domain.getJSONArray("faults") : null;

                    if ((name == null) || (desc == null) || (faults == null)) continue;

                    String title = name;
                    if (type != null) title += " (" + type + ")";

                    String text = "";

                    for (int cnt = 0; cnt < desc.length(); cnt++) text += desc.getString(cnt);
                    if (source != null) text += " (Quelle: " + source + ")";

                    cb = new DomainsCBPreference(context);

                    cb.setKey("firewall.domains." + name);
                    cb.setTitle(title);
                    cb.setSummary(text, name, faults);

                    preferences.add(cb);
                }

            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region DomainsCBPreference implementation

        private static class DomainsCBPreference extends CheckBoxPreference
        {
            public DomainsCBPreference(Context context)
            {
                super(context);
            }

            @Override
            protected void onBindView(View view)
            {
                super.onBindView(view);

                //
                // Stupid limitation on size of description.
                //

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                summary.setMaxLines(50);
            }

            @Override
            public void setKey(String text)
            {
                super.setKey(text);

                if ((sharedPrefs != null) && ! sharedPrefs.contains(text))
                {
                    //
                    // Initially commit shared preference.
                    //

                    sharedPrefs.edit().putBoolean(text,false).apply();
                }
            }

            public void setSummary(String text, String domain, JSONArray faults)
            {
                try
                {
                    text = "\n" + text + "\n";

                    for (int inx = 0; inx < faults.length(); inx++)
                    {
                        String fault = faults.getString(inx);

                        if (fault.equals("nohome"   )) text += "\n– Problem: Homepage www." + domain + " nicht abrufbar";
                        if (fault.equals("nopriv"   )) text += "\n– Problem: Fehlende Datenschutzerklärung";
                        if (fault.equals("noprivger")) text += "\n– Problem: Datenschutzerklärung nur in Englisch";
                        if (fault.equals("nooptin"  )) text += "\n– Problem: Nur nachträglicher Opt-Out";
                        if (fault.equals("nooptout" )) text += "\n– Problem: Keine Opt-Out Möglichkeit angeboten";
                        if (fault.equals("nofaults" )) text += "\n– Problem: Eigentlich keines.";

                        if (fault.equals("isporn"   )) text += "\n– Problem: Pornografie";
                        if (fault.equals("ispremium")) text += "\n– Problem: Premium Dienste";
                    }
                }
                catch (JSONException ignore)
                {
                }

                setSummaryOn(text + "\n\nZugriff ist freigegeben");
                setSummaryOff(text + "\n\nZugriff zurzeit gesperrt");
            }
        }

        //endregion DomainsCBPreference implementation
    }

    //endregion Firewall domains preferences

    //region IP Television preferences

    public static class IPTelevisionFragment extends IPStreamFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Internet Fernsehen";
            header.fragment = IPTelevisionFragment.class.getName();

            return header;
        }

        public IPTelevisionFragment()
        {
            super();

            root = "webiptv";
            jsonres = R.raw.default_webiptv;
            iconres = GlobalConfigs.IconResIPTelevision;
            keyprefix = "iptelevision";
            masterenable = "Internet Fernsehen freischalten";
        }
    }

    //endregion IP Television preferences

    //region IP Radio preferences

    public static class IPRadioFragment extends IPStreamFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Internet Radio";
            header.fragment = IPRadioFragment.class.getName();

            return header;
        }

        public IPRadioFragment()
        {
            super();

            root = "webradio";
            jsonres = R.raw.default_webradio;
            iconres = GlobalConfigs.IconResIPRadio;
            keyprefix = "ipradio";
            masterenable = "Internet Radio freischalten";
        }
    }

    //endregion IP Television preferences

    //region IP Stream abstract class

    public static class IPStreamFragment extends PreferenceFragment
    {
        private final ArrayList<Preference> preferences = new ArrayList<>();

        private JSONObject globalConfig;

        protected String root;
        protected int jsonres;
        protected int iconres;
        protected String keyprefix;
        protected String masterenable;

        private void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, jsonres);

                    if (ctemp != null)
                    {
                        globalConfig = ctemp.getJSONObject(root);
                    }
                }
                catch (NullPointerException | JSONException ex)
                {
                    Log.e(LOGTAG, "loadGlobalConfig: Cannot read default " + root + " config.");
                }
            }
        }

        public void registerAll(Context context)
        {
            preferences.clear();

            try
            {
                IPStreamSwitchPreference sw = new IPStreamSwitchPreference(context);

                sw.setKey(keyprefix + ".enable");
                sw.setTitle(masterenable);
                sw.setIcon(VersionUtils.getDrawableFromResources(context, iconres));
                sw.setDefaultValue(false);

                preferences.add(sw);

                boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable",false);

                loadGlobalConfig(context);

                CheckBoxPreference cb;
                Iterator<String> keysIterator = globalConfig.keys();

                while (keysIterator.hasNext())
                {
                    String website = keysIterator.next();

                    JSONObject sender = globalConfig.getJSONObject(website);

                    JSONArray channels = sender.getJSONArray("channels");

                    for (int inx = 0; inx < channels.length(); inx++)
                    {
                        JSONObject channel = channels.getJSONObject(inx);

                        cb = new CheckBoxPreference(context);

                        String label = channel.getString("label");
                        String key = keyprefix + ".channel." + website + ":" + label.replace(" ", "_");
                        String iconurl = channel.getString("icon");
                        Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                        Drawable drawable = new BitmapDrawable(context.getResources(),thumbnail);

                        cb.setKey(key);
                        cb.setTitle(label);
                        cb.setIcon(drawable);
                        cb.setEnabled(enabled);

                        preferences.add(cb);

                        boolean def = channel.has("default") && channel.getBoolean("default");

                        if (def && ! sharedPrefs.contains(key))
                        {
                            //
                            // Initially commit shared preference.
                            //

                            sharedPrefs.edit().putBoolean(key,true).apply();
                        }
                    }
                }

            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region IPStreanSwitchPreference implementation

        private class IPStreamSwitchPreference extends SwitchPreference
                implements Preference.OnPreferenceChangeListener
        {
            public IPStreamSwitchPreference(Context context)
            {
                super(context);

                setOnPreferenceChangeListener(this);
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG,"onPreferenceChange:" + obj.toString());

                for (Preference pref : preferences)
                {
                    if (pref instanceof CheckBoxPreference)
                    {
                        pref.setEnabled((boolean) obj);
                    }
                }

                return true;
            }
        }

        //endregion IPStreanSwitchPreference implementation
    }

    //endregion Stream abstract class
}
