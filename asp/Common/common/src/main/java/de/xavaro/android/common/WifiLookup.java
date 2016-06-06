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
    private static boolean visible;

    public static void makeVisible()
    {
        createSocket();
        visible = true;
    }

    public static void removeVisibility()
    {
        visible = false;
    }

    public static void deleteSocket()
    {
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

            JSONObject helomessage = new JSONObject();

            Json.put(helomessage, "type", "HELO");
            Json.put(helomessage, "identity", SystemIdentity.getIdentity());
            Json.put(helomessage, "gcmtoken", CommonStatic.gcm_token);

            byte[] txbuf = helomessage.toString().getBytes();
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
                    //Log.d(LOGTAG, "waiting...");

                    byte[] rxbuf = new byte[ 8192 ];
                    DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    //Log.d(LOGTAG, "received=" + message);

                    if (! message.startsWith("{")) continue;
                    JSONObject jmess = Json.fromString(message);
                    if (jmess == null) continue;

                    if (Json.equals(jmess, "type", "HELO"))
                    {
                        if (visible)
                        {
                            //
                            // Register identity of caller temporary.
                            //

                            String idremote = Json.getString(jmess, "identity");
                            String gcmtoken = Json.getString(jmess, "gcmtoken");
                            if ((idremote == null) || (gcmtoken == null)) continue;

                            RemoteContacts.setGCMTokenTemp(idremote, gcmtoken);

                            //
                            // Reply with own information.
                            //

                            JSONObject mejson = new JSONObject();
                            Json.put(mejson, "type", "MEME");
                            Json.put(mejson, "idremote", SystemIdentity.getIdentity());
                            RemoteContacts.deliverOwnContact(mejson);

                            byte[] txbuf = mejson.toString().getBytes();
                            DatagramPacket me = new DatagramPacket(txbuf, txbuf.length);
                            me.setAddress(multicastAddress);
                            me.setPort(port);

                            socket.send(me);
                        }
                    }

                    if (Json.equals(jmess, "type", "MEME"))
                    {
                        if (retrieved != null)
                        {
                            String selfident = SystemIdentity.getIdentity();
                            String idremote = Json.getString(jmess, "idremote");

                            if ((idremote != null) && ! idremote.equals(selfident))
                            {
                                synchronized (LOGTAG)
                                {
                                    Json.put(retrieved, jmess);
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