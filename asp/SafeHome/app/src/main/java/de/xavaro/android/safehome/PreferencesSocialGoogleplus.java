package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;

public class PreferencesSocialGoogleplus extends PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocialGoogleplus.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_social_googleplus;
        header.iconRes = CommonConfigs.IconResSocialGoogleplus;
        header.fragment = PreferencesSocialGoogleplus.class.getName();

        return header;
    }

    public PreferencesSocialGoogleplus()
    {
        super();

        keyprefix = "social.googleplus";
        iconres = CommonConfigs.IconResSocialGoogleplus;
        masterenable = Simple.getTrans(R.string.pref_social_googleplus_enable);
        summaryres = R.string.pref_social_googleplus_summary;

        accountSummary = R.string.pref_social_googleplus_account_summary;
        friendsSummary = R.string.pref_social_googleplus_friends_summary;
        likesSummary = R.string.pref_social_googleplus_likes_summary;

        social = SocialGoogleplus.getInstance();
    }
}