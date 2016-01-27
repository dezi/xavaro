package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

public class SettingsFragments
{
    private static final String LOGTAG = SettingsFragments.class.getSimpleName();

    private static SharedPreferences sharedPrefs;

    public static void initialize(Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

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

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            //
            // Bluetooth device selection preference
            //

            NicedPreferences.NiceCategoryPreference pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("BlueTooth Geräteauswahl");
            preferences.add(pc);

            devicePref = new NicedPreferences.NiceListPreference(context);

            devicePref.setKey(keyprefix + ".device");

            recentText.add("Nicht gesetzt");
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

    //region Xavaro communication preferences

    public static class XavaroFragment extends EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Xavaro";
            header.iconRes = GlobalConfigs.IconResXavaro;
            header.fragment = XavaroFragment.class.getName();

            return header;
        }

        protected final CharSequence[] sendtoText= {
                "Nicht aktiviert",
                "Home-Bildschirm",
                "App-Verzeichnis",
                "Kontakte-Verzeichnis"};

        protected final CharSequence[] sendtoVals = {
                "inact",
                "home",
                "appdir",
                "comdir" };

        public XavaroFragment()
        {
            super();

            iconres = GlobalConfigs.IconResXavaro;
            keyprefix = "xavaro";
            masterenable = "Xavaro Kommunikation freischalten";
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
            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference cb;

            //
            // Remote user contacts.
            //

            String usersxpath = "RemoteContacts/identities";
            JSONObject rcs = PersistManager.getXpathJSONObject(usersxpath);

            if (rcs != null)
            {
                Iterator<String> keysIterator = rcs.keys();

                while (keysIterator.hasNext())
                {
                    String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String prefkey = keyprefix + ".remote.users";

                    String name = RemoteContacts.getDisplayName(ident);

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(name);

                    preferences.add(pc);
                    if (!initial) getPreferenceScreen().addPreference(pc);

                    cb = new NicedPreferences.NiceListPreference(context);
                    cb.setKey(prefkey + ".chat." + ident);
                    cb.setEntries(sendtoText);
                    cb.setEntryValues(sendtoVals);
                    cb.setDefaultValue("inact");
                    cb.setTitle("Nachricht");

                    preferences.add(cb);
                    if (!initial) getPreferenceScreen().addPreference(cb);
                }
            }

            //
            // Remote group contacts.
            //

            String groupsxpath = "RemoteGroups/groupidentities";
            JSONObject rgs = PersistManager.getXpathJSONObject(groupsxpath);

            if (rgs != null)
            {
                Iterator<String> keysIterator = rgs.keys();

                while (keysIterator.hasNext())
                {
                    String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String prefkey = keyprefix + ".remote.groups";

                    String name = RemoteGroups.getDisplayName(ident);

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(name);

                    preferences.add(pc);
                    if (!initial) getPreferenceScreen().addPreference(pc);

                    cb = new NicedPreferences.NiceListPreference(context);
                    cb.setKey(prefkey + ".chat." + ident);
                    cb.setEntries(sendtoText);
                    cb.setEntryValues(sendtoVals);
                    cb.setDefaultValue("inact");
                    cb.setTitle("Nachricht");

                    preferences.add(cb);
                    if (!initial) getPreferenceScreen().addPreference(cb);
                }
            }
        }
    }

    //endregion Xavaro remote preferences

    //region Phone preferences

    public static class PhoneFragment extends ContactsFragmentStub
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

    //endregion Phone preferences

    //region Skype preferences

    public static class SkypeFragment extends ContactsFragmentStub
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
            installtext = "Skype App";
            installpack = CommonConfigs.packageSkype;
        }
    }

    //endregion Skype preferences

    //region WhatsApp preferences

    public static class WhatsAppFragment extends ContactsFragmentStub
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
            installtext = "WhatsApp App";
            installpack = CommonConfigs.packageWhatsApp;
        }
    }

    //endregion WhatsApp preferences

    //region Contacts preferences stub

    @SuppressWarnings("WeakerAccess")
    public static class ContactsFragmentStub extends EnablePreferenceFragment
    {
        protected boolean isPhone;
        protected boolean isSkype;
        protected boolean isWhatsApp;
        protected String installtext;
        protected String installpack;

        protected final CharSequence[] destText = {
                "Nicht aktiviert",
                "Home-Bildschirm",
                "App-Verzeichnis",
                "Kontakte-Verzeichnis"};

        protected final CharSequence[] destVals = {
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
                NicedPreferences.NiceListPreference ip = new NicedPreferences.NiceListPreference(context);

                boolean installed = Simple.isAppInstalled(installpack);

                ip.setEntries(installText);
                ip.setEntryValues(installVals);
                ip.setKey(keyprefix + ".installed");
                ip.setTitle(installtext);
                ip.setEnabled(enabled);

                //
                // This is nice about Java. No clue how it is done!
                //

                final String installName = installpack;

                ip.setOnclick(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Simple.installAppFromPlaystore(installName);
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

                    NicedPreferences.NiceCategoryPreference nc = new NicedPreferences.NiceCategoryPreference(context);
                    nc.setTitle(cattitle);
                    nc.setEnabled(enabled);
                    preferences.add(nc);

                    if (chatphone != null)
                    {
                        String key = keyprefix + ".chat." + chatphone;
                        NicedPreferences.NiceListPreference cb = new NicedPreferences.NiceListPreference(context);

                        cb.setEntries(destText);
                        cb.setEntryValues(destVals);
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
                        NicedPreferences.NiceListPreference cb = new NicedPreferences.NiceListPreference(context);

                        cb.setEntries(destText);
                        cb.setEntryValues(destVals);
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
                        NicedPreferences.NiceListPreference cb = new NicedPreferences.NiceListPreference(context);

                        cb.setEntries(destText);
                        cb.setEntryValues(destVals);
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
                        NicedPreferences.NiceListPreference cb = new NicedPreferences.NiceListPreference(context);

                        cb.setEntries(destText);
                        cb.setEntryValues(destVals);
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

        private static class SafetyCBPreference extends NicedPreferences.NiceCheckboxPreference
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
                OopsService.log(LOGTAG, ex);
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

        private static class DomainsCBPreference extends NicedPreferences.NiceCheckboxPreference
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
                    text += "\n";

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

    //region App discounter preferences

    public static class AppsDiscounterFragment extends JSONConfigFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Discounter";
            header.iconRes = GlobalConfigs.IconResAppsDiscounter;
            header.fragment = AppsDiscounterFragment.class.getName();

            return header;
        }

        public AppsDiscounterFragment()
        {
            super();

            root = "apps/discounter";
            isApps = true;
            jsonres = R.raw.default_apps;
            iconres = GlobalConfigs.IconResAppsDiscounter;
            keyprefix = "apps.discounter";
            masterenable = "Discounter Apps freischalten";
        }
    }

    //endregion App discounters preferences

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

        protected boolean isApps;

        private void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, jsonres);

                    if (ctemp != null)
                    {
                        String[] parts = root.split("/");

                        for (String part : parts)
                        {
                            ctemp = ctemp.getJSONObject(part);
                        }

                        globalConfig = ctemp;
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

                NicedPreferences.NiceCategoryPreference pc;
                NicedPreferences.NiceScorePreference cb;

                loadGlobalConfig(context);

                if (globalConfig == null) return;

                Iterator<String> keysIterator = globalConfig.keys();

                String key;
                String label;
                String iconurl;
                String summary;
                int iconres;
                Bitmap thumbnail;
                Drawable drawable;
                String apkname;
                int score;

                while (keysIterator.hasNext())
                {
                    String website = keysIterator.next();

                    JSONObject webitem = globalConfig.getJSONObject(website);

                    score = -1;
                    iconres = 0;
                    summary = null;
                    apkname = null;
                    drawable = null;

                    if (isApps)
                    {
                        label = webitem.getString("what");

                        JSONArray sumarray = webitem.getJSONArray("info");

                        summary = "";

                        for (int inx = 0; inx < sumarray.length(); inx++)
                        {
                            summary += sumarray.getString(inx);
                        }

                        thumbnail = CacheManager.getIconFromAppStore(context, website);
                        drawable = new BitmapDrawable(context.getResources(),thumbnail);
                        apkname = website;
                        score = Integer.parseInt(webitem.getString("score"));
                    }
                    else
                    {
                        label = webitem.getString("label");
                        iconurl = webitem.getString("icon");
                        thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                        drawable = new BitmapDrawable(context.getResources(), thumbnail);
                    }

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

                        cb = new NicedPreferences.NiceScorePreference(context);

                        cb.setKey(key);
                        cb.setTitle(label);
                        cb.setEnabled(enabled);
                        cb.setScore(score);
                        cb.setAPKName(apkname);

                        if (iconres != 0) cb.setIcon(iconres);
                        if (drawable != null) cb.setIcon(drawable);
                        if (summary != null) cb.setSummary(summary);

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
                        pc = new NicedPreferences.NiceCategoryPreference(context);

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

                            cb = new NicedPreferences.NiceScorePreference(context);

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

                    if (pref instanceof NicedPreferences.NiceCheckboxPreference)
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

    //region EnablePreferenceFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class EnablePreferenceFragment extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();
        protected final ArrayList<String> activekeys = new ArrayList<>();

        protected String keyprefix;
        protected String masterenable;
        protected boolean enabled;

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

            enabled = DitUndDat.SharedPrefs.sharedPrefs.getBoolean(keyprefix + ".enable", false);
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
}
