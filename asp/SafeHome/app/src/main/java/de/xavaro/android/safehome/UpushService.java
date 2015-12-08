package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Service to communicate with backend servers
 * via UPD packets.
 */

public class UpushService extends Service
{
    private static final String LOGTAG = UpushService.class.getSimpleName();

    //
    // Binder service.
    //

    private final IBinder binder = new UpushBinder();

    //
    // Watch dog background thread.
    //

    private Thread wdThread = null;

    private boolean running = false;

    private static final long INTERVAL = 5000;

    //region Static singleton methods.

    private static UpushService myService;

    public static void setInstance(UpushService service)
    {
        myService = service;
    }

    @Nullable
    public static UpushService getInstance()
    {
        return myService;
    }

    public static void RLog(String tag, String message)
    {
        if (myService == null) return;

        myService.sendMessage(tag,message);
    }

    //endregion

    private InetAddress serverAddr;
    private DatagramSocket datagramSocket;

    private void sendMessage(String tag, String message)
    {
        if (datagramSocket == null) return;

        String body = tag + ":" + message;

        byte[] data = body.getBytes();

        DatagramPacket packet = new DatagramPacket(data,data.length);

        try
        {
            datagramSocket.send(packet);

            Log.d(LOGTAG,"sendMessage: " + body);
        }
        catch (IOException ex)
        {
            OopsHandler.Log(LOGTAG,ex);

            int errno = -1;

            Throwable errnoex = ex.getCause();

            if (errnoex instanceof ErrnoException)
            {
                errno = ((ErrnoException) errnoex).errno;
            }

            //OsConstants.ECONNREFUSED
            //OsConstants.EINVAL
            //OsConstants.ENETUNREACH

            Log.d(LOGTAG, "SEND: (" + errno + ")" + ex.getMessage());
        }
    }

    //region Overriden methods.

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate();

        running = true;
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy...");

        running = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(LOGTAG, "onStartCommand...");

        if (wdThread == null)
        {
            try
            {
                datagramSocket = new DatagramSocket();

                InetSocketAddress socketAdress = (InetSocketAddress) datagramSocket.getLocalSocketAddress();

                Log.d(LOGTAG,socketAdress.getAddress().toString() + ":" + socketAdress.getPort());

                StaticUtils.getMACAddress("");

                try
                {
                    serverAddr = InetAddress.getByName("www.xavaro.de");
                }
                catch (UnknownHostException exception)
                {
                }

                datagramSocket.connect(serverAddr, 42742);
            }
            catch (SocketException ex)
            {
                ex.printStackTrace();
            }

            running = true;

            Log.d(LOGTAG, "onStartCommand: starting watchdog thread.");

            wdThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (running)
                    {
                        /*
                        if (datagramSocket != null)
                        {
                            byte[] buffer = new byte[ 2048 ];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                            try
                            {
                                datagramSocket.receive(packet);

                                Log.d(LOGTAG, "receive="
                                        + packet.getAddress().toString() + ":"
                                        + packet.getPort() + "="
                                        + packet.getLength());
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        */
                        {
                            try
                            {
                                Thread.sleep(INTERVAL);

                            } catch (InterruptedException e)
                            {
                                Log.d(LOGTAG, "wdThread: sleep interrupted.");
                            }
                        }
                    }
                }
            });

            wdThread.start();
        }

        return Service.START_NOT_STICKY;
    }

    //endregion

    //region Binder methods.

    //
    // Binder stuff allows anyobe to call methods here.
    //

    public class UpushBinder extends Binder
    {
        UpushService getService()
        {
            return UpushService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    //endregion
}

