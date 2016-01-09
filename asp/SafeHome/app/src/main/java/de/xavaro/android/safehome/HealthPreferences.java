package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import java.util.Random;

import de.xavaro.android.common.NicedPreferences;

public class HealthPreferences
{
    //region Health Person preferences

    public static class HealthPersonalFragment extends SettingsFragments.EnablePreferenceFragment
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

    public static class HealthUnitsFragment extends SettingsFragments.EnablePreferenceFragment
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

    //region Health BPM preferences

    public static class HealthBPMFragment extends SettingsFragments.BlueToothFragment
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
        }
    }

    //endregion Health BPM preferences

    //region Health Scale preferences

    public static class HealthScaleFragment extends SettingsFragments.BlueToothFragment
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

            if (! DitUndDat.SharedPrefs.sharedPrefs.contains(dt.getKey()))
            {
                //
                // Initially compute user id.
                //

                int userid = new Random().nextInt();
                DitUndDat.SharedPrefs.sharedPrefs.edit().putString(dt.getKey(), "" + userid).apply();
            }

            preferences.add(dt);
        }
    }

    //endregion Health Scale preferences

    //region Health Sensor preferences

    public static class HealthSensorFragment extends SettingsFragments.BlueToothFragment
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

    public static class HealthGlucoseFragment extends SettingsFragments.BlueToothFragment
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
}
