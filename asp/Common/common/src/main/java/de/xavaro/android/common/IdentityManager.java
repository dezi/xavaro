package de.xavaro.android.common;

import org.json.JSONException;
import org.json.JSONObject;

public class IdentityManager
{
    private static final String LOGTAG = IdentityManager.class.getSimpleName();

    //region Static singleton methods.

    private static IdentityManager instance;

    public static IdentityManager getInstance()
    {
        if (instance == null) instance = new IdentityManager();

        return instance;
    }

    public JSONObject getIdentity(String identity)
    {
        String xpath = "IdentityManager/identities/" + identity;

        JSONObject ident = PersistManager.getXpathJSONObject(xpath);

        return (ident != null) ? ident : new JSONObject();
    }

    public void putIdentity(String identity, JSONObject ident)
    {
        String xpath = "IdentityManager/identities/" + identity;

        PersistManager.putXpath(xpath, ident);
        PersistManager.flush();
    }

    public void put(String identity, String key, String value)
    {
        try
        {
            synchronized (LOGTAG)
            {
                JSONObject ident = getIdentity(identity);

                ident.put(key, value);

                putIdentity(identity, ident);
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}