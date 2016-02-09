package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CommSender
{
    private static final String LOGTAG = CommSender.class.getSimpleName();

    public static final ArrayList<JSONObject> sendFiles = new ArrayList<>();

    private static int privatePort;
    private static int publicPort;

    private static ServerSocket serverSocket;
    private static Thread serverThread;

    private static long ipRequestTime;
    private static long gwRequestTime;
    private static long upnpRequestTime;
    private static long natpmpRequestTime;
    private static long transnegRequestTime;

    public static void initialize()
    {
        if (SystemIdentity.getIdentity().equals("dddb5240-66b4-4561-b197-fb921442d283"))
        {
            sendFile("blablatest.png", "0c47a9e4-254b-4533-b900-d5e53c82435b");
        }
    }

    public static void sendFile(String filename, String idremote)
    {
        JSONObject request = new JSONObject();

        Json.put(request, "filename", filename);
        Json.put(request, "idremote", idremote);

        synchronized (sendFiles)
        {
            sendFiles.add(request);
        }
    }

    public static void commTick()
    {
        long now = new Date().getTime();

        //
        // Create a server socket and private port.
        //

        if (privatePort == 0)
        {
            try
            {
                serverSocket = new ServerSocket(0);
                privatePort = serverSocket.getLocalPort();
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        //
        // Request current GW-Address.
        //

        if (CommonStatic.gwIPaddress == null)
        {
            if ((now - gwRequestTime) > 100000)
            {
                CommonStatic.gwIPaddress = Simple.getDefaultGatewayAddress();
                CommonStatic.privateIPaddress = Simple.getWifiIPAddress();
                CommonStatic.wifiName = Simple.getWifiName();

                Log.d(LOGTAG, "commTick: request GW-Address: " + CommonStatic.gwIPaddress);
                Log.d(LOGTAG, "commTick: request IP-Address: " + CommonStatic.privateIPaddress);
                Log.d(LOGTAG, "commTick: request Wifi-Name: " + CommonStatic.wifiName);

                gwRequestTime = now;
            }

            return;
        }

        //
        // Check if NAT-PMP is possible.
        //

        if (publicPort == 0)
        {
            if ((now - natpmpRequestTime) > 100000)
            {
                if (checkNATPMP())
                {
                    Log.d(LOGTAG, "commTick: request public port: " + publicPort);
                    Log.d(LOGTAG, "commTick: request private port: " + privatePort);
                }

                natpmpRequestTime = now;

                return;
            }
        }

        //
        // Check if Upnp is possible.
        //

        if (publicPort == 0)
        {
            if ((now - upnpRequestTime) > 10000)
            {
                if (checkUPNP())
                {
                    Log.d(LOGTAG, "commTick: request public port: " + publicPort);
                    Log.d(LOGTAG, "commTick: request private port: " + privatePort);
                }

                upnpRequestTime = now;

                return;
            }
        }

        //
        // Request current public IP-Address.
        //

        if (CommonStatic.publicIPaddress == null)
        {
            if ((now - ipRequestTime) > 10000)
            {
                Log.d(LOGTAG, "commTick: request IP-Address");

                JSONObject myip = new JSONObject();
                Json.put(myip, "type", "myip");

                CommService.sendMessage(myip);

                ipRequestTime = now;
            }

            return;
        }

        if (sendFiles.size() == 0) return;

        //
        // Start server thread if required.
        //

        if (serverThread == null)
        {
            serverThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    serverLoop();
                }
            });

            serverThread.start();
        }

        //
        // Setup transfer negotiation packet.
        //

        if ((now - transnegRequestTime) > 10000)
        {
            String idremote;

            synchronized (sendFiles)
            {
                idremote = Json.getString(sendFiles.get(0), "idremote");
            }

            JSONObject fileTransferNeg = new JSONObject();

            Json.put(fileTransferNeg, "type", "fileTransferNeg");
            Json.put(fileTransferNeg, "idremote", idremote);
            Json.put(fileTransferNeg, "wifiName", CommonStatic.wifiName);
            Json.put(fileTransferNeg, "publicIP", CommonStatic.publicIPaddress);
            Json.put(fileTransferNeg, "privateIP", CommonStatic.privateIPaddress);
            Json.put(fileTransferNeg, "defaultGW", CommonStatic.gwIPaddress);
            Json.put(fileTransferNeg, "publicPort", publicPort);
            Json.put(fileTransferNeg, "privatePort", privatePort);

            Log.d(LOGTAG, "commTick: transneg" + fileTransferNeg.toString());

            CommService.sendEncrypted(fileTransferNeg, true);

            transnegRequestTime = now;
        }
    }

    private static void serverLoop()
    {
        try
        {
            //
            // Proxy HTTP server loop.
            //

            while (true)
            {
                Log.d(LOGTAG, "Waiting on port " + privatePort);

                Socket connect = serverSocket.accept();

                Log.d(LOGTAG, "Accepted connection on port " + privatePort);
            }
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            serverSocket.close();
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
        finally
        {
            serverSocket = null;
            serverThread = null;
        }
    }

    private static boolean checkUPNP()
    {
        final String DISCOVER_MESSAGE_ROOTDEVICE =
                "M-SEARCH * HTTP/1.1\r\n" +
                        "ST: upnp:rootdevice\r\n" +
                        "MX: 3\r\n" +
                        "MAN: ssdp:discover\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "\r\n";

        try
        {
            InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");

            final int port = 1900;
            MulticastSocket socket = new MulticastSocket(port);
            socket.setReuseAddress(true);
            socket.setSoTimeout(1000);
            socket.joinGroup(multicastAddress);

            byte[] txbuf = DISCOVER_MESSAGE_ROOTDEVICE.getBytes();
            DatagramPacket hi = new DatagramPacket(txbuf, txbuf.length, multicastAddress, port);
            socket.send(hi);

            Log.d(LOGTAG, "checkUPNP: SSDP discover sent");

            while (true)
            {
                byte[] rxbuf = new byte[ 8192 ];
                DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                socket.receive(packet);

                Log.d(LOGTAG, "checkUPNP: SSDP discover=" + rxbuf.length);
            }
        }
        catch (Exception ignore)
        {
            Log.d(LOGTAG,"checkUpnp: not available");
        }

        return false;
    }

    private static boolean checkNATPMP()
    {
        try
        {
            DatagramSocket dgramSock = new DatagramSocket();
            dgramSock.setSoTimeout(1000);

            InetAddress gwip = InetAddress.getByName(CommonStatic.gwIPaddress);

            byte[] pareqdata = new byte[] { 0, 0 };
            DatagramPacket pareq = new DatagramPacket(pareqdata, pareqdata.length, gwip, 5351);
            dgramSock.send(pareq);

            byte[] paresbuf = new byte[ 12 ];
            DatagramPacket pares = new DatagramPacket(paresbuf, paresbuf.length);
            dgramSock.receive(pares);

            CommonStatic.publicIPaddress
                    = (paresbuf[  8 ] & 0xff) + "."
                    + (paresbuf[  9 ] & 0xff) + "."
                    + (paresbuf[ 10 ] & 0xff) + "."
                    + (paresbuf[ 11 ] & 0xff);

            Log.d(LOGTAG, "checkNATPMP: paresponse:" + Simple.getHexBytesToString(pares.getData()));
            Log.d(LOGTAG, "checkNATPMP: public-ip:" + CommonStatic.publicIPaddress);

            byte[] pmreqdata = new byte[] {
                    0, // version
                    2, // udp (for tcp choose 2)
                    0, 0, // reserved
                    (byte) ((privatePort >> 8) & 0xff),
                    (byte) (privatePort & 0xff),
                    0x47, 0x11, // public port
                    0, 0, 0, 60 // time to live for mapping
                    };

            DatagramPacket pmreq = new DatagramPacket(pmreqdata, pmreqdata.length, gwip, 5351);
            dgramSock.send(pmreq);

            byte[] pmresbuf = new byte[ 12 ];
            DatagramPacket pmres = new DatagramPacket(pmresbuf, pmresbuf.length);
            dgramSock.receive(pmres);

            int pri = ((pmresbuf[  8 ] & 0xff) << 8) + (pmresbuf[  9 ] & 0xff);
            int pub = ((pmresbuf[ 10 ] & 0xff) << 8) + (pmresbuf[ 11 ] & 0xff);

            Log.d(LOGTAG, "checkNATPMP: pmresponse:" + Simple.getHexBytesToString(pmres.getData()));
            Log.d(LOGTAG, "checkNATPMP: priv-port:" + pri + " pub-port:" + pub);

            publicPort = pub;

            return true;
        }
        catch (Exception ignore)
        {
            Log.d(LOGTAG,"checkNATPMP: not available");
        }

        return false;
    }

    public static void onMessageReceived(JSONObject message)
    {
        // Log.d(LOGTAG, "onMessageReceived: " + message.toString());

        if (! message.has("type")) return;

        String type = Json.getString(message, "type");

        if (Simple.equals(type, "fileTransferNeg"))
        {
            String publicIPremote = Json.getString(message, "publicIP");
            String privateIPremote = Json.getString(message, "privateIP");
            String defaultGWremote = Json.getString(message, "defaultGW");

            //
            // Check if on local network.
            //

            if (Simple.equals(CommonStatic.publicIPaddress, publicIPremote) &&
                    Simple.equals(CommonStatic.gwIPaddress, defaultGWremote) &&
                    Simple.isSameSubnet(CommonStatic.privateIPaddress, privateIPremote))
            {
                //
                // Make test call on local port.
                //

                Log.d(LOGTAG, "onMessageReceived: fileTransferNeg: could be local");
            }
        }
    }
}
