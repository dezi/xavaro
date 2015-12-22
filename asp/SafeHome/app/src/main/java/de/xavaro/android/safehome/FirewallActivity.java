package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import java.util.List;

public class FirewallActivity extends PreferenceActivity
{
    private static final String LOGTAG = FirewallActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PreferenceFragments.initialize(this);
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
        header.fragment = PreferenceFragments.SafetyFragment.class.getName();

        target.add(header);

        header = new Header();
        header.title = "Domänen Freischaltung";
        header.fragment = PreferenceFragments.DomainsFragment.class.getName();

        target.add(header);
    }
}
