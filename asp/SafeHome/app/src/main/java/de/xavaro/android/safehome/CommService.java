package de.xavaro.android.safehome;

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

//
// Service to communicate with backend servers
// via datagramn packets.
//

public class CommService extends Service
{
    private static final String LOGTAG = CommService.class.getSimpleName();

    //
    // Worker background thread.
    //

    private Thread workerSend = null;
    private Thread workerRecv = null;

    private boolean running = false;

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

    //endregion

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

    private static final ArrayList<JSONObject> messageBacklog = new ArrayList<>();

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
            }
            catch (IOException ex)
            {
                Log.d(LOGTAG,"recvThread: " + ex.getMessage());
            }
        }

        Log.d(LOGTAG, "recvThread: finished");

        workerRecv = null;
    }

    private void sendThread()
    {
        Log.d(LOGTAG, "sendThread: running");

        long sleeptime = GlobalConfigs.CommServerSleepMin;

        while (running)
        {
            if (sleeptime > GlobalConfigs.CommServerSleepMax)
            {
                sleeptime = GlobalConfigs.CommServerSleepMax;
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
                msg.put("identity",SystemIdentity.identity);
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
                    serverAddr = InetAddress.getByName(GlobalConfigs.CommServerName);
                    serverPort = GlobalConfigs.CommServerPort;

                    datagramSocket = new NativeSocket();
                    datagramPacket = new DatagramPacket(new byte[ 0 ],0);

                    datagramPacket.setAddress(serverAddr);
                    datagramPacket.setPort(serverPort);

                    //
                    // Make sure a ping is sent immedeately.
                    //

                    lastping = 0;
                }
                catch (Exception ex)
                {
                    Log.d(LOGTAG,"sendThread: " + ex.getMessage());

                    //
                    // Reschedule message.
                    //
                    if (! msg.has("ping"))
                    {
                        synchronized (messageBacklog)
                        {
                            messageBacklog.add(0, msg);
                        }
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

                if (msg.has("pups"))
                {
                    Log.d(LOGTAG, "ispups");

                    datagramSocket.setTTL(5);
                    datagramSocket.send(datagramPacket);
                }
                else
                {
                    datagramSocket.setTTL(200);
                    datagramSocket.send(datagramPacket);
                }

                sleeptime = GlobalConfigs.CommServerSleepMin;

                Log.d(LOGTAG,"Send one message.");
            }
            catch (IOException ex)
            {
                Log.d(LOGTAG,"sendThread: " + ex.getMessage());

                //
                // Reschedule message.
                //

                if (! msg.has("ping"))
                {
                    synchronized (messageBacklog)
                    {
                        messageBacklog.add(0, msg);
                    }
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

            if ((lastpups + (GlobalConfigs.CommServerPupsSec * 1000)) < now)
            {
                ping = new JSONObject();
                ping.put("pups", new JSONObject());

                lastpups = now;
            }
            else
            if ((lastping + (GlobalConfigs.CommServerPingSec * 1000)) < now)
            {
                ping = new JSONObject();
                ping.put("ping", new JSONObject());

                lastping = now;
            }

            if (ping != null)
            {
                synchronized (messageBacklog)
                {
                    for (int inx = 0; inx < messageBacklog.size(); inx++)
                    {
                        if (messageBacklog.get(inx).has("ping"))
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
        CommService getService()
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

