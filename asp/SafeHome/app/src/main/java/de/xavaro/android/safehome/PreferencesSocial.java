package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Facebook;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;

public class PreferencesSocial
{
    private static final String LOGTAG = PreferencesSocial.class.getSimpleName();

    //region Facebook preferences

    public static class FacebookFragment extends PreferenceFragments.EnableFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Facebook";
            header.iconRes = CommonConfigs.IconResSocialFacebook;
            header.fragment = FacebookFragment.class.getName();

            return header;
        }

        public FacebookFragment()
        {
            super();

            iconres = CommonConfigs.IconResSocialFacebook;
            keyprefix = "social.facebook";
            masterenable = "Facebook freischalten";
        }

        NicedPreferences.NiceCategoryPreference facebookHead;
        NicedPreferences.NiceDisplayTextPreference facebookUser;
        NicedPreferences.NiceDisplayTextPreference facebookToken;

        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceDisplayTextPreference dp;

            String userid = Facebook.getProfileId();

            facebookHead = new NicedPreferences.NiceInfoPreference(context);
            facebookHead.setTitle("Facebook Account");
            facebookHead.setSummary(R.string.pref_basic_owner_profile_summary);
            facebookHead.setIcon(ProfileImages.getFacebookProfileDrawable(userid, true));

            preferences.add(facebookHead);

            String name = Facebook.getProfileDisplayName();

            facebookUser = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookUser.setTitle("Facebook Benutzer");
            facebookUser.setText(name);

            preferences.add(facebookUser);

            String expi = Facebook.getProfileExpiration();

            facebookToken = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookToken.setTitle("Eingeloggt bis");
            facebookToken.setText((expi == null) ? null : Simple.getLocalDateLong(expi));

            preferences.add(facebookToken);

            dp = new NicedPreferences.NiceDisplayTextPreference(context);
            dp.setTitle(Facebook.isLoggedIn()? "Account wechseln" : "Account festlegen");

            dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Facebook.login();

                    return false;
                }
            });

            preferences.add(dp);
        }
    }

    //endregion Facebook preferences
}
