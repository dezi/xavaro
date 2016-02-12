package de.xavaro.android.common;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CommSender
{
    private static final String LOGTAG = CommSender.class.getSimpleName();

    private static final ArrayList<JSONObject> sendFiles = new ArrayList<>();
    private static final ArrayList<JSONObject> recvFiles = new ArrayList<>();

    private static int runningExchangers;

    private static int privatePort;
    private static int publicPort;

    private static ServerSocket serverSocket;
    private static Thread serverThread;

    private static long ipRequestTime;
    private static long gwRequestTime;
    private static long upnpRequestTime;
    private static long natpmpRequestTime;
    private static long sendRequestTime;
    private static long receiveRequestTime;

    public static void sendFile(String filepath, String disposition, String idremote)
    {
        JSONObject request = new JSONObject();

        Json.put(request, "uuid", Simple.getUUID());
        Json.put(request, "idremote", idremote);
        Json.put(request, "filepath", filepath);
        Json.put(request, "filename", Simple.getFilename(filepath));
        Json.put(request, "disposition", disposition);

        synchronized (sendFiles)
        {
            sendFiles.add(request);
        }

        boolean exists = new File(filepath).exists();

        Log.d(LOGTAG, "sendFile: " + idremote + "=" + exists + "=" + filepath);
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

                return;
            }
        }

        //
        // Check if NAT-PMP is possible.
        //

        if (publicPort == 0)
        {
            if ((now - natpmpRequestTime) > 1000000)
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
            if ((now - upnpRequestTime) > 1000000)
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
            if ((now - ipRequestTime) > 1000000)
            {
                Log.d(LOGTAG, "commTick: request IP-Address");

                JSONObject myip = new JSONObject();
                Json.put(myip, "type", "myip");

                CommService.sendMessage(myip);

                ipRequestTime = now;

                return;
            }
        }

        if (runningExchangers > 0)
        {
            //
            // Do only one job at a time.
            //

            return;
        }

        //
        // Handle file sending packets.
        //

        if ((now - sendRequestTime) > 10000)
        {
            JSONObject sendFile = null;

            synchronized (sendFiles)
            {
                if (sendFiles.size() > 0)
                {
                    //
                    // Round robin.
                    //

                    sendFile = sendFiles.remove(0);
                    sendFiles.add(sendFile);
                }
            }

            if (sendFile != null)
            {
                if (sendFile.has("fileTransferUploaded"))
                {
                    negotiateUpload(sendFile);
                }
                else
                if (sendFile.has("fileTransferResponse"))
                {
                    uploadFile(sendFile);
                }
                else
                {
                    negotiateTransferRequest(sendFile);
                }

                sendRequestTime = now;

                return;
            }
        }

        //
        // Handle file receiving packets.
        //

        if ((now - receiveRequestTime) > 10000)
        {
            JSONObject recvFile = null;

            synchronized (recvFiles)
            {
                if (recvFiles.size() > 0)
                {
                    //
                    // Round robin.
                    //

                    recvFile = recvFiles.remove(0);
                    recvFiles.add(recvFile);
                }
            }

            if (recvFile != null)
            {
                if (recvFile.has("fileTransferUploaded"))
                {
                    downloadFile(recvFile);
                }
                else
                {
                    receiveFile(recvFile);
                }

                receiveRequestTime = now;
            }
        }
    }

    private static void negotiateTransferRequest(JSONObject sendFile)
    {
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

        String uuid = Json.getString(sendFile, "uuid");
        String idremote = Json.getString(sendFile, "idremote");
        String filename = Json.getString(sendFile, "filename");
        String disposition = Json.getString(sendFile, "disposition");

        JSONObject fileTransferRequest = new JSONObject();

        Json.put(fileTransferRequest, "type", "fileTransferRequest");
        Json.put(fileTransferRequest, "idremote", idremote);

        Json.put(fileTransferRequest, "wifiName", CommonStatic.wifiName);
        Json.put(fileTransferRequest, "publicIP", CommonStatic.publicIPaddress);
        Json.put(fileTransferRequest, "privateIP", CommonStatic.privateIPaddress);
        Json.put(fileTransferRequest, "defaultGW", CommonStatic.gwIPaddress);
        Json.put(fileTransferRequest, "publicPort", publicPort);
        Json.put(fileTransferRequest, "privatePort", privatePort);

        Json.put(fileTransferRequest, "uuid", uuid);
        Json.put(fileTransferRequest, "filename", Simple.getFilename(filename));
        Json.put(fileTransferRequest, "disposition", disposition);

        CommService.sendEncrypted(fileTransferRequest, true);

        Json.put(sendFile, "fileTransferRequest", true);
    }

    private static void negotiateTransferResponse(JSONObject recvFile)
    {
        Log.d(LOGTAG, "receiveFile: fileTransferRequest: respond with own public");

        JSONObject fileTransferResponse = Json.clone(recvFile);

        Json.put(fileTransferResponse, "type", "fileTransferResponse");
        Json.put(fileTransferResponse, "publicIP", CommonStatic.publicIPaddress);
        Json.put(fileTransferResponse, "publicPort", publicPort);

        CommService.sendEncrypted(fileTransferResponse, true);

        Json.put(recvFile, "fileTransferResponse", true);
    }

    private static void negotiateUpload(JSONObject sendFile)
    {
        JSONObject fileTransferUploaded = Json.clone(sendFile);

        Json.put(fileTransferUploaded, "type", "fileTransferUploaded");

        CommService.sendEncrypted(fileTransferUploaded, true);

        Json.put(sendFile, "fileTransferUploaded", true);
    }

    private static void negotiateDownload(JSONObject recvFile)
    {
        JSONObject fileTransferDownloaded = Json.clone(recvFile);

        Json.put(fileTransferDownloaded, "type", "fileTransferDownloaded");

        CommService.sendEncrypted(fileTransferDownloaded, true);

        Json.put(recvFile, "fileTransferDownloaded", true);
    }

    private static void serverLoop()
    {
        try
        {
            //noinspection InfiniteLoopStatement
            while (true)
            {
                Log.d(LOGTAG, "Waiting on port " + privatePort);

                Socket connect = serverSocket.accept();

                Log.d(LOGTAG, "Accepted connection on port " + privatePort);

                FileExchanger server = new FileExchanger(connect);
                server.start();
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

    @Nullable
    private static JSONObject getRecvFile(String uuid, boolean remove)
    {
        if (uuid == null) return null;

        synchronized (recvFiles)
        {
            for (int inx = 0; inx < recvFiles.size(); inx++)
            {
                JSONObject recvFile = recvFiles.get(inx);

                if (Json.equals(recvFile, "uuid", uuid))
                {
                    return remove ? recvFiles.remove(inx) : recvFile;
                }
            }
        }

        return null;
    }

    @Nullable
    private static JSONObject getSendFile(String uuid, boolean remove)
    {
        if (uuid == null) return null;

        synchronized (sendFiles)
        {
            for (int inx = 0; inx < sendFiles.size(); inx++)
            {
                JSONObject sendFile = sendFiles.get(inx);

                if (Json.equals(sendFile, "uuid", uuid))
                {
                    return remove ? sendFiles.remove(inx) : sendFile;
                }
            }
        }

        return null;
    }

    public static void onMessageReceived(JSONObject message)
    {
        if (! message.has("type")) return;

        String type = Json.getString(message, "type");

        if (Simple.equals(type, "fileTransferRequest"))
        {
            String uuid = Json.getString(message, "uuid");
            if (uuid == null) return;

            //
            // Store into receive files list.
            //

            synchronized (recvFiles)
            {
                boolean dup = false;

                for (JSONObject recvFile : recvFiles)
                {
                    if (Json.equals(recvFile, "uuid", uuid))
                    {
                        Json.copy(recvFile, message);
                        dup = true;
                        break;
                    }
                }

                if (!dup) recvFiles.add(message);
            }
        }

        if (Simple.equals(type, "fileTransferResponse"))
        {
            String uuid = Json.getString(message, "uuid");
            if (uuid == null) return;

            JSONObject sendFile = getSendFile(uuid, false);
            if (sendFile == null) return;

            String remotePublicIP = Json.getString(message, "publicIP");
            int remotePublicPort = Json.getInt(message, "publicPort");

            //
            // Add obtained info to sendFile packet.
            //

            Json.put(sendFile, "fileTransferResponse", true);
            Json.put(sendFile, "remotePublicIP", remotePublicIP);
            Json.put(sendFile, "remotePublicPort", remotePublicPort);
        }

        if (Simple.equals(type, "fileTransferUploaded"))
        {
            String uuid = Json.getString(message, "uuid");
            if (uuid == null) return;

            JSONObject recvFile = getRecvFile(uuid, false);
            if (recvFile == null) return;

            //
            // Add obtained info to recvFile packet.
            //

            Json.put(recvFile, "fileTransferUploaded", true);
        }

        if (Simple.equals(type, "fileTransferDownloaded"))
        {
            String uuid = Json.getString(message, "uuid");
            if (uuid == null) return;

            JSONObject sendFile = getSendFile(uuid, false);
            if (sendFile == null) return;

            notifyDownload(uuid);
        }
    }

    private static void downloadFile(JSONObject recvFile)
    {
        String uuid = Json.getString(recvFile, "uuid");

        Log.d(LOGTAG, "downloadFile: try share server");

        try
        {
            //
            // Try to download file from share server.
            //

            InetSocketAddress sa = new InetSocketAddress(
                    CommonConfigs.ShareServerName,
                    CommonConfigs.ShareServerPort);

            Socket clientSocket = new Socket();
            clientSocket.connect(sa, 2000);

            FileExchanger client = new FileExchanger(clientSocket, uuid, false);
            client.setHostname(CommonConfigs.ShareServerName);

            client.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void uploadFile(JSONObject sendFile)
    {
        String uuid = Json.getString(sendFile, "uuid");

        String publicIPremote = Json.getString(sendFile, "publicIPRemote");
        int publicPortremote = Json.getInt(sendFile, "publicPortRemote");

        boolean started = false;

        if ((publicIPremote != null) && (publicPortremote > 0))
        {
            Log.d(LOGTAG, "uploadFile: try public remote");

            try
            {
                //
                // Try to upload file on remotes public port.
                //

                InetSocketAddress sa = new InetSocketAddress(publicIPremote, publicPortremote);
                Socket clientSocket = new Socket();
                clientSocket.connect(sa, 2000);

                FileExchanger client = new FileExchanger(clientSocket, uuid, true);
                client.start();

                started = true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (! started)
        {
            Log.d(LOGTAG, "uploadFile: try share server");

            try
            {
                //
                // Try to upload file onto server.
                //

                InetSocketAddress sa = new InetSocketAddress(
                        CommonConfigs.ShareServerName,
                        CommonConfigs.ShareServerPort);

                Socket clientSocket = new Socket();
                clientSocket.connect(sa, 2000);

                FileExchanger client = new FileExchanger(clientSocket, uuid, true);
                client.setHostname(CommonConfigs.ShareServerName);

                client.start();

                started = true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (started) Log.d(LOGTAG, "uploadFile: upload started");
    }

    private static void receiveFile(JSONObject recvFile)
    {
        String uuid = Json.getString(recvFile, "uuid");
        String wifiNameremote = Json.getString(recvFile, "wifiName");
        String defaultGWremote = Json.getString(recvFile, "defaultGW");

        String publicIPremote = Json.getString(recvFile, "publicIP");
        int publicPortremote = Json.getInt(recvFile, "publicPort");

        String privIPremote = Json.getString(recvFile, "privateIP");
        int privPortremote = Json.getInt(recvFile, "privatePort");

        Log.d(LOGTAG, "receiveFile: " + CommonStatic.publicIPaddress + "=" + publicIPremote);
        Log.d(LOGTAG, "receiveFile: " + CommonStatic.privateIPaddress + "=" + privIPremote);
        Log.d(LOGTAG, "receiveFile: " + CommonStatic.gwIPaddress + "=" + defaultGWremote);
        Log.d(LOGTAG, "receiveFile: " + CommonStatic.wifiName + "=" + wifiNameremote);

        //
        // Check if on local network.
        //

        boolean started = false;

        if (Simple.equals(CommonStatic.publicIPaddress, publicIPremote) &&
                Simple.equals(CommonStatic.gwIPaddress, defaultGWremote) &&
                Simple.isSameSubnet(CommonStatic.privateIPaddress, privIPremote))
        {
            Log.d(LOGTAG, "receiveFile: could be local");

            try
            {
                //
                // Try to receive file on private port.
                //

                InetSocketAddress sa = new InetSocketAddress(privIPremote, privPortremote);
                Socket clientSocket = new Socket();
                clientSocket.connect(sa, 1000);

                FileExchanger client = new FileExchanger(clientSocket, uuid, false);
                client.start();

                started = true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if ((! started) && (publicIPremote != null) && (publicPortremote > 0))
        {
            Log.d(LOGTAG, "receiveFile: try public remote");

            try
            {
                //
                // Try to receive file on public port.
                //

                InetSocketAddress sa = new InetSocketAddress(publicIPremote, publicPortremote);
                Socket clientSocket = new Socket();
                clientSocket.connect(sa, 2000);

                FileExchanger client = new FileExchanger(clientSocket, uuid, false);
                client.start();

                started = true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (! started) negotiateTransferResponse(recvFile);
    }

    private static void notifySend(String uuid, boolean isupload)
    {
        JSONObject sendFile = getSendFile(uuid, ! isupload);
        if (sendFile == null) return;

        String idremote = Json.getString(sendFile, "idremote");
        String name = RemoteContacts.getDisplayName(idremote);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(Simple.getAnyContext());

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int iconRes = R.drawable.community_notification_64x64;

        nb.setAutoCancel(true);
        nb.setSmallIcon(iconRes);
        nb.setLargeIcon(Simple.getBitmapFromResource(iconRes));
        nb.setContentTitle("Bild versendet");
        nb.setContentText("Ein Bild wurde erfolgreich an " + name + " versendet.");
        nb.setSound(soundUri);

        NotificationManager nm = Simple.getNotificationManager();
        nm.notify(0, nb.build());
    }

    private static void notifyDownload(String uuid)
    {
        JSONObject sendFile = getSendFile(uuid, true);
        if (sendFile == null) return;

        String idremote = Json.getString(sendFile, "idremote");

        if (idremote == null) return;

        String name = RemoteContacts.getDisplayName(idremote);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(Simple.getAnyContext());

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int iconRes = R.drawable.community_notification_64x64;

        nb.setAutoCancel(true);
        nb.setSmallIcon(iconRes);
        nb.setLargeIcon(Simple.getBitmapFromResource(iconRes));
        nb.setContentTitle("Bild empfangen");
        nb.setContentText("Ein Bild wurde erfolgreich von " + name + " empfangen.");
        nb.setSound(soundUri);

        NotificationManager nm = Simple.getNotificationManager();
        nm.notify(0, nb.build());
    }

    private static void notifyReceived(String uuid, boolean isdownload)
    {
        JSONObject recvFile = getRecvFile(uuid, true);
        if (recvFile == null) return;

        if (isdownload) negotiateDownload(recvFile);

        String idremote = Json.getString(recvFile, "identity");
        String filename = Json.getString(recvFile, "filename");
        String disposition = Json.getString(recvFile, "disposition");

        if ((idremote == null) || (filename == null) || (disposition == null)) return;

        String name = RemoteContacts.getDisplayName(idremote);

        File tempfile = new File(Simple.getTempfile(uuid + ".tmp"));
        File mediapath = Simple.getMediaPath(disposition);
        File mediafile = new File(mediapath,filename);

        if (! mediapath.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            mediapath.mkdir();
        }

        Log.d(LOGTAG,"notifyReceived: from:" + tempfile.toString());
        Log.d(LOGTAG,"notifyReceived: toto:" + mediafile.toString());

        if (mediafile.exists())
        {
            //
            // Clobber file name.
            //

            String clobpath = mediafile.getParent();
            String clobfile = mediafile.getName();
            String clobname = Simple.getFileNameOnly(clobfile);
            String clobfext = Simple.getFileExtensionOnly(clobfile);

            for (int inx = 1; ; inx++)
            {
                mediafile = new File(clobpath, clobname + " (" +  inx + ")" + clobfext);

                if (! mediafile.exists()) break;
            }
        }

        if (! tempfile.renameTo(mediafile))
        {
            if (Simple.fileCopy(tempfile, mediafile))
            {
                //noinspection ResultOfMethodCallIgnored
                tempfile.delete();
            }
        }

        NotificationCompat.Builder nb = new NotificationCompat.Builder(Simple.getAnyContext());

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int iconRes = R.drawable.community_notification_64x64;

        nb.setAutoCancel(true);
        nb.setSmallIcon(iconRes);
        nb.setLargeIcon(Simple.getBitmapFromResource(iconRes));
        nb.setContentTitle("Bild empfangen");
        nb.setContentText("Ein Bild wurde erfolgreich von " + name + " empfangen.");
        nb.setSound(soundUri);

        NotificationManager nm = Simple.getNotificationManager();
        nm.notify(0, nb.build());
    }

    private static class FileExchanger extends Thread
    {
        private final static String LOGTAG = FileExchanger.class.getSimpleName();

        private int chunkSize = 8192;

        private InputStream requestInput;
        private OutputStream requestOutput;

        private final boolean isServer;
        private boolean isSender;
        private boolean isRelay;

        private String hostname;
        private String requestMethod;
        private String requestUuid;
        private long contentLength;

        public FileExchanger(Socket connect) throws IOException
        {
            this.isServer = true;

            requestInput = connect.getInputStream();
            requestOutput = connect.getOutputStream();
        }

        public FileExchanger(Socket connect, String uuid, boolean sender) throws IOException
        {
            this.isServer = false;
            this.isSender = sender;
            this.requestUuid = uuid;
            this.requestMethod = isSender ? "XPUT" : "XGET";

            requestInput = connect.getInputStream();
            requestOutput = connect.getOutputStream();
        }

        public void setHostname(String hostname)
        {
            this.isRelay = true;
            this.hostname = hostname;
        }

        @Override
        public void run()
        {
            synchronized (sendFiles)
            {
                runningExchangers++;
            }

            try
            {
                boolean ok1 = isServer ? getHeaders() : putHeaders();
                boolean ok2 = isServer ? putHeaders() : getHeaders();

                if (! (ok1 && ok2))
                {
                    Log.d(LOGTAG, "Headers failed:" + requestUuid);
                }
                else
                {
                    byte[] chunkBuffer = new byte[ chunkSize ];

                    if (requestMethod.equals("XPUT"))
                    {
                        JSONObject sendFile = getSendFile(requestUuid, false);

                        if (sendFile != null)
                        {
                            String filepath = Json.getString(sendFile, "filepath");
                            RandomAccessFile fd = new RandomAccessFile(filepath, "r");
                            if (contentLength > 0) fd.seek(contentLength);
                            long xsize = fd.length() - contentLength;
                            long xfer = 0;

                            Log.d(LOGTAG, "File sending:" + requestUuid + "=" + xsize);

                            while (xfer < xsize)
                            {
                                long chunk = xsize - xfer;
                                if (chunk > chunkSize) chunk = chunkSize;

                                int yfer = fd.read(chunkBuffer, 0, (int) chunk);
                                if (yfer < 0) break;

                                // todo AES encrypt.

                                requestOutput.write(chunkBuffer, 0, yfer);

                                Simple.sleep(100);

                                xfer += yfer;
                            }

                            fd.close();

                            if (xfer == xsize)
                            {
                                Log.d(LOGTAG, "File send:" + requestUuid);

                                notifySend(requestUuid, isRelay);
                            }
                        }
                    }

                    if (requestMethod.equals("XGET"))
                    {
                        JSONObject recvFile = getRecvFile(requestUuid, false);

                        if (recvFile != null)
                        {
                            String filepath = Simple.getTempfile(requestUuid + ".tmp");
                            RandomAccessFile fd = new RandomAccessFile(filepath, "rw");
                            if (fd.length() > 0) fd.seek(fd.length());
                            long xsize = contentLength - fd.length();
                            long xfer = 0;

                            Log.d(LOGTAG, "File receiving:" + requestUuid + "=" + xsize);

                            while (xfer < xsize)
                            {
                                long chunk = xsize - xfer;
                                if (chunk > chunkSize) chunk = chunkSize;

                                //
                                // Must read complete chunk or until eof
                                // for AES decryption.
                                //

                                int yfer = 0;

                                while (yfer < chunk)
                                {
                                    int rest = (int) (chunk - yfer);
                                    int part = requestInput.read(chunkBuffer, yfer, rest);
                                    if (part < 0) break;

                                    yfer += part;
                                }

                                // todo AES decrypt.

                                fd.write(chunkBuffer, 0, yfer);

                                xfer += yfer;
                            }

                            fd.close();

                            if (xfer == xsize)
                            {
                                Log.d(LOGTAG, "File received:" + requestUuid);

                                notifyReceived(requestUuid, isRelay);
                            }
                        }
                    }
                }

                requestInput.close();
                requestOutput.close();
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            synchronized (sendFiles)
            {
                runningExchangers--;
            }
        }

        private boolean putHeaders() throws Exception
        {
            String header = "";

            if (requestMethod.equals("XPUT"))
            {
                header += "XGET /sharehandler HTTP/1.0\r\n";

                JSONObject sendFile = getSendFile(requestUuid, false);
                String filepath = Json.getString(sendFile, "filepath");

                if ((filepath == null) || ! new File(filepath).exists()) return false;

                header += "Content-Length: " + Simple.getFilesize(filepath) + "\r\n";
            }

            if (requestMethod.equals("XGET"))
            {
                header += "XPUT /sharehandler HTTP/1.0\r\n";

                JSONObject recvFile = getRecvFile(requestUuid, false);
                if (recvFile == null) return false;

                String filepath = Simple.getTempfile(requestUuid + ".tmp");

                header += "Content-Length: " + Simple.getFilesize(filepath) + "\r\n";
            }

            if (hostname != null) header += "Host: " + hostname + "\r\n";

            header += "XRequest-Chunk: " + chunkSize + "\r\n";
            header += "XRequest-UUID: " + requestUuid + "\r\n";
            header += "\r\n";

            requestOutput.write(header.getBytes("UTF-8"));

            Log.d(LOGTAG, "putHeaders: " + header.replace("\r\n","_"));

            Log.d(LOGTAG, "putHeaders: " + requestMethod + "=" + requestUuid + "=" + contentLength);

            return true;
        }

        @SuppressWarnings("SameReturnValue")
        private boolean getHeaders() throws Exception
        {
            byte[] buffer = new byte[ 32000 ];

            int cnt = 0;

            while (! new String(buffer,0,cnt).endsWith("\r\n\r\n"))
            {
                if (requestInput.read(buffer, cnt++, 1) < 0) break;
            }

            String[] lines = new String(buffer,0,cnt).split("\r\n");

            for (String line : lines)
            {
                Log.d(LOGTAG,"Header: " + line);

                if (line.startsWith("Content-Length:"))
                {
                    contentLength = Long.parseLong(line.substring(15).trim(), 10);
                }

                if (line.startsWith("XRequest-UUID:"))
                {
                    requestUuid = line.substring(14).trim();
                }

                if (line.startsWith("XRequest-Chunk:"))
                {
                    chunkSize = Integer.parseInt(line.substring(15).trim());
                }
            }

            if (requestMethod == null) requestMethod = lines[ 0 ].substring(0,4);

            Log.d(LOGTAG,"getHeaders: " + requestMethod + "=" + requestUuid + "=" + contentLength);

            return true;
        }
    }

    //region Firewall stuff.

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

            //noinspection InfiniteLoopStatement
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
            Log.d(LOGTAG,"checkUPNP: not available");
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

            int pubTry = 10000 + new Random().nextInt(40000);

            byte[] pmreqdata = new byte[] {
                    0, // version
                    2, // udp (for tcp choose 2)
                    0, 0, // reserved
                    (byte) ((privatePort >> 8) & 0xff),
                    (byte) (privatePort & 0xff),
                    (byte) ((pubTry >> 8) & 0xff),
                    (byte) (pubTry & 0xff),
                    0, 0, 10, 0 // time to live for mapping
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

    //endregion Firewall stuff.
}
