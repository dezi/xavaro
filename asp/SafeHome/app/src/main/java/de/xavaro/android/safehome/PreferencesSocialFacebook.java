package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;

public class PreferencesSocialFacebook extends PreferenceFragments.EnableFragmentStub
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

    private final SocialFacebook facebook;

    public PreferencesSocialFacebook()
    {
        super();

        keyprefix = "social.facebook";
        iconres = CommonConfigs.IconResSocialFacebook;
        masterenable = Simple.getTrans(R.string.pref_social_facebook_enable);
        summaryres = R.string.pref_social_facebook_summary;

        facebook = SocialFacebook.getInstance();
    }

    ArrayList<String> knownpfids = new ArrayList<>();
    NicedPreferences.NiceCategoryPreference facebookHead;
    NicedPreferences.NiceDisplayTextPreference facebookUser;
    NicedPreferences.NiceDisplayTextPreference facebookExpi;
    NicedPreferences.NiceDisplayTextPreference facebookLogi;
    NicedPreferences.NiceDisplayTextPreference facebookCalls;

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

        // legacy remove.

        Simple.removeAllPreferences("facebook.");

        facebookHead = new NicedPreferences.NiceInfoPreference(context);
        facebookHead.setTitle(R.string.pref_social_facebook_account);
        facebookHead.setSummary(R.string.pref_social_facebook_account_summary);
        facebookHead.setEnabled(enabled);

        preferences.add(facebookHead);

        facebookUser = new NicedPreferences.NiceDisplayTextPreference(context);
        facebookUser.setTitle(R.string.pref_social_facebook_user);
        facebookUser.setEnabled(enabled);

        preferences.add(facebookUser);

        facebookExpi = new NicedPreferences.NiceDisplayTextPreference(context);
        facebookExpi.setTitle(R.string.pref_social_facebook_validuntil);
        facebookExpi.setEnabled(enabled);

        preferences.add(facebookExpi);

        facebookLogi = new NicedPreferences.NiceDisplayTextPreference(context);

        facebookLogi.setTitle(facebook.isLoggedIn()
                ? R.string.pref_social_facebook_logout
                : R.string.pref_social_facebook_login);

        facebookLogi.setText(Simple.getTrans(facebook.isLoggedIn()
                ? R.string.pref_social_facebook_isloggedin
                : R.string.pref_social_facebook_isloggedout));

        facebookLogi.setEnabled(enabled);

        facebookLogi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                if (facebook.isLoggedIn())
                {
                    facebook.logout();
                }
                else
                {
                    facebook.login();
                }

                return false;
            }
        });

        preferences.add(facebookLogi);

        if (GlobalConfigs.BetaFlag)
        {
            facebookCalls = new NicedPreferences.NiceDisplayTextPreference(context);
            facebookCalls.setTitle("API Calls");
            facebookCalls.setEnabled(enabled);

            preferences.add(facebookCalls);
        }

        dp = new NicedPreferences.NiceDisplayTextPreference(context);
        dp.setTitle(facebook.isLoggedIn() ? "Etwas testen" : "Nix machen");
        dp.setEnabled(enabled);

        dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Log.d(LOGTAG, "Permissions=" + facebook.getUserPermissions());

                facebook.getUserLikeslist();

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

        facebook.reconfigureFriendsAndLikes();

        registerFriends(context, true);
        registerLikes(context, true);

        monitorPrefs.run();
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

            lp.setKey(keyprefix + ".newfriends.default");
            lp.setTitle(R.string.pref_social_facebook_autofriends);
            lp.setEntryValues(R.array.pref_social_facebook_newfriends_keys);
            lp.setEntries(R.array.pref_social_facebook_newfriends_vals);
            lp.setDefaultValue("feed+folder");
            lp.setEnabled(enabled);
            lp.setOrder(10001);

            preferences.add(lp);
        }

        String friendsname = keyprefix + ".friend.name.";
        String friendsmode = keyprefix + ".friend.mode.";

        Map<String, Object> friendspref = Simple.getAllPreferences(friendsname);

        for (Map.Entry<String, ?> entry : friendspref.entrySet())
        {
            Object fnobj = entry.getValue();

            if (!(fnobj instanceof String)) continue;

            String fpfid = entry.getKey().substring(friendsname.length());
            String fname = (String) fnobj;

            if (knownpfids.contains(fpfid)) continue;
            knownpfids.add(fpfid);

            lp = new NicedPreferences.NiceListPreference(context);
            lp.setKey(friendsmode + fpfid);
            lp.setTitle(fname);
            lp.setIcon(ProfileImages.getFacebookProfileDrawable(fpfid, true));
            lp.setEntryValues(R.array.pref_social_facebook_newfriends_keys);
            lp.setEntries(R.array.pref_social_facebook_newfriends_vals);
            lp.setEnabled(enabled);

            //
            // Updated items get first.
            //

            lp.setOrder(19999 - preferences.size());

            preferences.add(lp);

            if (!initial) getPreferenceScreen().addPreference(lp);
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

            lp.setKey(keyprefix + ".newlikes.default");
            lp.setTitle(R.string.pref_social_facebook_autolikes);
            lp.setEntryValues(R.array.pref_social_facebook_newlikes_keys);
            lp.setEntries(R.array.pref_social_facebook_newlikes_vals);
            lp.setDefaultValue("folder");
            lp.setEnabled(enabled);
            lp.setOrder(20001);

            preferences.add(lp);
        }

        String likesname = keyprefix + ".like.name.";
        String likesmode = keyprefix + ".like.mode.";

        Map<String, Object> likespref = Simple.getAllPreferences(likesname);

        for (Map.Entry<String, ?> entry : likespref.entrySet())
        {
            Object fnobj = entry.getValue();

            if (!(fnobj instanceof String)) continue;

            String fpfid = entry.getKey().substring(likesname.length());
            String fname = (String) fnobj;

            if (knownpfids.contains(fpfid)) continue;
            knownpfids.add(fpfid);

            lp = new NicedPreferences.NiceListPreference(context);
            lp.setKey(likesmode + fpfid);
            lp.setTitle(fname);
            lp.setIcon(ProfileImages.getFacebookProfileDrawable(fpfid, true));
            lp.setEntryValues(R.array.pref_social_facebook_newlikes_keys);
            lp.setEntries(R.array.pref_social_facebook_newlikes_vals);
            lp.setEnabled(enabled);

            //
            // Updated items get first.
            //

            lp.setOrder(29999 - preferences.size());

            preferences.add(lp);

            if (!initial) getPreferenceScreen().addPreference(lp);
        }
    }

    private int monitorSequence;

    private final Runnable monitorPrefs = new Runnable()
    {
        @Override
        public void run()
        {
            //
            // Get login status. Store current credentials
            // into or get last loggedin credentials from
            // preferences.
            //

            String pfid = facebook.getUserId();
            String name = facebook.getUserDisplayName();
            String expi = facebook.getUserTokenExpiration();

            if (pfid != null) Simple.setSharedPrefString(keyprefix + ".pfid", pfid);
            if (name != null) Simple.setSharedPrefString(keyprefix + ".name", name);
            if (expi != null) Simple.setSharedPrefString(keyprefix + ".expi", expi);

            if (pfid == null) pfid = Simple.getSharedPrefString(keyprefix + ".pfid");
            if (name == null) name = Simple.getSharedPrefString(keyprefix + ".name");
            if (expi == null) expi = Simple.getSharedPrefString(keyprefix + ".expi");

            if (!Simple.equals(facebookUser.getText(), name))
            {
                facebookUser.setText(name);
                facebookHead.setIcon(ProfileImages.getFacebookProfileDrawable(pfid, true));
                facebookExpi.setText((expi == null) ? null : Simple.getLocalDateLong(expi));
            }

            facebookLogi.setTitle(facebook.isLoggedIn()
                    ? R.string.pref_social_facebook_logout
                    : R.string.pref_social_facebook_login);

            facebookLogi.setText(Simple.getTrans(facebook.isLoggedIn()
                    ? R.string.pref_social_facebook_isloggedin
                    : R.string.pref_social_facebook_isloggedout));

            if (facebookCalls != null)
            {
                int hourcount = facebook.getHourStatistic();
                int todaycount = facebook.getTodayStatistic();

                facebookCalls.setText(hourcount + "/" + todaycount);
            }

            if ((monitorSequence++ % 10) == 0)
            {
                facebook.reconfigureFriendsAndLikes();
                registerFriends(Simple.getActContext(), false);
                registerLikes(Simple.getActContext(), false);
            }

            Simple.makePost(monitorPrefs, 1000);
        }
    };
}