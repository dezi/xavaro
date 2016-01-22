package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteContacts
{
    private static final String LOGTAG = RemoteContacts.class.getSimpleName();

    private static final String xPathRoot = "RemoteContacts/identities";

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
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static boolean registerContact(JSONObject rc)
    {
        try
        {
            String ident = rc.getString("identity");
            String xpath = xPathRoot + "/"  + ident;
            JSONObject recontact = PersistManager.getXpathJSONObject(xpath);
            if (recontact == null) recontact = new JSONObject();

            recontact.put("appName", rc.getString("appName"));
            recontact.put("devName", rc.getString("devName"));
            recontact.put("macAddr", rc.getString("macAddr"));
            recontact.put("gcmUuid", rc.getString("gcmUuid"));
            recontact.put("ownerFirstName", rc.getString("ownerFirstName"));
            recontact.put("ownerGivenName", rc.getString("ownerGivenName"));

            PersistManager.putXpath(xpath, recontact);
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
    public static String getGCMToken(String ident)
    {
        JSONObject rc = PersistManager.getXpathJSONObject(xPathRoot + "/" + ident);

        if (rc != null)
        {
            try
            {
                if (rc.has("gcmUuid")) return rc.getString("gcmUuid");
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return null;
    }

    public static String getDisplayName(String ident)
    {
        JSONObject rc = PersistManager.getXpathJSONObject(xPathRoot + "/" + ident);

        if (rc != null)
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
}