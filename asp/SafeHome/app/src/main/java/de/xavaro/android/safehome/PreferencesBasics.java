package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
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
import android.os.Bundle;
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

    //region Owner preferences

    public static class OwnerFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Anwender";
            header.iconRes = GlobalConfigs.IconResOwner;
            header.fragment = OwnerFragment.class.getName();

            return header;
        }

        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceEditTextPreference et;
            NicedPreferences.NiceSwitchPreference sp;
            NicedPreferences.NiceListPreference lp;

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Persönliches");
            pc.setIcon(ProfileImagesNew.getOwnerProfileImage());
            preferences.add(pc);

            final CharSequence[] prefixText = { "Keine", "Herr", "Frau" };
            final CharSequence[] prefixVals = { "no",    "mr",   "ms"   };

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey("owner.prefix");
            lp.setEntries(prefixText);
            lp.setEntryValues(prefixVals);
            lp.setDefaultValue("no");
            lp.setTitle("Anrede");

            preferences.add(lp);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("owner.firstname");
            et.setTitle("Vorname");

            preferences.add(et);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("owner.givenname");
            et.setTitle("Nachname");

            preferences.add(et);

            lp = new NicedPreferences.NiceListPreference(context);

            final CharSequence[] siezenText = { "gesiezt werden", "geduzt werden"};
            final CharSequence[] siezenVals = { "siezen", "duzen" };

            lp.setKey("owner.siezen");
            lp.setEntries(siezenText);
            lp.setEntryValues(siezenVals);
            lp.setDefaultValue("siezen");
            lp.setTitle("Anwender möchte");

            preferences.add(lp);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey("owner.usephoto");
            sp.setTitle("Profilfoto freigeben");
            sp.setDefaultValue(true);

            preferences.add(sp);
        }
    }

    //endregion Owner preferences

    //region Administrator preferences

    public static class AdminFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Administration";
            header.iconRes = GlobalConfigs.IconResAdministrator;
            header.fragment = AdminFragment.class.getName();

            return header;
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceEditTextPreference et;
            NicedPreferences.NiceCategoryPreference pc;

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Zugang");
            preferences.add(pc);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("admin.password");
            et.setTitle("Admin Passwort");
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

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("System-Buttons");
            preferences.add(pc);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("admin.home.button");
            et.setTitle("Home-Button");
            et.setText(DefaultApps.getDefaultHomeLabel(context));
            et.setOnclick(selectHome);

            if (sharedPrefs.getString(et.getKey(), "").equals(StaticUtils.getAppName(context)))
            {
                ArchievementManager.archieved("configure.settings.homebutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.homebutton");
            }

            preferences.add(et);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("admin.assist.button");
            et.setTitle("Assistenz-Button");
            et.setText(DefaultApps.getDefaultAssistLabel(context));
            et.setOnclick(selectAssist);

            if (sharedPrefs.getString(et.getKey(),"").equals(DefaultApps.getAppLable(context)))
            {
                ArchievementManager.archieved("configure.settings.assistbutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.assistbutton");
            }

            preferences.add(et);

            NicedPreferences.NiceListPreference cb = new NicedPreferences.NiceListPreference(context);

            CharSequence[] menueText = { "Android-System", "SafeHome" };
            CharSequence[] menueVals = { "android", "safehome" };

            cb.setKey("admin.recent.button");
            cb.setEntries(menueText);
            cb.setEntryValues(menueVals);
            cb.setDefaultValue("safehome");
            cb.setTitle("Menü-Button");

            preferences.add(cb);
        }

        private final Runnable selectHome = new Runnable()
        {
            @Override
            public void run()
            {
                DefaultApps.setDefaultHome(Simple.getAppContext());
            }
        };

        private final Runnable selectAssist = new Runnable()
        {
            @Override
            public void run()
            {
                DefaultApps.setDefaultAssist(Simple.getAppContext());
            }
        };
    }

    //endregion Administrator preferences

    //region Community preferences

    public static class CommunityFragment extends PreferenceFragments.BasicFragmentStub implements
            CommService.CommServiceCallback
    {
        private static final String LOGTAG = CommunityFragment.class.getSimpleName();

        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Bezugspersonen";
            header.iconRes = GlobalConfigs.IconResCommunity;
            header.fragment = CommunityFragment.class.getName();

            return header;
        }

        public CommunityFragment()
        {
            super();

            keyprefix = "community";
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            Log.d(LOGTAG, "onDestroy");

            WifiLookup.deleteSocket();
        }

        private final ArrayList<String> remoteContacts = new ArrayList<>();

        private JSONObject remoteContact;
        private AlertDialog dialog;

        private NicedPreferences.NiceEditTextPreference sendPinPref;
        private NicedPreferences.NiceEditTextPreference recvPinPref;

        private TextView pincode;
        private TextView pinName;
        private EditText pinPart1;
        private EditText pinPart2;
        private EditText pinPart3;

        private NicedPreferences.NiceListPreference wifiFindPref;

        private JSONArray wifiRetrieved;
        private final ArrayList<String> wifiFoundText = new ArrayList<>();
        private final ArrayList<String> wifiFoundVals = new ArrayList<>();

        private void generatePincode()
        {
            Random rand = new Random();

            String pincode = String.format("%04d-%04d-%04d",
                    rand.nextInt(9999), rand.nextInt(9999), rand.nextInt(9999));

            if (Simple.equalsIgnoreCase(Simple.getMacAddress(), "38:2D:E8:E1:C2:2A"))
            {
                //
                // Dezi's Samsung Tablet
                //

                pincode = "0000-0000-0001";
            }

            if (Simple.equalsIgnoreCase(Simple.getMacAddress(), "64:BC:0C:18:BB:AC"))
            {
                //
                // Dezi's LG Phone
                //

                pincode = "0000-0000-0002";

            }

            sharedPrefs.edit().putString(keyprefix + ".sendpin", pincode).apply();
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceCheckboxPreference cp;
            NicedPreferences.NiceListPreference lp;

            //
            // Connect via pincode.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Über Pincode verbinden");
            preferences.add(pc);

            //
            // Send pin code.
            //

            generatePincode();

            sendPinPref = new NicedPreferences.NiceEditTextPreference(context);

            sendPinPref.setKey(keyprefix + ".sendpin");
            sendPinPref.setTitle("Pincode freigeben");
            sendPinPref.setOnclick(sendPinDialog);

            preferences.add(sendPinPref);

            //
            // Send pin code duration.
            //

            lp = new NicedPreferences.NiceListPreference(context);

            CharSequence[] durationText = {
                    "5 Minuten",
                    "60 Minuten",
                    "8 Stunden",
                    "1 Tag",
                    "2 Tage"
            };

            CharSequence[] durationVals = {
                    "5",
                    "60",
                    "640",
                    "1440",
                    "2880"
            };

            lp.setKey(keyprefix + ".sendpinduration");
            lp.setEntries(durationText);
            lp.setEntryValues(durationVals);
            lp.setDefaultValue("5");
            lp.setTitle("Pincode Timeout");

            preferences.add(lp);

            //
            // Receive pin code.
            //

            recvPinPref = new NicedPreferences.NiceEditTextPreference(context);

            recvPinPref.setKey(keyprefix + ".recvpin");
            recvPinPref.setTitle("Pincode verbinden");
            recvPinPref.setOnclick(recvPinDialog);

            preferences.add(recvPinPref);

            //
            // Connect via udp broadcast.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Über Wifi verbinden");
            preferences.add(pc);

            cp = new NicedPreferences.NiceCheckboxPreference(context);
            cp.setTitle("Gerät sichtbar machen");

            cp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if ((Boolean) newValue)
                    {
                        WifiLookup.makeVisible();
                    }
                    else
                    {
                        WifiLookup.removeVisibility();
                    }

                    return true;
                }
            });

            preferences.add(cp);

            wifiFindPref = new NicedPreferences.NiceListPreference(context);

            wifiFindPref.setTitle("Nach Geräten suchen");
            wifiFindPref.setOnclick(findWifiDialog);

            preferences.add(wifiFindPref);

            //
            // Confirmed connects.
            //

            registerRemotes(context, false);
        }

        private void removeRemoteContact(String idremote)
        {
            for (int inx = 0; inx < preferences.size(); inx++)
            {
                Preference pref = preferences.get(inx);

                if ((pref.getKey() != null) && pref.getKey().contains(idremote))
                {
                    root.removePreference(pref);
                    preferences.remove(pref);

                    inx--;
                }
            }

            remoteContacts.remove(idremote);

            //
            // Wipe from system.
            //

            RemoteContacts.removeContactFinally(idremote);
        }

        private void registerRemotes(Context context, boolean update)
        {
            JSONObject rcs = RemoteContacts.getAllContacts();
            if (rcs == null) return;

            Iterator<String> keysIterator = rcs.keys();

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceEditTextPreference ep;

            while (keysIterator.hasNext())
            {
                try
                {
                    final String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String prefkey = keyprefix + ".remote." + ident;
                    JSONObject rc = rcs.getJSONObject(ident);

                    String name = "";
                    if (rc.has("ownerFirstName")) name += " " + rc.getString("ownerFirstName");
                    if (rc.has("ownerGivenName")) name += " " + rc.getString("ownerGivenName");
                    name = name.trim();
                    if (name.length() == 0) name = "Unbekannter Benutzer";

                    String info = "";
                    if (rc.has("appName")) info += rc.getString("appName");
                    if (rc.has("devName")) info += " - " + rc.getString("devName");

                    if (GlobalConfigs.BetaFlag)
                    {
                        if (rc.has("macAddr")) info += " - " + rc.getString("macAddr");
                        info += "\n" + ident;
                    }

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setKey(prefkey);
                    pc.setTitle(name);
                    pc.setSummary(info);
                    pc.setDefaultValue(true);

                    pc.setOnLongClick(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d(LOGTAG, "onLongClick: " + ident);

                            removeRemoteContact(ident);
                        }
                    });

                    preferences.add(pc);
                    if (update) root.addPreference(pc);

                    ep = new NicedPreferences.NiceEditTextPreference(context);
                    ep.setKey(prefkey + ".nickname");
                    ep.setDefaultValue(name);
                    ep.setTitle("Nickname");

                    preferences.add(ep);
                    if (update) root.addPreference(ep);
                }
                catch (JSONException ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }
        }

        private final View.OnClickListener findWifiAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.setTitle("Suche nach Geräten...");
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);

                WifiLookup.findVisible();

                handler.postDelayed(onWifiFindDone, 2000);
            }
        };

        private final View.OnClickListener findWifiDialogCancel = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.cancel();
            }
        };

        private void findWifiDialogShow(boolean wassearch)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getAppContext());

            if (wassearch)
            {
                if (wifiFoundText.size() == 0)
                {
                    builder.setTitle("Es wurden keine Geräte gefunden.");
                }
                else
                {
                    builder.setTitle("Gefundene Geräte:");
                }
            }
            else
            {
                builder.setTitle("Suche nach Geräten...");
            }

            builder.setPositiveButton("Abbrechen", clickListener);
            builder.setNeutralButton("Suchen", clickListener);

            dialog = builder.create();

            RadioGroup rg = new RadioGroup(Simple.getAppContext());
            rg.setOrientation(RadioGroup.VERTICAL);
            rg.setPadding(40, 10, 0, 0);

            for (int inx = 0; inx < wifiFoundText.size(); inx++)
            {
                RadioButton rb = new RadioButton(Simple.getAppContext());

                rb.setId(4711 + inx);
                rb.setTextSize(18f);
                rb.setPadding(0, 10, 0, 10);

                //
                // Display unknown as text option, selected devices
                // with name and mac address to get better overwiev
                // for user.
                //

                rb.setText((inx == 0) ? wifiFoundText.get(inx) : wifiFoundVals.get(inx));
                rb.setTag(wifiFoundVals.get(inx));

                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
                    {
                        if (checked)
                        {
                            String identity = (String) compoundButton.getTag();

                            Log.d(LOGTAG,"onCheckedChanged: identity:" + identity);

                            if (wifiRetrieved != null)
                            {
                                for (int inx = 0; inx < wifiRetrieved.length(); inx++)
                                {
                                    JSONObject entry = Json.getObject(wifiRetrieved, inx);
                                    String idremote = Json.getString(entry, "idremote");
                                    if ((idremote == null) || ! idremote.equals(identity)) continue;

                                    Log.d(LOGTAG,"onCheckedChanged: requestKeyExchange...");

                                    CommService.subscribeMessage(CommunityFragment.this, "responsePublicKeyXChange");
                                    CommService.subscribeMessage(CommunityFragment.this, "responseAESpassXChange");
                                    CommService.subscribeMessage(CommunityFragment.this, "responseOwnerIdentity");

                                    requestKeyExchange(entry);
                                }
                            }
                        }
                    }
                });

                rg.addView(rb);
            }

            dialog.setView(rg);
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(findWifiDialogCancel);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(findWifiAction);
        }

        public final Runnable findWifiDialog = new Runnable()
        {
            @Override
            public void run()
            {
                findWifiDialogShow(false);

                findWifiAction.onClick(null);
            }
        };

        public final Runnable onWifiFindDone = new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(LOGTAG, "onWifiFindDone: ");

                wifiRetrieved = WifiLookup.getVisible();

                wifiFoundText.clear();
                wifiFoundVals.clear();

                if (wifiRetrieved != null)
                {
                    for (int inx = 0; inx < wifiRetrieved.length(); inx++)
                    {
                        JSONObject entry = Json.getObject(wifiRetrieved, inx);

                        String appName   = Json.getString(entry, "appName");
                        String devName   = Json.getString(entry, "devName");
                        String firstName = Json.getString(entry, "ownerFirstName");
                        String givenName = Json.getString(entry, "ownerGivenName");

                        String newValue = Json.getString(entry, "idremote");

                        String newEntry = firstName + " " + givenName
                                + " (" + appName + "/" + devName + ")";

                        if (! wifiFoundText.contains(newEntry))
                        {
                            wifiFoundText.add(newEntry);
                            wifiFoundVals.add(newValue);
                        }
                    }
                }

                dialog.cancel();

                findWifiDialogShow(true);
            }
        };

        public final Runnable sendPinDialog = new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getAppContext());
                builder.setTitle(sendPinPref.getTitle() + ":");

                builder.setPositiveButton("Abbrechen", clickListener);
                builder.setNegativeButton("Neu", clickListener);
                builder.setNeutralButton("Freigeben", clickListener);

                dialog = builder.create();

                pincode = new TextView(Simple.getAppContext());
                pincode.setTextSize(Simple.getPreferredEditSize());
                pincode.setPadding(40, 24, 0, 0);
                pincode.setText(sharedPrefs.getString(sendPinPref.getKey(), ""));

                dialog.setView(pincode);
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(sendPinAction);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(randPinAction);
            }
        };

        public final Runnable recvPinDialog = new Runnable()
        {
            @Override
            public void run()
            {
                String[] actpincode = sharedPrefs.getString(recvPinPref.getKey(), "").split("-");

                AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getAppContext());
                builder.setTitle(recvPinPref.getTitle());

                builder.setPositiveButton("Abbrechen", clickListener);
                builder.setNeutralButton("Jetzt verbinden", clickListener);

                dialog = builder.create();

                LinearLayout ll = new LinearLayout(Simple.getAppContext());
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setPadding(40, 24, 0, 0);

                LinearLayout lp = new LinearLayout(Simple.getAppContext());
                lp.setOrientation(LinearLayout.HORIZONTAL);
                ll.addView(lp);

                InputFilter[] filters = new InputFilter[ 1 ];
                filters[ 0 ] = new InputFilter.LengthFilter(4);

                pinPart1 = new EditText(Simple.getAppContext());
                pinPart1.setMinEms(3);
                pinPart1.setTextSize(Simple.getPreferredEditSize());
                pinPart1.setFilters(filters);
                pinPart1.setGravity(Gravity.CENTER);
                pinPart1.setBackgroundColor(0x80cccccc);
                pinPart1.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                pinPart1.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                if (actpincode.length > 0) pinPart1.setText(actpincode[ 0 ]);
                pinPart1.selectAll();

                lp.addView(pinPart1);

                pinPart1.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                        if (s.length() == 4) pinPart2.requestFocus();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3)
                    {
                    }

                    @Override
                    public void afterTextChanged(Editable arg0)
                    {
                    }
                });

                TextView sep1 = new TextView(Simple.getAppContext());
                sep1.setTextSize(Simple.getPreferredEditSize());
                sep1.setText(" – ");
                lp.addView(sep1);

                pinPart2 = new EditText(Simple.getAppContext());
                pinPart2.setMinEms(3);
                pinPart2.setTextSize(Simple.getPreferredEditSize());
                pinPart2.setFilters(filters);
                pinPart2.setGravity(Gravity.CENTER);
                pinPart2.setBackgroundColor(0x80cccccc);
                pinPart2.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                pinPart2.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                if (actpincode.length > 1) pinPart2.setText(actpincode[ 1 ]);
                pinPart2.selectAll();

                lp.addView(pinPart2);

                pinPart2.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                        if (s.length() == 4) pinPart3.requestFocus();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3)
                    {
                    }

                    @Override
                    public void afterTextChanged(Editable arg0)
                    {
                    }
                });

                TextView sep2 = new TextView(Simple.getAppContext());
                sep2.setTextSize(Simple.getPreferredEditSize());
                sep2.setText(" – ");
                lp.addView(sep2);

                pinPart3 = new EditText(Simple.getAppContext());
                pinPart3.setMinEms(3);
                pinPart3.setTextSize(Simple.getPreferredEditSize());
                pinPart3.setFilters(filters);
                pinPart3.setGravity(Gravity.CENTER);
                pinPart3.setBackgroundColor(0x80cccccc);
                pinPart3.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                pinPart3.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                if (actpincode.length > 2) pinPart3.setText(actpincode[ 2 ]);
                pinPart3.selectAll();

                lp.addView(pinPart3);

                pinPart3.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                        if (s.length() == 4)
                        {
                            pinPart1.setFocusable(false);
                            pinPart2.setFocusable(false);
                            pinPart3.setFocusable(false);

                            Simple.dismissKeyboard(pinPart3);

                            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3)
                    {
                    }

                    @Override
                    public void afterTextChanged(Editable arg0)
                    {
                    }
                });

                pinName = new TextView(Simple.getAppContext());
                pinName.setPadding(0, 16, 0, 0);
                pinName.setTextSize(Simple.getPreferredEditSize());

                ll.addView(pinName);

                dialog.setView(ll);
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(recvPinAction);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(actpincode.length == 3);

                pinPart1.requestFocus();
                Simple.showKeyboard(pinPart1);
            }
        };

        private final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    dialog.cancel();
                }

                if (which == DialogInterface.BUTTON_NEGATIVE)
                {
                    dialog.cancel();
                }
            }
        };

        private final View.OnClickListener randPinAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                generatePincode();

                String newpincode = sharedPrefs.getString(sendPinPref.getKey(), "");

                sendPinPref.setText(newpincode);
                pincode.setText(newpincode);
            }
        };

        private final View.OnClickListener sendPinAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    JSONObject sendPincode = new JSONObject();

                    String pincode = Simple.getSharedPrefString(sendPinPref.getKey());
                    String gcmtoken = CommonStatic.gcm_token;
                    int timeout = Simple.getSharedPrefInt(keyprefix + ".sendpinduration");

                    sendPincode.put("type", "sendPin");
                    sendPincode.put("pincode", pincode);
                    sendPincode.put("gcmtoken", gcmtoken);
                    sendPincode.put("timeout", timeout);

                    CommService.sendMessage(sendPincode);

                    dialog.cancel();
                }
                catch (JSONException ignore)
                {
                }
            }
        };

        private final View.OnClickListener recvPinAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    pinPart1.setFocusable(false);
                    pinPart2.setFocusable(false);
                    pinPart3.setFocusable(false);

                    Simple.dismissKeyboard(pinPart3);

                    String newpincode = pinPart1.getText() + "-"
                            + pinPart2.getText() + "-"
                            + pinPart3.getText();

                    recvPinPref.setText(newpincode);

                    JSONObject recvPincode = new JSONObject();

                    recvPincode.put("type", "requestPin");
                    recvPincode.put("pincode", newpincode);

                    CommService.subscribeMessage(CommunityFragment.this, "responsePin");
                    CommService.subscribeMessage(CommunityFragment.this, "responsePublicKeyXChange");
                    CommService.subscribeMessage(CommunityFragment.this, "responseAESpassXChange");
                    CommService.subscribeMessage(CommunityFragment.this, "responseOwnerIdentity");

                    CommService.sendMessage(recvPincode);
                }
                catch (JSONException ignore)
                {
                }
            }
        };

        private final View.OnClickListener storeContactAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                handler.post(storeContact);

                dialog.cancel();
            }
        };

        private final Runnable storeContact = new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(LOGTAG,"storeContact: =================>" + Json.toPretty(remoteContact));

                RemoteContacts.registerContact(remoteContact);
                registerRemotes(Simple.getAppContext(), true);
            }
        };

        private final Runnable gotContact = new Runnable()
        {
            @Override
            public void run()
            {
                dialog.setTitle("Benutzerdaten erhalten");

                if (pinName != null)
                {
                    String name = "";

                    try
                    {
                        if (remoteContact.has("ownerFirstName"))
                        {
                            name += " " + remoteContact.getString("ownerFirstName");
                        }

                        if (remoteContact.has("ownerGivenName"))
                        {
                            name += " " + remoteContact.getString("ownerGivenName");
                        }

                        if (remoteContact.has("appName"))
                        {
                            name += "\n" + remoteContact.getString("appName");
                        }

                        if (remoteContact.has("devName"))
                        {
                            name += " - " + remoteContact.getString("devName");
                        }
                    }
                    catch (JSONException ignore)
                    {
                    }

                    name = name.trim();
                    if (name.length() == 0) name = "Anonymer Benutzer";

                    dialog.setTitle("Pincode verbunden mit:");
                    pinName.setText(name);
                }

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Kontakt speichern");
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(storeContactAction);
            }
        };

        public void requestKeyExchange(JSONObject message)
        {
            //
            // Setup temporary identity to allow GCM messages.
            //

            String idremote = Json.getString(message, "idremote");
            String gcmtoken = Json.getString(message, "gcmtoken");
            String publickey = CryptUtils.RSAgetPublicKey(Simple.getAnyContext());

            if (gcmtoken == null) gcmtoken = Json.getString(message, "gcmUuid");
            if ((idremote == null) || (gcmtoken == null)) return;

            Log.d(LOGTAG, "requestKeyExchange: " + idremote);

            RemoteContacts.setGCMTokenTemp(idremote, gcmtoken);

            JSONObject requestPublicKeyXChange = new JSONObject();

            Json.put(requestPublicKeyXChange, "type", "requestPublicKeyXChange");
            Json.put(requestPublicKeyXChange, "publicKey", publickey);
            Json.put(requestPublicKeyXChange, "idremote", idremote);

            CommService.sendMessage(requestPublicKeyXChange, true);
        }

        public void onMessageReceived(JSONObject message)
        {
            Log.d(LOGTAG, "onMessageReceived: " + message.toString());

            try
            {
                if (message.has("type") && message.has("status"))
                {
                    String type = message.getString("type");
                    String status = message.getString("status");

                    if (type.equals("responsePin"))
                    {
                        if (status.equals("success"))
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    dialog.setTitle("Pincode gefunden...");
                                }
                            });

                            requestKeyExchange(message);

                            return;
                        }
                        else
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    dialog.setTitle("Pincode unbekannt");

                                    pinPart1.selectAll();
                                    pinPart2.selectAll();
                                    pinPart3.selectAll();

                                    pinPart1.setFocusableInTouchMode(true);
                                    pinPart2.setFocusableInTouchMode(true);
                                    pinPart3.setFocusableInTouchMode(true);

                                    pinPart1.requestFocus();

                                    Simple.showKeyboard(pinPart1);
                                }
                            });
                        }
                    }

                    if (type.equals("responsePublicKeyXChange"))
                    {
                        if (status.equals("success"))
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    dialog.setTitle("Schlüssel getauscht...");
                                }
                            });

                            String remoteIdentity = message.getString("identity");
                            String remotePublicKey = message.getString("publicKey");
                            String passPhrase = UUID.randomUUID().toString();
                            String encoPassPhrase = CryptUtils.RSAEncrypt(remotePublicKey, passPhrase);

                            IdentityManager.put(remoteIdentity, "publicKey", remotePublicKey);
                            IdentityManager.put(remoteIdentity, "passPhrase", passPhrase);

                            JSONObject requestAESpassXChange = new JSONObject();

                            requestAESpassXChange.put("type", "requestAESpassXChange");
                            requestAESpassXChange.put("idremote", remoteIdentity);
                            requestAESpassXChange.put("encodedPassPhrase", encoPassPhrase);

                            CommService.sendMessage(requestAESpassXChange, true);

                            return;
                        }
                    }

                    if (type.equals("responseAESpassXChange"))
                    {
                        if (status.equals("success"))
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    dialog.setTitle("Verschlüsselung aktiviert...");
                                }
                            });

                            String remoteIdentity = message.getString("identity");

                            JSONObject requestOwnerIdentity = new JSONObject();
                            requestOwnerIdentity.put("type", "requestOwnerIdentity");
                            requestOwnerIdentity.put("idremote", remoteIdentity);

                            RemoteContacts.deliverOwnContact(requestOwnerIdentity);

                            CommService.sendEncrypted(requestOwnerIdentity, true);

                            return;
                        }
                    }

                    if (type.equals("responseOwnerIdentity"))
                    {
                        if (status.equals("success"))
                        {
                            remoteContact = message;

                            handler.post(gotContact);
                        }
                    }
                }
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            CommService.unsubscribeAllMessages(CommunityFragment.this);
        }
    }

    //endregion Community preferences

    //region Alertgroup preferences

    public static class AlertgroupFragment extends EnablePreferenceFragment
            implements Preference.OnPreferenceChangeListener
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Assistenz";
            header.iconRes = GlobalConfigs.IconResAlertgroup;
            header.fragment = AlertgroupFragment.class.getName();

            return header;
        }

        public AlertgroupFragment()
        {
            super();

            iconres = GlobalConfigs.IconResAlertgroup;
            keyprefix = "alertgroup";
            masterenable = "Assistenz freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceNumberPreference np;

            //
            // Confirmed connects.
            //

            registerRemotes(context, true);
        }

        private final ArrayList<String> remoteContacts = new ArrayList<>();

        private void registerRemotes(Context context, boolean initial)
        {
            SharedPreferences sp = Simple.getSharedPrefs();

            String groupidentkey = keyprefix + ".groupidentity";
            String grouppasspkey = keyprefix + ".passphrase";
            String grouptypekey  = keyprefix + ".type";
            String groupnamekey  = keyprefix + ".name";

            if (! sp.contains(groupidentkey))
            {
                String groupident = UUID.randomUUID().toString();
                String grouppassp = UUID.randomUUID().toString();

                sp.edit().putString(groupidentkey, groupident).apply();
                sp.edit().putString(grouppasspkey, grouppassp).apply();
            }

            String groupname = "Assistenz";
            String grouptype = "alertcall";
            sp.edit().putString(groupnamekey, groupname).apply();
            sp.edit().putString(grouptypekey, grouptype).apply();

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceEditTextPreference ep;

            String[] prefixText = Simple.getTransArray(R.array.pref_alertgroup_vals);
            String[] prefixVals = Simple.getTransArray(R.array.pref_alertgroup_keys);

            String xpath = "RemoteContacts/identities";
            JSONObject rcs = PersistManager.getXpathJSONObject(xpath);
            if (rcs == null) return;

            Iterator<String> keysIterator = rcs.keys();

            while (keysIterator.hasNext())
            {
                String ident = keysIterator.next();

                if (remoteContacts.contains(ident)) continue;
                remoteContacts.add(ident);

                String name = RemoteContacts.getDisplayName(ident);

                pc = new NicedPreferences.NiceCategoryPreference(context);
                pc.setTitle(name);
                pc.setEnabled(enabled);

                preferences.add(pc);

                lp = new NicedPreferences.NiceListPreference(context);
                lp.setKey(keyprefix + ".member." + ident);
                lp.setEntries(prefixText);
                lp.setEntryValues(prefixVals);
                lp.setDefaultValue("inactive");
                lp.setTitle("Als Mitglied");
                lp.setEnabled(enabled);

                lp.setOnPreferenceChangeListener(this);

                preferences.add(lp);
                if (! initial) getPreferenceScreen().addPreference(lp);

                ep = new NicedPreferences.NiceEditTextPreference(context);
                ep.setKey(keyprefix + ".skypecallback." + ident);
                ep.setTitle("Skype Rückruf");
                ep.setEmptyText("Inaktiv");
                ep.setEnabled(enabled);

                ep.setOnPreferenceChangeListener(this);

                preferences.add(ep);
                if (! initial) getPreferenceScreen().addPreference(ep);
            }
        }

        private final Handler handler = new Handler();

        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            Log.d(LOGTAG, "onPreferenceChange:" + preference.getKey() + "=" + newValue.toString());

            handler.postDelayed(updateAlertGroup, 100);

            if (preference instanceof NicedPreferences.NiceListPreference)
            {
                return ((NicedPreferences.NiceListPreference) preference)
                        .onPreferenceChange(preference, newValue);
            }

            return true;
        }

        public final Runnable updateAlertGroup = new Runnable()
        {
            @Override
            public void run()
            {
                RemoteGroups.updateGroupFromPreferences(keyprefix);
            }
        };
    }

    //endregion Alertgroup preferences

    @SuppressWarnings("WeakerAccess")
    public static class EnablePreferenceFragment extends BasePreferenceFragment
    {
        protected String masterenable;
        protected Drawable icondraw;
        protected int iconres;

        public void registerAll(Context context)
        {
            EnableSwitchPreference sw = new EnableSwitchPreference(context);

            sw.setKey(keyprefix + ".enable");
            sw.setTitle(masterenable);
            sw.setDefaultValue(false);
            sw.setIcon((iconres > 0) ? Simple.getDrawable(iconres) : icondraw);

            preferences.add(sw);
            activekeys.add(sw.getKey());

            enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");
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

    //region BasePreferenceFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class BasePreferenceFragment extends PreferenceFragment
    {
        protected ArrayList<Preference> preferences = new ArrayList<>();
        protected ArrayList<String> activekeys = new ArrayList<>();

        protected String keyprefix;
        protected boolean enabled;

        public void registerAll(Context context)
        {
            preferences.clear();
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

    //endregion BasePreferenceFragment stub
}
