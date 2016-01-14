package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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

    //region Static singleton methods.

    private static CommService myService;

    public static void setInstance(CommService service)
    {
        myService = service;
    }

    @Nullable
    public static CommService getInstance()
    {
        return myService;
    }

    private static final ArrayList<MessageClass> messageBacklog = new ArrayList<>();

    private static final Map<String, ArrayList<CommServiceCallback>> messageSubscribers = new HashMap<>();

    private static class MessageClass
    {
        public static final int ENCRYPT_NONE = 0;
        public static final int ENCRYPT_CRYPT = 1;
        public static final int ENCRYPT_CRYPT_WITH_ACK = 2;

        public final JSONObject msg;
        public final int enc;

        public MessageClass(JSONObject message, int encrypt)
        {
            this.msg = message;
            this.enc = encrypt;
        }
    }

    public static void sendMessage(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.ENCRYPT_NONE));
        }
    }

    public static void sendEncrypted(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.ENCRYPT_CRYPT));
        }
    }

    public static void sendEncryptedWithAck(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(new MessageClass(message, MessageClass.ENCRYPT_CRYPT_WITH_ACK));
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

    private Thread workerSend = null;
    private Thread workerRecv = null;

    private boolean running = false;


    //region Overriden methods.

    @Override
    public void onCreate()
    {
        super.onCreate();

        ChatManager.initialize(getApplicationContext());

        running = true;
    }

    @Override
    public void onDestroy()
    {
        running = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        running = true;

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

        return Service.START_NOT_STICKY;
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

        while (running)
        {
            StaticUtils.sleep(1000);

            if (datagramSocket == null) continue;

            try
            {
                byte[] recvBytes = new byte[ 4096 ];
                DatagramPacket recvPacket = new DatagramPacket(recvBytes, recvBytes.length);

                datagramSocket.receive(recvPacket);

                Log.d(LOGTAG, "recvThread: received:" + recvPacket.getLength());

                //
                // Deliver message to subscribers callback.
                //

                deliverPacket(recvPacket);
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
    // Messages handled directly by comm service.
    //

    private boolean onMessageReceived(JSONObject json)
    {
        try
        {
            if (! json.has("type")) return false;

            String type = json.getString("type");

            if (type.equals("requestPublicKeyXChange"))
            {
                String remoteIdentity = json.getString("identity");
                String remotePublicKey = json.getString("publicKey");

                IdentityManager.getInstance().put(remoteIdentity, "publicKey", remotePublicKey);

                Log.d(LOGTAG, "onMessageReceived: requestPublicKeyXChange"
                        + " remoteIdentity=" + remoteIdentity
                        + " remotePublicKey=" + remotePublicKey);

                JSONObject responsePublicKeyXChange = new JSONObject();

                responsePublicKeyXChange.put("type", "responsePublicKeyXChange");
                responsePublicKeyXChange.put("idremote", remoteIdentity);
                responsePublicKeyXChange.put("publicKey", CryptUtils.RSAgetPublicKey(getApplicationContext()));
                responsePublicKeyXChange.put("status", "success");

                CommService.sendMessage(responsePublicKeyXChange);

                return true;
            }

            if (type.equals("requestAESpassXChange"))
            {
                String remoteIdentity = json.getString("identity");
                String encoPassPhrase = json.getString("encodedPassPhrase");
                String privateKey = CryptUtils.RSAgetPrivateKey(getApplicationContext());
                String passPhrase = CryptUtils.RSADecrypt(privateKey, encoPassPhrase);

                IdentityManager.getInstance().put(remoteIdentity, "passPhrase", passPhrase);

                Log.d(LOGTAG, "onMessageReceived: requestAESpassXChange"
                        + " remoteIdentity=" + remoteIdentity
                        + " passPhrase=" + passPhrase);

                JSONObject responseAESpassXChange = new JSONObject();

                responseAESpassXChange.put("type", "responseAESpassXChange");
                responseAESpassXChange.put("idremote", remoteIdentity);
                responseAESpassXChange.put("status", "success");

                CommService.sendEncrypted(responseAESpassXChange);

                return true;
            }

            if (type.equals("requestOwnerIdentity"))
            {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                String remoteIdentity = json.getString("identity");

                RemoteContacts.registerContact(json);

                //
                // Build response with own identiy.
                //

                JSONObject responseOwnerIdentity = new JSONObject();

                responseOwnerIdentity.put("type", "responseOwnerIdentity");
                responseOwnerIdentity.put("idremote", remoteIdentity);
                responseOwnerIdentity.put("status", "success");

                responseOwnerIdentity.put("appName", Simple.getAppName());
                responseOwnerIdentity.put("devName", Simple.getDeviceName());
                responseOwnerIdentity.put("macAddr", Simple.getMacAddress());
                responseOwnerIdentity.put("ownerFirstName", sp.getString("owner.firstname", null));
                responseOwnerIdentity.put("ownerGivenName", sp.getString("owner.givenname", null));

                CommService.sendEncrypted(responseOwnerIdentity);

                return true;
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    private void deliverPacket(DatagramPacket recvPacket)
    {
        byte[] data = recvPacket.getData();
        String ptype = new String(data, 0, 4);

        if (ptype.equals("CRYP"))
        {
            byte[] identBytes = new byte[ 16 ];
            System.arraycopy(data, 4, identBytes, 0, 16);

            String ident = StaticUtils.getUUIDString(identBytes);

            byte[] rest = new byte[ data.length - 36 ];
            System.arraycopy(data, 36, rest, 0, rest.length);

            data = CryptUtils.AESdecrypt(ident, rest);
            if (data == null) return;

            ptype = new String(data, 0, 4);
        }

        if (ptype.equals("CACK"))
        {
            byte[] identBytes = new byte[ 16 ];
            System.arraycopy(data, 4, identBytes, 0, 16);
            String ident = StaticUtils.getUUIDString(identBytes);

            byte[] idremBytes = new byte[ 16 ];
            System.arraycopy(data, 20, idremBytes, 0, 16);
            String idrem = StaticUtils.getUUIDString(idremBytes);

            byte[] ackidBytes = new byte[ 16 ];
            System.arraycopy(data, 36, ackidBytes, 0, 16);
            String ackid = StaticUtils.getUUIDString(ackidBytes);

            JSONObject serverAckMessage = new JSONObject();

            try
            {
                serverAckMessage.put("type", "serverAckMessage");
                serverAckMessage.put("identity", ident);
                serverAckMessage.put("idremote", idrem);
                serverAckMessage.put("uuid", ackid);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);

                return;
            }

            String ackmess = "JSON" + serverAckMessage.toString();

            data = ackmess.getBytes();
            ptype = new String(data, 0, 4);
        }

        if (! ptype.equals("JSON")) return;

        try
        {
            JSONObject json = new JSONObject(new String(data, 4, data.length - 4));

            if (! onMessageReceived(json))
            {
                if (! json.has("type")) return;

                String type = json.getString("type");

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
                                OopsService.log(LOGTAG, ex);
                            }
                        }
                    }
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
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

        while (running)
        {
            if (sleeptime > CommonConfigs.CommServerSleepMax)
            {
                sleeptime = CommonConfigs.CommServerSleepMax;
            }

            StaticUtils.sleep(sleeptime);

            checkPing();

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
            // Add identity to message.
            //

            try
            {
                mc.msg.put("identity", SystemIdentity.identity);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            try
            {
                String body = "JSON" + StaticUtils.defuckJSON(mc.msg.toString());
                datagramPacket.setData(body.getBytes());

                if ((mc.enc == MessageClass.ENCRYPT_CRYPT) && mc.msg.has("idremote"))
                {
                    //
                    // Encrypt message.
                    //

                    String ident = SystemIdentity.identity;
                    String remid = mc.msg.getString("idremote");
                    byte[] encrypted = CryptUtils.AESencrypt(remid, body);

                    if (encrypted != null)
                    {
                        byte[] cryp = new byte[ 4 + 16 + 16 + encrypted.length ];

                        System.arraycopy("CRYP".getBytes(), 0, cryp, 0, 4);
                        System.arraycopy(StaticUtils.getUUIDBytes(ident), 0, cryp,  4 , 16);
                        System.arraycopy(StaticUtils.getUUIDBytes(remid), 0, cryp, 20 , 16);
                        System.arraycopy(encrypted, 0, cryp, 36, encrypted.length);

                        datagramPacket.setData(cryp);
                    }
                }

                if ((mc.enc == MessageClass.ENCRYPT_CRYPT_WITH_ACK)
                        && mc.msg.has("idremote") && mc.msg.has("uuid"))
                {
                    //
                    // Encrypt message.
                    //

                    String ident = SystemIdentity.identity;
                    String remid = mc.msg.getString("idremote");
                    String ackid = mc.msg.getString("uuid");

                    byte[] encrypted = CryptUtils.AESencrypt(remid, body);

                    if (encrypted != null)
                    {
                        byte[] cryp = new byte[ 4 + 16 + 16 + 16 + encrypted.length ];

                        System.arraycopy("CACK".getBytes(), 0, cryp, 0, 4);
                        System.arraycopy(StaticUtils.getUUIDBytes(ident), 0, cryp,  4 , 16);
                        System.arraycopy(StaticUtils.getUUIDBytes(remid), 0, cryp, 20 , 16);
                        System.arraycopy(StaticUtils.getUUIDBytes(ackid), 0, cryp, 36 , 16);
                        System.arraycopy(encrypted, 0, cryp, 52, encrypted.length);

                        datagramPacket.setData(cryp);
                    }
                }

            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
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

                if (mc.msg.has("type"))
                {
                    if (mc.msg.getString("type").equals("pups"))
                    {
                        //
                        // Keep NAT connection alive with bogus packet.
                        //

                        datagramSocket.setTTL(200);
                        datagramPacket.setData("PUPS".getBytes());
                        datagramSocket.send(datagramPacket);
                    }
                    else
                    {
                        if (mc.msg.getString("type").equals("ping"))
                        {
                            byte[] ping = new byte[ 4 + 16 ];
                            byte[] uuid = StaticUtils.getUUIDBytes(SystemIdentity.identity);

                            System.arraycopy("PING".getBytes(), 0, ping, 0, 4);
                            System.arraycopy(uuid, 0, ping, 4 , 16);

                            datagramPacket.setData(ping);
                        }

                        Log.d(LOGTAG, "sendThread: " + datagramPacket.getData().length + "=" + mc.msg.getString("type"));
                        datagramSocket.setTTL(200);
                        datagramSocket.send(datagramPacket);
                    }
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

                    messageBacklog.add(new MessageClass(ping, MessageClass.ENCRYPT_NONE));
                }
            }
        }
        catch (JSONException ignore)
        {
        }
    }

    //region Binder methods.

    //
    // Binder stuff allows anyone to call methods here.
    //

    private final IBinder binder = new CommBinder();

    public class CommBinder extends Binder
    {
        public CommService getService()
        {
            return CommService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    //endregion
}

