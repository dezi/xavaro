package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialTwitter;

public class PreferencesSocialTwitter extends PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocialTwitter.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_social_twitter;
        header.iconRes = CommonConfigs.IconResSocialTwitter;
        header.fragment = PreferencesSocialTwitter.class.getName();

        return header;
    }

    public PreferencesSocialTwitter()
    {
        super();

        keyprefix = "social.twitter";
        iconres = CommonConfigs.IconResSocialTwitter;
        masterenable = Simple.getTrans(R.string.pref_social_twitter_enable);
        summaryres = R.string.pref_social_twitter_summary;

        accountSummary = R.string.pref_social_twitter_account_summary;
        friendsSummary = R.string.pref_social_twitter_friends_summary;
        likesSummary = R.string.pref_social_twitter_likes_summary;
        
        social = SocialTwitter.getInstance();
    }
}