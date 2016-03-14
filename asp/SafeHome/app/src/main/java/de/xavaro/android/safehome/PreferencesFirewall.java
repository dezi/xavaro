package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import org.json.JSONArray;
import org.json.JSONObject;


import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.WebLib;

public class PreferencesFirewall
{
    private static final String LOGTAG = PreferencesFirewall.class.getSimpleName();

    //region Firewall safety preferences

    public static class SafetyFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Anwenderschutz";
            header.iconRes = GlobalConfigs.IconResFireWallSafety;
            header.fragment = SafetyFragment.class.getName();

            return header;
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

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

            cb.setKey("firewall.safety.stay.onsite");
            cb.setTitle("Beim Surfen das Verlassen der Web-Seite unterbinden");

            cb.setSummaryOn("Der Anwender ist vor unbeabsichtigtem Verlassen "
                    + "der Web-Seite geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch ungewollte Verlinkungen gefährdet wird.");

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

        private static class SafetyCBPreference extends NicedPreferences.NiceCheckboxPreference
        {
            public SafetyCBPreference(Context context)
            {
                super(context);
            }

            public void setSummaryOff(String text)
            {
                text = "\n" + text + "\n\n" + "Zurzeit ist der Anwender gefährdet.";

                super.setSummaryOff(text);
            }
        }
    }

    //endregion Firewall safety preferences

    //region Firewall domains preferences

    public static class DomainsFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Domänen Freischaltung";
            header.iconRes = GlobalConfigs.IconResFireWallDomains;
            header.fragment = DomainsFragment.class.getName();

            return header;
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            JSONObject config = WebLib.getLocaleConfig("block");
            if (config == null) return;

            JSONArray domains = Json.getArray(config, "domains");
            if (domains == null) return;

            DomainsCBPreference cb;

            for (int inx = 0; inx < domains.length(); inx++)
            {
                JSONObject domain = Json.getObject(domains, inx);
                if (domain == null) continue;

                String name = Json.getString(domain, "domain");
                String type = Json.getString(domain, "type");
                String source = Json.getString(domain, "source");

                JSONArray desc = Json.getArray(domain, "description");
                JSONArray faults = Json.getArray(domain, "faults");

                if ((name == null) || (desc == null) || (faults == null)) continue;

                String title = name;
                if (type != null) title += " (" + type + ")";

                String text = "";

                for (int cnt = 0; cnt < desc.length(); cnt++) text += Json.getString(desc, cnt);
                if (source != null) text += " (Quelle: " + source + ")";

                cb = new DomainsCBPreference(context);

                cb.setKey("firewall.domains." + name);
                cb.setTitle(title);
                cb.setSummary(text, name, faults);

                preferences.add(cb);
            }
        }

        private static class DomainsCBPreference extends NicedPreferences.NiceCheckboxPreference
        {
            public DomainsCBPreference(Context context)
            {
                super(context);
            }

            public void setSummary(String text, String domain, JSONArray faults)
            {
                text += "\n";

                for (int inx = 0; inx < faults.length(); inx++)
                {
                    String fault = Json.getString(faults, inx);
                    if (fault == null) continue;

                    if (fault.equals("nohome"))
                        text += "\n– Problem: Homepage www." + domain + " nicht abrufbar";

                    if (fault.equals("nopriv"))
                        text += "\n– Problem: Fehlende Datenschutzerklärung";

                    if (fault.equals("noprivger"))
                        text += "\n– Problem: Datenschutzerklärung nur in Englisch";

                    if (fault.equals("nooptin"))
                        text += "\n– Problem: Nur nachträglicher Opt-Out";

                    if (fault.equals("nooptout"))
                        text += "\n– Problem: Keine Opt-Out Möglichkeit angeboten";

                    if (fault.equals("nofaults"))
                        text += "\n– Problem: Eigentlich keines";

                    if (fault.equals("isporn"))
                        text += "\n– Problem: Pornografie";

                    if (fault.equals("ispremium"))
                        text += "\n– Problem: Premium Dienste";
                }

                setSummaryOn(text + "\n\nZugriff ist freigegeben");
                setSummaryOff(text + "\n\nZugriff zurzeit gesperrt");
            }
        }
    }

    //endregion Firewall domains preferences
}
