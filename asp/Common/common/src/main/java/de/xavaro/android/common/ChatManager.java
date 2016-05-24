package de.xavaro.android.common;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatManager implements
        CommService.CommServiceCallback,
        PrepaidManager.PrepaidManagerBalanceCallback
{
    private static final String LOGTAG = ChatManager.class.getSimpleName();

    //region Static singleton methods.

    private static ChatManager instance;

    public static void initialize()
    {
        if (instance == null)
        {
            instance = new ChatManager(Simple.getAnyContext());

            Log.d(LOGTAG, "Initialized");
        }
    }

    public static ChatManager getInstance()
    {
        initialize();

        return instance;
    }

    private final Context context;
    private final Handler handler = new Handler();
    private final Map<String, ChatMessageCallback> callbacks = new HashMap<>();
    private final Map<String, String> outgoingChatStatus = new HashMap<>();
    private final Map<String, String> incomingChatStatus = new HashMap<>();
    private final Map<String, String> incomingOnlineStatus = new HashMap<>();

    public ChatManager(Context context)
    {
        this.context = context;

        CommService.subscribeMessage(this, "chatMessage");
        CommService.subscribeMessage(this, "skypeCallback");
        CommService.subscribeMessage(this, "sendPrepaidBalance");

        CommService.subscribeMessage(this, "feedbackMessage");

        CommService.subscribeMessage(this, "sendOnlineStatus");
        CommService.subscribeMessage(this, "recvOnlineStatus");

        CommService.subscribeMessage(this, "groupStatusUpdate");
        CommService.subscribeMessage(this, "groupMemberUpdate");
    }

    public void subscribe(String identity, ChatMessageCallback callback)
    {
        if (!callbacks.containsKey(identity)) callbacks.put(identity, callback);

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll != null) callback.onProtocollMessages(protocoll);

        outgoingChatStatus.put(identity, "joinchat=" + Simple.nowAsISO());

        JSONObject sendOnlineStatus = new JSONObject();

        Json.put(sendOnlineStatus, "type", "sendOnlineStatus");
        Json.put(sendOnlineStatus, "idremote", identity);
        Json.put(sendOnlineStatus, "chatstatus", "joinchat");
        Json.put(sendOnlineStatus, "chatdate", Simple.nowAsISO());
        Json.put(sendOnlineStatus, "date", Simple.nowAsISO());

        CommService.sendEncrypted(sendOnlineStatus, true);
    }

    public void unsubscribe(String identity, ChatMessageCallback callback)
    {
        if (callbacks.containsKey(identity)) callbacks.remove(identity);

        outgoingChatStatus.put(identity, "leftchat=" + Simple.nowAsISO());

        JSONObject sendOnlineStatus = new JSONObject();

        Json.put(sendOnlineStatus, "type", "sendOnlineStatus");
        Json.put(sendOnlineStatus, "idremote", identity);
        Json.put(sendOnlineStatus, "chatstatus", "leftchat");
        Json.put(sendOnlineStatus, "chatdate", Simple.nowAsISO());
        Json.put(sendOnlineStatus, "date", Simple.nowAsISO());

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
        JSONObject chatMessage = Json.clone(message);

        Json.put(chatMessage, "type", "chatMessage");
        Json.put(chatMessage, "idremote", idremote);

        if (!chatMessage.has("date")) Json.put(chatMessage, "date", Simple.nowAsISO());

        CommService.sendEncryptedReliable(chatMessage, true);

        updateOutgoingProtocoll(idremote, chatMessage, "send");
    }

    public void sendFeedbackMessage(JSONObject message, String status)
    {
        String idremote = getMessageIdentity(message);
        String uuid = getMessageUUID(message);

        JSONObject feedbackMessage = new JSONObject();

        Json.put(feedbackMessage, "type", "feedbackMessage");
        Json.put(feedbackMessage, "idremote", idremote);
        Json.put(feedbackMessage, "status", status);
        Json.put(feedbackMessage, "uuid", uuid);

        CommService.sendEncryptedReliable(feedbackMessage, true);

        Log.d(LOGTAG, "sendFeedbackMessage: " + feedbackMessage.toString());

        updateIncomingProtocoll(idremote, message, status);
    }

    public void onMessageReceived(JSONObject message)
    {
        try
        {
            if (message.has("type"))
            {
                String type = Json.getString(message, "type");

                if (Simple.equals(type, "skypeCallback"))
                {
                    String idremote = message.getString("identity");
                    String groupidentity = message.getString("groupidentity");
                    String skypecallback = message.getString("skypecallback");

                    String localskype = RemoteGroups.getSkypeCallback(groupidentity, idremote);

                    if (Simple.equals(skypecallback, localskype))
                    {
                        Uri uri = Uri.parse("skype:" + skypecallback + "?call&video=true");

                        Intent skype = new Intent(Intent.ACTION_VIEW);
                        skype.setData(uri);
                        skype.setPackage("com.skype.raider");

                        ProcessManager.launchIntent(skype);
                    }

                    return;
                }

                if (Simple.equals(type, "sendPrepaidBalance"))
                {
                    String cashcode = Json.getString(message, "cashcode");
                    PrepaidManager.makeRequest(this, true, message, cashcode);
                }

                if (Simple.equals(type, "sendOnlineStatus"))
                {
                    String idremote = message.getString("identity");
                    String chatstatus = message.getString("chatstatus");
                    String chatdate = message.getString("chatdate");
                    String date = message.getString("date");

                    incomingChatStatus.put(idremote, chatstatus + "=" + chatdate);
                    incomingOnlineStatus.put(idremote, date);

                    ChatMessageCallback callback = callbacks.get(idremote);
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

                if (Simple.equals(type, "recvOnlineStatus"))
                {
                    String idremote = message.getString("identity");
                    String chatstatus = message.getString("chatstatus");
                    String chatdate = message.getString("chatdate");
                    String date = message.getString("date");

                    incomingChatStatus.put(idremote, chatstatus + "=" + chatdate);
                    incomingOnlineStatus.put(idremote, date);

                    ChatMessageCallback callback = callbacks.get(idremote);
                    if (callback != null) callback.onRemoteStatus();

                    return;
                }

                if (Simple.equals(type, "chatMessage"))
                {
                    sendFeedbackMessage(message, "recv");
                    Boolean wasread = onIncomingMessage(message);
                    if (!wasread) sendNotification(message);

                    return;
                }

                if (Simple.equals(type, "groupStatusUpdate") && message.has("remotegroup"))
                {
                    JSONObject rgnew = message.getJSONObject("remotegroup");

                    RemoteGroups.updateGroup(rgnew);

                    return;
                }

                if (Simple.equals(type, "groupMemberUpdate") && message.has("remotegroup"))
                {
                    JSONObject rgnew = message.getJSONObject("remotegroup");
                    String groupidentity = rgnew.getString("groupidentity");
                    JSONObject updmember = rgnew.getJSONObject("member");

                    RemoteGroups.updateMember(groupidentity, updmember);

                    return;
                }

                if (Simple.equals(type, "feedbackMessage"))
                {
                    onSetMessageStatus(message);

                    return;
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        Log.d(LOGTAG, "onMessageReceived: unresolved: " + message.toString());
    }

    @Override
    public void onPrepaidBalanceReceived(String text, int money, JSONObject slug)
    {
        Log.d(LOGTAG, "onPrepaidBalanceReceived: " + money);

        String identity = Json.getString(slug, "identity");

        JSONObject recvPrepaidBalance = new JSONObject();

        Json.put(recvPrepaidBalance, "type", "recvPrepaidBalance");
        Json.put(recvPrepaidBalance, "idremote", identity);
        Json.put(recvPrepaidBalance, "text", text);
        Json.put(recvPrepaidBalance, "money", money);
        Json.put(recvPrepaidBalance, "date", Simple.nowAsISO());

        CommService.sendEncrypted(recvPrepaidBalance, true);
    }

    @Nullable
    private String getMessageUUID(JSONObject message)
    {
        return Json.getString(message, "uuid");
    }

    @Nullable
    private String getMessageIdentity(JSONObject message)
    {
        String idremote = Json.getString(message, "idremote");
        if (idremote == null) return null;

        if (idremote.equals(SystemIdentity.getIdentity()))
        {
            return Json.getString(message, "identity");
        }

        return Json.getString(message, "idremote");
    }

    public void clearProtocoll(String identity)
    {
        putProtocoll(identity, new JSONObject());
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
        updateProtocoll("incoming", identity, message, status);
    }

    public void updateOutgoingProtocoll(String identity, JSONObject message, String status)
    {
        updateProtocoll("outgoing", identity, message, status);
    }

    public void updateProtocoll(String what, String identity, JSONObject message, String status)
    {
        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return;

        JSONObject outgoing = Json.getObject(protocoll, what);
        if (outgoing == null) return;

        JSONObject proto = null;

        if (outgoing.has(uuid))
        {
            proto = Json.getObject(outgoing, uuid);
        }
        else
        {
            if (Simple.equals(Json.getString(message, "type"), "chatMessage"))
            {
                proto = Json.clone(message);

                Json.remove(proto, "uuid");

                //
                // Remove redundant entries from message.
                //

                if (Simple.equals(Json.getString(proto, "identity"), identity))
                {
                    Json.remove(proto, "identity");
                }

                if (Simple.equals(Json.getString(proto, "idremote"), SystemIdentity.getIdentity()))
                {
                    Json.remove(proto, "idremote");
                }

                Json.put(outgoing, uuid, proto);
            }
        }

        if (proto != null)
        {
            Json.put(proto, status, Simple.nowAsISO());
            putProtocoll(identity, protocoll);
        }
    }

    public void fakeIncomingMessage(JSONObject message)
    {
        onIncomingMessage(message);
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
                ChatMessageCallback callback = callbacks.get(cbidentity);
                if (callback != null) callback.onIncomingMessage(cbmessage);
            }
        });

        return true;
    }

    private void onSetMessageStatus(JSONObject message)
    {
        String identity = getMessageIdentity(message);
        if (identity == null) return;

        String idremote = Json.getString(message, "identity");
        if (idremote == null) return;

        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        String status = Json.getString(message, "status");
        if (status == null) return;

        updateOutgoingProtocoll(identity, message, status);

        final String cbuuid = uuid;
        final String cbstatus = status;
        final String cbidentity = identity;
        final String cbidremote = idremote;

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                ChatMessageCallback callback = callbacks.get(cbidentity);
                if (callback != null) callback.onSetMessageStatus(cbidremote, cbuuid, cbstatus);
            }
        });
    }

    private void sendNotification(JSONObject chatMessage)
    {
        String idremote = getMessageIdentity(chatMessage);
        String message  = Json.getString(chatMessage, "message");

        String sender;

        if (RemoteGroups.isGroup(idremote))
        {
            sender = RemoteGroups.getDisplayName(idremote);
        }
        else
        {
            sender = RemoteContacts.getDisplayName(idremote);
        }

        //
        // Internal notification service.
        //

        SimpleStorage.addInt("notifications", "xavaro" + ".count." + idremote, 1);
        SimpleStorage.addArray("notifications", "xavaro" + ".texts." + idremote, message);
        SimpleStorage.put("notifications", "xavaro" + ".stamp." + idremote, Simple.nowAsISO());

        NotificationService.doCallbacks("xavaro", idremote);

        //
        // Android notification service.
        //

        Intent intent = new Intent("de.xavaro.android.common.ChatActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("idremote", idremote);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                Simple.getAnyContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(Simple.getAnyContext());

        String priority = Json.getString(chatMessage, "priority");

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int iconRes = R.drawable.community_notification_64x64;

        if (Simple.equals(priority, "alertcall") || Simple.equals(priority, "alertinfo"))
        {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            iconRes = R.drawable.community_alarm_64x64;

            nb.setVibrate(getSOSPattern());
        }

        nb.setAutoCancel(true);
        nb.setSmallIcon(iconRes);
        nb.setLargeIcon(Simple.getBitmap(iconRes));
        nb.setContentTitle(sender);
        nb.setContentText(message);
        nb.setSound(soundUri);
        nb.setContentIntent(pendingIntent);

        NotificationManager nm = Simple.getNotificationManager();
        nm.notify("xavaro" + "." + idremote, 0, nb.build());
    }

    private long[] getSOSPattern()
    {
        int dot = 200;          // Length of a Morse Code "dot" in milliseconds
        int dash = 500;         // Length of a Morse Code "dash" in milliseconds
        int short_gap = 200;    // Length of Gap Between dots/dashes
        int medium_gap = 500;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words

        return new long[]{
                0,  // Start immediately
                dot, short_gap, dot, short_gap, dot,    // s
                medium_gap,
                dash, short_gap, dash, short_gap, dash, // o
                medium_gap,
                dot, short_gap, dot, short_gap, dot,    // s
                long_gap
        };
    }

    //region Callback interface

    public interface ChatMessageCallback
    {
        void onProtocollMessages(JSONObject protocoll);
        void onIncomingMessage(JSONObject message);
        void onSetMessageStatus(String idremote, String uuid, String what);
        void onRemoteStatus();
    }

    //endregion Callback interface
}
