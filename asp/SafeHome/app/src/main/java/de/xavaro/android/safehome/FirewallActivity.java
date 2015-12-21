package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;
import android.os.Bundle;

import java.util.List;

public class FirewallActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
        Bundle b;

        header = new Header();
        header.title = "Datenschutz und Sicherheit";
        header.summary = "Einstellungen für Datenschutz und Sicherheit";
        header.fragment = DefaultFragment.class.getName();

        target.add(header);

        header = new Header();
        header.title = "Ausnahmeregeln";
        header.summary = "Ändern sie hier die Zugriffsrechte für einzelne Anbieter";
        header.fragment = DefaultFragment.class.getName();

        target.add(header);
    }

    private static class CBPreference extends CheckBoxPreference
    {
        public CBPreference(Context context)
        {
            super(context);
        }

        public void setSummaryOff(String text)
        {
            text = "\n" + text + "\n\n" + "Zurzeit ist der Anwender gefährdet.";

            super.setSummaryOff(text);
        }
    }

    public static class DefaultFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            CBPreference cb;

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_respect_privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_stay_onsite");
            cb.setTitle("Beim Surfen das Verlassen der Web-Seite unterbinden");

            cb.setSummaryOn("Der Anwender ist vor unbeabsichtigtem Verlassen der Web-Seite geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unqualifizierte Verlinkungen gefährdet wird.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_block_popups");
            cb.setTitle("Popups blockieren");

            cb.setSummaryOn("Der Anwender ist vor Popups geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                   + "Anwender nicht durch plötzlich auftauchende Popups belästigt wird.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_block_premium");
            cb.setTitle("Kostenpflichtige Premium-Angebote blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Premium-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender keine Abonnements oder kostenpflichtige Premium-Angebote "
                    + "bestellen kannn.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_block_shops");
            cb.setTitle("Zugang zu Website-Shops blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Shop-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht versehentlich Shop-Angebote wahrnimmt.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_block_inlinead");
            cb.setTitle("Werbe-Verknüpfungen im Lesetext blockieren");

            cb.setSummaryOn("Der Anwender ist vor Werbelinks im redaktionellen Text geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch irreführenden Werbelinks im redaktionellen. "
                    + "Text einer Webseite auf Anzeigen geführt wird.");

            root.addPreference(cb);

            cb = new CBPreference(getActivity());

            cb.setKey("firewall_block_ads");
            cb.setTitle("Anzeigenblöcke ausblenden");

            cb.setSummaryOn("Anzeigenblöcke werden nach Möglichkeit ausgeblendet.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unerwünschte Werbung behindert, belästig oder "
                    + "das Datenvolumen unnötig belastet wird.");

            root.addPreference(cb);
        }
    }
}
