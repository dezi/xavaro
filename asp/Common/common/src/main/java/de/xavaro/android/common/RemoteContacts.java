package de.xavaro.android.common;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteContacts
{
    private static final String LOGTAG = RemoteContacts.class.getSimpleName();

    public static boolean registerContact(JSONObject rc)
    {
        try
        {
            String ident = rc.getString("identity");
            String appna = rc.has("appName") ? rc.getString("appName") : "";
            String devna = rc.has("devName") ? rc.getString("devName") : "";
            String macad = rc.has("macAddr") ? rc.getString("macAddr") : "";
            String fname = rc.has("ownerFirstName") ? rc.getString("ownerFirstName") : "";
            String lname = rc.has("ownerGivenName") ? rc.getString("ownerGivenName") : "";

            String xpath = "RemoteContacts/identities/" + ident;
            JSONObject recontact = PersistManager.getXpathJSONObject(xpath);
            if (recontact == null) recontact = new JSONObject();

            recontact.put("appName", appna);
            recontact.put("devName", devna);
            recontact.put("macAddr", macad);
            recontact.put("ownerFirstName", fname);
            recontact.put("ownerGivenName", lname);

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
    public static String getDisplayName(String ident)
    {
        String xpath = "RemoteContacts/identities/" + ident;
        JSONObject rc = PersistManager.getXpathJSONObject(xpath);

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
