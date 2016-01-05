package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SettingsNiced
{
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

        @SuppressLint("SimpleDateFormat")
        public SimpleDateFormat formatter()
        {
            return new SimpleDateFormat("yyyy.MM.dd");
        }

        @SuppressLint("SimpleDateFormat")
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

    public static class NiceDialogPreference extends DialogPreference
    {
        protected TextView current;
        protected boolean disabled;
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
            this.disabled = ! enabled;

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

            current = new TextView(getContext());
            current.setGravity(Gravity.END);
            current.setTextSize(18f);

            current.setTextColor(disabled
                    ? GlobalConfigs.PreferenceTextDisabledColor
                    : GlobalConfigs.PreferenceTextEnabledColor);

            if (DitUndDat.SharedPrefs.sharedPrefs.contains(this.getKey()))
            {
                if (isInteger)
                {
                    this.setValue(DitUndDat.SharedPrefs.sharedPrefs.getInt(this.getKey(), 0));
                }
                else
                {
                    this.setValue(DitUndDat.SharedPrefs.sharedPrefs.getString(this.getKey(), null));
                }
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END);

            ((LinearLayout) view).addView(current, lp);
        }
    }

    public static class NiceListPreference extends ListPreference
            implements Preference.OnPreferenceChangeListener
    {
        private TextView current;
        private boolean disabled;
        private CharSequence[] entries;
        private CharSequence[] values;
        private Runnable onClickRunner;

        public NiceListPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
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

            disabled = ! enabled;

            if (current != null)
            {
                current.setTextColor(disabled
                        ? GlobalConfigs.PreferenceTextDisabledColor
                        : GlobalConfigs.PreferenceTextEnabledColor);
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

                current.setTextColor(disabled
                        ? GlobalConfigs.PreferenceTextDisabledColor
                        : GlobalConfigs.PreferenceTextEnabledColor);

                if (DitUndDat.SharedPrefs.sharedPrefs.contains(getKey()))
                {
                    current.setText(getDisplayValue(DitUndDat.SharedPrefs.sharedPrefs.getString(getKey(),null)));
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

    public static class NiceDisplayTextPreference extends SettingsNiced.NiceEditTextPreference
    {
        public NiceDisplayTextPreference(Context context)
        {
            super(context);
        }

        @Override
        protected void showDialog(Bundle state)
        {
            //
            // Simply do nothing.
            //
        }
    }

    public static class NiceEditTextPreference extends EditTextPreference
            implements Preference.OnPreferenceChangeListener
    {
        private TextView current;
        protected boolean disabled;
        private boolean isPassword;
        private boolean isUppercase;
        private Runnable onClickRunner;

        public NiceEditTextPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);

            this.disabled = ! enabled;

            if (current != null)
            {
                current.setTextColor(enabled
                        ? GlobalConfigs.PreferenceTextEnabledColor
                        : GlobalConfigs.PreferenceTextDisabledColor);
            }
        }

        @Override
        public void setText(String text)
        {
            super.setText(text);

            if (current != null) current.setText(text);
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

            current.setTextColor(disabled
                    ? GlobalConfigs.PreferenceTextDisabledColor
                    : GlobalConfigs.PreferenceTextEnabledColor);

            if (isPassword)
            {
                current.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            if (isUppercase)
            {
                current.setAllCaps(true);
            }

            current.setText(getText());

            ((ViewGroup) view).addView(current);
        }
    }

    public static class NiceCheckboxPreference extends CheckBoxPreference
    {
        public NiceCheckboxPreference(Context context)
        {
            super(context);
        }
    }

    public static class NiceCategoryPreference extends PreferenceCategory
    {
        public NiceCategoryPreference(Context context)
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

}
