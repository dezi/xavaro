package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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

    public static String getGroupType(String groupidentity)
    {
        JSONObject rg = PersistManager.getXpathJSONObject(xPathRoot + "/" + groupidentity);

        if ((rg != null) && rg.has("type")) return Json.getString(rg, "type");

        return "unknown";
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

        String groupidentity = Simple.getSharedPrefString(groupprefix + ".groupidentity");

        JSONObject group = new JSONObject();

        Json.put(group, "groupidentity", groupidentity);
        Json.put(group, "type", Simple.getSharedPrefString(groupprefix + ".type"));
        Json.put(group, "owner", SystemIdentity.getIdentity());
        Json.put(group, "passPhrase", Simple.getSharedPrefString(groupprefix + ".passphrase"));
        Json.put(group, "name", Simple.getSharedPrefString(groupprefix + ".name"));

        boolean dirty = updateGroup(group);

        ArrayList<String> dirtylist = new ArrayList<>();

        //
        // We add ourselves first and can never exit group.
        //

        JSONObject ourselves = new JSONObject();
        RemoteContacts.deliverOwnContact(ourselves);
        Json.put(ourselves, "identity", SystemIdentity.getIdentity());
        Json.put(ourselves, "groupstatus", "invited");
        Json.put(ourselves, "userstatus", "joined");

        if (updateMember(groupidentity, ourselves)) dirtylist.add(SystemIdentity.getIdentity());

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

            if (updateMember(groupidentity, minfo)) dirtylist.add(memberident);
        }

        //
        // Now send final group information to all individual members
        // to exchange groups passphrase with individual encryption.
        //

        JSONObject finalGroup = getGroup(groupidentity);
        JSONArray finalMembers = Json.getArray(finalGroup, "members");

        //if (dirty)
        {
            JSONObject pubgroup = Json.clone(finalGroup);
            Json.remove(pubgroup, "members");

            for (int send = 0; send < finalMembers.length(); send++)
            {
                JSONObject finalMember = Json.getObject(finalMembers, send);
                String fmIdentity = Json.getString(finalMember, "identity");
                if (Simple.equals(fmIdentity, SystemIdentity.getIdentity())) continue;

                JSONObject groupStatusUpdate = new JSONObject();

                Json.put(groupStatusUpdate, "type", "groupStatusUpdate");
                Json.put(groupStatusUpdate, "idremote", fmIdentity);
                Json.put(groupStatusUpdate, "remotegroup", pubgroup);

                Log.d(LOGTAG, "updateGroup=============>" + fmIdentity);

                CommService.sendEncrypted(groupStatusUpdate, true);
            }
        }

        for (int inx = 0; inx < finalMembers.length(); inx++)
        {
            JSONObject pubmember = Json.clone(Json.getObject(finalMembers, inx));

            JSONObject pubgroup = new JSONObject();
            Json.put(pubgroup, "groupidentity", groupidentity);
            Json.put(pubgroup, "member", pubmember);

            for (int send = 0; send < finalMembers.length(); send++)
            {
                JSONObject finalMember = Json.getObject(finalMembers, send);
                String fmIdentity = Json.getString(finalMember, "identity");
                if (Simple.equals(fmIdentity, SystemIdentity.getIdentity())) continue;

                JSONObject groupMemberUpdate = new JSONObject();

                Json.put(groupMemberUpdate, "type", "groupMemberUpdate");
                Json.put(groupMemberUpdate, "idremote", fmIdentity);
                Json.put(groupMemberUpdate, "remotegroup", pubgroup);

                Log.d(LOGTAG, "updateMember=============>" + fmIdentity);

                CommService.sendEncrypted(groupMemberUpdate, true);
            }
        }
    }

    public static boolean updateGroup(JSONObject group)
    {
        String groupidentity = Json.getString(group, "groupidentity");
        if (groupidentity == null) return false;

        boolean dirty = false;

        synchronized (LOGTAG)
        {
            JSONObject oldgroup = getGroup(groupidentity);

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

                if (! Json.equals(oldgroup, "passPhrase", group))
                {
                    Json.copy(oldgroup, "passPhrase", group);
                    dirty = true;
                }
            }

            if (dirty) putGroup(oldgroup);
        }

        //
        // Make sure, the actual group passphrase is
        // known in our system identities.
        //

        if (group.has("passPhrase"))
        {
            String passPhrase = Json.getString(group, "passPhrase");

            IdentityManager.put(groupidentity, "passPhrase", passPhrase);

            Simple.makeToast("groupStatusUpdate=" + groupidentity + " => " + passPhrase);
        }

        return dirty;
    }

    public static boolean updateMember(String groupidentity, JSONObject member)
    {
        boolean dirty = false;

        Simple.makeToast("groupMemberUpdate=" + member.toString());

        synchronized (LOGTAG)
        {
            JSONObject oldgroup = getGroup(groupidentity);
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
                JSONObject oldmember = Json.getObject(members, inx);
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
                dirty = true;
            }

            if (dirty) putGroup(oldgroup);
        }

        return dirty;
    }

    //endregion Group updates

    public static void removeGroupFinally(String groupidentity)
    {
        PersistManager.delXpath(xPathRoot + "/" + groupidentity);
        PersistManager.flush();

        Log.w(LOGTAG, "removeFinally: group=" + groupidentity);
    }

    public static void removeMemberFinally(String identity)
    {
        if (identity == null) return;

        Log.w(LOGTAG, "removeFinally: " + identity);

        JSONObject groups = PersistManager.getXpathJSONObject(xPathRoot);
        if (groups == null) return;

        Iterator<String> keysIterator = groups.keys();

        while (keysIterator.hasNext())
        {
            String groupidentity = keysIterator.next();

            JSONObject group = Json.getObject(groups, groupidentity);
            if (group == null) continue;

            String owner = Json.getString(group, "owner");
            if ((owner != null) && owner.equals(identity))
            {
                //
                // Remove group from preferences.
                //

                Map<String, ?> prefs = Simple.getSharedPrefs().getAll();

                for (Map.Entry<String, ?> entry : prefs.entrySet())
                {
                    if (entry.getKey().contains(groupidentity))
                    {
                        Simple.removeSharedPref(entry.getKey());

                        Log.w(LOGTAG, "removeFinally: pref=" + entry.getKey());
                    }
                }

                //
                // Remove group.
                //

                removeGroupFinally(groupidentity);

                continue;
            }

            //
            // Check members of group.
            //

            JSONArray members = Json.getArray(group, "members");
            if (members == null) continue;

            for (int inx = 0; inx < members.length(); inx++)
            {
                JSONObject member = Json.getObject(members, inx);
                if (member == null) continue;

                if (Json.equals(member, "identity", identity))
                {
                    Json.remove(members, inx--);
                    putGroup(group);

                    Log.w(LOGTAG, "removeFinally: member=" + groupidentity + "=" + identity);
                }
            }
        }
    }
}
