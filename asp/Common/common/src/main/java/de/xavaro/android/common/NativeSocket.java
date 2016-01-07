package de.xavaro.android.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

//
// Small native datagram socket class with ability
// to set the TTL. This is required to keep Router
// NAT-Tables alive w/o really sending packets to
// server outside in the internet.
//

public class NativeSocket
{
    static
    {
        System.loadLibrary("NativeSocket");
    }

    public static native int nativeCreate();
    public static native int nativeClose(int socketfd);

    public static native int nativeGetTTL(int socketfd);
    public static native int nativeSetTTL(int socketfd, int ttl);

    public static native int nativeSend(int socketfd, byte[] data, int offset, int length, String destip, int destport);
    public static native int nativeReceive(int socketfd, byte[] data, int length);

    public static native String nativeStrError(int errnum);

    private static int socketfd;
    private static int initialTTL;
    private static int currentTTL;

    private String errorMessage(int errnum)
    {
        return nativeStrError(errnum) + " (" + errnum + ")";
    }

    public NativeSocket() throws IOException
    {
        socketfd = nativeCreate();

        if (socketfd > 0)
        {
            initialTTL = currentTTL = this.getTTL();

            return;
        }

        throw new IOException(errorMessage(socketfd));
    }

    public void close()
    {
        if (socketfd > 0)
        {
            nativeClose(socketfd);

            socketfd = 0;
        }
    }

    public int getTTL() throws IOException
    {
        int ttl = nativeGetTTL(socketfd);

        if (ttl > 0) return ttl;

        throw new IOException(errorMessage(ttl));
    }

    public void setTTL(int ttl) throws IOException
    {
        if (ttl == 0) ttl = initialTTL;
        if (ttl == currentTTL) return;

        int err = nativeSetTTL(socketfd, ttl);

        if (err >= 0)
        {
            currentTTL = ttl;

            return;
        }

        throw new IOException(errorMessage(err));
    }

    public void send(DatagramPacket packet) throws IOException
    {
        byte[] buffer = packet.getData();
        int length = buffer.length;
        String destip = packet.getAddress().getHostAddress();
        int destport = packet.getPort();

        int xfer = nativeSend(socketfd,buffer,0,length,destip,destport);

        if (xfer > 0) return;

        throw new IOException(errorMessage(xfer));
    }

    public void receive(DatagramPacket packet) throws IOException
    {
        byte[] buffer = packet.getData();
        int length = buffer.length;

        int xfer = nativeReceive(socketfd, buffer, length);

        if (xfer > 0)
        {
            packet.setData(Arrays.copyOfRange(buffer, 0, xfer));

            return;
        }

        throw new IOException(errorMessage(xfer));
    }
}
