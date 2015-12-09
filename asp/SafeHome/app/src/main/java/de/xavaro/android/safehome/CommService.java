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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    private Thread worker = null;

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

        if (worker == null)
        {
            worker = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    workerThread();
                }
            });

            worker.start();
        }

        return Service.START_NOT_STICKY;
    }

    //endregion

    private static final ArrayList<JSONObject> messageBacklog = new ArrayList<>();

    private void workerThread()
    {
        Log.d(LOGTAG, "workerThread: running");

        long sleeptime = GlobalConfigs.CommServerSleepMin;

        DatagramSocket datagramSocket = null;
        DatagramPacket datagramPacket = null;
        InetAddress serverAddr;
        int serverPort;

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

                    datagramSocket = new DatagramSocket();
                    datagramPacket = new DatagramPacket(new byte[ 0 ],0);

                    datagramPacket.setAddress(serverAddr);
                    datagramPacket.setPort(serverPort);
                }
                catch (UnknownHostException | SocketException ex)
                {
                    Log.d(LOGTAG,"workerThread: " + ex.getMessage());

                    //
                    // Reschedule message.
                    //

                    synchronized (messageBacklog)
                    {
                        messageBacklog.add(0,msg);
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

                datagramSocket.send(datagramPacket);

                sleeptime = GlobalConfigs.CommServerSleepMin;

                Log.d(LOGTAG,"Send one message.");
            }
            catch (IOException ex)
            {
                Log.d(LOGTAG,"workerThread: " + ex.getMessage());

                //
                // Reschedule message.
                //

                synchronized (messageBacklog)
                {
                    messageBacklog.add(0,msg);
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

        Log.d(LOGTAG, "workerThread: finished");

        worker = null;
    }

    private long lastping = 0;

    private void checkPing()
    {
        long now = System.currentTimeMillis();

        if ((lastping + (GlobalConfigs.CommServerPingSec * 1000)) > now) return;

        try
        {
            JSONObject ping = new JSONObject();
            ping.put("ping", new JSONObject());

            synchronized (messageBacklog)
            {
                boolean duplicate = false;

                for (JSONObject msg : messageBacklog)
                {
                    if (msg.has("ping"))
                    {
                        duplicate = true;
                        break;
                    }
                }

                if (! duplicate) messageBacklog.add(ping);
            }
        }
        catch (JSONException ignore)
        {
        }

        lastping = now;
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

