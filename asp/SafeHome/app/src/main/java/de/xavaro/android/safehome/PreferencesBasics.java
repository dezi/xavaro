package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.CryptUtils;
import de.xavaro.android.common.IdentityManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.ProfileImagesNew;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.WifiLookup;

public class PreferencesBasics
{
    private static final String LOGTAG = PreferencesBasics.class.getSimpleName();

    //region Alertgroup preferences

    public static class AlertgroupFragment extends PreferenceFragments.EnableFragmentStub
            implements Preference.OnPreferenceChangeListener
    {
    }

    //endregion Alertgroup preferences

    //region Important calls preferences

    public static class ImportantCallsFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Rufnummern";
            header.iconRes = GlobalConfigs.IconResCallImportant;
            header.fragment = ImportantCallsFragment.class.getName();

            return header;
        }

        public ImportantCallsFragment()
        {
            super();

            type = "calls";
            subtype = "important";
            iscalls = true;
            iconres = GlobalConfigs.IconResCallImportant;
            keyprefix = type + "." + subtype;
            masterenable = "Wichtige Rufnummern freischalten";
            residKeys = R.array.pref_where_keys;
            residVals = R.array.pref_where_vals;
        }
    }

    //endregion Important calls preferences
}
