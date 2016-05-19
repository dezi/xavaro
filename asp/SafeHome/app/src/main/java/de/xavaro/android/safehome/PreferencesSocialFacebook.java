package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Social;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;

public class PreferencesSocialFacebook extends PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocialFacebook.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_social_facebook;
        header.iconRes = CommonConfigs.IconResSocialFacebook;
        header.fragment = PreferencesSocialFacebook.class.getName();

        return header;
    }

    public PreferencesSocialFacebook()
    {
        super();

        keyprefix = "social.facebook";
        iconres = CommonConfigs.IconResSocialFacebook;
        masterenable = Simple.getTrans(R.string.pref_social_facebook_enable);
        summaryres = R.string.pref_social_facebook_summary;
        social = SocialFacebook.getInstance();

        accountSummary = R.string.pref_social_facebook_account_summary;
        friendsSummary = R.string.pref_social_facebook_friends_summary;
        likesSummary = R.string.pref_social_facebook_likes_summary;

        // legacy remove.

        Simple.removeAllPreferences("facebook.");
    }
}