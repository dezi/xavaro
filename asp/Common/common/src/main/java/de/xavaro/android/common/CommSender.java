package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CommSender
{
    private static final String LOGTAG = CommSender.class.getSimpleName();

    public static final ArrayList<JSONObject> sendFiles = new ArrayList<>();

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

    private static long ipRequestTime;
    private static long gwRequestTime;
    private static long natpmpRequestTime;

    public static void commTick()
    {
        //if (sendFiles.size() == 0) return;

        //
        // Request current IP-Address.
        //

        if (CommonStatic.myIPaddress == null)
        {
            long now = new Date().getTime();

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

        //
        // Request current GW-Address.
        //

        if (CommonStatic.gwIPaddress == null)
        {
            long now = new Date().getTime();

            if ((now - gwRequestTime) > 10000)
            {
                CommonStatic.gwIPaddress = Simple.getDefaultGatewayAddress();

                Log.d(LOGTAG, "commTick: request GW-Address: " + CommonStatic.gwIPaddress);

                gwRequestTime = now;
            }

            return;
        }

        //
        // Check if NAT-PMP is possible.
        //

        if (CommonStatic.natPublicPort == 0)
        {
            long now = new Date().getTime();

            if ((now - natpmpRequestTime) > 10000)
            {
                checkNATPMP();

                Log.d(LOGTAG, "commTick: request public port: " + CommonStatic.natPublicPort);
                Log.d(LOGTAG, "commTick: request private port: " + CommonStatic.natPrivatePort);

                natpmpRequestTime = now;
            }

            return;
        }
    }

    private static void checkNATPMP()
    {
        try
        {
            DatagramSocket dgramSock = new DatagramSocket();

            InetAddress gwip = InetAddress.getByName(CommonStatic.gwIPaddress);

            byte[] pareqdata = new byte[] { 0, 0 };
            DatagramPacket pareq = new DatagramPacket(pareqdata, pareqdata.length, gwip, 5351);
            dgramSock.send(pareq);

            byte[] paresbuf = new byte[ 12 ];
            DatagramPacket pares = new DatagramPacket(paresbuf, paresbuf.length);
            dgramSock.receive(pares);

            CommonStatic.myIPaddress
                    = (paresbuf[  8 ] & 0xff) + "."
                    + (paresbuf[  9 ] & 0xff) + "."
                    + (paresbuf[ 10 ] & 0xff) + "."
                    + (paresbuf[ 11 ] & 0xff);

            Log.d(LOGTAG, "checkNATPMP: paresponse:" + Simple.getHexBytesToString(pares.getData()));
            Log.d(LOGTAG, "checkNATPMP: public-ip:" + CommonStatic.myIPaddress);

            byte[] pmreqdata = new byte[] {
                    0, // version
                    2, // udp (for tcp choose 2)
                    0, 0, // reserved
                    0x47, 0x12, // private port
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

            CommonStatic.natPrivatePort = pri;
            CommonStatic.natPublicPort  = pub;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
