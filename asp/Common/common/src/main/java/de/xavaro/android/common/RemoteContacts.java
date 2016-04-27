package de.xavaro.android.common;

import android.preference.Preference;
import android.support.annotation.Nullable;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RemoteContacts
{
    private static final String LOGTAG = RemoteContacts.class.getSimpleName();

    private static final String xPathRoot = "RemoteContacts/identities";
    private static final Map<String, String> tempGCMTokens = new HashMap<>();

    public static void deliverOwnContact(JSONObject rc)
    {
        SharedPreferences sp = Simple.getSharedPrefs();

        try
        {
            rc.put("appName", Simple.getAppName());
            rc.put("devName", Simple.getDeviceName());
            rc.put("macAddr", Simple.getMacAddress());
            rc.put("gcmUuid", Simple.getGCMToken());
            rc.put("ownerFirstName", sp.getString("owner.firstname", null));
            rc.put("ownerGivenName", sp.getString("owner.givenname", null));
            rc.put("ownerNickName", sp.getString("owner.nickname", null));
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static boolean registerContact(JSONObject rc)
    {
        String ident = Json.getString(rc, "identity");

        if (ident != null)
        {
            String xpath = xPathRoot + "/"  + ident;
            JSONObject recontact = PersistManager.getXpathJSONObject(xpath);
            if (recontact == null) recontact = new JSONObject();

            Json.copy(recontact, "appName", rc);
            Json.copy(recontact, "devName", rc);
            Json.copy(recontact, "macAddr", rc);
            Json.copy(recontact, "gcmUuid", rc);
            Json.copy(recontact, "ownerFirstName", rc);
            Json.copy(recontact, "ownerGivenName", rc);
            Json.copy(recontact, "ownerNickName", rc);

            PersistManager.putXpath(xpath, recontact);
            PersistManager.flush();

            return true;
        }

        return false;
    }

    public static boolean isContact(String ident)
    {
        return (getContact(ident) != null);
    }

    public static boolean putContact(JSONObject rc)
    {
        try
        {
            String ident = rc.getString("identity");
            String xpath = xPathRoot + "/" + ident;

            PersistManager.putXpath(xpath, rc);
            PersistManager.flush();

            return true;
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    @Nullable
    public static JSONObject getContact(String ident)
    {
        return PersistManager.getXpathJSONObject(xPathRoot + "/" + ident);
    }

    public static void setGCMTokenTemp(String ident, String gcmUuid)
    {
        if ((ident != null) && (gcmUuid != null))
        {
            tempGCMTokens.put(ident, gcmUuid);
        }
    }

    @Nullable
    public static String getGCMToken(String ident)
    {
        JSONObject rc = PersistManager.getXpathJSONObject(xPathRoot + "/" + ident);

        if (rc != null) return Json.getString(rc, "gcmUuid");

        if (tempGCMTokens.containsKey(ident))  return tempGCMTokens.get(ident);

        return null;
    }

    public static JSONObject getAllContacts()
    {
        return PersistManager.getXpathJSONObject(xPathRoot);
    }

    public static String getDisplayName(String ident)
    {
        JSONObject rc = PersistManager.getXpathJSONObject(xPathRoot + "/" + ident);

        if (rc == null)
        {
            //
            // Check if identity is our own. We could be member
            // in a group which requests the name.
            //

            if (Simple.equals(ident, SystemIdentity.getIdentity()))
            {
                SharedPreferences sp = Simple.getSharedPrefs();

                String name = "";

                name += " " + sp.getString("owner.firstname", "");
                name += " " + sp.getString("owner.givenname", "");
                name = name.trim();

                if (name.length() > 0) return name;
            }
        }
        else
        {
            try
            {
                String name = "";

                SharedPreferences sp = Simple.getSharedPrefs();
                String nickpref = "community.remote." + ident + ".nickname";

                if (sp.contains(nickpref) && ! sp.getString(nickpref, "").equals(""))
                {
                    name = sp.getString(nickpref, "");
                }
                else
                {
                    if (rc.has("ownerFirstName")) name += " " + rc.getString("ownerFirstName");
                    if (rc.has("ownerGivenName")) name += " " + rc.getString("ownerGivenName");
                }

                return name.trim();
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return "Unbekannt";
    }

    public static void removeContactFinally(String identity)
    {
        if (identity == null) return;

        Log.w(LOGTAG, "removeFinally: " + identity);

        //
        // Remove from remote groups.
        //

        RemoteGroups.removeMemberFinally(identity);

        //
        // Remove from preferences.
        //

        Map<String, ?> prefs = Simple.getSharedPrefs().getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet())
        {
            if (entry.getKey().contains(identity))
            {
                Simple.removeSharedPref(entry.getKey());

                Log.w(LOGTAG, "removeFinally: pref=" + entry.getKey());
            }
        }

        //
        // Remove from remote contacts.
        //

        PersistManager.delXpath(xPathRoot + "/" + identity);
        PersistManager.flush();

        //
        // Remove from identities.
        //

        IdentityManager.removeIdentityFinally(identity);

        //
        // Remove profile image.
        //

        ProfileImages.removeXavaroProfileImageFile(identity);
    }
}
