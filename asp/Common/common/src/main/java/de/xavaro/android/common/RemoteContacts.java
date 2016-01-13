package de.xavaro.android.common;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteContacts
{
    private static final String LOGTAG = RemoteContacts.class.getSimpleName();

    public static String getDisplayName(String ident)
    {
        String xpath = "RemoteContacts/identities/" + ident;
        JSONObject contact = PersistManager.getXpathJSONObject(xpath);

        if (contact != null)
        {
            try
            {
                String name = "";

                if (contact.has("ownerFirstName")) name += " " + contact.getString("ownerFirstName");
                if (contact.has("ownerGivenName")) name += " " + contact.getString("ownerGivenName");

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
