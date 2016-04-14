package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class WifiLookup
{
    private static final String LOGTAG = WifiLookup.class.getSimpleName();

    private static final int port = 42742;
    private static InetAddress multicastAddress;
    private static MulticastSocket socket;
    private static WifiLookupWorker worker;
    private static JSONArray retrieved;

    public static void makeVisible()
    {
        try
        {
            Log.d(LOGTAG,"makeVisible");

            createSocket();
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void removeVisibility()
    {
        Log.d(LOGTAG,"removeVisibility");

        if (worker != null)
        {
            worker.terminate();
            worker = null;
        }

        if (socket != null)
        {
            socket.close();
            socket = null;
        }
    }

    public static void findVisible()
    {
        try
        {
            createSocket();

            retrieved = new JSONArray();

            byte[] txbuf = "HELO".getBytes();
            DatagramPacket helo = new DatagramPacket(txbuf, txbuf.length);
            helo.setAddress(multicastAddress);
            helo.setPort(port);

            socket.send(helo);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static JSONArray getVisible()
    {
        if (retrieved != null)
        {
            synchronized (LOGTAG)
            {
                return Json.clone(retrieved);
            }
        }

        return new JSONArray();
    }

    private static void createSocket()
    {
        try
        {
            if (socket == null)
            {
                multicastAddress = InetAddress.getByName("239.255.255.250");

                socket = new MulticastSocket(port);
                socket.setReuseAddress(true);
                socket.setSoTimeout(15000);
                socket.joinGroup(multicastAddress);
            }

            if (worker == null)
            {
                worker = new WifiLookupWorker();
                worker.start();
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static class WifiLookupWorker extends Thread
    {
        private final String LOGTAG = WifiLookupWorker.class.getSimpleName();

        private boolean running;

        public WifiLookupWorker()
        {
            running = true;
        }

        public void terminate()
        {
            running = false;
        }

        @Override
        public void run()
        {
            while (running)
            {
                try
                {
                    Log.d(LOGTAG, "waiting...");

                    byte[] rxbuf = new byte[ 8192 ];
                    DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    Log.d(LOGTAG, "received=" + message);

                    if (message.equals("HELO"))
                    {
                        JSONObject mejson = new JSONObject();
                        Json.put(mejson, "identity", SystemIdentity.getIdentity());
                        RemoteContacts.deliverOwnContact(mejson);

                        byte[] txbuf = mejson.toString().getBytes();
                        DatagramPacket me = new DatagramPacket(txbuf, txbuf.length);
                        me.setAddress(multicastAddress);
                        me.setPort(port);

                        socket.send(me);
                    }
                    else
                    {
                        JSONObject contact = Json.fromString(message);

                        if ((contact != null) && (retrieved != null))
                        {
                            String selfident = SystemIdentity.getIdentity();
                            String identity = Json.getString(contact, "identity");

                            if ((identity != null) && ! identity.equals(selfident))
                            {
                                synchronized (LOGTAG)
                                {
                                    Json.put(retrieved, contact);
                                }
                            }
                        }
                    }
                }
                catch (Exception ignore)
                {
                    //
                    // Either timeout or closed.
                    //
                }
            }

            Log.d(LOGTAG, "exitted.");
            worker = null;
        }
    }
}