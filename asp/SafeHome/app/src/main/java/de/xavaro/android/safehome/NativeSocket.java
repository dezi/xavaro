package de.xavaro.android.safehome;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class NativeSocket
{
    private static final String LOGTAG = NativeSocket.class.getSimpleName();

    static
    {
        System.loadLibrary("NativeSocket");
    }

    public static native int nativeCreate();
    public static native int nativeClose(int socketfd);
    public static native int nativeSetTTL(int socketfd, int ttl);
    public static native int nativeSend(int socketfd, byte[] data, int offset, int length, String destip, int destport);
    public static native int nativeReceive(int socketfd, byte[] data, int length);

    private static int socketfd;

    public NativeSocket()
    {
        socketfd = nativeCreate();
    }

    public void close()
    {
        if (socketfd > 0)
        {
            nativeClose(socketfd);

            socketfd = 0;
        }
    }

    public void setTTL(int ttl)
    {
        nativeSetTTL(socketfd,ttl);
    }

    public void send(DatagramPacket packet) throws IOException
    {
        byte[] buffer = packet.getData();
        int length = buffer.length;
        String destip = packet.getAddress().getHostAddress();
        int destport = packet.getPort();

        int xfer = nativeSend(socketfd,buffer,0,length,destip,destport);

        Log.d(LOGTAG, "send=" + xfer + "=" + destip + ":" + destport);
    }

    public void receive(DatagramPacket packet) throws IOException
    {
        byte[] buffer = packet.getData();
        int length = buffer.length;

        int xfer = nativeReceive(socketfd,buffer,length);

        packet.setData(Arrays.copyOfRange(buffer, 0, xfer));

        Log.d(LOGTAG, "recv=" + xfer + "=" + new String(packet.getData()));
    }
}
