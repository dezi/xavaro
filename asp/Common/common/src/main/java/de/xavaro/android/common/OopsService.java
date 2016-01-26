package de.xavaro.android.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.system.OsConstants;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.io.IOException;

//
// Remote logging for "This should never happen" problems
// or other interesting things.
//

public class OopsService extends Service
{
    private static final String LOGTAG = OopsService.class.getSimpleName();

    //region Public methods.

    public static void log(String tag, String message)
    {
        try
        {
            Log.d(tag, message);

            JSONObject msg = new JSONObject();

            msg.put("tag", tag);
            msg.put("msg", message);

            JSONObject log = new JSONObject();
            log.put("log",msg);

            synchronized (messageBacklog)
            {
                if (messageBacklog.size() < 3)
                {
                    messageBacklog.add(log);
                }
            }
        }
        catch (JSONException ignore)
        {
            // Unlikely. Fuckit.
        }
    }

    public static void log(String tag, Exception exception)
    {
        try
        {
            StackTraceElement[] st = exception.getStackTrace();

            Log.e(tag, st[ 0 ].getMethodName() + ": " + exception.getMessage());

            exception.printStackTrace();

            JSONObject err = new JSONObject();

            err.put("tag", tag);
            err.put("msg", exception.getMessage());

            if (VersionUtils.getErrno(exception) > 0)
            {
                err.put("err", VersionUtils.getErrno(exception));
            }

            //
            // Put most recent caller as the one to blame.
            //

            JSONObject ex = new JSONObject();
            ex.put("cn", st[ 0 ].getClassName());
            ex.put("mn", st[ 0 ].getMethodName());
            ex.put("ln", st[ 0 ].getLineNumber());

            err.put("ex", ex);

            //
            // Put at most two callers from own app into dump.
            //

            String appname = CommService.class.getPackage().getName();

            JSONArray bys = new JSONArray();

            for (int inx = 1; inx < st.length; inx++)
            {
                if (! st[ inx ].getClassName().startsWith(appname))
                {
                    continue;
                }

                JSONObject by = new JSONObject();
                by.put("cn", st[ inx ].getClassName());
                by.put("mn", st[ inx ].getMethodName());
                by.put("ln", st[ inx ].getLineNumber());

                bys.put(by);

                if (bys.length() >= 2) break;
            }

            err.put("by", bys);

            JSONObject error = new JSONObject();
            error.put("error",err);

            synchronized (messageBacklog)
            {
                if (messageBacklog.size() < 3)
                {
                    messageBacklog.add(error);
                }
            }
        }
        catch (JSONException ignore)
        {
            // Manno. Fuckit.
        }
    }

    //endregion

    //region Service start/stop.

    private boolean running = false;
    private Thread worker = null;

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

    //region Message remote handling.

    private static final ArrayList<JSONObject> messageBacklog = new ArrayList<>();

    private void workerThread()
    {
        Log.d(LOGTAG, "workerThread: running");

        long sleeptime = CommonConfigs.OopsServerSleepMin;

        DatagramSocket datagramSocket = null;
        DatagramPacket datagramPacket = null;
        InetAddress serverAddr;
        int serverPort;

        while (running)
        {
            if (sleeptime > CommonConfigs.OopsServerSleepMax)
            {
                sleeptime = CommonConfigs.OopsServerSleepMax;
            }

            StaticUtils.sleep(sleeptime);

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

                msg.put("identity",SystemIdentity.getIdentity());
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
                    serverAddr = InetAddress.getByName(CommonConfigs.OopsServerName);
                    serverPort = CommonConfigs.OopsServerPort;

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

            String body = "JSON" + Simple.JSONdefuck(msg.toString());
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

                sleeptime = CommonConfigs.OopsServerSleepMin;

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
                    // Oops receiving server is down.
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

    //endregion

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
