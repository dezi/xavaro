package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CryptUtils;
import de.xavaro.android.common.IdentityManager;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.PreferenceFragments;

public class PreferencesBasics
{
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
            NicedPreferences.NiceListPreference lp;

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Persönliches");
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

            final CharSequence[] siezenText =
                    {
                            "gesiezt werden",
                            "geduzt werden",
                            "Hamburger Sie",
                            "Münchner Du"
                    };

            final CharSequence[] siezenVals = { "siezen", "duzen", "hamsie", "mucdu" };

            lp.setKey("owner.siezen");
            lp.setEntries(siezenText);
            lp.setEntryValues(siezenVals);
            lp.setDefaultValue("siezen");
            lp.setTitle("Anwender möchte");

            preferences.add(lp);
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
            header.title = "Administrator";
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

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Abgeschlossenheit");
            preferences.add(pc);

            et = new NicedPreferences.NiceEditTextPreference(context);

            et.setKey("admin.home.button");
            et.setTitle("Anwendung auf dem Home-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultHomeLabel(context));
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
            et.setTitle("Anwendung auf dem Assistenz-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultAssistLabel(context));
            et.setOnclick(selectAssist);

            if (sharedPrefs.getString(et.getKey(),"").equals(DitUndDat.DefaultApps.getAppLable(context)))
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
            cb.setTitle("Anwendung auf dem Menü-Button");

            preferences.add(cb);
        }

        private final Runnable selectHome = new Runnable()
        {
            @Override
            public void run()
            {
                DitUndDat.DefaultApps.setDefaultHome(context);
            }
        };

        private final Runnable selectAssist = new Runnable()
        {
            @Override
            public void run()
            {
                DitUndDat.DefaultApps.setDefaultAssist(context);
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

        private final ArrayList<String> remoteContacts = new ArrayList<>();

        private NicedPreferences.NiceEditTextPreference sendPinPref;
        private NicedPreferences.NiceEditTextPreference recvPinPref;

        private JSONObject remoteContact;
        private AlertDialog dialog;
        private TextView pincode;

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;

            //
            // Connect.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Verbindungen herstellen");
            preferences.add(pc);

            //
            // Send pin code.
            //

            sendPinPref = new NicedPreferences.NiceEditTextPreference(context);

            sendPinPref.setKey(keyprefix + ".sendpin");
            sendPinPref.setDefaultValue("1234-5678-0000");
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
                    "2 Tage",
                    "Permanent"
            };

            CharSequence[] durationVals = {
                    "5",
                    "60",
                    "640",
                    "1440",
                    "2880",
                    "0"
            };

            lp.setKey(keyprefix + ".sendpinduration");
            lp.setEntries(durationText);
            lp.setEntryValues(durationVals);
            lp.setDefaultValue("5");
            lp.setTitle("Pincode freigeben für");

            preferences.add(lp);

            //
            // Receive pin code.
            //

            recvPinPref = new NicedPreferences.NiceEditTextPreference(context);

            recvPinPref.setKey(keyprefix + ".recvpin");
            recvPinPref.setDefaultValue("1234-5678-0000");
            recvPinPref.setTitle("Pincode verbinden");
            recvPinPref.setOnclick(recvPinDialog);

            preferences.add(recvPinPref);

            //
            // Confirmed connects.
            //

            registerRemotes(context, true);
        }

        private void registerRemotes(Context context, boolean initial)
        {
            String xpath = "RemoteContacts/identities";
            JSONObject rcs = PersistManager.getXpathJSONObject(xpath);
            if (rcs == null) return;

            Iterator<String> keysIterator = rcs.keys();

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceSwitchPreference sp;

            while (keysIterator.hasNext())
            {
                try
                {
                    String ident = keysIterator.next();

                    if (remoteContacts.contains(ident)) continue;
                    remoteContacts.add(ident);

                    String prefkey = keyprefix + ".remote." + ident;
                    JSONObject rc = rcs.getJSONObject(ident);

                    String name = "";
                    if (rc.has("ownerFirstName")) name += " " + rc.getString("ownerFirstName");
                    if (rc.has("ownerGivenName")) name += " " + rc.getString("ownerGivenName");
                    if (rc.has("appName")) name += " (" + rc.getString("appName") + ")";
                    name = name.trim();
                    if (name.length() == 0) name = "Anonymer Benutzer";

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setKey(prefkey);
                    pc.setDefaultValue(true);
                    pc.setTitle(name);

                    preferences.add(pc);
                    if (! initial) getPreferenceScreen().addPreference(pc);

                    sp = new NicedPreferences.NiceSwitchPreference(context);
                    sp.setKey(prefkey + ".isremoteadmin");
                    sp.setTitle("Remote Administration");

                    preferences.add(sp);
                    if (! initial) getPreferenceScreen().addPreference(sp);
                }
                catch (JSONException ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }
        }

        public final Runnable sendPinDialog = new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(sendPinPref.getTitle());

                builder.setPositiveButton("Abbrechen", clickListener);
                builder.setNegativeButton("Neuer Code", clickListener);
                builder.setNeutralButton("Jetzt freigeben", clickListener);

                dialog = builder.create();

                pincode = new TextView(context);
                pincode.setTextSize(24f);
                pincode.setPadding(40, 24, 0, 0);
                pincode.setText(sharedPrefs.getString(sendPinPref.getKey(), ""));

                dialog.setView(pincode);
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(sendPinAction);
            }
        };

        public final Runnable recvPinDialog = new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(recvPinPref.getTitle());

                builder.setPositiveButton("Abbrechen", clickListener);
                builder.setNeutralButton("Jetzt verbinden", clickListener);

                dialog = builder.create();

                pincode = new TextView(context);
                pincode.setTextSize(24f);
                pincode.setPadding(40, 24, 0, 0);
                pincode.setText(sharedPrefs.getString(sendPinPref.getKey(), ""));

                dialog.setView(pincode);
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(recvPinAction);
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

        private final View.OnClickListener sendPinAction = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    JSONObject sendPincode = new JSONObject();

                    sendPincode.put("type", "sendPin");
                    sendPincode.put("pincode", sharedPrefs.getString(sendPinPref.getKey(), ""));

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
                    JSONObject recvPincode = new JSONObject();

                    recvPincode.put("type", "requestPin");
                    recvPincode.put("pincode", sharedPrefs.getString(sendPinPref.getKey(), ""));

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
                try
                {
                    JSONObject rc = remoteContact;

                    String ident = rc.getString("identity");
                    String appna = rc.has("appName") ? rc.getString("appName") : "";
                    String fname = rc.has("ownerFirstName") ? rc.getString("ownerFirstName") : "";
                    String lname = rc.has("ownerGivenName") ? rc.getString("ownerGivenName") : "";

                    String xpath = "RemoteContacts/identities/" + ident;
                    JSONObject recontact = PersistManager.getXpathJSONObject(xpath);
                    if (recontact == null) recontact = new JSONObject();

                    recontact.put("appName", appna);
                    recontact.put("ownerFirstName", fname);
                    recontact.put("ownerGivenName", lname);

                    PersistManager.putXpath(xpath, recontact);
                    PersistManager.flush();

                    registerRemotes(context, false);
                }
                catch (JSONException ex)
                {
                    OopsService.log(LOGTAG, ex);
                }

            }
        };

        private final Runnable gotContact = new Runnable()
        {
            @Override
            public void run()
            {
                dialog.setTitle("Benutzerdaten erhalten.");

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
                        name += " (" + remoteContact.getString("appName") + ")";
                    }
                }
                catch (JSONException ignore)
                {
                }

                name = name.trim();
                if (name.length() == 0) name = "Anonymer Benutzer";

                pincode.setText(name);

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Als Kontakt übernehmen");
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(storeContactAction);
            }
        };

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

                            String remoteIdentity = message.getString("idremote");

                            JSONObject requestPublicKeyXChange = new JSONObject();

                            requestPublicKeyXChange.put("type", "requestPublicKeyXChange");
                            requestPublicKeyXChange.put("publicKey", CryptUtils.RSAgetPublicKey(context));
                            requestPublicKeyXChange.put("idremote", remoteIdentity);

                            CommService.sendMessage(requestPublicKeyXChange);

                            return;
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
                                    dialog.setTitle("Öffentliche Schlüssel getauscht...");
                                }
                            });

                            String remoteIdentity = message.getString("identity");
                            String remotePublicKey = message.getString("publicKey");
                            String passPhrase = UUID.randomUUID().toString();
                            String encoPassPhrase = CryptUtils.RSAEncrypt(remotePublicKey, passPhrase);

                            IdentityManager.getInstance().put(remoteIdentity, "publicKey", remotePublicKey);
                            IdentityManager.getInstance().put(remoteIdentity, "passPhrase", passPhrase);

                            JSONObject requestAESpassXChange = new JSONObject();

                            requestAESpassXChange.put("type", "requestAESpassXChange");
                            requestAESpassXChange.put("idremote", remoteIdentity);
                            requestAESpassXChange.put("encodedPassPhrase", encoPassPhrase);

                            CommService.sendMessage(requestAESpassXChange);

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

                            CommService.sendEncrypted(requestOwnerIdentity);

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
}
