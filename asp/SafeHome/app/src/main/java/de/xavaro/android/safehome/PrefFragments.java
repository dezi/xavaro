package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class PrefFragments
{
    private static final String LOGTAG = PrefFragments.class.getSimpleName();

    private static SharedPreferences sharedPrefs;

    public static void initialize(Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region Phone preferences

    public static class PhoneFragment extends ContactsFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Telefonie";
            header.iconRes = GlobalConfigs.IconResPhoneApp;
            header.fragment = PhoneFragment.class.getName();

            return header;
        }

        public PhoneFragment()
        {
            super();

            iconres = GlobalConfigs.IconResPhoneApp;
            keyprefix = "phone";
            masterenable = "Telefonie freischalten";
            isPhone = true;
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

            iconres = GlobalConfigs.IconResSkype;
            keyprefix = "skype";
            masterenable = "Skype freischalten";
            isSkype = true;
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

            iconres = GlobalConfigs.IconResWhatsApp;
            keyprefix = "whatsapp";
            masterenable = "WhatsApp freischalten";
            isWhatsApp = true;
        }
    }

    //endregion WhatsApp preferences

    //region Contacts preferences stub

    public static class ContactsFragment extends EnablePreferenceFragment
    {
        protected boolean isPhone;
        protected boolean isSkype;
        protected boolean isWhatsApp;

        public void registerAll(Context context)
        {
            super.registerAll(context);

            boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable", false);

            CharSequence[] entries = {
                    "Nicht aktiviert",
                    "Home-Bildschirm",
                    "App-Verzeichnis",
                    "Kontakte-Verzeichnis"};

            CharSequence[] evalues = { "inact", "home", "appdir", "comdir" };

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

                        if (!sharedPrefs.contains(key))
                        {
                            sharedPrefs.edit().putString(key, "inact").apply();
                        }
                    }
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

    public static class EnablePreferenceFragment extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();

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
            header.title = "Datenschutz und Sicherheit";
            header.iconRes = GlobalConfigs.IconResFireWall;
            header.fragment = PrefFragments.SafetyFragment.class.getName();

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
            header.fragment = PrefFragments.DomainsFragment.class.getName();

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
                        if (fault.equals("nofaults" )) text += "\n– Problem: Eigentlich keines.";

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
            header.title = "Tageszeitungen";
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
            masterenable = "Online Tageszeitungen freischalten";
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

    public static class JSONConfigFragment extends PreferenceFragment
    {
        private final ArrayList<Preference> preferences = new ArrayList<>();

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

                boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable",false);

                loadGlobalConfig(context);

                CheckBoxPreference cb;
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
                        NicePreferenceCategory pc = new NicePreferenceCategory(context);

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

        @Override
        public void setEntries(CharSequence[] entries)
        {
            super.setEntries(entries);
            this.entries = entries;
        }

        @Override
        public void setEntryValues(CharSequence[] values)
        {
            super.setEntryValues(values);
            this.values = values;
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

    //endregion Niced preferences
}
