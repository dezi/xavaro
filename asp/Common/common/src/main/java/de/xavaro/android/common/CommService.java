package de.xavaro.android.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.system.OsConstants;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//
// Service to communicate with backend servers
// via datagramn packets.
//

public class CommService extends Service
{
    private static final String LOGTAG = CommService.class.getSimpleName();

    private static boolean isRunning;
    private static CommService instance;

    private static final ArrayList<MessageClass> messageBacklog = new ArrayList<>();

    private static final Map<String, ArrayList<CommServiceCallback>> messageSubscribers = new HashMap<>();

    private static final int COMMCLASS_UDP = 1;
    private static final int COMMCLASS_GCM = 2;

    private static class MessageClass
    {
        public static final int NONE = 0;
        public static final int CRYPT = 1;
        public static final int CRYPT_WITH_ACK = 2;
        public static final int CRYPT_RELIABLE = 3;
        public static final int CLIENT_ACK = 4;

        public final JSONObject msg;
        public final int enc;
        public final boolean gcm;

        public MessageClass(JSONObject message, int encrypt, boolean allowGCM)
        {
            msg = message;
            enc = encrypt;
            gcm = allowGCM;
        }
    }

    public static CommService getInstance()
    {
        return instance;
    }

    public static boolean getIsRunning()
    {
        return isRunning;
    }

    private static void sendClientAck(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.CLIENT_ACK, false));
        }
    }

    public static void sendMessage(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.NONE, false));
        }
    }

    public static void sendMessage(JSONObject message, boolean allowGCM)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.NONE, allowGCM));
        }
    }

    public static void sendEncrypted(JSONObject message, boolean allowGCM)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.CRYPT, allowGCM));
        }
    }

    @SuppressWarnings("unused")
    public static void sendEncryptedWithAck(JSONObject message, boolean allowGCM)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.CRYPT_WITH_ACK, allowGCM));
        }
    }

    public static void sendEncryptedReliable(JSONObject message, boolean allowGCM)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.CRYPT_RELIABLE, allowGCM));
        }
    }

    public static void subscribeMessage(CommServiceCallback callback, String type)
    {
        synchronized (messageSubscribers)
        {
            if (! messageSubscribers.containsKey(type))
            {
                messageSubscribers.put(type, new ArrayList<CommServiceCallback>());
            }

            ArrayList<CommServiceCallback> callbacks = messageSubscribers.get(type);
            if (! callbacks.contains(callback)) callbacks.add(callback);
        }
    }

    @SuppressWarnings("unused")
    public static void unsubscribeMessage(CommServiceCallback callback, String type)
    {
        synchronized (messageSubscribers)
        {
            if (! messageSubscribers.containsKey(type)) return;

            ArrayList callbacks = messageSubscribers.get(type);
            if (callbacks.contains(callback)) callbacks.remove(callback);
        }
    }

    public static void unsubscribeAllMessages(CommServiceCallback callback)
    {
        synchronized (messageSubscribers)
        {
            for (String type : messageSubscribers.keySet())
            {
                ArrayList callbacks = messageSubscribers.get(type);
                if (callbacks.contains(callback)) callbacks.remove(callback);
            }
        }
    }

    public interface CommServiceCallback
    {
        void onMessageReceived(JSONObject message);
    }

    //endregion

    //
    // Worker background thread.
    //

    private Thread workerSend;
    private Thread workerRecv;

    //region Overriden methods.

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d(LOGTAG, "onCreate: running with " + getApplicationContext().getPackageName());

        instance = this;

        ChatManager.initialize();
    }

    @Override
    public void onDestroy()
    {
        isRunning = false;

        Log.d(LOGTAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (! isRunning)
        {
            isRunning = true;

            if (workerSend == null)
            {
                workerSend = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sendThread();
                    }
                });

                workerSend.start();
            }

            if (workerRecv == null)
            {
                workerRecv = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        recvThread();
                    }
                });

                workerRecv.start();
            }
        }

        //return Service.START_NOT_STICKY;
        return Service.START_STICKY;
    }

    //endregion

    private NativeSocket datagramSocket = null;
    private DatagramPacket datagramPacket = null;

    private long sleeptime = CommonConfigs.CommServerSleepMin;
    private long lastping = 0;
    private long lastpups = 0;

    private void recvThread()
    {
        Log.d(LOGTAG, "recvThread: running");

        while (isRunning)
        {
            if (datagramSocket == null)
            {
                StaticUtils.sleep(1000);

                continue;
            }

            try
            {
                byte[] recvBytes = new byte[ 8192 ];
                DatagramPacket recvPacket = new DatagramPacket(recvBytes, recvBytes.length);

                datagramSocket.receive(recvPacket);

                Log.d(LOGTAG, "recvThread: received:" + recvPacket.getLength());

                decryptPacket(recvPacket.getData(), COMMCLASS_UDP);
            }
            catch (Exception ex)
            {
                Log.d(LOGTAG,"recvThread: " + ex.getMessage());
            }
        }

        Log.d(LOGTAG, "recvThread: finished");

        workerRecv = null;
    }

    //
    // Messages handled directly by comm service
    // or delegate services.
    //

    private boolean onMessageReceived(JSONObject json)
    {
        try
        {
            if (! json.has("type")) return false;

            String type = json.getString("type");

            if (type.equals("fileTransferRequest")
                    || type.equals("fileTransferResponse")
                    || type.equals("fileTransferUploaded")
                    || type.equals("fileTransferDownloaded"))
            {
                CommSender.onMessageReceived(json);

                return true;
            }

            if (type.equals("requestPublicKeyXChange"))
            {
                String remoteIdentity = json.getString("identity");
                String remotePublicKey = json.getString("publicKey");
                String gcmtoken = Json.getString(json, "gcmtoken");

                //
                // Legacy token name...
                //

                if (gcmtoken == null) gcmtoken = Json.getString(json, "gcmUuid");

                //
                // Store remote public key for remote identity.
                //

                IdentityManager.put(remoteIdentity, "publicKey", remotePublicKey);

                //
                // Store GCM token temporary until contact validated.
                //

                RemoteContacts.setGCMTokenTemp(remoteIdentity, gcmtoken);

                Log.d(LOGTAG, "onMessageReceived: requestPublicKeyXChange"
                        + " remoteIdentity=" + remoteIdentity
                        + " remotePublicKey=" + remotePublicKey);

                //
                // Respond to requester with own public key.
                //

                JSONObject responsePublicKeyXChange = new JSONObject();

                responsePublicKeyXChange.put("type", "responsePublicKeyXChange");
                responsePublicKeyXChange.put("idremote", remoteIdentity);
                responsePublicKeyXChange.put("publicKey", CryptUtils.RSAgetPublicKey());
                responsePublicKeyXChange.put("status", "success");

                CommService.sendMessage(responsePublicKeyXChange, true);

                return true;
            }

            if (type.equals("requestAESpassXChange"))
            {
                String remoteIdentity = json.getString("identity");
                String encoPassPhrase = json.getString("encodedPassPhrase");

                //
                // Decode AES passphrase with private key.
                //

                String privateKey = CryptUtils.RSAgetPrivateKey();
                String passPhrase = CryptUtils.RSADecrypt(privateKey, encoPassPhrase);

                if (Simple.getSharedPrefBoolean("developer.debug.gcmstuff"))
                {
                    Simple.makeAlertPost("\"" + passPhrase + "\"", type);
                }

                IdentityManager.put(remoteIdentity, "passPhrase", passPhrase);

                Log.d(LOGTAG, "onMessageReceived: requestAESpassXChange"
                        + " remoteIdentity=" + remoteIdentity
                        + " passPhrase=" + passPhrase);

                //
                // Prepare and send hand shake packet.
                //

                JSONObject responseAESpassXChange = new JSONObject();

                responseAESpassXChange.put("type", "responseAESpassXChange");
                responseAESpassXChange.put("idremote", remoteIdentity);
                responseAESpassXChange.put("status", "success");

                //
                // Success packet already send encrypted.
                //

                CommService.sendEncrypted(responseAESpassXChange, true);

                return true;
            }

            if (type.equals("requestOwnerIdentity"))
            {
                String remoteIdentity = json.getString("identity");

                RemoteContacts.registerContact(json);

                //
                // Build response with own identiy.
                //

                JSONObject responseOwnerIdentity = new JSONObject();

                responseOwnerIdentity.put("type", "responseOwnerIdentity");
                responseOwnerIdentity.put("idremote", remoteIdentity);
                responseOwnerIdentity.put("status", "success");

                RemoteContacts.deliverOwnContact(responseOwnerIdentity);

                CommService.sendEncrypted(responseOwnerIdentity, true);

                ProfileImages.sendOwnerImage(remoteIdentity);

                return true;
            }

            if (type.equals("recvPrepaidBalance"))
            {
                //
                // Inspect remote prepaid balance messages
                // and store results into preferences.
                //

                int money = Json.getInt(json, "money");

                if (money >= 0)
                {
                    //
                    // The identity could either be a direct response
                    // for a prepaid request or an assistance broadcast.
                    //

                    String identity = Json.getString(json, "identity");

                    if (RemoteGroups.isGroup(identity))
                    {
                        identity = RemoteGroups.getGroupOwner(identity);
                    }

                    String date = Json.getString(json, "date");
                    if (date == null) date = Simple.nowAsISO();

                    String pfix = "monitoring.prepaid.remote.";
                    Simple.setSharedPrefInt(pfix + "money:" + identity, money);
                    Simple.setSharedPrefString(pfix + "stamp:" + identity, date);
                }

                return false;
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    public void onRawMessageReceived(byte[] rawMessage)
    {
        decryptPacket(rawMessage, COMMCLASS_GCM);
    }

    private void decryptPacket(byte[] data, int comclass)
    {
        String ptype = new String(data, 0, 4);

        if (ptype.equals("MYIP"))
        {
            CommonStatic.publicIPaddress = (data[ 4 ] & 0xff) + "." + (data[ 5 ] & 0xff)
                    + "." + (data[ 6 ] & 0xff) + "." + (data[ 7 ] & 0xff);

            Log.d(LOGTAG,"decryptPacket myip=" + CommonStatic.publicIPaddress);

            return;
        }

        if (ptype.equals("CRYP"))
        {
            //
            // Packet consists of:
            //
            // -  4 bytes header "CRYP" => crypted
            // - 16 bytes remote identity from sender perspektive
            // - 16 bytes owner identity from sender perspektive
            // -  n bytes payload with serialized json
            //

            byte[] idremBytes = new byte[ 16 ];
            System.arraycopy(data, 4, idremBytes, 0, 16);
            String idrem = Simple.getUUIDString(idremBytes);

            byte[] identBytes = new byte[ 16 ];
            System.arraycopy(data, 20, identBytes, 0, 16);
            String ident = Simple.getUUIDString(identBytes);

            byte[] rest = new byte[ data.length - 36 ];
            System.arraycopy(data, 36, rest, 0, rest.length);

            //
            // If receiver is not our system identity, this is a
            // group message. Use group identity for decryption.
            //

            if (ident.equals(SystemIdentity.getIdentity()))
            {
                data = CryptUtils.AESdecrypt(idrem, rest);
            }
            else
            {
                data = CryptUtils.AESdecrypt(ident, rest);
            }

            if (data == null) return;

            //
            // ptype should be JSON after decrypt.
            //

            ptype = new String(data, 0, 4);
        }

        if (ptype.equals("CARL"))
        {
            //
            // Packet consists of:
            //
            // -  4 bytes header "CARL" => crypted with reliable ack
            // - 16 bytes remote identity from sender perspektive
            // - 16 bytes owner identity from sender perspektive
            // - 16 bytes message uuid for acknowledgement
            // -  n bytes payload with serialized json
            //

            byte[] idremBytes = new byte[ 16 ];
            System.arraycopy(data, 4, idremBytes, 0, 16);
            String idrem = Simple.getUUIDString(idremBytes);

            byte[] identBytes = new byte[ 16 ];
            System.arraycopy(data, 20, identBytes, 0, 16);
            String ident = Simple.getUUIDString(identBytes);

            byte[] ackidBytes = new byte[ 16 ];
            System.arraycopy(data, 36, ackidBytes, 0, 16);
            String ackid = Simple.getUUIDString(ackidBytes);

            if (comclass == COMMCLASS_UDP)
            {
                //
                // Send client ack to server if via UDP.
                // No ack required if send via GCM.
                //

                JSONObject clientAckMessage = new JSONObject();
                Simple.JSONput(clientAckMessage, "type", "clientAckMessage");
                Simple.JSONput(clientAckMessage, "uuid", ackid);
                sendClientAck(clientAckMessage);
            }

            byte[] rest = new byte[ data.length - 52 ];
            System.arraycopy(data, 52, rest, 0, rest.length);

            //
            // If receiver is not our system identity, this is a
            // group message. Use group identity for decryption.
            //

            if (ident.equals(SystemIdentity.getIdentity()))
            {
                data = CryptUtils.AESdecrypt(idrem, rest);
            }
            else
            {
                data = CryptUtils.AESdecrypt(ident, rest);
            }

            if (data == null) return;

            //
            // ptype should be JSON after decrypt.
            //

            ptype = new String(data, 0, 4);
        }

        if (ptype.equals("CACK"))
        {
            //
            // Packet consists of:
            //
            // -  4 bytes header "CACK" => crypted acknowledgement
            // - 16 bytes remote identity from sender perspektive
            // - 16 bytes owner identity from sender perspektive
            // - 16 bytes message uuid from acknowledgement
            // - no payload (is acknowledgement only)
            //

            byte[] idremBytes = new byte[ 16 ];
            System.arraycopy(data, 4, idremBytes, 0, 16);
            String idrem = Simple.getUUIDString(idremBytes);

            byte[] identBytes = new byte[ 16 ];
            System.arraycopy(data, 20, identBytes, 0, 16);
            String ident = Simple.getUUIDString(identBytes);

            byte[] ackidBytes = new byte[ 16 ];
            System.arraycopy(data, 36, ackidBytes, 0, 16);
            String ackid = Simple.getUUIDString(ackidBytes);

            //
            // Build a virtual json packet from acknowlegdement info.
            //

            JSONObject feedbackMessage = new JSONObject();

            Simple.JSONput(feedbackMessage, "type", "feedbackMessage");
            Simple.JSONput(feedbackMessage, "status", "acks");
            Simple.JSONput(feedbackMessage, "identity", ident);
            Simple.JSONput(feedbackMessage, "idremote", idrem);
            Simple.JSONput(feedbackMessage, "uuid", ackid);

            String ackmess = "JSON" + feedbackMessage.toString();
            data = ackmess.getBytes();
            ptype = new String(data, 0, 4);
        }

        if (! ptype.equals("JSON"))
        {
            //
            // Decrypt failed or packet is junk.
            //

            return;
        }

        try
        {
            //
            // Get json object from sequence after JSON magic.
            //

            JSONObject json = new JSONObject(new String(data, 4, data.length - 4));

            if (Simple.getSharedPrefBoolean("developer.debug.gcmstuff"))
            {
                String pretty = Json.toPretty(json);
                String title = LOGTAG + ":decryptPacket:";

                Simple.makeAlertPost(pretty, title);
            }

            //
            // Dispatch clear message to whom it might concern.
            //

            deliverMessage(json);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private void deliverMessage(JSONObject json)
    {
        if (onMessageReceived(json))
        {
            //
            // Message was handled by comm service itself.
            //

            return;
        }

        if (! json.has("type"))
        {
            //
            // We cannot deliver messages w/o a type.
            //

            return;
        }

        String type = Simple.JSONgetString(json, "type");

        synchronized (messageSubscribers)
        {
            if (messageSubscribers.containsKey(type))
            {
                ArrayList<CommService.CommServiceCallback> callbacks = messageSubscribers.get(type);

                for (CommService.CommServiceCallback callback : callbacks)
                {
                    try
                    {
                        callback.onMessageReceived(json);
                    }
                    catch (Exception ex)
                    {
                        //
                        // Some uncaught exception happened within client.
                        //

                        OopsService.log(LOGTAG, ex);
                    }
                }
            }
        }
    }

    private boolean checkSocket()
    {
        //
        // Check datagram socket.
        //

        if (datagramSocket == null)
        {
            try
            {
                InetAddress serverAddr = InetAddress.getByName(CommonConfigs.CommServerName);
                int serverPort = CommonConfigs.CommServerPort;

                datagramSocket = new NativeSocket();
                datagramPacket = new DatagramPacket(new byte[ 0 ],0);

                datagramPacket.setAddress(serverAddr);
                datagramPacket.setPort(serverPort);

                //
                // Make sure a ping is sent immediately.
                //

                lastping = 0;
            }
            catch (Exception ex)
            {
                Log.d(LOGTAG,"checkSocket: " + ex.getMessage());

                sleeptime = sleeptime * 2;

                return false;
            }
        }

        return true;
    }

    private void sendThread()
    {
        Log.d(LOGTAG, "sendThread: running");

        while (isRunning)
        {
            if (sleeptime > CommonConfigs.CommServerSleepMax)
            {
                sleeptime = CommonConfigs.CommServerSleepMax;
            }

            StaticUtils.sleep(sleeptime);

            //checkPing();
            //CommSender.commTick();

            WebAppCache.commTick();
            EventManager.commTick();
            BatteryManager.commTick();

            SocialTinder.getInstance().commTick();
            SocialTwitter.getInstance().commTick();
            SocialFacebook.getInstance().commTick();
            SocialInstagram.getInstance().commTick();
            SocialGoogleplus.getInstance().commTick();

            if (! checkSocket()) continue;

            //
            // Get message if one present.
            //

            MessageClass mc;

            synchronized (messageBacklog)
            {
                if (messageBacklog.size() == 0) continue;

                mc = messageBacklog.remove(0);
            }

            //
            // Add own identity to message and prepare.
            //

            Simple.JSONput(mc.msg, "identity", SystemIdentity.getIdentity());
            Simple.JSONput(mc.msg, "date", Simple.nowAsISO());

            String body = "JSON" + Simple.JSONdefuck(mc.msg.toString());
            datagramPacket.setData(body.getBytes());

            String idrem = null;
            String ident = null;
            String ackid = null;
            String uuid;

            if ((mc.enc == MessageClass.NONE) && mc.msg.has("type")
                    && Simple.equals(Simple.JSONgetString(mc.msg, "type"), "myip"))
            {
                datagramPacket.setData("MYIP".getBytes());
            }

            if ((mc.enc == MessageClass.NONE) && mc.msg.has("type")
                    && Simple.equals(Simple.JSONgetString(mc.msg, "type"), "pups"))
            {
                datagramPacket.setData("PUPS".getBytes());
            }

            if ((mc.enc == MessageClass.NONE) && mc.msg.has("type")
                    && Simple.equals(Simple.JSONgetString(mc.msg, "type"), "ping"))
            {
                ident = SystemIdentity.getIdentity();

                byte[] ping = new byte[ 4 + 16 ];

                System.arraycopy("PING".getBytes(), 0, ping, 0, 4);
                System.arraycopy(Simple.getUUIDBytes(ident), 0, ping, 4 , 16);

                datagramPacket.setData(ping);
            }

            if ((mc.enc == MessageClass.NONE) && mc.msg.has("idremote"))
            {
                idrem = Simple.JSONgetString(mc.msg, "idremote");
            }

            if ((mc.enc == MessageClass.CLIENT_ACK) && mc.msg.has("uuid"))
            {
                ident = SystemIdentity.getIdentity();
                uuid = Simple.JSONgetString(mc.msg, "uuid");

                byte[] acme = new byte[ 4 + 16 + 16 ];
                System.arraycopy("ACME".getBytes(), 0, acme, 0, 4);
                System.arraycopy(Simple.getUUIDBytes(ident), 0, acme,  4 , 16);
                System.arraycopy(Simple.getUUIDBytes(uuid), 0, acme, 20, 16);

                datagramPacket.setData(acme);
            }

            if ((mc.enc == MessageClass.CRYPT) && mc.msg.has("idremote"))
            {
                //
                // Encrypt message.
                //

                ident = SystemIdentity.getIdentity();
                idrem = Simple.JSONgetString(mc.msg, "idremote");
                byte[] encrypted = CryptUtils.AESencrypt(idrem, body);

                if (encrypted != null)
                {
                    byte[] cryp = new byte[ 4 + 16 + 16 + encrypted.length ];

                    System.arraycopy("CRYP".getBytes(), 0, cryp, 0, 4);
                    System.arraycopy(Simple.getUUIDBytes(ident), 0, cryp,  4 , 16);
                    System.arraycopy(Simple.getUUIDBytes(idrem), 0, cryp, 20 , 16);
                    System.arraycopy(encrypted, 0, cryp, 36, encrypted.length);

                    datagramPacket.setData(cryp);
                }
            }

            if (((mc.enc == MessageClass.CRYPT_WITH_ACK) ||
                    (mc.enc == MessageClass.CRYPT_RELIABLE))
                    && mc.msg.has("idremote") && mc.msg.has("uuid"))
            {
                //
                // Encrypt message with server ack or reliable.
                //

                String mtype = (mc.enc == MessageClass.CRYPT_WITH_ACK) ? "CACK" : "CARL";

                ident = SystemIdentity.getIdentity();
                idrem = Simple.JSONgetString(mc.msg, "idremote");
                ackid = Simple.JSONgetString(mc.msg, "uuid");

                byte[] encrypted = CryptUtils.AESencrypt(idrem, body);

                if (encrypted != null)
                {
                    byte[] cryp = new byte[ 4 + 16 + 16 + 16 + encrypted.length ];

                    System.arraycopy(mtype.getBytes(), 0, cryp, 0, 4);
                    System.arraycopy(Simple.getUUIDBytes(ident), 0, cryp,  4 , 16);
                    System.arraycopy(Simple.getUUIDBytes(idrem), 0, cryp, 20 , 16);
                    System.arraycopy(Simple.getUUIDBytes(ackid), 0, cryp, 36 , 16);
                    System.arraycopy(encrypted, 0, cryp, 52, encrypted.length);

                    datagramPacket.setData(cryp);
                }
            }

            //
            // Send datagram packet.
            //

            try
            {
                //
                // This will NOT fail if server is not alive!
                // Means, packet is lost in this case.
                //

                Log.d(LOGTAG, "sendThread"
                        + ": " + datagramPacket.getData().length
                        + "=" + mc.msg.getString("type")
                        + "=" + idrem + ":" + mc.gcm);

                if (mc.gcm && (idrem != null) && Simple.isGCMInitialized())
                {
                    //
                    // GCM allowed and initialized.
                    //

                    Log.d(LOGTAG,"sendThread: send via GCM");

                    if (GCMMessageService.sendMessage(idrem, datagramPacket.getData()))
                    {
                        Log.d(LOGTAG, "sendThread: GCM message:" + Json.toPretty(mc.msg));

                        if ((mc.enc == MessageClass.CRYPT_WITH_ACK) ||
                                (mc.enc == MessageClass.CRYPT_RELIABLE))
                        {
                            //
                            // We need to send a server ack message to our clients.
                            // With GCM we get the server accepted packate response
                            // when we send the packet upstream. We need to exchange
                            // remote and local identity, since we fake this message.
                            //

                            JSONObject feedbackMessage = new JSONObject();

                            Simple.JSONput(feedbackMessage, "type", "feedbackMessage");
                            Simple.JSONput(feedbackMessage, "status", "acks");
                            Simple.JSONput(feedbackMessage, "identity", idrem);
                            Simple.JSONput(feedbackMessage, "idremote", ident);
                            Simple.JSONput(feedbackMessage, "uuid", ackid);

                            deliverMessage(feedbackMessage);
                        }
                    }
                }
                else
                {
                    datagramSocket.setTTL(200);
                    datagramSocket.send(datagramPacket);
                }

                sleeptime = CommonConfigs.CommServerSleepMin;
            }
            catch (Exception ex)
            {
                Log.d(LOGTAG,"sendThread: " + ex.getMessage());

                //
                // Reschedule message.
                //

                try
                {
                    if (!(mc.msg.has("type") && mc.msg.getString("type").equals("ping")))
                    {
                        synchronized (messageBacklog)
                        {
                            messageBacklog.add(0, mc);
                        }
                    }
                }
                catch (JSONException exex)
                {
                    Log.d(LOGTAG,"sendThread: " + exex.getMessage());
                }

                //
                // Figure out what happened.
                //

                int errno = VersionUtils.getErrno(ex);

                if (errno == OsConstants.ECONNREFUSED)
                {
                    //
                    // Comm receiving server is down.
                    //

                    sleeptime = sleeptime * 2;

                    continue;
                }

                //
                // Network down or reconfigured.
                //

                datagramSocket.close();
                datagramSocket = null;
            }
        }

        Log.d(LOGTAG, "sendThread: finished");

        workerSend = null;
    }

    private void checkPing()
    {
        long now = System.currentTimeMillis();

        try
        {
            JSONObject ping = null;

            if ((lastpups + (CommonConfigs.CommServerPupsSec * 1000)) < now)
            {
                ping = new JSONObject();
                ping.put("type", "pups");

                lastpups = now;
            }
            else
            if ((lastping + (CommonConfigs.CommServerPingSec * 1000)) < now)
            {
                ping = new JSONObject();
                ping.put("type", "ping");

                lastping = now;
            }

            if (ping != null)
            {
                synchronized (messageBacklog)
                {
                    for (int inx = 0; inx < messageBacklog.size(); inx++)
                    {
                        if (messageBacklog.get(inx).msg.has("type") &&
                                messageBacklog.get(inx).msg.getString("type").equals("ping"))
                        {
                            messageBacklog.remove(inx--);
                        }
                    }

                    messageBacklog.add(new MessageClass(ping, MessageClass.NONE, false));
                }
            }
        }
        catch (JSONException ignore)
        {
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    //endregion
}

