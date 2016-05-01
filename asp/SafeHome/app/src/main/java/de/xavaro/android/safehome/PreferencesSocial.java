package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Facebook;
import de.xavaro.android.common.Json;
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
        NicedPreferences.NiceDisplayTextPreference facebookExpi;

        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceDisplayTextPreference dp;
            NicedPreferences.NiceDeletePreference xp;
            NicedPreferences.NiceCheckboxPreference cp;

            //
            // Get login status. Store current credentials
            // into or get last loggedin credentials from
            // preferences.
            //

            String fbid = Facebook.getUserId();
            String name = Facebook.getUserDisplayName();
            String expi = Facebook.getUserTokenExpiration();

            if (fbid != null) Simple.setSharedPrefString("facebook.fbid", fbid);
            if (name != null) Simple.setSharedPrefString("facebook.name", name);
            if (expi != null) Simple.setSharedPrefString("facebook.expi", expi);

            if (fbid == null) fbid = Simple.getSharedPrefString("facebook.fbid");
            if (name == null) name = Simple.getSharedPrefString("facebook.name");
            if (expi == null) expi = Simple.getSharedPrefString("facebook.expi");

            facebookHead = new NicedPreferences.NiceInfoPreference(context);
            facebookHead.setTitle("Facebook Account");
            facebookHead.setSummary(R.string.pref_basic_owner_profile_summary);
            facebookHead.setIcon(ProfileImages.getFacebookProfileDrawable(fbid, true));
            facebookHead.setEnabled(enabled);

            preferences.add(facebookHead);

            facebookUser = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookUser.setTitle("Facebook Benutzer");
            facebookUser.setText(name);
            facebookUser.setEnabled(enabled);

            preferences.add(facebookUser);

            facebookExpi = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookExpi.setTitle("Gültig bis");
            facebookExpi.setText((expi == null) ? null : Simple.getLocalDateLong(expi));
            facebookExpi.setEnabled(enabled);

            preferences.add(facebookExpi);

            dp = new NicedPreferences.NiceDisplayTextPreference(context);
            dp.setTitle(Facebook.isLoggedIn() ? "Ausloggen" : "Einloggen");
            dp.setText(Facebook.isLoggedIn() ? "ist eingeloggt" : "ist ausgelogt");
            dp.setEnabled(enabled);

            dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if (Facebook.isLoggedIn())
                    {
                        Facebook.logout();
                    }
                    else
                    {
                        Facebook.login();
                    }

                    return false;
                }
            });

            preferences.add(dp);

            dp = new NicedPreferences.NiceDisplayTextPreference(context);
            dp.setTitle(Facebook.isLoggedIn() ? "Etwas testen" : "Nix machen");
            dp.setEnabled(enabled);

            dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Log.d(LOGTAG, "Permissions=" + Facebook.getUserPermissions());
                    Facebook.getUserFriendlist();
                    return false;
                }
            });

            preferences.add(dp);

            //
            // Display already known friends list.
            //

            ArrayList<String> oldfriends = new ArrayList<>();

            String friendsname = "facebook.friend.name.";
            String friendsenabled = "facebook.friend.enabled.";

            Map<String, Object> oldfriendspref = Simple.getAllPreferences(friendsname);

            for (Map.Entry<String, ?> entry : oldfriendspref.entrySet())
            {
                Object fnobj = entry.getValue();

                if (! (fnobj instanceof String)) continue;

                String ffbid = entry.getKey().substring(friendsname.length());
                String fname = (String) fnobj;

                xp = new NicedPreferences.NiceDeletePreference(context);

                xp.setTitle(fname);
                xp.setIcon(ProfileImages.getFacebookProfileDrawable(ffbid, true));
                xp.setEnabled(enabled);

                preferences.add(xp);

                cp = new NicedPreferences.NiceCheckboxPreference(context);
                cp.setKey(friendsenabled + ffbid);
                cp.setTitle("Aktiviert");
                cp.setEnabled(enabled);

                preferences.add(cp);
                oldfriends.add(ffbid);
            }

            //
            // Display new potential friends list.
            //

            if (Facebook.isLoggedIn())
            {
                JSONArray allfriends = Facebook.getUserFriendlist();

                if (allfriends != null)
                {
                    for (int inx = 0; inx < allfriends.length(); inx++)
                    {
                        JSONObject allfriend = Json.getObject(allfriends, inx);
                        if (allfriend == null) continue;

                        String ffbid = Json.getString(allfriend, "id");
                        String fname = Json.getString(allfriend, "name");

                        if (oldfriends.contains(ffbid)) continue;

                        Simple.setSharedPrefString(friendsname + ffbid, fname);

                        xp = new NicedPreferences.NiceDeletePreference(context);

                        xp.setTitle(fname);
                        xp.setIcon(ProfileImages.getFacebookProfileDrawable(ffbid, true));
                        xp.setEnabled(enabled);

                        preferences.add(xp);

                        cp = new NicedPreferences.NiceCheckboxPreference(context);
                        cp.setKey(friendsenabled + ffbid);
                        cp.setTitle("Einbeziehen");
                        cp.setDefaultValue(true);
                        cp.setEnabled(enabled);

                        preferences.add(cp);
                    }
                }
            }
        }
    }

    //endregion Facebook preferences
}
