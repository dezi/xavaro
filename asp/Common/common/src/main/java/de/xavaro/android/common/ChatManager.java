package de.xavaro.android.common;

import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Handler;
import android.util.Log;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager implements CommService.CommServiceCallback
{
    private static final String LOGTAG = ChatManager.class.getSimpleName();

    //region Static singleton methods.

    private static ChatManager instance;

    public static void initialize()
    {
        if (instance == null)
        {
            instance = new ChatManager(Simple.getAnyContext());

            Log.d(LOGTAG,"Initialized");
        }
    }

    public static ChatManager getInstance()
    {
        initialize();

        return instance;
    }

    private final Context context;
    private final Handler handler = new Handler();
    private final Map<String, MessageCallback> callbacks = new HashMap<>();
    private final Map<String, String> outgoingChatStatus = new HashMap<>();
    private final Map<String, String> incomingChatStatus = new HashMap<>();
    private final Map<String, String> incomingOnlineStatus = new HashMap<>();

    public ChatManager(Context context)
    {
        this.context = context;

        CommService.subscribeMessage(this, "sendChatMessage");

        CommService.subscribeMessage(this, "recvChatMessage");
        CommService.subscribeMessage(this, "readChatMessage");
        CommService.subscribeMessage(this, "serverAckMessage");

        CommService.subscribeMessage(this, "sendOnlineStatus");
        CommService.subscribeMessage(this, "recvOnlineStatus");

        CommService.subscribeMessage(this, "groupStatusUpdate");
        CommService.subscribeMessage(this, "groupMemberUpdate");
    }

    public void subscribe(String identity, MessageCallback callback)
    {
        if (! callbacks.containsKey(identity)) callbacks.put(identity, callback);

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll != null) callback.onProtocollMessages(protocoll);

        outgoingChatStatus.put(identity, "joinchat=" + Simple.nowAsISO());

        JSONObject sendOnlineStatus = new JSONObject();

        Simple.JSONput(sendOnlineStatus, "type", "sendOnlineStatus");
        Simple.JSONput(sendOnlineStatus, "idremote", identity);
        Simple.JSONput(sendOnlineStatus, "chatstatus", "joinchat");
        Simple.JSONput(sendOnlineStatus, "chatdate", Simple.nowAsISO());
        Simple.JSONput(sendOnlineStatus, "date", Simple.nowAsISO());

        CommService.sendEncrypted(sendOnlineStatus, true);
    }

    public void unsubscribe(String identity, MessageCallback callback)
    {
        if (callbacks.containsKey(identity)) callbacks.remove(identity);

        outgoingChatStatus.put(identity, "leftchat=" + Simple.nowAsISO());

        JSONObject sendOnlineStatus = new JSONObject();

        Simple.JSONput(sendOnlineStatus, "type", "sendOnlineStatus");
        Simple.JSONput(sendOnlineStatus, "idremote", identity);
        Simple.JSONput(sendOnlineStatus, "chatstatus", "leftchat");
        Simple.JSONput(sendOnlineStatus, "chatdate", Simple.nowAsISO());
        Simple.JSONput(sendOnlineStatus, "date", Simple.nowAsISO());

        CommService.sendEncrypted(sendOnlineStatus, true);
    }

    @Nullable
    public String getLastChatStatus(String idremote)
    {
        if (incomingChatStatus.containsKey(idremote))
        {
            return incomingChatStatus.get(idremote);
        }

        return null;
    }

    public String getLastOnlineDate(String idremote)
    {
        if (incomingOnlineStatus.containsKey(idremote))
        {
            return incomingOnlineStatus.get(idremote);
        }

        return null;
    }

    public void sendOutgoingMessage(String idremote, JSONObject message)
    {
        JSONObject sendChatMessage = Json.clone(message);

        Json.put(sendChatMessage, "type", "sendChatMessage");
        Json.put(sendChatMessage, "idremote", idremote);

        CommService.sendEncryptedReliable(sendChatMessage, true);

        updateOutgoingProtocoll(idremote, sendChatMessage, "send");
    }

    public void clearProtocoll(String identity)
    {
        JSONObject clear = new JSONObject();

        putProtocoll(identity, clear);
    }

    public void onMessageReceived(JSONObject message)
    {
        try
        {
            if (message.has("type"))
            {
                String type = message.getString("type");

                if (type.equals("sendOnlineStatus"))
                {
                    String idremote = message.getString("identity");
                    String chatstatus = message.getString("chatstatus");
                    String chatdate = message.getString("chatdate");
                    String date = message.getString("date");

                    incomingChatStatus.put(idremote, chatstatus + "=" + chatdate);
                    incomingOnlineStatus.put(idremote, date);

                    MessageCallback callback = callbacks.get(idremote);
                    if (callback != null) callback.onRemoteStatus();

                    chatstatus = "online";
                    chatdate = Simple.nowAsISO();

                    if (outgoingChatStatus.containsKey(idremote))
                    {
                        String[] parts = outgoingChatStatus.get(idremote).split("=");

                        if (parts.length == 2)
                        {
                            chatstatus = parts[ 0 ];
                            chatdate = parts[ 1 ];
                        }
                    }

                    JSONObject recvOnlineStatus = new JSONObject();

                    recvOnlineStatus.put("type", "recvOnlineStatus");
                    recvOnlineStatus.put("idremote", idremote);
                    recvOnlineStatus.put("chatstatus", chatstatus);
                    recvOnlineStatus.put("chatdate", chatdate);
                    recvOnlineStatus.put("date", Simple.nowAsISO());

                    CommService.sendEncrypted(recvOnlineStatus, true);

                    return;
                }

                if (type.equals("recvOnlineStatus"))
                {
                    String idremote = message.getString("identity");
                    String chatstatus = message.getString("chatstatus");
                    String chatdate = message.getString("chatdate");
                    String date = message.getString("date");

                    incomingChatStatus.put(idremote, chatstatus + "=" + chatdate);
                    incomingOnlineStatus.put(idremote, date);

                    MessageCallback callback = callbacks.get(idremote);
                    if (callback != null) callback.onRemoteStatus();

                    return;
                }

                if (type.equals("sendChatMessage"))
                {
                    String uuid = message.getString("uuid");
                    String idremote = message.getString("identity");
                    Boolean wasread = onIncomingMessage(message);

                    if (! wasread)
                    {
                        JSONObject feedbackChatMessage = new JSONObject();

                        feedbackChatMessage.put("type", "recvChatMessage");
                        feedbackChatMessage.put("idremote", idremote);
                        feedbackChatMessage.put("uuid", uuid);

                        CommService.sendEncryptedReliable(feedbackChatMessage, true);

                        updateIncomingProtocoll(idremote, message, "recv");

                        sendNotification(message);
                    }

                    return;
                }

                if (type.equals("groupStatusUpdate") && message.has("remotegroup"))
                {
                    JSONObject rgnew = message.getJSONObject("remotegroup");

                    RemoteGroups.updateGroup(rgnew, false);

                    return;
                }

                if (type.equals("groupMemberUpdate") && message.has("remotegroup"))
                {
                    JSONObject rgnew = message.getJSONObject("remotegroup");
                    String groupidentity = rgnew.getString("groupidentity");
                    JSONObject updmember = rgnew.getJSONObject("member");

                    RemoteGroups.updateMember(groupidentity, updmember, false);

                    return;
                }

                if (type.equals("serverAckMessage"))
                {
                    onSetMessageStatus(message, "acks");

                    return;
                }

                if (type.equals("recvChatMessage"))
                {
                    onSetMessageStatus(message, "recv");

                    return;
                }

                if (type.equals("readChatMessage"))
                {
                    onSetMessageStatus(message, "read");

                    return;
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        Log.d(LOGTAG, "onMessageReceived: " + message.toString());
    }

    @Nullable
    private String getMessageUUID(JSONObject message)
    {
        String uuid = null;

        try
        {
            uuid = message.getString("uuid");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return uuid;
    }

    @Nullable
    private String getMessageIdentity(JSONObject message)
    {
        String identity = null;

        try
        {
            identity = message.getString("identity");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return identity;
    }

    @Nullable
    private String getMessageIdremote(JSONObject message)
    {
        String identity = null;

        try
        {
            identity = message.getString("idremote");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return identity;
    }

    @Nullable
    private JSONObject getProtocoll(String identity)
    {
        if (identity == null) return null;

        String filename = context.getPackageName() + ".chatprotocoll." + identity + ".json";
        String content = Simple.readDatadirFile(filename);
        JSONObject protocoll = Json.fromString(content);

        if (! protocoll.has("incoming")) Json.put(protocoll, "incoming", new JSONObject());
        if (! protocoll.has("outgoing")) Json.put(protocoll, "outgoing", new JSONObject());

        return protocoll;
    }

    private void putProtocoll(String identity, JSONObject protocoll)
    {
        if (identity == null) return;

        String filename = context.getPackageName() + ".chatprotocoll." + identity + ".json";
        Simple.writeDatadirFile(filename, Json.toPretty(protocoll));
    }

    public void updateIncomingProtocoll(String identity, JSONObject message, String status)
    {
        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return;

        JSONObject incoming = Json.getObject(protocoll, "incoming");
        if (incoming == null) return;

        Log.d(LOGTAG, "updateIncomingProtocoll:" + identity + "=" + status);

        JSONObject proto = null;

        if (incoming.has(uuid))
        {
            proto = Json.getObject(incoming, uuid);
        }
        else
        {
            if (message.has("type") && Simple.equals(Json.getString(message, "type"), "sendChatMessage"))
            {
                proto = Json.clone(message);

                Json.remove(proto, "uuid");
                Json.remove(proto, "identity");
                Json.remove(proto, "idremote");

                Json.put(incoming, uuid, proto);
            }
        }

        if (proto != null)
        {
            Json.put(proto, status, Simple.nowAsISO());
            putProtocoll(identity, protocoll);
        }
    }

    public void updateOutgoingProtocoll(String identity, JSONObject message, String status)
    {
        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return;

        JSONObject outgoing = Json.getObject(protocoll, "outgoing");
        if (outgoing == null) return;

        JSONObject proto = null;

        if (outgoing.has(uuid))
        {
            proto = Json.getObject(outgoing, uuid);
        }
        else
        {
            if (message.has("type") && Simple.equals(Json.getString(message, "type"), "sendChatMessage"))
            {
                proto = Json.clone(message);

                Json.remove(proto, "uuid");
                Json.remove(proto, "identity");
                Json.remove(proto, "idremote");

                Json.put(outgoing, uuid, proto);
            }
        }

        if (proto != null)
        {
            Json.put(proto, status, Simple.nowAsISO());
            putProtocoll(identity, protocoll);
        }
    }

    private boolean onIncomingMessage(JSONObject message)
    {
        String identity = getMessageIdentity(message);
        if (identity == null) return false;

        String uuid = getMessageUUID(message);
        if (uuid == null) return false;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return false;

        updateIncomingProtocoll(identity, message, "date");

        if (! callbacks.containsKey(identity)) return false;

        //
        // The chat activity is present.
        //

        final String cbidentity = identity;
        final JSONObject cbmessage = message;

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                MessageCallback callback = callbacks.get(cbidentity);
                if (callback != null) callback.onIncomingMessage(cbmessage);
            }
        });

        return true;
    }

    private void onSetMessageStatus(JSONObject message, String status)
    {
        Log.d(LOGTAG,"onSetMessageStatus:" + status + "=" + message.toString());

        String idremote = getMessageIdentity(message);
        if (idremote == null) return;

        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        updateOutgoingProtocoll(idremote, message, status);

        final String cbuuid = uuid;
        final String cbstatus = status;
        final String cbidentity = idremote;

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                MessageCallback callback = callbacks.get(cbidentity);
                if (callback != null) callback.onSetMessageStatus(cbuuid, cbstatus);
            }
        });
    }

    private void sendNotification(JSONObject chatMessage)
    {
        String idremote = Simple.JSONgetString(chatMessage, "identity");
        String sender   = RemoteContacts.getDisplayName(idremote);
        String message  = Simple.JSONgetString(chatMessage,"message");

        Intent intent = new Intent("de.xavaro.android.common.ChatActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("idremote", idremote);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                Simple.getAnyContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        int resid = R.drawable.community_alarm_64x64;

        NotificationCompat.Builder nb = new NotificationCompat.Builder(Simple.getAnyContext());

        nb.setAutoCancel(true);
        nb.setSmallIcon(resid);
        nb.setLargeIcon(Simple.getBitmapFromResource(resid));
        nb.setContentTitle(sender);
        nb.setContentText(message);
        nb.setSound(defaultSoundUri);
        nb.setContentIntent(pendingIntent);

        NotificationManager nm = Simple.getNotificationManager();
        nm.notify(0, nb.build());
    }

    //region Callback interface

    public interface MessageCallback
    {
        void onProtocollMessages(JSONObject protocoll);
        void onIncomingMessage(JSONObject message);
        void onSetMessageStatus(String uuid, String what);
        void onRemoteStatus();
    }

    //endregion Callback interface
}
