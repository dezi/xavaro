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

        PrefFragments.initialize(this);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        target.add(PrefFragments.SafetyFragment.getHeader());
        target.add(PrefFragments.DomainsFragment.getHeader());

        target.add(PrefFragments.IPTelevisionFragment.getHeader());
        target.add(PrefFragments.IPRadioFragment.getHeader());

        target.add(PrefFragments.WebConfigNewspaperFragment.getHeader());
        target.add(PrefFragments.WebConfigMagazineFragment.getHeader());
        target.add(PrefFragments.WebConfigShoppingFragment.getHeader());
        target.add(PrefFragments.WebConfigEroticsFragment.getHeader());
    }
}
