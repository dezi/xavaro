package de.xavaro.android.common;

import android.annotation.SuppressLint;

import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class NicedPreferences
{
    private static final String LOGTAG = NicedPreferences.class.getSimpleName();

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
            Calendar calendar = getDate();

            datePicker = new DatePicker(getContext());
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
                return new GregorianCalendar();
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
                dateString = getPersistedString(defaultValue());
                setTheDate(dateString);
            }
            else
            {
                setDate((String) def);
                persistString((String) def);
            }
        }

        public void onDateChanged(DatePicker view, int year, int month, int day)
        {
            Calendar selected = new GregorianCalendar(year, month, day);
            changedValueCanBeNull = formatter().format(selected.getTime());
        }

        @Override
        protected void showDialog(Bundle state)
        {
            super.showDialog(state);

            CaptureOverlay.monitorAllTouches(getDialog());
        }

        @Override
        protected void onDialogClosed(boolean shouldSave)
        {
            if (shouldSave && changedValueCanBeNull != null)
            {
                setTheDate(changedValueCanBeNull);

                callChangeListener(changedValueCanBeNull);

                changedValueCanBeNull = null;
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

        @Override
        public void setDefaultValue(Object defaultValue)
        {
            super.setDefaultValue(defaultValue);

            if (defaultValue instanceof String) this.dateString = (String) defaultValue;
        }

        private String defaultValue()
        {
            if (this.dateString == null)
            {
                setDate(formatter().format(new GregorianCalendar().getTime()));
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
        protected void showDialog(Bundle state)
        {
            super.showDialog(state);

            CaptureOverlay.monitorAllTouches(getDialog());
        }

        @Override
        protected void onDialogClosed(boolean positiveResult)
        {
            if (positiveResult)
            {
                int newvalue = numberPicker.getValue();

                if (stepValues != null) newvalue = Integer.parseInt(stepValues[ newvalue ]);

                setValue(newvalue);
                callChangeListener(newvalue);
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

    public static class NiceDualpickPreference extends NiceDialogPreference
    {
        private NumberPicker numberPicker1;
        private NumberPicker numberPicker2;

        private int actValue1;
        private int actValue2;
        private int minValue1 = Integer.MIN_VALUE;
        private int minValue2 = Integer.MIN_VALUE;
        private int maxValue1 = Integer.MAX_VALUE;
        private int maxValue2 = Integer.MAX_VALUE;

        private int stepValue1 = 1;
        private int stepValue2 = 1;
        private String[] stepValues1;
        private String[] stepValues2;

        private String separator = ":";

        public NiceDualpickPreference(Context context)
        {
            super(context);

            setPositiveButtonText(android.R.string.ok);
            setNegativeButtonText(android.R.string.cancel);
        }

        public void setMinMaxValue1(int min, int max, int step)
        {
            minValue1 = min;
            maxValue1 = max;
            stepValue1 = step;
        }

        public void setMinMaxValue2(int min, int max, int step)
        {
            minValue2 = min;
            maxValue2 = max;
            stepValue2 = step;
        }

        public void setSeparator(String separator)
        {
            this.separator = separator;
        }

        @Override
        protected View onCreateDialogView()
        {
            numberPicker1 = new NumberPicker(getContext());

            numberPicker1.setMinValue(minValue1);
            numberPicker1.setMaxValue(maxValue1);
            numberPicker1.setValue(actValue1);

            if (stepValue1 != 1)
            {
                int steps = 1 + ((maxValue1 - minValue1) / stepValue1);

                if (steps < 100)
                {
                    stepValues1 = new String[ steps ];

                    for (int inx = 0; inx < steps; inx++)
                    {
                        stepValues1[ inx ] = "" + (minValue1 + (inx * stepValue1));
                    }

                    numberPicker1.setMinValue(0);
                    numberPicker1.setMaxValue(stepValues1.length - 1);
                    numberPicker1.setValue((actValue1 - minValue1) / stepValue1);
                    numberPicker1.setDisplayedValues(stepValues1);
                }
            }

            numberPicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            numberPicker2 = new NumberPicker(getContext());

            numberPicker2.setMinValue(minValue2);
            numberPicker2.setMaxValue(maxValue2);
            numberPicker2.setValue(actValue2);

            if (stepValue2 != 1)
            {
                int steps =  1 + ((maxValue2 - minValue2) / stepValue2);

                if (steps < 100)
                {
                    stepValues2 = new String[ steps ];

                    for (int inx = 0; inx < steps; inx++)
                    {
                        stepValues2[ inx ] = "" + (minValue2 + (inx * stepValue2));
                    }

                    numberPicker2.setMinValue(0);
                    numberPicker2.setMaxValue(stepValues2.length - 1);
                    numberPicker2.setValue((actValue2 - minValue2) / stepValue2);
                    numberPicker2.setDisplayedValues(stepValues2);
                }
            }

            numberPicker2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            LinearLayout view = new LinearLayout(getContext());
            view.setOrientation(LinearLayout.HORIZONTAL);
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setGravity(Gravity.CENTER);
            view.addView(numberPicker1);
            view.addView(numberPicker2);

            return view;
        }

        @Override
        protected void showDialog(Bundle state)
        {
            super.showDialog(state);

            CaptureOverlay.monitorAllTouches(getDialog());
        }

        @Override
        protected void onDialogClosed(boolean positiveResult)
        {
            if (positiveResult)
            {
                String newvalue;

                if ((stepValues1 == null) || (stepValues2 == null))
                {
                    newvalue = numberPicker1.getValue() + separator + numberPicker2.getValue();
                }
                else
                {
                    String val1 = stepValues1[ numberPicker1.getValue() ];
                    String val2 = stepValues2[ numberPicker2.getValue() ];

                    newvalue = val1 + separator + val2;
                }

                setValue(newvalue);
                callChangeListener(newvalue);
            }
        }

        @Override
        protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
        {
            setValue(restoreValue ? getPersistedString((String) defaultValue) : (String) defaultValue);
        }

        @Override
        public void setValue(String value)
        {
            super.setValue(value);

            if (shouldPersist()) persistString(value);

            try
            {
                String[] parts = value.split(separator);

                if (parts.length == 2)
                {
                    int value1 = Integer.parseInt(parts[ 0 ]);
                    int value2 = Integer.parseInt(parts[ 1 ]);

                    if ((value1 != actValue1) || (value2 != actValue2))
                    {
                        actValue1 = value1;
                        actValue2 = value2;

                        notifyChanged();
                    }
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
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
            this.disabled = !enabled;

            if (current != null)
            {
                current.setTextColor(enabled
                        ? CommonConfigs.PreferenceTextEnabledColor
                        : CommonConfigs.PreferenceTextDisabledColor);
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
            current.setTextSize(Simple.getPreferredTextSize());

            current.setTextColor(disabled
                    ? CommonConfigs.PreferenceTextDisabledColor
                    : CommonConfigs.PreferenceTextEnabledColor);

            if (isInteger)
            {
                this.setValue(getPersistedInt(0));
            }
            else
            {
                this.setValue(getPersistedString(""));
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END);

            ((LinearLayout) view).addView(current, lp);
        }
    }

    public static class NiceMultiListPreference extends MultiSelectListPreference
            implements Preference.OnPreferenceChangeListener
    {
        private TextView current;
        private boolean disabled;
        private CharSequence[] entries;
        private CharSequence[] values;

        public NiceMultiListPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
        }

        @Override
        public void setEntries(CharSequence[] entries)
        {
            super.setEntries(entries);
            this.entries = entries;
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

        public void setEntries(JSONArray entries)
        {
            String[] intern = new String[ entries.length() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = Json.getString(entries, inx);
            }

            super.setEntries(intern);
            this.entries = intern;
        }

        @Override
        public void setEntryValues(CharSequence[] values)
        {
            super.setEntryValues(values);
            this.values = values;
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

        public void setEntryValues(JSONArray values)
        {
            String[] intern = new String[ values.length() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = Json.getString(values, inx);
            }

            super.setEntryValues(intern);
            this.values = intern;
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);

            disabled = ! enabled;

            if (current != null)
            {
                current.setTextColor(disabled
                        ? CommonConfigs.PreferenceTextDisabledColor
                        : CommonConfigs.PreferenceTextEnabledColor);
            }
        }

        private String getDisplayValue(Set<String> dvalues)
        {
            String display = "";

            if ((values != null) && (dvalues != null))
            {
                for (int inx = 0; inx < values.length; inx++)
                {
                    for (String dvalue : dvalues)
                    {
                        if (values[ inx ].equals(dvalue))
                        {
                            if (display.length() > 0) display += ", ";
                            display += (String) entries[ inx ];
                        }
                    }
                }
            }

            return display;
        }

        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            Log.d(LOGTAG, "onPreferenceChange: " + obj);

            if ((current != null) && (obj instanceof Set))
            {
                try
                {
                    current.setText(getDisplayValue((Set<String>) obj));
                }
                catch (Exception ignore)
                {
                }
            }

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
                current.setTextSize(Simple.getPreferredTextSize());

                current.setTextColor(disabled
                        ? CommonConfigs.PreferenceTextDisabledColor
                        : CommonConfigs.PreferenceTextEnabledColor);

                current.setText(getDisplayValue(Simple.getSharedPrefStringSet(getKey())));
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

    public static class NiceListPreference extends ListPreference implements
            Preference.OnPreferenceChangeListener,
            View.OnLongClickListener
    {
        private TextView current;
        private boolean disabled;
        private CharSequence[] entries;
        private CharSequence[] values;
        private Runnable onClickRunner;
        private JSONObject jsonSlug;

        private Runnable onLongClickRunner;

        public NiceListPreference(Context context)
        {
            super(context);

            setOnPreferenceChangeListener(this);
        }

        public void setOnLongClick(Runnable onclick)
        {
            onLongClickRunner = onclick;
        }

        public void setSlug(JSONObject slug)
        {
            jsonSlug = slug;
        }

        public JSONObject getSlug()
        {
            return jsonSlug;
        }

        @Override
        public void setDefaultValue(Object defaultValue)
        {
            super.setDefaultValue(defaultValue);

            if (defaultValue instanceof String) persistString((String) defaultValue);
        }

        @Override
        public void setEntries(CharSequence[] entries)
        {
            super.setEntries(entries);
            this.entries = entries;
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

        public void setEntries(JSONArray entries)
        {
            String[] intern = new String[ entries.length() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = Json.getString(entries, inx);
            }

            super.setEntries(intern);
            this.entries = intern;
        }

        public void setEntries(int resid)
        {
            String[] entries = Simple.getTransArray(resid);
            super.setEntries(entries);
            this.entries = entries;
        }

        @Override
        public void setEntryValues(CharSequence[] values)
        {
            super.setEntryValues(values);
            this.values = values;
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

        public void setEntryValues(JSONArray values)
        {
            String[] intern = new String[ values.length() ];

            for (int inx = 0; inx < intern.length; inx++)
            {
                intern[ inx ] = Json.getString(values, inx);
            }

            super.setEntryValues(intern);
            this.values = intern;
        }

        public void setEntryValues(int resid)
        {
            String[] entries = Simple.getTransArray(resid);
            super.setEntryValues(entries);
            this.values = entries;
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);

            disabled = ! enabled;

            if (current != null)
            {
                current.setTextColor(disabled
                        ? CommonConfigs.PreferenceTextDisabledColor
                        : CommonConfigs.PreferenceTextEnabledColor);
            }
        }

        private String getDisplayValue(String value)
        {
            if (values != null)
            {
                for (int inx = 0; inx < values.length; inx++)
                {
                    if (values[ inx ].equals(value)) return (String) entries[ inx ];
                }
            }

            return value;
        }

        public void setOnclick(Runnable onclick)
        {
            onClickRunner = onclick;
        }

        @Override
        protected void showDialog(Bundle state)
        {
            if (onClickRunner == null)
            {
                super.showDialog(state);

                CaptureOverlay.monitorAllTouches(getDialog());

                return;
            }

            onClickRunner.run();
        }

        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            if (current != null) current.setText(getDisplayValue((String) obj));

            return true;
        }

        @Override
        public void setValue(String value)
        {
            super.setValue(value);

            if (current != null) current.setText(getDisplayValue(value));
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            LinearLayout newlayout = FuckIntoShape(getContext(), view);

            if (current == null)
            {
                current = new TextView(getContext());
                current.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                current.setTextSize(Simple.getPreferredTextSize());

                current.setTextColor(disabled
                        ? CommonConfigs.PreferenceTextDisabledColor
                        : CommonConfigs.PreferenceTextEnabledColor);

                current.setText(getDisplayValue(getValue()));
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
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.END);

            newlayout.addView(current, lp);
        }

        @Override
        public boolean onLongClick(View view)
        {
            if (onLongClickRunner != null)
            {
                onLongClickRunner.run();

                return true;
            }

            return false;
        }
    }

    public static class NiceDisplayTextPreference extends NicedPreferences.NiceEditTextPreference
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
        private boolean isPhonenumber;
        private Runnable onClickRunner;
        private String emptytext;

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
                        ? CommonConfigs.PreferenceTextEnabledColor
                        : CommonConfigs.PreferenceTextDisabledColor);
            }
        }

        @Override
        public void setText(String text)
        {
            super.setText(text);

            if (current == null) return;

            if (isPassword)
            {
                if ((getText() == null) || getText().equals(""))
                {
                    current.setText("Nicht gesetzt");
                    current.setTextColor(Color.RED);
                }
                else
                {
                    current.setText("Zum Anzeigen klicken");
                }
            }
            else
            {
                if ((emptytext != null) && ((getText() == null) || getText().equals("")))
                {
                    current.setText(emptytext);
                }
                else
                {
                    if (isPhonenumber)
                    {
                        current.setText(VersionUtils.formatPhoneNumber(getText()));
                    }
                    else
                    {
                        current.setText(text);
                    }
                }
            }
        }

        public void setEmptyText(String text)
        {
            emptytext = text;
        }

        public void setIsPassword()
        {
            isPassword = true;
        }

        public void setIsUppercase()
        {
            isUppercase = true;
        }

        public void setIsPhonenumber()
        {
            isPhonenumber = true;
        }

        public void setOnclick(Runnable onclick)
        {
            onClickRunner = onclick;
        }

        @Override
        protected void showDialog(Bundle state)
        {
            if (onClickRunner == null)
            {
                super.showDialog(state);

                CaptureOverlay.monitorAllTouches(getDialog());

                if (getDialog().getWindow() != null)
                {
                    View view = getDialog().getWindow().getDecorView();

                    if (view != null)
                    {
                        View edit = view.findViewById(android.R.id.edit);

                        if ((edit != null) && (edit instanceof EditText))
                        {
                            ((EditText) edit).setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                            if (isUppercase)
                            {
                                ((EditText) edit).setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                            }

                            if (isPhonenumber)
                            {
                                ((EditText) edit).setInputType(InputType.TYPE_CLASS_PHONE);
                            }
                        }
                    }
                }

                return;
            }

            onClickRunner.run();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            setText((String) obj);

            return true;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            current = new TextView(getContext());
            current.setGravity(Gravity.END);
            current.setTextSize(Simple.getPreferredTextSize());

            current.setTextColor(disabled
                    ? CommonConfigs.PreferenceTextDisabledColor
                    : CommonConfigs.PreferenceTextEnabledColor);

            if (isUppercase) current.setAllCaps(true);

            setText(getText());

            ((ViewGroup) view).addView(current);
        }
    }

    public static class NiceSwitchPreference extends SwitchPreference
    {
        public NiceSwitchPreference(Context context)
        {
            super(context);
        }
    }

    public static class NiceScorePreference extends NiceListPreference implements
            View.OnTouchListener
    {
        public NiceScorePreference(Context context)
        {
            super(context);
        }

        private String apkname;
        private int score = -1;

        private final ImageView stars[] = new ImageView[ 5 ];

        private String[] degreeText = {

                "ungenügend",
                "mangelhaft", "mangelhaft", "mangelhaft",
                "ausreichend", "ausreichend", "ausreichend",
                "befriedigend", "befriedigend", "befriedigend",
                "gut", "gut", "gut",
                "sehr gut", "sehr gut", "sehr gut"
        };

        public void setScore(int score)
        {
            this.score = score;
        }

        public void setAPKName(String apkname)
        {
            this.apkname = apkname;
        }

        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                view.playSoundEffect(android.view.SoundEffectConstants.CLICK);

                if (view.getTag().toString().equals("open"))
                {
                    ProcessManager.launchApp(apkname);
                }

                if (view.getTag().toString().equals("install"))
                {
                    Simple.installAppFromPlaystore(apkname);
                }

                if (view.getTag().toString().equals("deinstall"))
                {
                    Simple.uninstallApp(apkname);
                }
            }

            return true;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            if (score >= 0)
            {
                LinearLayout scorelayout = new LinearLayout(getContext());
                scorelayout.setOrientation(LinearLayout.HORIZONTAL);
                scorelayout.setLayoutParams(Simple.layoutParamsXX(Simple.MP, Simple.DP(36)));

                int rest = score;

                for (int inx = 0; inx < 5; inx++)
                {
                    stars[ inx ] = new ImageView(getContext());
                    stars[ inx ].setLayoutParams(Simple.layoutParamsXX(Simple.DP(25), Simple.DP(30)));
                    Simple.setPadding(stars[ inx ], 0, 5, 0, 0);
                    scorelayout.addView(stars[ inx ]);

                    if (rest >= 3)
                    {
                        stars[ inx ].setImageResource(R.drawable.score_3_40x40);
                    }
                    else
                    {
                        if (rest >= 2)
                        {
                            stars[ inx ].setImageResource(R.drawable.score_2_40x40);
                        }
                        else
                        {
                            if (rest >= 1)
                            {
                                stars[ inx ].setImageResource(R.drawable.score_1_40x40);
                            }
                            else
                            {
                                stars[ inx ].setImageResource(R.drawable.score_0_40x40);
                            }
                        }
                    }

                    rest -= 3;
                }

                TextView degree = new TextView(getContext());
                degree.setLayoutParams(Simple.layoutParamsWM());
                degree.setGravity(Gravity.CENTER_VERTICAL);
                degree.setTextSize(Simple.getPreferredTextSize());
                degree.setText(degreeText[ score ]);
                Simple.setPadding(degree, 8, 0, 0, 0);
                scorelayout.addView(degree);

                ((ViewGroup) view).addView(scorelayout, 1);

                if (apkname != null)
                {
                    FrameLayout installframe = new FrameLayout(getContext());
                    installframe.setLayoutParams(Simple.layoutParamsMW());

                    ((ViewGroup) view).addView(installframe, 3);

                    LinearLayout installcenter = new LinearLayout(getContext());
                    installcenter.setOrientation(LinearLayout.HORIZONTAL);
                    installcenter.setLayoutParams(Simple.layoutParamsWM());
                    installcenter.setPadding(0, 10, 0, 10);

                    if (Simple.isAppInstalled(apkname))
                    {
                        TextView deinstall = new TextView(getContext());
                        Simple.makeStandardButton(deinstall, false);
                        deinstall.setOnTouchListener(this);
                        deinstall.setText("Deinstallieren");
                        deinstall.setTag("deinstall");
                        installcenter.addView(deinstall);

                        TextView open = new TextView(getContext());
                        Simple.makeStandardButton(open, true);
                        open.setOnTouchListener(this);
                        open.setText("Öffnen");
                        open.setTag("open");
                        installcenter.addView(open);
                    }
                    else
                    {
                        TextView install = new TextView(getContext());
                        Simple.makeStandardButton(install, true);
                        install.setOnTouchListener(this);
                        install.setText("Installieren");
                        install.setTag("install");
                        installcenter.addView(install);
                    }

                    installframe.addView(installcenter, Simple.layoutParamsWM(Gravity.END));
                }
            }
        }
    }

    private static LinearLayout FuckIntoShape(Context context, View view)
    {
        //
        // Re-arrange completely shitty checkbox layout.
        // The summary must be last item in layout. Icon,
        // title and checkbox need to be in one layout.
        //

        //
        // Step one: move all items into new horizontal linear layout.
        //

        LinearLayout newlayout = new LinearLayout(context);
        newlayout.setOrientation(LinearLayout.HORIZONTAL);
        newlayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ViewGroup vg = (ViewGroup) view;

        while (vg.getChildCount() > 0)
        {
            newlayout.addView(Simple.removeFromParent(vg.getChildAt(0)));
        }

        vg.addView(newlayout);

        //
        // Step two: fuck top level layout to be vertical match parent.
        //

        ((LinearLayout) vg).setOrientation(LinearLayout.VERTICAL);

        ImageView icon = (ImageView) view.findViewById(android.R.id.icon);

        if ((icon != null) && (icon.getParent() instanceof LinearLayout))
        {
            LinearLayout icon_frame = (LinearLayout) icon.getParent();
            icon_frame.setLayoutParams(new LinearLayout.LayoutParams(Simple.WC, Simple.MP));
            icon_frame.setPadding(0, 0, 0, 0);
        }

        //
        // Step three: remove summary from horizontal layout and add
        // to top layout now beeing vertical.
        //

        TextView summary = (TextView) view.findViewById(android.R.id.summary);

        vg.addView(Simple.removeFromParent(summary));

        //
        // Step four: do not forget to fuck stupid max lines on summary.
        //

        summary.setMaxLines(50);
        summary.setPadding(0, 0, 16, 16);

        //
        // Fucked into shape.
        //

        return newlayout;
    }

    public static class NiceCheckboxPreference extends CheckBoxPreference
    {
        public NiceCheckboxPreference(Context context)
        {
            super(context);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            FuckIntoShape(getContext(), view);
        }
    }

    public static class NiceSeparatorPreference extends NiceCategoryPreference
    {
        public NiceSeparatorPreference(Context context)
        {
            super(context);
        }
    }

    public static class NiceCategoryPreference extends Preference implements
            View.OnClickListener,
            View.OnLongClickListener
    {
        public NiceCategoryPreference(Context context)
        {
            super(context);
        }

        protected Runnable onClickRunner;
        protected Runnable onLongClickRunner;
        protected ImageView actionIcon;

        public void setOnClick(Runnable onclick)
        {
            onClickRunner = onclick;
        }

        public void setOnLongClick(Runnable onclick)
        {
            onLongClickRunner = onclick;
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            LinearLayout newlayout = FuckIntoShape(getContext(), view);

            //
            // Grey background.
            //

            view.setBackgroundColor(0xcccccccc);

            //
            // Add an action icon.
            //

            actionIcon = new ImageView(getContext());
            actionIcon.setLayoutParams(Simple.layoutParamsXX(Simple.WC, Simple.MP));
            actionIcon.setPadding(10, 16, 10, 10);
            newlayout.addView(actionIcon);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (onClickRunner != null) onClickRunner.run();
        }

        @Override
        public boolean onLongClick(View view)
        {
            if (onLongClickRunner != null) onLongClickRunner.run();

            return false;
        }
    }

    public static class NiceInfoPreference extends NiceCategoryPreference
    {
        public NiceInfoPreference(Context context)
        {
            super(context);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            if ((summaryResid != 0) || (summaryText != null))
            {
                actionIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }

        private int summaryResid;
        private String summaryText;
        private boolean summaryOpen;

        @Override
        public void setSummary(int resid)
        {
            summaryResid = resid;
        }

        @Override
        public void setSummary(CharSequence summary)
        {
            if (summary != null) summaryText = summary.toString();
        }

        @Override
        public void onClick(View view)
        {
            if ((summaryResid != 0) || (summaryText != null))
            {
                if (summaryOpen)
                {
                    super.setSummary(null);
                    summaryOpen = false;
                }
                else
                {
                    super.setSummary((summaryText != null) ? summaryText : Simple.getTrans(summaryResid));
                    summaryOpen = true;
                }
            }
        }
    }

    public static class NiceDeletePreference extends NiceCategoryPreference
    {
        public NiceDeletePreference(Context context)
        {
            super(context);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            actionIcon.setImageResource(android.R.drawable.ic_menu_delete);
        }

        public void setDeleteCallback(DeleteCallback callback)
        {
            this.callback = callback;
        }

        private DeleteCallback callback;
        private AlertDialog dialog;

        @Override
        public void onClick(View view)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getTitle());

            builder.setNegativeButton("Abbrechen", null);
            builder.setPositiveButton("Löschen", null);

            dialog = builder.create();
            dialog.show();

            Simple.adjustAlertDialog(dialog);

            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negative.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onCancelClick();
                }
            });

            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onDeleteClick();
                }
            });
        }

        public void onCancelClick()
        {
            dialog.cancel();
            dialog = null;
        }

        public void onDeleteClick()
        {
            if (callback != null) callback.onDeleteRequest(getKey());

            dialog.cancel();
            dialog = null;
        }

        public interface DeleteCallback
        {
            void onDeleteRequest(String prefkey);
        }
    }

    public static class NiceSearchPreference extends NiceCategoryPreference
    {
        public NiceSearchPreference(Context context)
        {
            super(context);
        }

        @Override
        protected void onBindView(View view)
        {
            super.onBindView(view);

            actionIcon.setImageResource(android.R.drawable.ic_menu_search);
        }

        public void setSearchCallback(SearchCallback callback)
        {
            this.callback = callback;
        }

        private SearchCallback callback;
        private AlertDialog dialog;
        private EditText search;
        private String query;

        public void onCancelClick()
        {
            if (callback != null)
            {
                callback.onSearchCancel(getKey());
            }

            dialog.cancel();
            dialog = null;
            search = null;
        }

        public void onSearchClick()
        {
            query = search.getText().toString().trim();

            if (callback != null)
            {
                callback.onSearchRequest(getKey(), query);
            }

            dialog.cancel();
            dialog = null;
            search = null;
        }

        @Override
        public void onClick(View view)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getTitle());

            builder.setNegativeButton("Abbrechen", null);

            builder.setPositiveButton("Suchen", null);

            dialog = builder.create();

            LinearLayout content = new LinearLayout(getContext());
            content.setOrientation(LinearLayout.HORIZONTAL);
            content.setPadding(20, 8, 20, 8);
            content.setLayoutParams(Simple.layoutParamsMM());

            search = new EditText(getContext());
            search.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            search.setLayoutParams(Simple.layoutParamsMM());
            search.setText((query == null) ? "" : query);
            search.setSelection(search.length());
            content.addView(search);

            dialog.setView(content);
            dialog.show();

            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negative.setTextSize(24f);

            negative.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onCancelClick();
                }
            });

            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setTextSize(24f);

            positive.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onSearchClick();
                }
            });

            CaptureOverlay.monitorAllTouches(dialog);
        }

        public interface SearchCallback
        {
            void onSearchCancel(String prefkey);
            void onSearchRequest(String prefkey, String query);
        }
    }
}
