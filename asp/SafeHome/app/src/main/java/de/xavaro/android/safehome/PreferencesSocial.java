package de.xavaro.android.safehome;

import android.preference.Preference;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Social;

public class PreferencesSocial extends PreferenceFragments.EnableFragmentStub
{
    private static final String LOGTAG = PreferencesSocial.class.getSimpleName();

    protected Social.SocialInterface social;

    protected int accountSummary;
    protected int friendsSummary;
    protected int likesSummary;

    protected ArrayList<String> knownpfids = new ArrayList<>();

    protected NicedPreferences.NiceCategoryPreference categoryPref;
    protected NicedPreferences.NiceDisplayTextPreference userPref;
    protected NicedPreferences.NiceDisplayTextPreference expirationPref;
    protected NicedPreferences.NiceDisplayTextPreference loginPref;
    protected NicedPreferences.NiceDisplayTextPreference apicallsPref;

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

        categoryPref = new NicedPreferences.NiceInfoPreference(context);
        categoryPref.setTitle(R.string.pref_social_account);
        categoryPref.setSummary(accountSummary);
        categoryPref.setEnabled(enabled);

        preferences.add(categoryPref);

        userPref = new NicedPreferences.NiceDisplayTextPreference(context);
        userPref.setTitle(R.string.pref_social_user);
        userPref.setEnabled(enabled);

        preferences.add(userPref);

        expirationPref = new NicedPreferences.NiceDisplayTextPreference(context);
        expirationPref.setTitle(R.string.pref_social_validuntil);
        expirationPref.setEnabled(enabled);

        preferences.add(expirationPref);

        loginPref = new NicedPreferences.NiceDisplayTextPreference(context);

        loginPref.setTitle(social.isLoggedIn()
                ? R.string.pref_social_logout
                : R.string.pref_social_login);

        loginPref.setText(Simple.getTrans(social.isLoggedIn()
                ? R.string.pref_social_isloggedin
                : R.string.pref_social_isloggedout));

        loginPref.setEnabled(enabled);

        loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                if (social.isLoggedIn())
                {
                    social.logout();
                }
                else
                {
                    social.login();
                }

                return false;
            }
        });

        preferences.add(loginPref);

        if (GlobalConfigs.BetaFlag)
        {
            apicallsPref = new NicedPreferences.NiceDisplayTextPreference(context);
            apicallsPref.setTitle("API Calls");
            apicallsPref.setEnabled(enabled);

            preferences.add(apicallsPref);
        }

        dp = new NicedPreferences.NiceDisplayTextPreference(context);
        dp.setTitle(social.isLoggedIn() ? "Etwas testen" : "Nix machen");
        dp.setEnabled(enabled);

        dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                //Log.d(LOGTAG, "Permissions=" + social.getUserPermissions());

                social.getTest();

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

        social.reconfigureFriendsAndLikes();

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
            ip.setTitle(R.string.pref_social_friends);
            ip.setSummary(friendsSummary);
            ip.setEnabled(enabled);
            ip.setOrder(10000);

            preferences.add(ip);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".newfriends.default");
            lp.setTitle(R.string.pref_social_autofriends);
            lp.setEntryValues(R.array.pref_social_newfriends_keys);
            lp.setEntries(R.array.pref_social_newfriends_vals);
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
            lp.setIcon(social.getProfileDrawable(fpfid, true));
            lp.setEntryValues(R.array.pref_social_newfriends_keys);
            lp.setEntries(R.array.pref_social_newfriends_vals);
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
            ip.setTitle(R.string.pref_social_likes);
            ip.setSummary(likesSummary);
            ip.setEnabled(enabled);
            ip.setOrder(20000);

            preferences.add(ip);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".newlikes.default");
            lp.setTitle(R.string.pref_social_autolikes);
            lp.setEntryValues(R.array.pref_social_newlikes_keys);
            lp.setEntries(R.array.pref_social_newlikes_vals);
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
            lp.setIcon(social.getProfileDrawable(fpfid, true));
            lp.setEntryValues(R.array.pref_social_newlikes_keys);
            lp.setEntries(R.array.pref_social_newlikes_vals);
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

            String pfid = social.getUserId();
            String name = social.getUserDisplayName();
            String expi = social.getAccessExpiration();

            if (pfid != null) Simple.setSharedPrefString(keyprefix + ".pfid", pfid);
            if (name != null) Simple.setSharedPrefString(keyprefix + ".name", name);
            if (expi != null) Simple.setSharedPrefString(keyprefix + ".expi", expi);

            if (pfid == null) pfid = Simple.getSharedPrefString(keyprefix + ".pfid");
            if (name == null) name = Simple.getSharedPrefString(keyprefix + ".name");
            if (expi == null) expi = Simple.getSharedPrefString(keyprefix + ".expi");

            if (! Simple.equals(userPref.getText(), name))
            {
                String expiration = (expi == null) ? null : Simple.getLocalDateLong(expi);

                if ((expi != null) && (Simple.getTimeStamp(expi) == 0))
                {
                    expiration = Simple.getTrans(R.string.pref_social_noexpiration);
                }

                userPref.setText(name);
                categoryPref.setIcon(social.getProfileDrawable(pfid, true));

                expirationPref.setText(expiration);
            }

            loginPref.setTitle(social.isLoggedIn()
                    ? R.string.pref_social_logout
                    : R.string.pref_social_login);

            loginPref.setText(Simple.getTrans(social.isLoggedIn()
                    ? R.string.pref_social_isloggedin
                    : R.string.pref_social_isloggedout));

            if (apicallsPref != null)
            {
                int hourcount = social.getHourStatistic();
                int todaycount = social.getTodayStatistic();

                apicallsPref.setText(hourcount + "/" + todaycount);
            }

            if ((monitorSequence++ % 10) == 0)
            {
                social.reconfigureFriendsAndLikes();
                registerFriends(Simple.getActContext(), false);
                registerLikes(Simple.getActContext(), false);
            }

            Simple.makePost(monitorPrefs, 1000);
        }
    };
}
