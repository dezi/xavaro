package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class SettingsFragments
{
    private static final String LOGTAG = SettingsFragments.class.getSimpleName();

    private static final CharSequence[] recentText = { "Android-System", "SafeHome" };
    private static final CharSequence[] recentVals = { "android", "safehome" };

    private static SharedPreferences sharedPrefs;

    public static void initialize(Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region Administrator preferences

    public static class AdminFragment extends PreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Administrator";
            header.iconRes = GlobalConfigs.IconResAdministrator;
            header.fragment = AdminFragment.class.getName();

            return header;
        }

        private final static ArrayList<Preference> preferences = new ArrayList<>();

        private static Context runctx;

        public static void registerAll(Context context)
        {
            runctx = context;

            preferences.clear();

            NiceEditTextPreference et;

            et = new NiceEditTextPreference(context);

            et.setKey("admin.password");
            et.setTitle("Administrator Passwort (zum Anzeigen clicken)");
            et.setIsPassword();

            if (! sharedPrefs.getString(et.getKey(),"").equals(""))
            {
                ArchievementManager.archieved("configure.settings.password");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.password");
            }

            preferences.add(et);

            et = new NiceEditTextPreference(context);

            et.setKey("admin.home.button");
            et.setTitle("Anwendung auf dem Home-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultHomeLabel(context));

            if (sharedPrefs.getString(et.getKey(),"").equals(DitUndDat.DefaultApps.getAppLable(context)))
            {
                ArchievementManager.archieved("configure.settings.homebutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.homebutton");
            }

            et.setOnclick(new Runnable()
            {
                @Override
                public void run()
                {
                    DitUndDat.DefaultApps.setDefaultHome(runctx);
                }
            });

            preferences.add(et);

            et = new NiceEditTextPreference(context);

            et.setKey("admin.assist.button");
            et.setTitle("Anwendung auf dem Assistenz-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultAssistLabel(context));

            if (sharedPrefs.getString(et.getKey(),"").equals(DitUndDat.DefaultApps.getAppLable(context)))
            {
                ArchievementManager.archieved("configure.settings.assistbutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.assistbutton");
            }

            et.setOnclick(new Runnable()
            {
                @Override
                public void run()
                {
                    DitUndDat.DefaultApps.setDefaultAssist(runctx);
                }
            });

            preferences.add(et);

            NiceListPreference cb = new NiceListPreference(context);

            cb.setEntries(recentText);
            cb.setEntryValues(recentVals);
            cb.setDefaultValue("safehome");
            cb.setKey("admin.recent.button");
            cb.setTitle("Anwendung auf dem Menü-Button");

            if (!sharedPrefs.contains(cb.getKey()))
            {
                sharedPrefs.edit().putString(cb.getKey(), "safehome").apply();
            }

            preferences.add(cb);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }
    }

    //endregion Administrator preferences

    //region Health Person preferences

    public static class HealthPersonalFragment extends EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Persönliches";
            header.iconRes = GlobalConfigs.IconResHealthPerson;
            header.fragment = HealthPersonalFragment.class.getName();

            return header;
        }

        public HealthPersonalFragment()
        {
            super();

            iconres = GlobalConfigs.IconResHealthPerson;
            keyprefix = "health";
            masterenable = "Vitaldaten freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            NicePreferenceCategory pc;
            NiceListPreference lp;
            NiceDatePreference dp;
            NiceNumberPreference np;

            //
            // Personal user data required for calculations.
            //

            pc = new NicePreferenceCategory(context);
            pc.setTitle("Persönliche Daten");
            preferences.add(pc);

            lp = new NiceListPreference(context);

            String[] genderText = { "Nicht gesetzt", "Männlich", "Weiblich"  };
            String[] genderVals = { "unknown", "male", "female" };

            lp.setKey(keyprefix + ".user.gender");
            lp.setEntries(genderText);
            lp.setEntryValues(genderVals);
            lp.setDefaultValue("unknown");
            lp.setTitle("Geschlecht");
            lp.setEnabled(enabled);

            preferences.add(lp);

            dp = new NiceDatePreference(context);

            dp.setKey(keyprefix + ".user.birthdate");
            dp.setTitle("Geburtsdatum");
            dp.setEnabled(enabled);

            preferences.add(dp);

            np = new NiceNumberPreference(context);

            np.setKey(keyprefix + ".user.size");
            np.setUnit("cm");
            np.setMinMaxValue(100, 250, 1);
            np.setDefaultValue(170);
            np.setTitle("Größe");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NiceNumberPreference(context);

            np.setKey(keyprefix + ".user.weight");
            np.setUnit("kg");
            np.setMinMaxValue(40, 250, 1);
            np.setDefaultValue(75);
            np.setTitle("Gewicht");
            np.setEnabled(enabled);

            preferences.add(np);

            lp = new NiceListPreference(context);

            String[] userActivityText = { "Nicht aktiv", "Leicht aktiv", "Moderat aktiv", "Sehr aktiv", "Besonders aktiv"  };
            String[] userActivityVals = { "0", "1", "2", "3", "4" };

            lp.setKey(keyprefix + ".user.activity");
            lp.setEntries(userActivityText);
            lp.setEntryValues(userActivityVals);
            lp.setDefaultValue("2");
            lp.setTitle("Aktivität");
            lp.setEnabled(enabled);

            preferences.add(lp);
        }
    }

    //endregion Health Person preferences

    //region Health Units preferences

    public static class HealthUnitsFragment extends EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Maßeinheiten";
            header.iconRes = GlobalConfigs.IconResHealthUnits;
            header.fragment = HealthUnitsFragment.class.getName();

            return header;
        }

        public HealthUnitsFragment()
        {
            super();

            iconres = GlobalConfigs.IconResHealthUnits;
            keyprefix = "health.units";
        }

        @Override
        public void registerAll(Context context)
        {
            NiceListPreference lp;

            lp = new NiceListPreference(context);

            String[] unitTimeText = { "24 Stunden", "12 Stunden"  };
            String[] unitTimeVals = { "24h", "12h" };

            lp.setKey(keyprefix + ".timedisp");
            lp.setEntries(unitTimeText);
            lp.setEntryValues(unitTimeVals);
            lp.setDefaultValue("24h");
            lp.setTitle("Zeitangabe mit");

            preferences.add(lp);

            lp = new NiceListPreference(context);

            String[] unitSizeText = {"Zentimeter", "Inch"};
            String[] unitSizeVals = {"cm", "inch"};

            lp.setKey(keyprefix + ".size");
            lp.setEntries(unitSizeText);
            lp.setEntryValues(unitSizeVals);
            lp.setDefaultValue("cm");
            lp.setTitle("Größe in");

            preferences.add(lp);

            lp = new NiceListPreference(context);

            String[] unitDistText = { "Meter", "Yards"  };
            String[] unitDistVals = { "m", "yard" };

            lp.setKey(keyprefix + ".distance");
            lp.setEntries(unitDistText);
            lp.setEntryValues(unitDistVals);
            lp.setDefaultValue("m");
            lp.setTitle("Distanz in");

            preferences.add(lp);

            lp = new NiceListPreference(context);

            String[] unitWeightText = { "Kilogramm", "Pfund"  };
            String[] unitWeightVals = { "kg", "lbs" };

            lp.setKey(keyprefix + ".weight");
            lp.setEntries(unitWeightText);
            lp.setEntryValues(unitWeightVals);
            lp.setDefaultValue("kg");
            lp.setTitle("Gewicht in");

            preferences.add(lp);

            lp = new NiceListPreference(context);

            String[] unitPressureText = { "mmHg", "kPa" };
            String[] unitPressureVals = { "mmhg", "kpa" };

            lp.setKey(keyprefix + ".pressure");
            lp.setEntries(unitPressureText);
            lp.setEntryValues(unitPressureVals);
            lp.setDefaultValue("mmhg");
            lp.setTitle("Blutdruck in");

            preferences.add(lp);
        }
    }

    //endregion Health Units preferences

    //region Health BPM preferences

    public static class HealthBPMFragment extends BlueToothFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Blutdruck";
            header.iconRes = GlobalConfigs.IconResHealthBPM;
            header.fragment = HealthBPMFragment.class.getName();

            return header;
        }

        public HealthBPMFragment()
        {
            super();

            isBPM = true;

            iconres = GlobalConfigs.IconResHealthBPM;
            keyprefix = "health.bpm";
            masterenable = "Blutdruck freischalten";
            devicetitle = "Bultdruckmessgerät";
            devicesearch = "Bultdruckmessgeräte werden gesucht...";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            NicePreferenceCategory pc;
            NiceListPreference lp;
            CheckBoxPreference cb;

            //
            // User.
            //

            pc = new NicePreferenceCategory(context);
            pc.setTitle("Benutzerauswahl");
            preferences.add(pc);

            lp = new NiceListPreference(context);

            String[] userSelectText = { "Benutzer 1", "Benutzer 2", "Benutzer 3", "Benutzer 4" };
            String[] userSelectVals = { "1", "2", "3", "4" };

            lp.setKey(keyprefix + ".selecteduser");
            lp.setEntries(userSelectText);
            lp.setEntryValues(userSelectVals);
            lp.setDefaultValue("1");
            lp.setTitle("Gerätebenutzer");
            lp.setEnabled(enabled);

            preferences.add(lp);

            cb = new CheckBoxPreference(context);

            cb.setKey(keyprefix + ".anyuser");
            cb.setTitle("Jeden Gerätebenutzer akzeptieren");
            cb.setEnabled(enabled);

            cb.setSummary("Das Blutdruckmessgerät erlaubt eine Auswahl von Benutzer 1 - 4. "
                    + "Erfahrungsgemäß wird der Benutzer im Blutdruck-Messgerät "
                    + "versehentlich vom Anwender verstellt. Das führt zu Unmut bei "
                    + "der Messung, wenn der Messvorgang nicht aufgezeichnet wird. "
                    + "Mit dieser Option akzeptiert das Vitalsystem jeden Benutzer auf "
                    + "dem Gerät, der misst und ordnet die Messsung automatisch dem "
                    + "hiesigen Anwender zu.");

            preferences.add(cb);

        }
    }

    //endregion Health BPM preferences

    //region Health Scale preferences

    public static class HealthScaleFragment extends BlueToothFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Gewicht";
            header.iconRes = GlobalConfigs.IconResHealthScale;
            header.fragment = HealthScaleFragment.class.getName();

            return header;
        }

        public HealthScaleFragment()
        {
            super();

            isScale = true;

            iconres = GlobalConfigs.IconResHealthScale;
            keyprefix = "health.scale";
            masterenable = "Gewicht freischalten";
            devicetitle = "Waage";
            devicesearch = "Waagen werden gesucht...";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            NicePreferenceCategory pc;
            NiceEditTextPreference ep;
            NiceDisplayTextPreference dt;

            //
            // Personal user data required for calculations.
            //

            pc = new NicePreferenceCategory(context);
            pc.setTitle("Waagenbenutzer");
            preferences.add(pc);

            ep = new NiceEditTextPreference(context);

            ep.setKey(keyprefix + ".initials");
            ep.setTitle("Initialen");
            ep.setDefaultValue("XXX");
            ep.setIsUppercase();
            ep.setEnabled(enabled);

            preferences.add(ep);

            dt = new NiceDisplayTextPreference(context);

            dt.setKey(keyprefix + ".userid");
            dt.setTitle("User-ID");
            dt.setEnabled(enabled);

            if (! sharedPrefs.contains(dt.getKey()))
            {
                //
                // Initially compute user id.
                //

                int userid = new Random().nextInt();
                sharedPrefs.edit().putString(dt.getKey(), "" + userid).apply();
            }

            preferences.add(dt);
        }
    }

    //endregion Health Scale preferences

    //region Health Sensor preferences

    public static class HealthSensorFragment extends BlueToothFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Aktivität";
            header.iconRes = GlobalConfigs.IconResHealthSensor;
            header.fragment = HealthSensorFragment.class.getName();

            return header;
        }

        public HealthSensorFragment()
        {
            super();

            isSensor = true;

            iconres = GlobalConfigs.IconResHealthSensor;
            keyprefix = "health.sensor";
            masterenable = "Aktivität freischalten";
            devicetitle = "Aktivitäts-Sensor";
            devicesearch = "Aktivitäts-Sensoren werden gesucht...";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            NicePreferenceCategory pc;
            NiceListPreference lp;
            NiceNumberPreference np;

            //
            // Goals.
            //

            pc = new NicePreferenceCategory(context);
            pc.setTitle("Tagesziele");
            preferences.add(pc);

            np = new NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.steps");
            DitUndDat.SharedPrefs.sharedPrefs.edit().remove(np.getKey()).apply();
            np.setMinMaxValue(1000, 20000, 1000);
            np.setDefaultValue(3000);
            np.setTitle("Schritte machen");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.calories");
            DitUndDat.SharedPrefs.sharedPrefs.edit().remove(np.getKey()).apply();
            np.setMinMaxValue(1000, 5000, 100);
            np.setDefaultValue(2000);
            np.setTitle("Kalorien verbrauchen");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.sleephours");
            DitUndDat.SharedPrefs.sharedPrefs.edit().remove(np.getKey()).apply();
            np.setMinMaxValue(4, 10, 1);
            np.setDefaultValue(8);
            np.setTitle("Stunden schlafen");
            np.setEnabled(enabled);

            preferences.add(np);

            lp = new NiceListPreference(context);

            String[] unitGoalText = { "Schritte", "Kalorien"  };
            String[] unitGoalVals = { "steps", "calories" };

            lp.setKey(keyprefix + ".goals.sensor");
            lp.setEntries(unitGoalText);
            lp.setEntryValues(unitGoalVals);
            lp.setDefaultValue("steps");
            lp.setTitle("Hauptziel im Sensor");
            lp.setEnabled(enabled);

            preferences.add(lp);
        }
    }

    //endregion Health Sensor preferences

    //region Health Glucose preferences

    public static class HealthGlucoseFragment extends BlueToothFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Blutzucker";
            header.iconRes = GlobalConfigs.IconResHealthGlucose;
            header.fragment = HealthGlucoseFragment.class.getName();

            return header;
        }

        public HealthGlucoseFragment()
        {
            super();

            isSensor = true;

            iconres = GlobalConfigs.IconResHealthGlucose;
            keyprefix = "health.glucose";
            masterenable = "Blutzucker freischalten";
            devicetitle = "Blutzucker-Messgerät";
            devicesearch = "Blutzucker-Messgeräte werden gesucht...";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            NicePreferenceCategory pc;
            NiceListPreference lp;
            NiceDatePreference dp;
            NiceNumberPreference np;
            NiceEditTextPreference ep;
        }
    }

    //endregion Health Glucose preferences

    //region BlueTooth preferences stub

    public static class BlueToothFragment extends EnablePreferenceFragment implements
            DialogInterface.OnClickListener,
            BlueTooth.BlueToothDiscoverCallback
    {
        protected String devicetitle;
        protected String devicesearch;

        protected boolean isBPM;
        protected boolean isScale;
        protected boolean isSensor;

        private final BlueToothFragment self = this;
        private Context context;

        private NiceListPreference devicePref;

        final ArrayList<String> recentText = new ArrayList<>();
        final ArrayList<String> recentVals = new ArrayList<>();

        @Override
        public void registerAll(Context context)
        {
            this.context = context;

            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            //
            // Bluetooth device selection preference
            //

            NicePreferenceCategory pc = new NicePreferenceCategory(context);
            pc.setTitle("BlueTooth Geräteauswahl");
            preferences.add(pc);

            devicePref = new NiceListPreference(context);

            devicePref.setKey(keyprefix + ".device");

            recentText.add("Nicht zugeordnet");
            recentVals.add("unknown");

            if (sharedPrefs.contains(devicePref.getKey()))
            {
                String btDevice = sharedPrefs.getString(devicePref.getKey(), "unknown");
                String[] btDeviceparts = btDevice.split(" => ");

                if ((! btDevice.equals("unknown")) && (btDeviceparts.length == 2))
                {
                    recentText.add(btDeviceparts[ 0 ]);
                    recentVals.add(btDevice);
                }
            }

            devicePref.setEntries(recentText);
            devicePref.setEntryValues(recentVals);
            devicePref.setDefaultValue("unknown");
            devicePref.setTitle(devicetitle);
            devicePref.setEnabled(enabled);

            devicePref.setOnclick(discoverDialog);

            if (!sharedPrefs.contains(devicePref.getKey()))
            {
                sharedPrefs.edit().putString(devicePref.getKey(), "unknown").apply();
            }

            preferences.add(devicePref);
        }

        private final Handler handler = new Handler();
        private AlertDialog dialog;

        public final Runnable cancelDialog = new Runnable()
        {
            @Override
            public void run()
            {
                dialog.cancel();
            }
        };

        public final Runnable discoverDialog = new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(devicetitle);

                builder.setNegativeButton("Abbrechen", self);
                builder.setNeutralButton("Nach Geräten suchen", self);

                dialog = builder.create();

                String btDevice = sharedPrefs.getString(keyprefix + ".device", "unknown");

                RadioGroup rg = new RadioGroup(context);
                rg.setOrientation(RadioGroup.VERTICAL);
                rg.setPadding(40, 10, 0, 0);

                for (int inx = 0; inx < recentText.size(); inx++)
                {
                    RadioButton rb = new RadioButton(context);

                    rb.setId(4711 + inx);
                    rb.setTextSize(18f);
                    rb.setPadding(0, 10, 0, 10);

                    //
                    // Display unknown as text option, selected devices
                    // with name and mac address to get better overwiev
                    // for user.
                    //

                    rb.setText((inx == 0) ? recentText.get(inx) : recentVals.get(inx));

                    rb.setTag(recentVals.get(inx));
                    rb.setChecked(recentVals.get(inx).equals(btDevice));

                    rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
                        {
                            if (checked)
                            {
                                Log.d(LOGTAG, "onCheckedChanged:" + compoundButton.getTag());

                                sharedPrefs.edit().putString(keyprefix + ".device",
                                        (String) compoundButton.getTag()).apply();

                                devicePref.onPreferenceChange(devicePref, compoundButton.getTag());
                            }

                            handler.postDelayed(cancelDialog, 200);
                        }
                    });

                    rg.addView(rb);
                }

                dialog.setView(rg);
                dialog.show();

                //
                // Set neutral button handler to avoid closing
                // of list preference dialog.
                //

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (isBPM) new BlueToothBPM(context).discover(self);
                        if (isScale) new BlueToothScale(context).discover(self);
                        if (isSensor) new BlueToothSensor(context).discover(self);
                    }
                });
            }
        };

        public void onDiscoverStarted()
        {
            Log.d(LOGTAG,"onDiscoverStarted");

            Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    dialog.setTitle(devicesearch);
                }
            };

            handler.post(runner);
        }

        public void onDiscoverFinished()
        {
            Log.d(LOGTAG, "onDiscoverFinished");

            Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    dialog.cancel();
                    discoverDialog.run();
                }
            };

            handler.post(runner);
        }

        public void onDeviceDiscovered(BluetoothDevice device)
        {
            Log.d(LOGTAG, "onDeviceDiscovered: " + device.getName());

            String newEntry = device.getName();
            String newValue = device.getName() + " => " + device.getAddress();

            if (! recentText.contains(newEntry))
            {
                recentText.add(newEntry);
                recentVals.add(newValue);
            }

            devicePref.setEntries(recentText);
            devicePref.setEntryValues(recentVals);
        }

        public void onClick(DialogInterface dialog, int which)
        {
            if (which == DialogInterface.BUTTON_NEGATIVE)
            {
                dialog.cancel();
            }
        }
    }

    //endregion BlueTooth preferences stub

    //region Phone preferences

    public static class PhoneFragment extends ContactsFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Telefon";
            header.iconRes = GlobalConfigs.IconResPhoneApp;
            header.fragment = PhoneFragment.class.getName();

            return header;
        }

        public PhoneFragment()
        {
            super();

            isPhone = true;
            iconres = GlobalConfigs.IconResPhoneApp;
            keyprefix = "phone";
            masterenable = "Telefon freischalten";
        }
    }

    //endregion Telefon preferences

    //region Skype preferences

    public static class SkypeFragment extends ContactsFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Skype";
            header.iconRes = GlobalConfigs.IconResSkype;
            header.fragment = SkypeFragment.class.getName();

            return header;
        }

        public SkypeFragment()
        {
            super();

            isSkype = true;
            iconres = GlobalConfigs.IconResSkype;
            keyprefix = "skype";
            masterenable = "Skype freischalten";
            installtext = "Skype Anwendung auf diesem Tablet";
            installpack = GlobalConfigs.packageSkype;
        }
    }

    //endregion Skype preferences

    //region WhatsApp preferences

    public static class WhatsAppFragment extends ContactsFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "WhatsApp";
            header.iconRes = GlobalConfigs.IconResWhatsApp;
            header.fragment = WhatsAppFragment.class.getName();

            return header;
        }

        public WhatsAppFragment()
        {
            super();

            isWhatsApp = true;
            iconres = GlobalConfigs.IconResWhatsApp;
            keyprefix = "whatsapp";
            masterenable = "WhatsApp freischalten";
            installtext = "WhatsApp Anwendung auf diesem Tablet";
            installpack = GlobalConfigs.packageWhatsApp;
        }
    }

    //endregion WhatsApp preferences

    //region Contacts preferences stub

    @SuppressWarnings("WeakerAccess")
    public static class ContactsFragment extends EnablePreferenceFragment
    {
        protected boolean isPhone;
        protected boolean isSkype;
        protected boolean isWhatsApp;
        protected String installtext;
        protected String installpack;

        protected final CharSequence[] entries = {
                "Nicht aktiviert",
                "Home-Bildschirm",
                "App-Verzeichnis",
                "Kontakte-Verzeichnis"};

        protected final CharSequence[] evalues = {
                "inact",
                "home",
                "appdir",
                "comdir" };

        protected final CharSequence[] installText = {
                "Installieren",
                "Bereit"};

        protected final CharSequence[] installVals = {
                "notinst",
                "ready"};

        @Override
        @SuppressWarnings("ConstantConditions")
        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            if (installpack != null)
            {
                NiceListPreference ip = new NiceListPreference(context);

                boolean installed = StaticUtils.isAppInstalled(context, installpack);

                ip.setEntries(installText);
                ip.setEntryValues(installVals);
                ip.setKey(keyprefix + ".installed");
                ip.setTitle(installtext);
                ip.setEnabled(enabled);

                //
                // This is nice about Java. No clue how it is done!
                //

                final String installName = installpack;
                final Context installContext = context;

                ip.setOnclick(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DitUndDat.DefaultApps.installAppFromPlaystore(installContext, installName);
                    }
                });

                sharedPrefs.edit().putString(ip.getKey(), installed ? "ready" : "notinst").apply();

                preferences.add(ip);
                activekeys.add(ip.getKey());
            }

            try
            {
                JSONObject contacts = ContactsHandler.getJSONData(context);
                Iterator<String> keysIterator = contacts.keys();

                while (keysIterator.hasNext())
                {
                    String cid = keysIterator.next();

                    //
                    // We are in a contact.
                    //

                    String name = null;
                    String chatphone = null;
                    String voipphone = null;
                    String vicaphone = null;

                    JSONArray items = contacts.getJSONArray(cid);

                    for (int inx = 0; inx < items.length(); inx++)
                    {
                        JSONObject item = items.getJSONObject(inx);

                        if (!item.has("KIND")) continue;

                        String kind = item.getString("KIND");

                        if (kind.equals("StructuredName"))
                        {
                            //
                            // Workaround for Skype which puts
                            // nickname as display name and
                            // duplicates it into given name.
                            //

                            String disp = item.getString("DISPLAY_NAME");
                            String gina = item.getString("GIVEN_NAME");

                            if ((name == null) || ! disp.equals(gina)) name = disp;
                        }

                        if (isPhone)
                        {
                            if (kind.equals("Phone"))
                            {
                                String number = item.getString("NUMBER");

                                if (number.startsWith("+")) voipphone = number;
                            }
                        }

                        if (isSkype)
                        {
                            if (kind.equals("@com.skype.android.chat.action"))
                            {
                                chatphone = item.getString("DATA1");
                            }

                            if (kind.equals("@com.skype.android.skypecall.action"))
                            {
                                voipphone = item.getString("DATA1");
                            }

                            if (kind.equals("@com.skype.android.videocall.action"))
                            {
                                vicaphone = item.getString("DATA1");
                            }
                        }

                        if (isWhatsApp)
                        {
                            if (kind.equals("@vnd.com.whatsapp.profile"))
                            {
                                chatphone = item.getString("DATA1");
                            }

                            if (kind.equals("@vnd.com.whatsapp.voip.call"))
                            {
                                voipphone = item.getString("DATA1");
                            }
                        }
                    }

                    if ((chatphone == null) && (voipphone == null) && (vicaphone == null)) continue;

                    //
                    // Check how many different nicknames / phonenumbers.
                    //

                    if (chatphone != null) chatphone = chatphone.replaceAll("@s.whatsapp.net", "");
                    if (voipphone != null) voipphone = voipphone.replaceAll("@s.whatsapp.net", "");
                    if (vicaphone != null) vicaphone = vicaphone.replaceAll("@s.whatsapp.net", "");

                    ArrayList<String> check = new ArrayList<>();

                    if ((chatphone != null) && ! check.contains(chatphone)) check.add(chatphone);
                    if ((voipphone != null) && ! check.contains(voipphone)) check.add(voipphone);
                    if ((vicaphone != null) && ! check.contains(vicaphone)) check.add(vicaphone);

                    boolean alike = (check.size() == 1);

                    //
                    // Build contacts category preference.

                    String cattitle = name;

                    if (alike) cattitle += " (" + check.get(0) + ")";

                    NicePreferenceCategory nc = new NicePreferenceCategory(context);
                    nc.setTitle(cattitle);
                    nc.setEnabled(enabled);
                    preferences.add(nc);

                    if (chatphone != null)
                    {
                        String key = keyprefix + ".chat." + chatphone;
                        NiceListPreference cb = new NiceListPreference(context);

                        cb.setEntries(entries);
                        cb.setEntryValues(evalues);
                        cb.setDefaultValue("inact");
                        cb.setKey(key);
                        cb.setTitle("Nachricht" + (alike ? "" : " " + chatphone));
                        cb.setEnabled(enabled);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        if (!sharedPrefs.contains(key))
                        {
                            sharedPrefs.edit().putString(key, "inact").apply();
                        }
                    }

                    if ((voipphone != null) && isPhone)
                    {
                        String key = keyprefix + ".text." + voipphone;
                        NiceListPreference cb = new NiceListPreference(context);

                        cb.setEntries(entries);
                        cb.setEntryValues(evalues);
                        cb.setDefaultValue("inact");
                        cb.setKey(key);
                        cb.setTitle("SMS" + (alike ? "" : " " + voipphone));
                        cb.setEnabled(enabled);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        if (!sharedPrefs.contains(key))
                        {
                            sharedPrefs.edit().putString(key, "inact").apply();
                        }
                    }

                    if (voipphone != null)
                    {
                        String key = keyprefix + ".voip." + voipphone;
                        NiceListPreference cb = new NiceListPreference(context);

                        cb.setEntries(entries);
                        cb.setEntryValues(evalues);
                        cb.setDefaultValue("inact");
                        cb.setKey(key);
                        cb.setTitle("Anruf" + (alike ? "" : " " + voipphone));
                        cb.setEnabled(enabled);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        if (!sharedPrefs.contains(key))
                        {
                            sharedPrefs.edit().putString(key, "inact").apply();
                        }
                    }

                    if (vicaphone != null)
                    {
                        String key = keyprefix + ".vica." + vicaphone;
                        NiceListPreference cb = new NiceListPreference(context);

                        cb.setEntries(entries);
                        cb.setEntryValues(evalues);
                        cb.setDefaultValue("inact");
                        cb.setKey(key);
                        cb.setTitle("Videoanruf" + (alike ? "" : " " + vicaphone));
                        cb.setEnabled(enabled);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        if (!sharedPrefs.contains(key))
                        {
                            sharedPrefs.edit().putString(key, "inact").apply();
                        }
                    }
                }

                //
                // Remove disabled or obsoleted preferences.
                //

                String websiteprefix = keyprefix + ".";

                Map<String, ?> exists = sharedPrefs.getAll();

                for (Map.Entry<String, ?> entry : exists.entrySet())
                {
                    if (! entry.getKey().startsWith(websiteprefix)) continue;

                    if (activekeys.contains(entry.getKey())) continue;

                    sharedPrefs.edit().remove(entry.getKey()).apply();

                    Log.d(LOGTAG, "registerAll: obsolete:" + entry.getKey() + "=" + entry.getValue());
                }
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    //endregion Contacts preferences stub

    //region EnablePreferenceFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class EnablePreferenceFragment extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();
        protected final ArrayList<String> activekeys = new ArrayList<>();

        protected String keyprefix;
        protected String masterenable;

        protected int iconres;

        public void registerAll(Context context)
        {
            preferences.clear();

            EnableSwitchPreference sw = new EnableSwitchPreference(context);

            sw.setKey(keyprefix + ".enable");
            sw.setTitle(masterenable);
            sw.setIcon(VersionUtils.getDrawableFromResources(context, iconres));
            sw.setDefaultValue(false);

            preferences.add(sw);

            activekeys.add(sw.getKey());
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        protected class EnableSwitchPreference extends SwitchPreference
                implements Preference.OnPreferenceChangeListener
        {
            public EnableSwitchPreference(Context context)
            {
                super(context);

                setOnPreferenceChangeListener(this);
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG, "onPreferenceChange:" + obj.toString());

                for (Preference pref : preferences)
                {
                    if (pref == this) continue;

                    pref.setEnabled((boolean) obj);
                }

                return true;
            }
        }
    }

    //endregion EnablePreferenceFragment stub

    //region Firewall safety preferences

    public static class SafetyFragment extends PreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Anwenderschutz";
            header.iconRes = GlobalConfigs.IconResFireWall;
            header.fragment = SettingsFragments.SafetyFragment.class.getName();

            return header;
        }

        private final static ArrayList<Preference> preferences = new ArrayList<>();

        public static void registerAll(Context context)
        {
            preferences.clear();

            SafetyCBPreference cb;

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.respect.privacy");
            cb.setTitle("Datenschutz immer sicherstellen");

            cb.setSummaryOn("Der Datenschutz wird immer sichergestellt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass "
                    + "der Anwender beim Surfen in Bezug auf den Datenschutz immer "
                    + "möglichst geschützt ist.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.stay.onsite");
            cb.setTitle("Beim Surfen das Verlassen der Web-Seite unterbinden");

            cb.setSummaryOn("Der Anwender ist vor unbeabsichtigtem Verlassen der Web-Seite geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch ungewollte Verlinkungen gefährdet wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.popups");
            cb.setTitle("Popups blockieren");

            cb.setSummaryOn("Der Anwender ist vor Popups geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch plötzlich auftauchende Popups belästigt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.premium");
            cb.setTitle("Kostenpflichtige Premium-Angebote blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Premium-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender keine Abonnements oder kostenpflichtige Premium-Angebote "
                    + "bestellen kannn.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.shops");
            cb.setTitle("Zugang zu Website-Shops blockieren");

            cb.setSummaryOn("Der Anwender ist vor kostenpflichtigen Shop-Angeboten geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht versehentlich Shop-Angebote wahrnimmt.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.inlinead");
            cb.setTitle("Werbe-Verknüpfungen im Lesetext blockieren");

            cb.setSummaryOn("Der Anwender ist vor Werbelinks im redaktionellen Text geschützt.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch irreführenden Werbelinks im redaktionellen. "
                    + "Text einer Webseite auf Anzeigen geführt wird.");

            preferences.add(cb);

            cb = new SafetyCBPreference(context);

            cb.setKey("firewall.safety.block.ads");
            cb.setTitle("Anzeigenblöcke ausblenden");

            cb.setSummaryOn("Anzeigenblöcke werden nach Möglichkeit ausgeblendet.");

            cb.setSummaryOff("Durch Setzen dieser Option stellen sie sicher, dass der "
                    + "Anwender nicht durch unerwünschte Werbung behindert, belästig oder "
                    + "das Datenvolumen unnötig belastet wird.");

            preferences.add(cb);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region SafetyCBPreference implementation.

        private static class SafetyCBPreference extends CheckBoxPreference
        {
            public SafetyCBPreference(Context context)
            {
                super(context);
            }

            @Override
            protected void onBindView(View view)
            {
                super.onBindView(view);

                //
                // Stupid limitation on size of description.
                //

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                summary.setMaxLines(50);
            }

            @Override
            public void setKey(String text)
            {
                super.setKey(text);

                if ((sharedPrefs != null) && ! sharedPrefs.contains(text))
                {
                    //
                    // Initially commit shared preference.
                    //

                    sharedPrefs.edit().putBoolean(text,false).apply();
                }
            }

            public void setSummaryOff(String text)
            {
                text = "\n" + text + "\n\n" + "Zurzeit ist der Anwender gefährdet.";

                super.setSummaryOff(text);
            }
        }

        //endregion SafetyCBPreference implementation.
    }

    //endregion Firewall safety preferences

    //region Firewall domains preferences

    public static class DomainsFragment extends PreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Domänen Freischaltung";
            header.iconRes = GlobalConfigs.IconResFireWall;
            header.fragment = SettingsFragments.DomainsFragment.class.getName();

            return header;
        }

        private static final ArrayList<Preference> preferences = new ArrayList<>();

        private static JSONObject globalConfig;

        private static void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, R.raw.default_firewall);

                    if (ctemp != null)
                    {
                        globalConfig = ctemp.getJSONObject("firewall");
                    }
                }
                catch (NullPointerException | JSONException ex)
                {
                    Log.e(LOGTAG, "loadGlobalConfig: Cannot read default firewall config.");
                }
            }
        }

        public static void registerAll(Context context)
        {
            preferences.clear();

            loadGlobalConfig(context);
            if (globalConfig == null) return;

            try
            {
                JSONArray domains = globalConfig.getJSONArray("domains");

                DomainsCBPreference cb;

                for (int inx = 0; inx < domains.length(); inx++)
                {
                    JSONObject domain = domains.getJSONObject(inx);

                    // @formatter:off
                    String name   = domain.has("domain") ? domain.getString("domain") : null;
                    String type   = domain.has("type")   ? domain.getString("type")   : null;
                    String source = domain.has("source") ? domain.getString("source") : null;
                    // @formatter:on

                    JSONArray desc = domain.has("description") ? domain.getJSONArray("description") : null;
                    JSONArray faults = domain.has("faults") ? domain.getJSONArray("faults") : null;

                    if ((name == null) || (desc == null) || (faults == null)) continue;

                    String title = name;
                    if (type != null) title += " (" + type + ")";

                    String text = "";

                    for (int cnt = 0; cnt < desc.length(); cnt++) text += desc.getString(cnt);
                    if (source != null) text += " (Quelle: " + source + ")";

                    cb = new DomainsCBPreference(context);

                    cb.setKey("firewall.domains." + name);
                    cb.setTitle(title);
                    cb.setSummary(text, name, faults);

                    preferences.add(cb);
                }

            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region DomainsCBPreference implementation

        private static class DomainsCBPreference extends CheckBoxPreference
        {
            public DomainsCBPreference(Context context)
            {
                super(context);
            }

            @Override
            protected void onBindView(View view)
            {
                super.onBindView(view);

                //
                // Stupid limitation on size of description.
                //

                TextView summary = (TextView) view.findViewById(android.R.id.summary);
                summary.setMaxLines(50);
            }

            @Override
            public void setKey(String text)
            {
                super.setKey(text);

                if ((sharedPrefs != null) && ! sharedPrefs.contains(text))
                {
                    //
                    // Initially commit shared preference.
                    //

                    sharedPrefs.edit().putBoolean(text,false).apply();
                }
            }

            public void setSummary(String text, String domain, JSONArray faults)
            {
                try
                {
                    text = "\n" + text + "\n";

                    for (int inx = 0; inx < faults.length(); inx++)
                    {
                        String fault = faults.getString(inx);

                        if (fault.equals("nohome"   )) text += "\n– Problem: Homepage www." + domain + " nicht abrufbar";
                        if (fault.equals("nopriv"   )) text += "\n– Problem: Fehlende Datenschutzerklärung";
                        if (fault.equals("noprivger")) text += "\n– Problem: Datenschutzerklärung nur in Englisch";
                        if (fault.equals("nooptin"  )) text += "\n– Problem: Nur nachträglicher Opt-Out";
                        if (fault.equals("nooptout" )) text += "\n– Problem: Keine Opt-Out Möglichkeit angeboten";
                        if (fault.equals("nofaults" )) text += "\n– Problem: Eigentlich keines";

                        if (fault.equals("isporn"   )) text += "\n– Problem: Pornografie";
                        if (fault.equals("ispremium")) text += "\n– Problem: Premium Dienste";
                    }
                }
                catch (JSONException ignore)
                {
                }

                setSummaryOn(text + "\n\nZugriff ist freigegeben");
                setSummaryOff(text + "\n\nZugriff zurzeit gesperrt");
            }
        }

        //endregion DomainsCBPreference implementation
    }

    //endregion Firewall domains preferences

    //region Webframe newspaper preferences

    public static class WebConfigNewspaperFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Zeitungen";
            header.iconRes = GlobalConfigs.IconResWebConfigNewspaper;
            header.fragment = WebConfigNewspaperFragment.class.getName();

            return header;
        }

        public WebConfigNewspaperFragment()
        {
            super();

            root = "webconfig";
            subtype = "newspaper";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigNewspaper;
            keyprefix = "webconfig.newspaper";
            masterenable = "Online Zeitungen freischalten";
        }
    }

    //endregion Webframe newspaper preferences

    //region Webframe magazine preferences

    public static class WebConfigMagazineFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Magazine";
            header.iconRes = GlobalConfigs.IconResWebConfigMagazine;
            header.fragment = WebConfigMagazineFragment.class.getName();

            return header;
        }

        public WebConfigMagazineFragment()
        {
            super();

            root = "webconfig";
            subtype = "magazine";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigMagazine;
            keyprefix = "webconfig.magazine";
            masterenable = "Online Magazine freischalten";
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe magazine preferences

    public static class WebConfigPictorialFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Illustrierte";
            header.iconRes = GlobalConfigs.IconResWebConfigPictorial;
            header.fragment = WebConfigPictorialFragment.class.getName();

            return header;
        }

        public WebConfigPictorialFragment()
        {
            super();

            root = "webconfig";
            subtype = "pictorial";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigPictorial;
            keyprefix = "webconfig.pictorial";
            masterenable = "Online Illustrierte freischalten";
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe shopping preferences

    public static class WebConfigShoppingFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Shopping";
            header.iconRes = GlobalConfigs.IconResWebConfigShopping;
            header.fragment = WebConfigShoppingFragment.class.getName();

            return header;
        }

        public WebConfigShoppingFragment()
        {
            super();

            root = "webconfig";
            subtype = "shopping";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigShopping;
            keyprefix = "webconfig.shopping";
            masterenable = "Online Shopping freischalten";
        }
    }

    //endregion Webframe shopping preferences

    //region Webframe erotics preferences

    public static class WebConfigEroticsFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Erotisches";
            header.iconRes = GlobalConfigs.IconResWebConfigErotics;
            header.fragment = WebConfigEroticsFragment.class.getName();

            return header;
        }

        public WebConfigEroticsFragment()
        {
            super();

            root = "webconfig";
            subtype = "erotics";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigErotics;
            keyprefix = "webconfig.erotics";
            masterenable = "Online Erotisches freischalten";
        }
    }

    //endregion Webframe erotics preferences

    //region IP Radio preferences

    public static class IPRadioFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Radio";
            header.iconRes = GlobalConfigs.IconResIPRadio;
            header.fragment = IPRadioFragment.class.getName();

            return header;
        }

        public IPRadioFragment()
        {
            super();

            root = "webradio";
            jsonres = R.raw.default_webradio;
            iconres = GlobalConfigs.IconResIPRadio;
            keyprefix = "ipradio";
            masterenable = "Internet Radio freischalten";
        }
    }

    //endregion IP Radio preferences

    //region IP Television preferences

    public static class IPTelevisionFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Fernsehen";
            header.iconRes = GlobalConfigs.IconResIPTelevision;
            header.fragment = IPTelevisionFragment.class.getName();

            return header;
        }

        public IPTelevisionFragment()
        {
            super();

            root = "webiptv";
            jsonres = R.raw.default_webiptv;
            iconres = GlobalConfigs.IconResIPTelevision;
            keyprefix = "iptelevision";
            masterenable = "Internet Fernsehen freischalten";
        }
    }

    //endregion IP Television preferences

    //region JSONConfigFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class JSONConfigFragment extends PreferenceFragment
    {
        private final ArrayList<Preference> preferences = new ArrayList<>();
        protected final ArrayList<String> activekeys = new ArrayList<>();

        private JSONObject globalConfig;

        protected String root;
        protected String subtype;
        protected int jsonres;
        protected int iconres;
        protected String keyprefix;
        protected String masterenable;

        private void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, jsonres);

                    if (ctemp != null)
                    {
                        globalConfig = ctemp.getJSONObject(root);
                    }
                }
                catch (NullPointerException | JSONException ex)
                {
                    Log.e(LOGTAG, "loadGlobalConfig: Cannot read default " + root + " config.");
                }
            }
        }

        public void registerAll(Context context)
        {
            preferences.clear();

            try
            {
                JSONConfigSwitchPreference sw = new JSONConfigSwitchPreference(context);

                sw.setKey(keyprefix + ".enable");
                sw.setTitle(masterenable);
                sw.setIcon(VersionUtils.getDrawableFromResources(context, iconres));
                sw.setDefaultValue(false);

                preferences.add(sw);
                activekeys.add(sw.getKey());

                boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable",false);

                NicePreferenceCategory pc;
                CheckBoxPreference cb;

                loadGlobalConfig(context);

                Iterator<String> keysIterator = globalConfig.keys();

                String key;
                String label;
                String iconurl;
                Bitmap thumbnail;
                Drawable drawable;

                while (keysIterator.hasNext())
                {
                    String website = keysIterator.next();

                    JSONObject webitem = globalConfig.getJSONObject(website);

                    label = webitem.getString("label");
                    iconurl = webitem.getString("icon");
                    thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                    drawable = new BitmapDrawable(context.getResources(),thumbnail);

                    if (webitem.has("enabled") && ! webitem.getBoolean("enabled"))
                    {
                        continue;
                    }

                    if (webitem.has("subtype") && (subtype != null)
                            && ! webitem.getString("subtype").equals(subtype))
                    {
                        continue;
                    }

                    if (! webitem.has("channels"))
                    {
                        key = keyprefix + ".website." + website;

                        cb = new CheckBoxPreference(context);

                        cb.setKey(key);
                        cb.setTitle(label);
                        cb.setIcon(drawable);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        boolean def = webitem.has("default") && webitem.getBoolean("default");

                        if (def && ! sharedPrefs.contains(key))
                        {
                            //
                            // Initially commit shared preference.
                            //

                            sharedPrefs.edit().putBoolean(key, true).apply();
                        }
                    }
                    else
                    {
                        pc = new NicePreferenceCategory(context);

                        pc.setTitle(label);
                        pc.setIcon(drawable);

                        preferences.add(pc);

                        JSONArray channels = webitem.getJSONArray("channels");

                        for (int inx = 0; inx < channels.length(); inx++)
                        {
                            JSONObject channel = channels.getJSONObject(inx);

                            label = channel.getString("label");
                            key = keyprefix + ".channel." + website + ":" + label.replace(" ", "_");
                            iconurl = channel.getString("icon");
                            thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                            drawable = new BitmapDrawable(context.getResources(), thumbnail);

                            cb = new CheckBoxPreference(context);

                            cb.setKey(key);
                            cb.setTitle(label);
                            cb.setIcon(drawable);
                            cb.setEnabled(enabled);

                            preferences.add(cb);
                            activekeys.add(cb.getKey());

                            boolean def = channel.has("default") && channel.getBoolean("default");

                            if (def && !sharedPrefs.contains(key))
                            {
                                //
                                // Initially commit shared preference.
                                //

                                sharedPrefs.edit().putBoolean(key, true).apply();
                            }
                        }
                    }
                }

                //
                // Remove disabled or obsoleted preferences.
                //

                String websiteprefix = keyprefix + ".website.";

                Map<String, ?> exists = sharedPrefs.getAll();

                for (Map.Entry<String, ?> entry : exists.entrySet())
                {
                    if (! entry.getKey().startsWith(websiteprefix)) continue;

                    if (activekeys.contains(entry.getKey())) continue;

                    sharedPrefs.edit().remove(entry.getKey()).apply();

                    Log.d(LOGTAG, "registerAll: obsolete:" + entry.getKey() + "=" + entry.getValue());
                }
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region JSONConfigSwitchPreference implementation

        private class JSONConfigSwitchPreference extends SwitchPreference
                implements Preference.OnPreferenceChangeListener
        {
            public JSONConfigSwitchPreference(Context context)
            {
                super(context);

                setOnPreferenceChangeListener(this);
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG, "onPreferenceChange:" + obj.toString());

                for (Preference pref : preferences)
                {
                    if (pref == this) continue;

                    if (pref instanceof CheckBoxPreference)
                    {
                        pref.setEnabled((boolean) obj);
                    }
                }

                return true;
            }
        }

        //endregion JSONConfigSwitchPreference implementation
    }

    //endregion JSONConfigFragment stub

    //region Niced preferences

    public static class NicePreferenceCategory extends PreferenceCategory
    {
        public NicePreferenceCategory(Context context)
        {
            super(context);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            view.setPadding(20, 20, 20, 20);
            view.setBackgroundColor(0xcccccccc);

            TextView title = (TextView) view.findViewById(android.R.id.title);
            title.setTextSize(20f);
        }
    }

    public static class NiceListPreference extends ListPreference
            implements Preference.OnPreferenceChangeListener
    {
        private String key;
        private TextView current;
        private CharSequence[] entries;
        private CharSequence[] values;
        private Runnable onClickRunner;

        private boolean enabled = true;

        public NiceListPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
        }

        @Override
        public void setKey(String key)
        {
            super.setKey(key);
            this.key = key;
        }

        public void setEntries(ArrayList<String> entries)
        {
            String[] intern = new String[ entries.size() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = entries.get(inx);
            }

            super.setEntries(intern);
            this.entries = intern;
        }

        @Override
        public void setEntries(CharSequence[] entries)
        {
            super.setEntries(entries);
            this.entries = entries;
        }

        public void setEntryValues(ArrayList<String> values)
        {
            String[] intern = new String[ values.size() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = values.get(inx);
            }

            super.setEntryValues(intern);
            this.values = intern;
        }

        @Override
        public void setEntryValues(CharSequence[] values)
        {
            super.setEntryValues(values);
            this.values = values;
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);

            this.enabled = enabled;

            if (current != null)
            {
                current.setTextColor(enabled
                        ? GlobalConfigs.PreferenceTextEnabledColor
                        : GlobalConfigs.PreferenceTextDisabledColor);
            }
        }

        private String getDisplayValue(String value)
        {
            for (int inx = 0; inx < values.length; inx++)
            {
                if (values[ inx ].equals(value))
                {
                    return (String) entries[ inx ];
                }
            }

            return "unknown";
        }

        public void setOnclick(Runnable onlick)
        {
            onClickRunner = onlick;
        }

        @Override
        protected void showDialog(Bundle state)
        {
            if (onClickRunner == null)
            {
                super.showDialog(state);

                return;
            }

            onClickRunner.run();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            if (current != null) current.setText(getDisplayValue((String) obj));

            return true;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            if (current == null)
            {
                current = new TextView(getContext());
                current.setGravity(Gravity.END);
                current.setTextSize(18f);

                current.setTextColor(enabled
                        ? GlobalConfigs.PreferenceTextEnabledColor
                        : GlobalConfigs.PreferenceTextDisabledColor);

                if (sharedPrefs.contains(key))
                {
                    current.setText(getDisplayValue(sharedPrefs.getString(key,null)));
                }
            }

            if (current.getParent() != null)
            {
                //
                // Der inder calls bind view every now and then because
                // of bad programming. So check if textview is child
                // of obsoleted view and remove before processing.
                //

                ((LinearLayout) current.getParent()).removeView(current);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END);

            ((LinearLayout) view).addView(current, lp);
        }
    }

    public static class NiceDisplayTextPreference extends NiceEditTextPreference
    {
        public NiceDisplayTextPreference(Context context)
        {
            super(context);
        }

        @Override
        protected void showDialog(Bundle state)
        {
        }
    }

    public static class NiceEditTextPreference extends EditTextPreference
            implements Preference.OnPreferenceChangeListener
    {
        private String key;
        private TextView current;
        private Runnable onClickRunner;
        private boolean isPassword;
        private boolean isUppercase;

        public NiceEditTextPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
        }

        @Override
        public void setKey(String key)
        {
            super.setKey(key);
            this.key = key;
        }

        @Override
        public void setText(String text)
        {
            super.setText(text);

            sharedPrefs.edit().putString(key, text).apply();
        }

        public void setIsPassword()
        {
            isPassword = true;
        }

        public void setIsUppercase()
        {
            isUppercase = true;
        }

        public void setOnclick(Runnable onlick)
        {
            onClickRunner = onlick;
        }

        @Override
        protected void showDialog(Bundle state)
        {
            if (onClickRunner == null)
            {
                super.showDialog(state);

                return;
            }

            onClickRunner.run();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            if (current != null) current.setText((String) obj);

            return true;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            current = new TextView(getContext());
            current.setGravity(Gravity.END);
            current.setTextSize(18f);

            if (isPassword)
            {
                current.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            if (isUppercase)
            {
                current.setAllCaps(true);
            }

            if (sharedPrefs.contains(key))
            {
                current.setText(sharedPrefs.getString(key, null));
            }

            ((ViewGroup) view).addView(current);
        }
    }

    public static class NiceDialogPreference extends DialogPreference
    {
        protected TextView current;
        protected boolean enabled;
        protected boolean isInteger;
        protected String unit;

        public NiceDialogPreference(Context context)
        {
            super(context, null);
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);
            this.enabled = enabled;

            if (current != null)
            {
                current.setTextColor(enabled
                        ? GlobalConfigs.PreferenceTextEnabledColor
                        : GlobalConfigs.PreferenceTextDisabledColor);
            }
        }

        public void setValue(String value)
        {
            if (current != null)
            {
                current.setText(value);
            }
        }

        public void setValue(int value)
        {
            if (current != null)
            {
                String text = "" + value;

                if (unit != null)
                {
                    text += " " + unit;
                }

                current.setText(text);
            }
        }

        public void setUnit(String unit)
        {
            this.unit = unit;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            if (current == null)
            {
                current = new TextView(getContext());
                current.setGravity(Gravity.END);
                current.setTextSize(18f);

                current.setTextColor(enabled
                        ? GlobalConfigs.PreferenceTextEnabledColor
                        : GlobalConfigs.PreferenceTextDisabledColor);

                if (sharedPrefs.contains(this.getKey()))
                {
                    if (isInteger)
                    {
                        this.setValue(sharedPrefs.getInt(this.getKey(), 0));
                    }
                    else
                    {
                        this.setValue(sharedPrefs.getString(this.getKey(), null));
                    }
                }
            }

            if (current.getParent() != null)
            {
                //
                // Der inder calls bind view every now and then because
                // of bad programming. So check if textview is child
                // of obsoleted view and remove before processing.
                //

                ((LinearLayout) current.getParent()).removeView(current);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END);

            ((LinearLayout) view).addView(current, lp);
        }
    }

    public static class NiceDatePreference extends NiceDialogPreference implements
            DatePicker.OnDateChangedListener
    {
        private String dateString;
        private String changedValueCanBeNull;
        private DatePicker datePicker;

        public NiceDatePreference(Context context)
        {
            super(context);
        }

        @Override
        protected View onCreateDialogView()
        {
            this.datePicker = new DatePicker(getContext());

            Calendar calendar = getDate();

            datePicker.init(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    this);

            return datePicker;
        }

        public Calendar getDate()
        {
            try
            {
                Date date = formatter().parse(defaultValue());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal;
            }
            catch (java.text.ParseException ex)
            {
                return new GregorianCalendar(2000, 0, 1);
            }
        }

        public void setDate(String dateString)
        {
            this.dateString = dateString;
        }

        public SimpleDateFormat formatter()
        {
            return new SimpleDateFormat("yyyy.MM.dd");
        }

        public SimpleDateFormat summaryFormatter()
        {
            return new SimpleDateFormat("dd.MM.yyyy");
        }

        @Override
        protected Object onGetDefaultValue(TypedArray a, int index)
        {
            return a.getString(index);
        }

        @Override
        protected void onSetInitialValue(boolean restoreValue, Object def)
        {
            if (restoreValue)
            {
                this.dateString = getPersistedString(defaultValue());
                setTheDate(this.dateString);
            }
            else
            {
                boolean wasNull = this.dateString == null;

                setDate((String) def);

                if (!wasNull) persistDate(this.dateString);
            }
        }

        public void onDateChanged(DatePicker view, int year, int month, int day)
        {
            Calendar selected = new GregorianCalendar(year, month, day);
            this.changedValueCanBeNull = formatter().format(selected.getTime());
        }

        @Override
        protected void onDialogClosed(boolean shouldSave)
        {
            if (shouldSave && this.changedValueCanBeNull != null)
            {
                setTheDate(this.changedValueCanBeNull);
                this.changedValueCanBeNull = null;
            }
        }

        private void setTheDate(String s)
        {
            setDate(s);
            persistDate(s);
        }

        private void persistDate(String s)
        {
            persistString(s);

            if (current != null) current.setText(summaryFormatter().format(getDate().getTime()));
        }

        private String defaultValue()
        {
            if (this.dateString == null)
            {
                setDate(formatter().format(new GregorianCalendar(2000, 0, 1).getTime()));
            }

            return this.dateString;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            current.setText(summaryFormatter().format(getDate().getTime()));
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            super.onClick(dialog, which);

            datePicker.clearFocus();

            onDateChanged(datePicker,
                    datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth());

            onDialogClosed(which == DialogInterface.BUTTON_POSITIVE);
        }
    }

    public static class NiceNumberPreference extends NiceDialogPreference
    {
        private NumberPicker numberPicker;

        private int actValue;
        private int minValue = Integer.MIN_VALUE;
        private int maxValue = Integer.MAX_VALUE;

        private int stepValue = 1;
        private String[] stepValues;

        public NiceNumberPreference(Context context)
        {
            super(context);

            isInteger = true;

            setPositiveButtonText(android.R.string.ok);
            setNegativeButtonText(android.R.string.cancel);
        }

        public void setMinMaxValue(int min, int max, int step)
        {
            minValue = min;
            maxValue = max;
            stepValue = step;
        }

        @Override
        protected View onCreateDialogView()
        {
            numberPicker = new NumberPicker(getContext());

            numberPicker.setMinValue(minValue);
            numberPicker.setMaxValue(maxValue);
            numberPicker.setValue(actValue);

            if (stepValue != 1)
            {
                int steps = (maxValue - minValue) / stepValue;

                if (steps < 100)
                {
                    stepValues = new String[ steps ];

                    for (int inx = 0; inx < steps; inx++)
                    {
                        stepValues[ inx ] = "" + (minValue + (inx * stepValue));
                    }

                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(stepValues.length - 1);
                    numberPicker.setValue((actValue - minValue) / stepValue);
                    numberPicker.setDisplayedValues(stepValues);
                }
            }

            //
            // Inhibit display of completely useless keyboard.
            //

            numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            return numberPicker;
        }

        @Override
        protected void onDialogClosed(boolean positiveResult)
        {
            if (positiveResult)
            {
                if (stepValues == null)
                {
                    setValue(numberPicker.getValue());
                }
                else
                {
                    setValue(Integer.parseInt(stepValues[ numberPicker.getValue() ]));
                }
            }
        }

        @Override
        protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
        {
            setValue(restoreValue ? getPersistedInt(actValue) : (int) defaultValue);
        }

        @Override
        public void setValue(int value)
        {
            super.setValue(value);

            if (shouldPersist())
            {
                persistInt(value);
            }

            if (value != actValue)
            {
                actValue = value;
                notifyChanged();
            }
        }
    }

    //endregion Niced preferences
}
