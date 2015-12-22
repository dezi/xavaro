package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FirewallActivity extends PreferenceActivity
{
    private static final String LOGTAG = FirewallActivity.class.getSimpleName();

    private static SharedPreferences sharedPrefs;

    //region Static configuration methods.

    private static JSONObject globalConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadGlobalConfig(this);
    }

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

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        Header header;

        header = new Header();
        header.title = "Datenschutz und Sicherheit";
        header.summary = "Einstellungen für Datenschutz und Sicherheit";
        header.fragment = SafetyFragment.class.getName();

        target.add(header);

        header = new Header();
        header.title = "Ausnahmeregeln";
        header.summary = "Ändern sie hier die Zugriffsrechte für einzelne Anbieter";
        header.fragment = DomainsFragment.class.getName();

        target.add(header);
    }

    private static class DomainsCBPreference extends CheckBoxPreference
            implements Preference.OnPreferenceChangeListener
    {
        private View view;
        private TextView summary;

        public DomainsCBPreference(Context context)
        {
            super(context);

            setPersistent(true);
            setDefaultValue(false);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            this.view = view;

            //
            // Stupid limitation on size of description.
            //

            summary = (TextView) view.findViewById(android.R.id.summary);
            summary.setMaxLines(50);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            boolean value = (boolean) newValue;

            return true;
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

    public static class DomainsFragment extends PreferenceFragment
    {
        private static final ArrayList<Preference> preferences = new ArrayList<>();

        public static void registerAll(Context context)
        {
            if (sharedPrefs == null) sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

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

                    cb.setKey("firewall_domain_" + name);
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
    }

    private static class SafetyCBPreference extends CheckBoxPreference
        implements Preference.OnPreferenceChangeListener
    {
        private TextView summary;

        public SafetyCBPreference(Context context)
        {
            super(context);

            setPersistent(true);

            setOnPreferenceChangeListener(this);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            //
            // Stupid limitation on size of description.
            //

            summary = (TextView) view.findViewById(android.R.id.summary);
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

        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            boolean value = (boolean) newValue;

            return true;
        }
    }

    public static class SafetyFragment extends PreferenceFragment
    {
        private final static ArrayList<Preference> preferences = new ArrayList<>();

        public static void registerAll(Context context)
        {
            if (sharedPrefs == null) sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            preferences.clear();

            SafetyCBPreference cb;

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_respect_privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_respect_privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_stay_onsite");
            cb.setTitle("Beim Surfen das Verlassen der Web-Seite unterbinden");

            cb.setSummaryOn("Der Anwender ist vor unbeabsichtigtem Verlassen der Web-Seite geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unqualifizierte Verlinkungen gefährdet wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_block_popups");
            cb.setTitle("Popups blockieren");

            cb.setSummaryOn("Der Anwender ist vor Popups geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch plötzlich auftauchende Popups belästigt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_block_premium");
            cb.setTitle("Kostenpflichtige Premium-Angebote blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Premium-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender keine Abonnements oder kostenpflichtige Premium-Angebote "
                    + "bestellen kannn.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_block_shops");
            cb.setTitle("Zugang zu Website-Shops blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Shop-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht versehentlich Shop-Angebote wahrnimmt.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_block_inlinead");
            cb.setTitle("Werbe-Verknüpfungen im Lesetext blockieren");

            cb.setSummaryOn("Der Anwender ist vor Werbelinks im redaktionellen Text geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch irreführenden Werbelinks im redaktionellen. "
                    + "Text einer Webseite auf Anzeigen geführt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall_block_ads");
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
    }
}
