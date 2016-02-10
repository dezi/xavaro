package de.xavaro.android.common;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Random;

public class UPNPManager
{
    private static final String LOGTAG = UPNPManager.class.getSimpleName();

    private final static String DISCOVER_MESSAGE_ROOTDEVICE =
            "M-SEARCH * HTTP/1.1\r\n" +
            "ST: upnp:rootdevice\r\n" +
            "MX: 3\r\n" +
            "MAN: ssdp:discover\r\n" +
            "HOST: 239.255.255.250:1900\r\n" +
            "\r\n";

    public static void discover()
    {
        try
        {
            InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");

            final int port = 1900;
            MulticastSocket socket = new MulticastSocket(port);
            socket.setReuseAddress(true);
            socket.setSoTimeout(15000);
            socket.joinGroup(multicastAddress);

            byte[] txbuf = DISCOVER_MESSAGE_ROOTDEVICE.getBytes();
            DatagramPacket hi = new DatagramPacket(txbuf, txbuf.length, multicastAddress, port);
            socket.send(hi);

            Log.d(LOGTAG, "SSDP discover sent");

            while (true)
            {
                byte[] rxbuf = new byte[ 8192 ];
                DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                socket.receive(packet);

                Log.d(LOGTAG,"SSDP discover=" + new String(packet.getData()));
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
