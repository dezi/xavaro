package de.xavaro.android.common;

import android.support.annotation.Nullable;

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
        PersistManager.delXpath(xPathRoot);
        PersistManager.flush();

        Log.d(LOGTAG,"exportGroup: " + groupprefix);

            String groupidentity = Simple.getPreferenceString(groupprefix + ".groupidentity");

            JSONObject group = getGroup(groupidentity);

            if (group == null)
            {
                group = new JSONObject();

                Json.put(group, "groupidentity", groupidentity);
                Json.put(group, "type", Simple.getPreferenceString(groupprefix + ".type"));
                Json.put(group, "owner", SystemIdentity.getIdentity());
            }

            Json.put(group, "passphrase", Simple.getPreferenceString(groupprefix + ".passphrase"));
            Json.put(group, "name", Simple.getPreferenceString(groupprefix + ".name"));

            JSONArray jmembers = new JSONArray();

            //
            // We add ourselves first and can never exit group.
            //

            JSONObject ourselves = new JSONObject();
            RemoteContacts.deliverOwnContact(ourselves);

            Json.put(ourselves, "identity", SystemIdentity.getIdentity());
            Json.put(ourselves, "status", "active");
            Json.put(jmembers, ourselves);

            String keyprefix = groupprefix + ".member.";
            Map<String, Object> members = Simple.getAllPreferences(keyprefix);

            for (Map.Entry<String, ?> entry : members.entrySet())
            {
                String memberpref = entry.getKey();
                if (! memberpref.startsWith(keyprefix)) continue;

                String memberident = memberpref.substring(keyprefix.length());
                String memberstatus = (String) entry.getValue();

                if (memberident.equals(SystemIdentity.getIdentity()))
                {
                    //
                    // We do not add ourselves here whatever.
                    //

                    continue;
                }

                JSONObject minfo = RemoteContacts.getContact(memberident);
                if (minfo == null) continue;

                Json.put(minfo, "identity", memberident);
                Json.put(minfo, "status", memberstatus);
                Json.put(jmembers, minfo);
            }

            Json.put(group, "members", jmembers);

            putGroup(group);
            publishGroup(groupidentity);
    }

    public static void publishGroup(String groupidentity)
    {
        Log.d(LOGTAG,"publishGroup: " + groupidentity);

        JSONObject rg = getGroup(groupidentity);
        if (rg == null) return;

        Log.d(LOGTAG,"publishGroup: found: " + groupidentity);

        //
        // In the first step we only send the group header
        // information with an empty members list for update.
        //

        JSONObject remotegroup = Json.clone(rg);
        JSONArray members = Json.getArray(remotegroup,"members");
        Json.remove(remotegroup, "members");

        JSONObject groupStatusUpdate = new JSONObject();

        Json.put(groupStatusUpdate, "type", "groupStatusUpdate");
        Json.put(groupStatusUpdate, "idremote", groupidentity);
        Json.put(groupStatusUpdate, "remotegroup", remotegroup);

        Log.d(LOGTAG, "sta:" + Json.toPretty(remotegroup));

        CommService.sendEncrypted(groupStatusUpdate, true);

        //
        // In the second step we send each member separately.
        //

        for (int inx = 0; inx < members.length(); inx++)
        {
            remotegroup = new JSONObject();
            Json.put(remotegroup, "groupidentity", groupidentity);

            JSONObject member = Json.getObject(members, inx);
            Json.put(remotegroup, "member", member);

            JSONObject groupMemberUpdate = new JSONObject();

            Json.put(groupMemberUpdate, "type", "groupMemberUpdate");
            Json.put(groupMemberUpdate, "idremote", groupidentity);
            Json.put(groupMemberUpdate, "remotegroup", remotegroup);

            Log.d(LOGTAG,"mem:" + Json.toPretty(remotegroup));

            CommService.sendEncrypted(groupMemberUpdate, true);
        }
    }

    public static boolean isGroup(String groupidentity)
    {
        return (getGroup(groupidentity) != null);
    }

    public static JSONObject getGroup(String groupidentity)
    {
        String xpath = xPathRoot + "/"  + groupidentity;
        return PersistManager.getXpathJSONObject(xpath);
    }

    public static boolean putGroup(JSONObject remotegroup)
    {
        String groupidentity = Json.getString(remotegroup, "groupidentity");
        if (groupidentity == null) return false;

        String xpath = xPathRoot + "/"  + groupidentity;
        PersistManager.putXpath(xpath, remotegroup);
        PersistManager.flush();
        return true;
    }

    @Nullable
    public static JSONArray getGCMTokens(String groupidentity)
    {
        JSONObject rg = PersistManager.getXpathJSONObject(xPathRoot + "/"  + groupidentity);

        if ((rg != null) && rg.has("members"))
        {
            JSONArray members = Json.getArray(rg, "members");

            if (members != null)
            {
                JSONArray tokens = new JSONArray();

                for (int inx = 0; inx < members.length(); inx++)
                {
                    JSONObject member = Json.getObject(members, inx);
                    if (member == null) continue;

                    String memberident = Json.getString(member, "identity");
                    if (memberident == null) continue;

                    if (memberident.equals(SystemIdentity.getIdentity()))
                    {
                        //
                        // Exclude ourselves from recipients list.
                        //

                        continue;
                    }

                    if (member.has("gcmUuid")) tokens.put(Json.getString(member, "gcmUuid"));
                }

                return tokens;
            }
        }

        return null;
    }

    public static String getDisplayName(String groupidentity)
    {
        JSONObject rg = PersistManager.getXpathJSONObject(xPathRoot + "/" + groupidentity);

        if (rg != null)
        {
            String groupname = "";

            if (rg.has("name")) groupname += " " + Json.getString(rg, "name");

            if (rg.has("owner"))
            {
                String ownername = RemoteContacts.getDisplayName(Json.getString(rg, "owner"));

                groupname += " ";
                groupname += "(" + ownername + ")";
            }

            return groupname.trim();
        }

        return "Unbekannt";
    }
}
