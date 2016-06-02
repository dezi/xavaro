package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialTinder;
import de.xavaro.android.common.SocialTwitter;

public class PreferencesSocialTinder extends PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocialTinder.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_social_tinder;
        header.iconRes = CommonConfigs.IconResSocialTinder;
        header.fragment = PreferencesSocialTinder.class.getName();

        return header;
    }

    public PreferencesSocialTinder()
    {
        super();

        keyprefix = "social.tinder";
        iconres = CommonConfigs.IconResSocialTinder;
        masterenable = Simple.getTrans(R.string.pref_social_tinder_enable);
        summaryres = R.string.pref_social_tinder_summary;

        social = SocialTinder.getInstance();
    }
}