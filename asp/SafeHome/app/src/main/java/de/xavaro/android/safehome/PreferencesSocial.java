package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Facebook;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WifiLookup;

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
            header.titleRes = R.string.pref_social_facebook;
            header.iconRes = CommonConfigs.IconResSocialFacebook;
            header.fragment = FacebookFragment.class.getName();

            return header;
        }

        public FacebookFragment()
        {
            super();

            iconres = CommonConfigs.IconResSocialFacebook;
            keyprefix = "social.facebook";
            masterenable = Simple.getTrans(R.string.pref_social_facebook_enable);
            summaryres = R.string.pref_social_facebook_summary;
        }

        ArrayList<String> knownfbids = new ArrayList<>();
        NicedPreferences.NiceCategoryPreference facebookHead;
        NicedPreferences.NiceDisplayTextPreference facebookUser;
        NicedPreferences.NiceDisplayTextPreference facebookExpi;

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            Log.d(LOGTAG, "onDestroy");

            Simple.removePost(monitorPrefs);
        }

        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceDisplayTextPreference dp;

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
            facebookHead.setTitle(R.string.pref_social_facebook_account);
            facebookHead.setSummary(R.string.pref_social_facebook_account_summary);
            facebookHead.setIcon(ProfileImages.getFacebookProfileDrawable(fbid, true));
            facebookHead.setEnabled(enabled);

            preferences.add(facebookHead);

            facebookUser = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookUser.setTitle(R.string.pref_social_facebook_user);
            facebookUser.setText(name);
            facebookUser.setEnabled(enabled);

            preferences.add(facebookUser);

            facebookExpi = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookExpi.setTitle(R.string.pref_social_facebook_validuntil);
            facebookExpi.setText((expi == null) ? null : Simple.getLocalDateLong(expi));
            facebookExpi.setEnabled(enabled);

            preferences.add(facebookExpi);

            dp = new NicedPreferences.NiceDisplayTextPreference(context);

            dp.setTitle(Facebook.isLoggedIn()
                    ? R.string.pref_social_facebook_logout
                    : R.string.pref_social_facebook_login);

            dp.setText(Simple.getTrans(Facebook.isLoggedIn()
                    ?  R.string.pref_social_facebook_isloggedin
                    :  R.string.pref_social_facebook_isloggedout));

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

                    Facebook.getUserLikeslist();

                    return false;
                }
            });

            preferences.add(dp);

            //
            // Define preference order.
            //

            int order = 1;

            for (Preference pref : preferences)
            {
                pref.setOrder(order++);
            }

            //
            // Preset all current friends and likes preferences.
            //

            Facebook.reconfigureFriendsAndLikes();

            registerFriends(context, true);
            registerLikes(context, true);

            Simple.makePost(monitorPrefs, 5 * 1000);
        }

        public void registerFriends(Context context, boolean initial)
        {
            NicedPreferences.NiceInfoPreference ip;
            NicedPreferences.NiceListPreference lp;

            if (initial)
            {
                ip = new NicedPreferences.NiceInfoPreference(context);
                ip.setTitle(R.string.pref_social_facebook_friends);
                ip.setSummary(R.string.pref_social_facebook_friends_summary);
                ip.setEnabled(enabled);
                ip.setOrder(10000);

                preferences.add(ip);

                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey("facebook.newfriends.default");
                lp.setTitle(R.string.pref_social_facebook_autofriends);
                lp.setEntryValues(R.array.pref_social_facebook_newfriends_keys);
                lp.setEntries(R.array.pref_social_facebook_newfriends_vals);
                lp.setDefaultValue("feed");
                lp.setEnabled(enabled);
                lp.setOrder(10001);

                preferences.add(lp);
            }

            String friendsname = "facebook.friend.name.";
            String friendsmode = "facebook.friend.mode.";

            Map<String, Object> friendspref = Simple.getAllPreferences(friendsname);

            for (Map.Entry<String, ?> entry : friendspref.entrySet())
            {
                Object fnobj = entry.getValue();

                if (! (fnobj instanceof String)) continue;

                String ffbid = entry.getKey().substring(friendsname.length());
                String fname = (String) fnobj;

                if (knownfbids.contains(ffbid)) continue;
                knownfbids.add(ffbid);

                lp = new NicedPreferences.NiceListPreference(context);
                lp.setKey(friendsmode + ffbid);
                lp.setTitle(fname);
                lp.setIcon(ProfileImages.getFacebookProfileDrawable(ffbid, true));
                lp.setEntryValues(R.array.pref_social_facebook_newfriends_keys);
                lp.setEntries(R.array.pref_social_facebook_newfriends_vals);
                lp.setEnabled(enabled);

                //
                // Updated items get first.
                //

                lp.setOrder(19999 - preferences.size());

                preferences.add(lp);

                if (! initial) getPreferenceScreen().addPreference(lp);
            }
        }

        public void registerLikes(Context context, boolean initial)
        {
            NicedPreferences.NiceInfoPreference ip;
            NicedPreferences.NiceListPreference lp;

            if (initial)
            {
                ip = new NicedPreferences.NiceInfoPreference(context);
                ip.setTitle(R.string.pref_social_facebook_likes);
                ip.setSummary(R.string.pref_social_facebook_likes_summary);
                ip.setEnabled(enabled);
                ip.setOrder(20000);

                preferences.add(ip);

                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey("facebook.newlikes.default");
                lp.setTitle(R.string.pref_social_facebook_autolikes);
                lp.setEntryValues(R.array.pref_social_facebook_newlikes_keys);
                lp.setEntries(R.array.pref_social_facebook_newlikes_vals);
                lp.setDefaultValue("folder");
                lp.setEnabled(enabled);
                lp.setOrder(20001);

                preferences.add(lp);
            }

            String likesname = "facebook.like.name.";
            String likesmode = "facebook.like.mode.";

            Map<String, Object> likespref = Simple.getAllPreferences(likesname);

            for (Map.Entry<String, ?> entry : likespref.entrySet())
            {
                Object fnobj = entry.getValue();

                if (! (fnobj instanceof String)) continue;

                String ffbid = entry.getKey().substring(likesname.length());
                String fname = (String) fnobj;

                if (knownfbids.contains(ffbid)) continue;
                knownfbids.add(ffbid);

                lp = new NicedPreferences.NiceListPreference(context);
                lp.setKey(likesmode + ffbid);
                lp.setTitle(fname);
                lp.setIcon(ProfileImages.getFacebookProfileDrawable(ffbid, true));
                lp.setEntryValues(R.array.pref_social_facebook_newlikes_keys);
                lp.setEntries(R.array.pref_social_facebook_newlikes_vals);
                lp.setEnabled(enabled);

                //
                // Updated items get first.
                //

                lp.setOrder(29999 - preferences.size());

                preferences.add(lp);

                if (! initial) getPreferenceScreen().addPreference(lp);
            }
        }

        private final Runnable monitorPrefs = new Runnable()
        {
            @Override
            public void run()
            {
                Facebook.reconfigureFriendsAndLikes();

                registerFriends(Simple.getActContext(), false);
                registerLikes(Simple.getActContext(), false);

                Simple.makePost(monitorPrefs, 5 * 1000);
            }
        };
    }

    //endregion Facebook preferences
}
