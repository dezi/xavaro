package de.xavaro.android.common;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.system.OsConstants;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

    private static final ArrayList<JSONObject> messageBacklog = new ArrayList<>();

    private static final Map<String, ArrayList<CommServiceCallback>> messageSubscribers = new HashMap<>();

    public static void sendMessage(JSONObject message)
    {
        synchronized (messageBacklog)
        {
            messageBacklog.add(message);
        }
    }

    public static void subscribeMessage(String type, CommServiceCallback callback)
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

    public static void unsubscribeMessage(String type, CommServiceCallback callback)
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

    NativeSocket datagramSocket = null;
    DatagramPacket datagramPacket = null;
    InetAddress serverAddr;
    int serverPort;

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
                responsePublicKeyXChange.put("remoteIdentity", remoteIdentity);
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
                responseAESpassXChange.put("remoteIdentity", remoteIdentity);
                responseAESpassXChange.put("status", "success");

                CommService.sendMessage(responseAESpassXChange);

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
        String recvString = new String(recvPacket.getData());

        if (! recvString.startsWith("JSON")) return;

        try
        {
            JSONObject json = new JSONObject(recvString.substring(4));

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

    private void sendThread()
    {
        Log.d(LOGTAG, "sendThread: running");

        long sleeptime = CommonConfigs.CommServerSleepMin;

        while (running)
        {
            if (sleeptime > CommonConfigs.CommServerSleepMax)
            {
                sleeptime = CommonConfigs.CommServerSleepMax;
            }

            StaticUtils.sleep(sleeptime);

            checkPing();

            //
            // Get message if one present.
            //

            JSONObject msg;

            synchronized (messageBacklog)
            {
                if (messageBacklog.size() == 0) continue;

                msg = messageBacklog.remove(0);
            }

            //
            // Add identity to message.
            //

            try
            {
                String[] packageName = getApplicationContext().getPackageName().split("\\.");

                msg.put("identity",SystemIdentity.identity);
                msg.put("app",packageName[ packageName.length - 1 ]);
            }
            catch (JSONException ignore)
            {
            }

            //
            // Check datagram socket.
            //

            if (datagramSocket == null)
            {
                try
                {
                    serverAddr = InetAddress.getByName(CommonConfigs.CommServerName);
                    serverPort = CommonConfigs.CommServerPort;

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
                    Log.d(LOGTAG,"sendThread: " + ex.getMessage());

                    //
                    // Reschedule message.
                    //

                    try
                    {
                        if (!(msg.has("type") && msg.getString("type").equals("ping")))
                        {
                            synchronized (messageBacklog)
                            {
                                messageBacklog.add(0, msg);
                            }
                        }
                    }
                    catch (JSONException exex)
                    {
                        Log.d(LOGTAG,"sendThread: " + exex.getMessage());
                    }

                    sleeptime = sleeptime * 2;

                    continue;
                }
            }

            //
            // Assemble packet.
            //

            String body = "JSON" + StaticUtils.defuckJSON(msg.toString());
            byte[] data = body.getBytes();

            datagramPacket.setData(data);

            //
            // Send datagram packet.
            //

            try
            {
                //
                // This will NOT fail if server is not alive!
                // Means, packet is lost in this case.
                //

                if (msg.has("type"))
                {
                    if (msg.getString("type").equals("pups"))
                    {
                        //
                        // Keep NAT connection alive with bogus packet.
                        //

                        datagramSocket.setTTL(4);
                        datagramPacket.setData("PUPS".getBytes());
                        datagramSocket.send(datagramPacket);
                    }
                    else
                    {
                        Log.d(LOGTAG, "sendThread: " + msg.getString("type"));
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
                    if (!(msg.has("type") && msg.getString("type").equals("ping")))
                    {
                        synchronized (messageBacklog)
                        {
                            messageBacklog.add(0, msg);
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

    private long lastping = 0;
    private long lastpups = 0;

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
                        if (messageBacklog.get(inx).has("type") &&
                                messageBacklog.get(inx).getString("type").equals("ping"))
                        {
                            messageBacklog.remove(inx--);
                        }
                    }

                    messageBacklog.add(ping);
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

