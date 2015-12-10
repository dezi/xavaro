package de.xavaro.android.safehome;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NativeSocket
{
    private static final String LOGTAG = NativeSocket.class.getSimpleName();

    static
    {
        System.loadLibrary("NativeSocket");
    }

    public static native int FortyTwo(String text, int num);

    private static DatagramSocket dsock;

    public NativeSocket() throws SocketException
    {
        dsock = new DatagramSocket();
    }

    public void send(DatagramPacket packet) throws IOException
    {
        dsock.send(packet);
    }

    public void receive(DatagramPacket packet) throws IOException
    {
        dsock.receive(packet);
    }

    public void close()
    {
        dsock.close();
        dsock = null;
    }


}
