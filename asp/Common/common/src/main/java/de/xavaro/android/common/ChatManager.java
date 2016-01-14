package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager implements
        CommService.CommServiceCallback
{
    private static final String LOGTAG = ChatManager.class.getSimpleName();

    //region Static singleton methods.

    private static ChatManager instance;

    public static void initialize(Context context)
    {
        if (instance == null)
        {
            instance = new ChatManager(context);

            Log.d(LOGTAG,"Initialized");
        }
    }

    public static ChatManager getInstance(Context context)
    {
        initialize(context);

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

        CommService.sendEncrypted(sendOnlineStatus);
    }

    public void unsubscribe(String identity, MessageCallback callback)
    {
        if (callbacks.containsKey(identity)) callbacks.put(identity, callback);

        outgoingChatStatus.put(identity, "leftchat=" + Simple.nowAsISO());

        JSONObject sendOnlineStatus = new JSONObject();

        Simple.JSONput(sendOnlineStatus, "type", "sendOnlineStatus");
        Simple.JSONput(sendOnlineStatus, "idremote", identity);
        Simple.JSONput(sendOnlineStatus, "chatstatus", "leftchat");
        Simple.JSONput(sendOnlineStatus, "chatdate", Simple.nowAsISO());
        Simple.JSONput(sendOnlineStatus, "date", Simple.nowAsISO());

        CommService.sendEncrypted(sendOnlineStatus);
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

    public String sendOutgoingMessage(String idremote, String message)
    {
        String uuid = UUID.randomUUID().toString();

        try
        {
            JSONObject sendChatMessage = new JSONObject();

            sendChatMessage.put("type", "sendChatMessage");
            sendChatMessage.put("idremote", idremote);
            sendChatMessage.put("message", message);
            sendChatMessage.put("uuid", uuid);

            CommService.sendEncryptedWithAck(sendChatMessage);

            updateOutgoingProtocoll(idremote, sendChatMessage, "date");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return uuid;
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

                    CommService.sendEncrypted(recvOnlineStatus);

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

                    JSONObject feedbackChatMessage = new JSONObject();

                    feedbackChatMessage.put("type",wasread ? "readChatMessage" : "recvChatMessage");
                    feedbackChatMessage.put("idremote", idremote);
                    feedbackChatMessage.put("uuid", uuid);

                    CommService.sendEncrypted(feedbackChatMessage);

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

        try
        {
            if (new File(context.getFilesDir(),filename).exists())
            {
                FileInputStream inputStream;
                inputStream = context.openFileInput(filename);
                int size = (int) inputStream.getChannel().size();
                byte[] content = new byte[ size ];
                int xfer = inputStream.read(content);
                inputStream.close();

                return new JSONObject(new String(content, 0, xfer));
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return new JSONObject();
    }

    private void putProtocoll(String identity, JSONObject protocoll)
    {
        //Log.d(LOGTAG,"putProtocoll: " + protocoll.toString());

        String filename = context.getPackageName() + ".chatprotocoll." + identity + ".json";

        try
        {
            FileOutputStream outputStream;
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(protocoll.toString(2).getBytes());
            outputStream.close();
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private void updateOutgoingProtocoll(String identity, JSONObject message, String status)
    {
        String uuid = getMessageUUID(message);
        if (uuid == null) return;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return;

        try
        {
            if (! protocoll.has("outgoing")) protocoll.put("outgoing", new JSONObject());
            JSONObject outgoing = protocoll.getJSONObject("outgoing");

            JSONObject proto = new JSONObject(message.toString());

            if (! outgoing.has(uuid))
            {
                if (proto.has("uuid")) proto.remove("uuid");
                if (proto.has("identity")) proto.remove("identity");
                if (proto.has("idremote")) proto.remove("idremote");

                outgoing.put(uuid, proto);
            }
            else
            {
                proto = outgoing.getJSONObject(uuid);
            }

            proto.put(status, Simple.nowAsISO());

            putProtocoll(identity, protocoll);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private boolean onIncomingMessage(JSONObject message)
    {
        String identity = getMessageIdremote(message);
        if (identity == null) return false;

        String uuid = getMessageUUID(message);
        if (uuid == null) return false;

        JSONObject protocoll = getProtocoll(identity);
        if (protocoll == null) return false;

        try
        {
            message.put("date", Simple.nowAsISO());

            if (! protocoll.has("incoming")) protocoll.put("incoming", new JSONObject());
            JSONObject incoming = protocoll.getJSONObject("incoming");

            JSONObject proto = new JSONObject(message.toString());

            if (proto.has("uuid")) proto.remove("uuid");
            if (proto.has("identity")) proto.remove("identity");
            if (proto.has("idremote")) proto.remove("idremote");

            incoming.put(uuid, proto);

            putProtocoll(identity, protocoll);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (! callbacks.containsKey(identity)) return false;

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

        String idremote = getMessageIdremote(message);
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
                Log.d(LOGTAG,"hier1:" + cbidentity);
                MessageCallback callback = callbacks.get(cbidentity);
                Log.d(LOGTAG,"hier2:" + callback);

                if (callback != null) callback.onSetMessageStatus(cbuuid, cbstatus);
            }
        });

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
