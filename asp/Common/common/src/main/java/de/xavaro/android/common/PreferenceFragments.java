package de.xavaro.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

public class PreferenceFragments
{
    //region BasicFragmentStub extends PreferenceFragment

    public static class BasicFragmentStub extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();

        protected SharedPreferences sharedPrefs;
        protected PreferenceScreen root;
        protected Context context;

        public void registerAll(Context context)
        {
            this.context = context;

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }
    }

    //endregion BasicFragmentStub extends PreferenceFragment
}
