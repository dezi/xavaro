package de.xavaro.android.common;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteGroups
{
    private static final String LOGTAG = RemoteGroups.class.getSimpleName();

    private static final String xPathRoot = "RemoteGroups/identities";

    public static void exportAlertGroup()
    {

    }

    public static boolean putGroup(JSONObject rc)
    {
        try
        {
            String ident = rc.getString("identity");
            String xpath = xPathRoot + "/"  + ident;
            JSONObject regroup = PersistManager.getXpathJSONObject(xpath);
            if (regroup == null) regroup = new JSONObject();

            //todo

            PersistManager.putXpath(xpath, regroup);
            PersistManager.flush();

            return true;
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }
}
