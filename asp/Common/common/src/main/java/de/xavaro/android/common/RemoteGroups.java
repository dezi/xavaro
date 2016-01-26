package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class RemoteGroups
{
    private static final String LOGTAG = RemoteGroups.class.getSimpleName();

    private static final String xPathRoot = "RemoteGroups/groupidentities";

    public static void exportGroup(String groupprefix)
    {
        Log.d(LOGTAG, "exportGroup");

        try
        {
            String groupidentity = Simple.getPreferenceString(groupprefix + ".groupidentity");

            JSONObject group = getGroup(groupidentity);

            if (group == null)
            {
                group = new JSONObject();

                group.put("groupidentity", groupidentity);
                group.put("type", Simple.getPreferenceString(groupprefix + ".type"));
                group.put("owner", SystemIdentity.getIdentity());
            }

            group.put("passphrase", Simple.getPreferenceString(groupprefix + ".passphrase"));
            group.put("name", Simple.getPreferenceString(groupprefix + ".name"));

            JSONArray jmembers = new JSONArray();

            //
            // We add ourselves first and can never exit group.
            //

            JSONObject ourselves = new JSONObject();
            RemoteContacts.deliverOwnContact(ourselves);

            ourselves.put("identity", SystemIdentity.getIdentity());
            ourselves.put("memberstatus", "active");

            jmembers.put(ourselves);

            String keyprefix = groupprefix + ".member.";
            Map<String, Object> members = Simple.getAllPreferences(keyprefix);

            Log.d(LOGTAG, "exportGroup: " + members.size());

            for (Map.Entry<String, ?> entry : members.entrySet())
            {
                String memberpref = entry.getKey();
                if (! memberpref.startsWith(keyprefix)) continue;

                String memberident = memberpref.substring(keyprefix.length());

                if (memberident.equals(SystemIdentity.getIdentity()))
                {
                    //
                    // We do not add ourselves here whatever.
                    //

                    continue;
                }

                String memberstatus = (String) entry.getValue();

                Log.d(LOGTAG, "exportGroup: " + memberident + "=" + memberstatus);

                JSONObject minfo = RemoteContacts.getContact(memberident);
                if (minfo == null) continue;

                minfo.put("identity", memberident);
                minfo.put("memberstatus", memberstatus);

                jmembers.put(minfo);
            }

            group.put("members", jmembers);

            putGroup(group);

            Log.d(LOGTAG,group.toString(2));
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static JSONObject getGroup(String groupidentity)
    {
        String xpath = xPathRoot + "/"  + groupidentity;
        return PersistManager.getXpathJSONObject(xpath);
    }

    public static boolean putGroup(JSONObject remotegroup)
    {
        try
        {
            String groupidentity = remotegroup.getString("groupidentity");
            String xpath = xPathRoot + "/"  + groupidentity;
            PersistManager.putXpath(xpath, remotegroup);
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
