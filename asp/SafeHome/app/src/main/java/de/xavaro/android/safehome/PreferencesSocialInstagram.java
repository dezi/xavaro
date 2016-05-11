package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.SocialInstagram;

public class PreferencesSocialInstagram extends PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocialInstagram.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_social_instagram;
        header.iconRes = CommonConfigs.IconResSocialInstagram;
        header.fragment = PreferencesSocialInstagram.class.getName();

        return header;
    }

    public PreferencesSocialInstagram()
    {
        super();

        keyprefix = "social.instagram";
        iconres = CommonConfigs.IconResSocialInstagram;
        masterenable = Simple.getTrans(R.string.pref_social_instagram_enable);
        summaryres = R.string.pref_social_instagram_summary;

        accountSummary = R.string.pref_social_instagram_account_summary;
        friendsSummary = R.string.pref_social_instagram_friends_summary;
        likesSummary = R.string.pref_social_instagram_likes_summary;

        social = SocialInstagram.getInstance();

        // legacy remove.

        Simple.removeAllPreferences("instagram.");
    }
}