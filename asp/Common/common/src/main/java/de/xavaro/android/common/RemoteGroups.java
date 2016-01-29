package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class RemoteGroups
{
    private static final String LOGTAG = RemoteGroups.class.getSimpleName();

    private static final String xPathRoot = "RemoteGroups/groupidentities";

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
        JSONObject rg = PersistManager.getXpathJSONObject(xPathRoot + "/" + groupidentity);

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

                        //continue;
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

            if (Simple.equals(Json.getString(rg, "type"), "alertcall"))
            {
                if (rg.has("owner"))
                {
                    String ownername = RemoteContacts.getDisplayName(Json.getString(rg, "owner"));
                    groupname += " " + "für" + " " + ownername;
                }
            }

            return groupname.trim();
        }

        return "Unbekannt";
    }

    //region Group updates

    public static void updateGroupFromPreferences(String groupprefix)
    {
        Log.d(LOGTAG,"updateGroupFromPreferences: " + groupprefix);

        String groupidentity = Simple.getPreferenceString(groupprefix + ".groupidentity");

        JSONObject group = new JSONObject();

        Json.put(group, "groupidentity", groupidentity);
        Json.put(group, "type", Simple.getPreferenceString(groupprefix + ".type"));
        Json.put(group, "owner", SystemIdentity.getIdentity());
        Json.put(group, "passphrase", Simple.getPreferenceString(groupprefix + ".passphrase"));
        Json.put(group, "name", Simple.getPreferenceString(groupprefix + ".name"));

        updateGroup(group, true);

        //
        // We add ourselves first and can never exit group.
        //

        JSONObject ourselves = new JSONObject();
        RemoteContacts.deliverOwnContact(ourselves);
        Json.put(ourselves, "identity", SystemIdentity.getIdentity());
        Json.put(ourselves, "groupstatus", "invited");
        Json.put(ourselves, "userstatus", "joined");

        updateMember(groupidentity, ourselves, true);

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
            Json.put(minfo, "groupstatus", memberstatus);

            updateMember(groupidentity, minfo, true);
        }
    }

    public static boolean updateGroup(JSONObject group, boolean publish)
    {
        String groupidentity = Json.getString(group, "groupidentity");
        if (groupidentity == null) return false;

        JSONObject oldgroup = null;
        boolean dirty = false;

        synchronized (LOGTAG)
        {
            oldgroup = getGroup(groupidentity);

            if (oldgroup == null)
            {
                oldgroup = Json.clone(group);

                if (! oldgroup.has("members")) Json.put(oldgroup, "members", new JSONArray());

                dirty = true;
            }
            else
            {
                if (! Json.equals(oldgroup, "name", group))
                {
                    Json.copy(oldgroup, "name", group);
                    dirty = true;
                }

                if (! Json.equals(oldgroup, "owner", group))
                {
                    Json.copy(oldgroup, "owner", group);
                    dirty = true;
                }

                if (! Json.equals(oldgroup, "passphrase", group))
                {
                    Json.copy(oldgroup, "passphrase", group);
                    dirty = true;
                }
            }

            if (dirty) putGroup(oldgroup);
        }

        if (dirty && publish)
        {
            JSONObject pubgroup = Json.clone(oldgroup);
            Json.remove(pubgroup, "members");

            JSONObject groupStatusUpdate = new JSONObject();

            Json.put(groupStatusUpdate, "type", "groupStatusUpdate");
            Json.put(groupStatusUpdate, "idremote", groupidentity);
            Json.put(groupStatusUpdate, "remotegroup", pubgroup);

            CommService.sendEncrypted(groupStatusUpdate, true);
        }

        return true;
    }

    public static boolean updateMember(String groupidentity, JSONObject member, boolean publish)
    {
        JSONObject oldgroup = null;
        JSONObject oldmember = null;
        boolean dirty = false;

        synchronized (LOGTAG)
        {
            oldgroup = getGroup(groupidentity);
            if (oldgroup == null) return false;

            String ident = Json.getString(member, "identity");
            if (ident == null) return false;

            String groupstatus = Json.getString(member, "groupstatus");
            boolean inactive = (groupstatus == null) || groupstatus.equals("inactive");
            boolean wasold = false;

            JSONArray members = Json.getArray(oldgroup, "members");
            if (members == null) Json.put(oldgroup, "members", members = new JSONArray());

            for (int inx = 0; inx < members.length(); inx++)
            {
                oldmember = Json.getObject(members, inx);
                if (oldmember == null) continue;

                if (! Simple.equals(Json.getString(oldmember, "identity"), ident)) continue;

                if (! Json.equals(oldmember, "groupstatus", member))
                {
                    Json.copy(oldmember, "groupstatus", member);
                    dirty = true;
                }

                if (member.has("userstatus") && ! Json.equals(oldmember, "userstatus", member))
                {
                    Json.copy(oldmember, "userstatus", member);
                    dirty = true;
                }

                wasold = true;
                break;
            }

            if ((! wasold) && (! inactive))
            {
                Json.put(members, member);
                oldmember = member;
                dirty = true;
            }

            if (dirty) putGroup(oldgroup);
        }

        if (dirty && publish)
        {
            JSONObject pubgroup = new JSONObject();
            Json.put(pubgroup, "groupidentity", groupidentity);
            Json.put(pubgroup, "member", oldmember);

            JSONObject groupMemberUpdate = new JSONObject();

            Json.put(groupMemberUpdate, "type", "groupMemberUpdate");
            Json.put(groupMemberUpdate, "idremote", groupidentity);
            Json.put(groupMemberUpdate, "remotegroup", pubgroup);

            CommService.sendEncrypted(groupMemberUpdate, true);
        }

        return true;
    }

    //endregion Group updates
}
