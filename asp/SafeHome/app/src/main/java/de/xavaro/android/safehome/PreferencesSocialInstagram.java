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

public class PreferencesSocialInstagram extends PreferenceFragments.EnableFragmentStub
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

    private final SocialInstagram instagram;

    public PreferencesSocialInstagram()
    {
        super();

        keyprefix = "social.instagram";
        iconres = CommonConfigs.IconResSocialInstagram;
        masterenable = Simple.getTrans(R.string.pref_social_instagram_enable);
        summaryres = R.string.pref_social_instagram_summary;

        instagram = SocialInstagram.getInstance();
    }

    ArrayList<String> knownpfids = new ArrayList<>();
    NicedPreferences.NiceCategoryPreference instagramHead;
    NicedPreferences.NiceDisplayTextPreference instagramUser;
    NicedPreferences.NiceDisplayTextPreference instagramExpi;
    NicedPreferences.NiceDisplayTextPreference instagramLogi;
    NicedPreferences.NiceDisplayTextPreference instagramCalls;

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

        Simple.removeAllPreferences("instagram.");

        instagramHead = new NicedPreferences.NiceInfoPreference(context);
        instagramHead.setTitle(R.string.pref_social_instagram_account);
        instagramHead.setSummary(R.string.pref_social_instagram_account_summary);
        instagramHead.setEnabled(enabled);

        preferences.add(instagramHead);

        instagramUser = new NicedPreferences.NiceDisplayTextPreference(context);
        instagramUser.setTitle(R.string.pref_social_instagram_user);
        instagramUser.setEnabled(enabled);

        preferences.add(instagramUser);

        instagramExpi = new NicedPreferences.NiceDisplayTextPreference(context);
        instagramExpi.setTitle(R.string.pref_social_instagram_validuntil);
        instagramExpi.setEnabled(enabled);

        preferences.add(instagramExpi);

        instagramLogi = new NicedPreferences.NiceDisplayTextPreference(context);

        instagramLogi.setTitle(instagram.isLoggedIn()
                ? R.string.pref_social_instagram_logout
                : R.string.pref_social_instagram_login);

        instagramLogi.setText(Simple.getTrans(instagram.isLoggedIn()
                ? R.string.pref_social_instagram_isloggedin
                : R.string.pref_social_instagram_isloggedout));

        instagramLogi.setEnabled(enabled);

        instagramLogi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                if (instagram.isLoggedIn())
                {
                    instagram.logout();
                }
                else
                {
                    instagram.login();
                }

                return false;
            }
        });

        preferences.add(instagramLogi);

        if (GlobalConfigs.BetaFlag)
        {
            instagramCalls = new NicedPreferences.NiceDisplayTextPreference(context);
            instagramCalls.setTitle("API Calls");
            instagramCalls.setEnabled(enabled);

            preferences.add(instagramCalls);
        }


        dp = new NicedPreferences.NiceDisplayTextPreference(context);
        dp.setTitle(instagram.isLoggedIn() ? "Etwas testen" : "Nix machen");
        dp.setEnabled(enabled);

        dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                JSONArray data = instagram.getUserFriendlist();

                if (data != null) Log.d(LOGTAG, "====================" + data.toString());

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
        // Preset all current friends preferences.
        //

        instagram.reconfigureFriendsAndLikes();

        registerFriends(context, true);

        monitorPrefs.run();
    }

    public void registerFriends(Context context, boolean initial)
    {
        NicedPreferences.NiceInfoPreference ip;
        NicedPreferences.NiceListPreference lp;

        if (initial)
        {
            ip = new NicedPreferences.NiceInfoPreference(context);
            ip.setTitle(R.string.pref_social_instagram_friends);
            ip.setSummary(R.string.pref_social_instagram_friends_summary);
            ip.setEnabled(enabled);
            ip.setOrder(10000);

            preferences.add(ip);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".newfriends.default");
            lp.setTitle(R.string.pref_social_instagram_autofriends);
            lp.setEntryValues(R.array.pref_social_instagram_newfriends_keys);
            lp.setEntries(R.array.pref_social_instagram_newfriends_vals);
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
            lp.setIcon(ProfileImages.getInstagramProfileDrawable(fpfid, true));
            lp.setEntryValues(R.array.pref_social_instagram_newfriends_keys);
            lp.setEntries(R.array.pref_social_instagram_newfriends_vals);
            lp.setEnabled(enabled);

            //
            // Updated items get first.
            //

            lp.setOrder(19999 - preferences.size());

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

            String pfid = instagram.getUserId();
            String name = instagram.getUserDisplayName();
            String expi = instagram.getUserTokenExpiration();

            if (pfid != null) Simple.setSharedPrefString(keyprefix + ".pfid", pfid);
            if (name != null) Simple.setSharedPrefString(keyprefix + ".name", name);
            if (expi != null) Simple.setSharedPrefString(keyprefix + ".expi", expi);

            if (pfid == null) pfid = Simple.getSharedPrefString(keyprefix + ".pfid");
            if (name == null) name = Simple.getSharedPrefString(keyprefix + ".name");
            if (expi == null) expi = Simple.getSharedPrefString(keyprefix + ".expi");

            if (!Simple.equals(instagramUser.getText(), name))
            {
                instagramUser.setText(name);
                instagramHead.setIcon(ProfileImages.getInstagramProfileDrawable(pfid, true));
                instagramExpi.setText((expi == null) ? null : Simple.getLocalDateLong(expi));
            }

            instagramLogi.setTitle(instagram.isLoggedIn()
                    ? R.string.pref_social_instagram_logout
                    : R.string.pref_social_instagram_login);

            instagramLogi.setText(Simple.getTrans(instagram.isLoggedIn()
                    ? R.string.pref_social_instagram_isloggedin
                    : R.string.pref_social_instagram_isloggedout));

            if (instagramCalls != null)
            {
                int hourcount = instagram.getHourStatistic();
                int todaycount = instagram.getTodayStatistic();

                instagramCalls.setText(hourcount + "/" + todaycount);
            }

            if ((monitorSequence++ % 10) == 0)
            {
                instagram.reconfigureFriendsAndLikes();
                registerFriends(Simple.getActContext(), false);
            }

            Simple.makePost(monitorPrefs, 1000);
        }
    };
}