package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Random;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebApp;

public class PreferencesHealth
{
    //region Health medicator preferences

    public static class HealthMedicatorFragment extends PreferencesWebApps.WebappFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = WebApp.getLabel("medicator");
            header.fragment = PreferencesWebApps.WebappFragment.class.getName();
            header.fragmentArguments = new Bundle();
            header.fragmentArguments.putString("webappname", "medicator");

            return header;
        }
    }

    //endregion Health medicator preferences

    //region Health Person preferences

    public static class HealthPersonalFragment extends PreferenceFragments.EnableFragmentStub
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

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceNumberPreference np;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;

            //
            // Personal user data required for calculations.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Persönliche Daten");
            preferences.add(pc);

            lp = new NicedPreferences.NiceListPreference(context);

            String[] genderText = { "Nicht gesetzt", "Männlich", "Weiblich"  };
            String[] genderVals = { "unknown", "male", "female" };

            lp.setKey(keyprefix + ".user.gender");
            lp.setEntries(genderText);
            lp.setEntryValues(genderVals);
            lp.setDefaultValue("unknown");
            lp.setTitle("Geschlecht");
            lp.setEnabled(enabled);

            preferences.add(lp);

            dp = new NicedPreferences.NiceDatePreference(context);

            dp.setKey(keyprefix + ".user.birthdate");
            dp.setTitle("Geburtsdatum");
            dp.setEnabled(enabled);

            preferences.add(dp);

            np = new NicedPreferences.NiceNumberPreference(context);

            np.setKey(keyprefix + ".user.size");
            np.setUnit("cm");
            np.setMinMaxValue(100, 250, 1);
            np.setDefaultValue(170);
            np.setTitle("Größe");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NicedPreferences.NiceNumberPreference(context);

            np.setKey(keyprefix + ".user.weight");
            np.setUnit("kg");
            np.setMinMaxValue(40, 250, 1);
            np.setDefaultValue(75);
            np.setTitle("Gewicht");
            np.setEnabled(enabled);

            preferences.add(np);

            lp = new NicedPreferences.NiceListPreference(context);

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

    public static class HealthUnitsFragment extends PreferenceFragments.EnableFragmentStub
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
            NicedPreferences.NiceListPreference lp;

            lp = new NicedPreferences.NiceListPreference(context);

            String[] unitTimeText = { "24 Stunden", "12 Stunden"  };
            String[] unitTimeVals = { "24h", "12h" };

            lp.setKey(keyprefix + ".timedisp");
            lp.setEntries(unitTimeText);
            lp.setEntryValues(unitTimeVals);
            lp.setDefaultValue("24h");
            lp.setTitle("Zeitangabe mit");

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            String[] unitSizeText = {"Zentimeter", "Inch"};
            String[] unitSizeVals = {"cm", "inch"};

            lp.setKey(keyprefix + ".size");
            lp.setEntries(unitSizeText);
            lp.setEntryValues(unitSizeVals);
            lp.setDefaultValue("cm");
            lp.setTitle("Größe in");

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            String[] unitDistText = { "Meter", "Yards"  };
            String[] unitDistVals = { "m", "yard" };

            lp.setKey(keyprefix + ".distance");
            lp.setEntries(unitDistText);
            lp.setEntryValues(unitDistVals);
            lp.setDefaultValue("m");
            lp.setTitle("Distanz in");

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            String[] unitWeightText = { "Kilogramm", "Pfund"  };
            String[] unitWeightVals = { "kg", "lbs" };

            lp.setKey(keyprefix + ".weight");
            lp.setEntries(unitWeightText);
            lp.setEntryValues(unitWeightVals);
            lp.setDefaultValue("kg");
            lp.setTitle("Gewicht in");

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

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

    //region Health Oxy preferences

    public static class HealthOxyFragment extends BlueToothFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Blutsauerstoff";
            header.iconRes = GlobalConfigs.IconResHealthOxy;
            header.fragment = HealthOxyFragment.class.getName();

            return header;
        }

        public HealthOxyFragment()
        {
            super();

            isOxy = true;

            iconres = GlobalConfigs.IconResHealthOxy;
            keyprefix = "health.oxy";
            masterenable = "Puls-Oxymeter freischalten";
            devicetitle = "Puls-Oxymeter";
            devicesearch = "Puls-Oxymeter werden gesucht...";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);
        }
    }

    //endregion Health Oxy preferences

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

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceCheckboxPreference cb;
            NicedPreferences.NiceDualpickPreference dp;
            NicedPreferences.NiceSwitchPreference sp;

            //
            // User.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Benutzerauswahl");
            preferences.add(pc);

            lp = new NicedPreferences.NiceListPreference(context);

            String[] userSelectText = { "Benutzer 1", "Benutzer 2", "Benutzer 3", "Benutzer 4" };
            String[] userSelectVals = { "1", "2", "3", "4" };

            lp.setKey(keyprefix + ".selecteduser");
            lp.setEntries(userSelectText);
            lp.setEntryValues(userSelectVals);
            lp.setDefaultValue("1");
            lp.setTitle("Gerätebenutzer");
            lp.setEnabled(enabled);

            preferences.add(lp);

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Vereinfachte Bedienung");
            preferences.add(pc);

            cb = new NicedPreferences.NiceCheckboxPreference(context);

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

            cb = new NicedPreferences.NiceCheckboxPreference(context);

            cb.setKey(keyprefix + ".anytime");
            cb.setTitle("Zurückgesetztes Datum im Gerät kompensieren");
            cb.setEnabled(enabled);

            cb.setSummary("Das Blutdruckmessgerät verliert seine Uhrzeit, wenn "
                    + "die Batterien gewechselt werden. Wenn der Anwender nicht "
                    + "in der Lage ist, Datum und Uhrzeit wieder korrekt einzustellen "
                    + "empfiehlt sich diese Option.");

            preferences.add(cb);

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Warnungen");
            preferences.add(pc);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".alert.enable");
            sp.setTitle("Aktivieren");
            sp.setEnabled(enabled);

            sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    for (Preference pref : preferences)
                    {
                        if (pref.getKey() == null) continue;
                        if (pref.getKey().equals(keyprefix + ".alert.enable")) continue;
                        if (! pref.getKey().startsWith(keyprefix + ".alert.")) continue;

                        pref.setEnabled((boolean) newValue);
                    }

                    return true;
                }
            });

            preferences.add(sp);

            dp = new NicedPreferences.NiceDualpickPreference(context);

            dp.setKey(keyprefix + ".alert.highbp");
            dp.setMinMaxValue1(100, 250, 10);
            dp.setMinMaxValue2(70, 140, 10);
            dp.setDefaultValue("160:100");
            dp.setTitle("Zu hoher Blutdruck");
            dp.setEnabled(enabled);

            preferences.add(dp);

            dp = new NicedPreferences.NiceDualpickPreference(context);

            dp.setKey(keyprefix + ".alert.lowbp");
            dp.setMinMaxValue1(60, 110, 10);
            dp.setMinMaxValue2(60, 80, 10);
            dp.setDefaultValue("80:60");
            dp.setTitle("Zu niedriger Blutdruck");
            dp.setEnabled(enabled);

            preferences.add(dp);

            cb = new NicedPreferences.NiceCheckboxPreference(context);

            cb.setKey(keyprefix + ".alert.alertgroup");
            cb.setTitle("Assistenz informieren");
            cb.setEnabled(enabled);

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

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceEditTextPreference ep;
            NicedPreferences.NiceDisplayTextPreference dt;

            //
            // Personal user data required for calculations.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Waagenbenutzer");
            preferences.add(pc);

            ep = new NicedPreferences.NiceEditTextPreference(context);

            ep.setKey(keyprefix + ".initials");
            ep.setTitle("Initialen");
            ep.setDefaultValue("XXX");
            ep.setIsUppercase();
            ep.setEnabled(enabled);

            preferences.add(ep);

            dt = new NicedPreferences.NiceDisplayTextPreference(context);

            dt.setKey(keyprefix + ".userid");
            dt.setTitle("User-ID");
            dt.setEnabled(enabled);

            if (! Simple.getSharedPrefs().contains(dt.getKey()))
            {
                //
                // Initially compute user id.
                //

                String userid = "" + new Random().nextInt();
                Simple.setSharedPrefString(dt.getKey(), userid);
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

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceNumberPreference np;

            //
            // Goals.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Tagesziele");
            preferences.add(pc);

            np = new NicedPreferences.NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.steps");
            np.setMinMaxValue(100, 10000, 100);
            np.setDefaultValue(500);
            np.setTitle("Schritte machen");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NicedPreferences.NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.calories");
            np.setMinMaxValue(100, 2000, 100);
            np.setDefaultValue(300);
            np.setTitle("Kalorien verbrennen");
            np.setEnabled(enabled);

            preferences.add(np);

            np = new NicedPreferences.NiceNumberPreference(context);

            np.setKey(keyprefix + ".goals.sleephours");
            np.setMinMaxValue(4, 10, 1);
            np.setDefaultValue(8);
            np.setTitle("Stunden schlafen");
            np.setEnabled(enabled);

            preferences.add(np);

            lp = new NicedPreferences.NiceListPreference(context);

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

            isGlucose = true;

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

            /*
            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceEditTextPreference ep;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;
            NicedPreferences.NiceNumberPreference np;
            */
        }
    }

    //endregion Health Glucose preferences

    //region BlueTooth preferences stub

    @SuppressWarnings("WeakerAccess")
    public static class BlueToothFragment extends PreferenceFragments.EnableFragmentStub
            implements
            DialogInterface.OnClickListener,
            BlueTooth.BlueToothDiscoverCallback
    {
        private static final String LOGTAG = BlueToothFragment.class.getSimpleName();

        protected String devicetitle;
        protected String devicesearch;

        protected boolean isBPM;
        protected boolean isOxy;
        protected boolean isScale;
        protected boolean isSensor;
        protected boolean isGlucose;

        private final BlueToothFragment self = this;
        private Context context;

        private NicedPreferences.NiceListPreference devicePref;

        final ArrayList<String> recentText = new ArrayList<>();
        final ArrayList<String> recentVals = new ArrayList<>();

        @Override
        public void registerAll(Context context)
        {
            this.context = context;

            super.registerAll(context);

            boolean enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;

            //
            // Bluetooth device selection preference
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("BlueTooth Geräteauswahl");
            preferences.add(pc);

            devicePref = new NicedPreferences.NiceListPreference(context);

            devicePref.setKey(keyprefix + ".device");

            recentText.add("Nicht gesetzt");
            recentVals.add("unknown");

            String btDevice = Simple.getSharedPrefString(devicePref.getKey());

            if ((btDevice != null) && ! btDevice.equals("unknown"))
            {
                String[] btDeviceparts = btDevice.split(" => ");

                if (btDeviceparts.length == 2)
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

            preferences.add(devicePref);

            //
            // Icon location preference.
            //

            String[] keys =  Simple.getTransArray(R.array.pref_health_where_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_health_where_vals);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".icon");
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("home");
            lp.setTitle("Anzeigen");
            lp.setEnabled(enabled);

            preferences.add(lp);
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

                String btDevice = Simple.getSharedPrefString(keyprefix + ".device");
                if (btDevice == null) btDevice = "unknown";

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
                                Simple.getSharedPrefs().edit().putString(keyprefix + ".device",
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
                        if (isOxy) new BlueToothOxy(context).discover(self);
                        if (isScale) new BlueToothScale(context).discover(self);
                        if (isSensor) new BlueToothSensor(context).discover(self);
                        if (isGlucose) new BlueToothGlucose(context).discover(self);
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
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
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
}
