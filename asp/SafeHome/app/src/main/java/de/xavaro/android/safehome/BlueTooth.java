package de.xavaro.android.safehome;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class BlueTooth extends BroadcastReceiver
{
    private static final String LOGTAG = BlueTooth.class.getSimpleName();

    protected final Context context;
    protected final BluetoothAdapter bta;

    protected String modelName;
    protected String macAddress;

    protected BluetoothGatt currentGatt;
    protected boolean currentConnectState;

    protected BluetoothGattCharacteristic currentPrimary;
    protected BluetoothGattCharacteristic currentSecondary;

    protected final Handler gattHandler = new Handler();
    protected final ArrayList<GattAction> gattSchedule = new ArrayList<>();

    protected BlueToothDataCallback dataCallback;
    protected BlueToothConnectCallback connectCallback;

    private static final String NOTIFY_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public BlueTooth(Context context)
    {
        this.context = context;

        this.bta = BluetoothAdapter.getDefaultAdapter();
    }

    public BlueTooth(Context context, String deviceTag)
    {
        this(context);

        if (deviceTag != null)
        {
            String[] parts = deviceTag.split(" => ");

            modelName = parts[ 0 ];

            if (parts.length == 2)
            {
                macAddress = parts[ 1 ];
            }
        }
    }

    public void connect()
    {
        BluetoothDevice device = bta.getRemoteDevice(this.macAddress);

        Log.d(LOGTAG,"connect: device=" + device.getName() + " => " + device.getAddress());

        currentGatt = device.connectGatt(context, true, gattCallback);
    }

    public void setConnectCallback(BlueToothConnectCallback callback)
    {
        connectCallback = callback;

        if (currentConnectState)
        {
            connectCallback.onBluetoothConnect(currentGatt.getDevice());
        }
    }

    protected boolean isCompatibleDevice(String devicename)
    {
        return false;
    }

    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return false;
    }

    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    public void setDataCallback(BlueToothDataCallback callback)
    {
        dataCallback = callback;
    }

    public boolean isDiscovering;
    public int discoverBGJobs;

    public boolean discoverLE = true;
    public boolean discoverClassic = false;

    public BlueToothDiscoverCallback discoverCallback;

    public void discover(BlueToothDiscoverCallback callback)
    {
        if (isDiscovering) return;

        isDiscovering = true;
        discoverCallback = callback;

        IntentFilter filter;

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(this, filter);

        bta.startDiscovery();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_UUID.equals(action))
        {
            Log.d(LOGTAG, "onReceive: found UUID");

            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

            if (uuidExtra == null)
            {
                Log.e(LOGTAG, "onReceive: UUID = null");
            }
            else
            {
                for (Parcelable ep : uuidExtra)
                {
                    Log.d(LOGTAG, "onReceive: UUID records : " + ep.toString());
                }
            }

            discoverBGJobs--;

            if (isDiscovering && (discoverBGJobs == 0))
            {
                isDiscovering = false;

                if (discoverCallback != null) discoverCallback.onDiscoverFinished();
            }

            return;
        }

        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.d(LOGTAG, "onReceive: Found"
                    + ":" + device.getAddress()
                    + "=" + device.getType()
                    + "=" + device.getName()
                    + "=" + device.getBondState()
                    + "=" + getDeviceTypeString(device.getType()));

            if (discoverClassic && ((device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) ||
                    (device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL)))
            {
                Log.d(LOGTAG, "onReceive: fetchUuidsWithSdp=" + device.getName());
                device.fetchUuidsWithSdp();
                discoverBGJobs++;
            }

            if (discoverLE && ((device.getType() == BluetoothDevice.DEVICE_TYPE_LE) ||
                    (device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL)))
            {
                Log.d(LOGTAG, "onReceive: connectGatt=" + device.getName());
                device.connectGatt(context, true, gattCallback);
                discoverBGJobs++;
            }
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
        {
            Log.d(LOGTAG, "onReceive: discovery started");

            if (discoverCallback != null) discoverCallback.onDiscoverStarted();

            discoverBGJobs = 0;
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
        {
            Log.d(LOGTAG, "onReceive: discoveryfinished");

            if (isDiscovering && (discoverBGJobs == 0))
            {
                isDiscovering = false;

                if (discoverCallback != null) discoverCallback.onDiscoverFinished();
            }

            context.unregisterReceiver(this);
        }
    }

    protected class GattAction
    {
        public GattAction()
        {
        }

        public GattAction(int mode)
        {
            this.mode = mode;
        }

        public GattAction(byte[] data)
        {
            this.mode = MODE_WRITE;
            this.data = data;
        }

        static final int MODE_UNDEFINED = 0;
        static final int MODE_INDICATE = 1;
        static final int MODE_NOTIFY = 2;
        static final int MODE_WRITE = 3;
        static final int MODE_READ = 4;

        int mode;

        BluetoothGatt gatt;
        BluetoothGattCharacteristic characteristic;

        byte[] data;
    }

    protected void fireNext()
    {
        fireNext(false);
    }

    protected void fireNext(boolean delayed)
    {
        if (gattSchedule.size() == 0) return;

        if (delayed)
        {
            gattHandler.postDelayed(fireNextRunnable, 300);

            return;
        }

        gattHandler.removeCallbacks(fireNextRunnable);

        fireNextRunnable.run();
    }

    private final Runnable fireNextRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (gattSchedule.size() == 0) return;

            GattAction ga = gattSchedule.remove(0);

            if (ga.gatt == null) ga.gatt = currentGatt;
            if (ga.characteristic == null) ga.characteristic = currentPrimary;
            if (ga.mode == GattAction.MODE_UNDEFINED) ga.mode = GattAction.MODE_WRITE;

            if (ga.mode == GattAction.MODE_READ)
            {
                ga.gatt.readCharacteristic(ga.characteristic);
            }

            if (ga.mode == GattAction.MODE_WRITE)
            {
                Log.d(LOGTAG, "fireNext: " + StaticUtils.hexBytesToString(ga.data));
                ga.characteristic.setValue(ga.data);
                ga.gatt.writeCharacteristic(ga.characteristic);
            }

            if (ga.mode == GattAction.MODE_NOTIFY)
            {
                ga.gatt.setCharacteristicNotification(ga.characteristic, true);

                UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
                BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                ga.gatt.writeDescriptor(descriptor);
            }

            if (ga.mode == GattAction.MODE_INDICATE)
            {
                ga.gatt.setCharacteristicNotification(ga.characteristic, true);

                UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
                BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                ga.gatt.writeDescriptor(descriptor);
            }
        }
    };

    protected final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.i(LOGTAG, "onConnectionStateChange [" + newState + "]");

            String devicetag = gatt.getDevice().getName() + " => " + gatt.getDevice().getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.i(LOGTAG, "onConnectionStateChange: device " + devicetag + " connected");

                currentConnectState = true;

                gatt.discoverServices();

                if (connectCallback != null) connectCallback.onBluetoothConnect(gatt.getDevice());
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.i(LOGTAG, "onConnectionStateChange: device " + devicetag + " disconnected");

                currentConnectState = false;

                if (connectCallback != null) connectCallback.onBluetoothDisconnect(gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered (BluetoothGatt gatt, int status)
        {
            Log.i(LOGTAG, "onServicesDiscovered");

            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService service : services)
            {
                Log.d(LOGTAG, "serv=" + service.getUuid());

                if (isCompatibleService(service))
                {
                    Log.d(LOGTAG,"Found compatible service=" + gatt.getDevice().getName());
                }

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics)
                {
                    String props = getPropsString(characteristic.getProperties());
                    Log.d(LOGTAG, "  chara=" + characteristic.getUuid() + ":" + props);

                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

                    for (BluetoothGattDescriptor descriptor : descriptors)
                    {
                        Log.d(LOGTAG, "    descs=" + descriptor.getUuid());

                        if (isCompatiblePrimary(characteristic))
                        {
                            currentGatt = gatt;
                            currentPrimary = characteristic;

                            Log.d(LOGTAG,"Found compatible primary=" + gatt.getDevice().getName());

                            if (discoverCallback != null)
                            {
                                discoverCallback.onDeviceDiscovered(gatt.getDevice());
                            }
                        }

                        if (isCompatibleSecondary(characteristic))
                        {
                            currentGatt = gatt;
                            currentSecondary = characteristic;

                            Log.d(LOGTAG,"Found compatible secondary=" + gatt.getDevice().getName());
                        }
                    }
                }
            }

            //
            // Do not access device while discovering.
            //

            if (! isDiscovering)
            {
                enableDevice();
            }

            discoverBGJobs--;

            if (isDiscovering && (discoverBGJobs == 0))
            {
                isDiscovering = false;

                if (discoverCallback != null) discoverCallback.onDiscoverFinished();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicWrite=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicWrite=" + status
                    + "=" + StaticUtils.hexBytesToString(characteristic.getValue()));

            //
            // A change notification might come.
            //

            fireNext(true);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorWrite=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorWrite=" + status
                    + "=" + StaticUtils.hexBytesToString(descriptor.getValue()));

            //
            // Fire next event immedeately.
            //

            fireNext(false);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d(LOGTAG, "onCharacteristicChanged=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicChanged="
                    + StaticUtils.hexBytesToString(characteristic.getValue()));

            boolean intermediate = (characteristic == currentSecondary);
            parseResponse(characteristic.getValue(), intermediate);

            //
            // Fire next event immedeately.
            //

            fireNext(false);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicRead=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicRead=" + status
                    + "=" + StaticUtils.hexBytesToString(characteristic.getValue()));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorRead=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorRead=" + status
                    + "=" + StaticUtils.hexBytesToString(descriptor.getValue()));
        }
    };

    protected void forceDisconnect()
    {
        currentGatt.disconnect();
        currentGatt = currentGatt.getDevice().connectGatt(context, true, gattCallback);
    }

    @SuppressWarnings("UnusedParameters")
    protected void parseResponse(byte[] rd, boolean intermediate)
    {
        Log.d(LOGTAG,"parseResponse: Please implement this method in derived class.");
    }

    @SuppressWarnings("UnusedParameters")
    public void sendCommand(JSONObject command)
    {
        Log.d(LOGTAG,"sendCommand: Please implement this method in derived class.");
    }

    protected void enableDevice()
    {
        //
        // Overriden by sub class if there is anything to do.
        //

        Log.d(LOGTAG,"enableDevice: " + currentPrimary);
    }

    private String getDeviceTypeString(int devtype)
    {
        String pstr = "";

        if ((devtype & BluetoothDevice.DEVICE_TYPE_CLASSIC) > 0) pstr += "CLASSIC ";
        if ((devtype & BluetoothDevice.DEVICE_TYPE_DUAL) > 0) pstr += "DUAL ";
        if ((devtype & BluetoothDevice.DEVICE_TYPE_LE) > 0) pstr += "LE ";
        if (devtype == BluetoothDevice.DEVICE_TYPE_UNKNOWN) pstr += "UNKNOWN ";

        return pstr.trim();
    }

    private String getPropsString(int props)
    {
        String pstr = "";

        if ((props & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0) pstr += "BROADCAST ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0) pstr += "EXTENDED_PROPS ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) pstr += "INDICATE ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) pstr += "NOTIFY ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_READ) > 0) pstr += "READ ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) pstr += "WRITE ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) pstr += "SIGNED_WRITE ";
        if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) pstr += "WRITE_NO_RESPONSE ";

        return pstr.trim();
    }

    public interface BlueToothDiscoverCallback
    {
        void onDiscoverStarted();
        void onDiscoverFinished();
        void onDeviceDiscovered(BluetoothDevice device);
    }

    public interface BlueToothConnectCallback
    {
        void onBluetoothConnect(BluetoothDevice device);
        void onBluetoothActive(BluetoothDevice device);
        void onBluetoothDisconnect(BluetoothDevice device);
    }

    public interface BlueToothDataCallback
    {
        void onBluetoothReceivedData(BluetoothDevice device, JSONObject data);
    }

    //region Conversion helper

    protected int unsignedByteToInt(byte b)
    {
        return b & 0xff;
    }

    protected int unsignedToSigned(int unsigned, int size)
    {
        if ((unsigned & (1 << size - 1)) != 0)
        {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }

        return unsigned;
    }

    protected float bytesToFloat(byte b0, byte b1)
    {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);

        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);

        return (float) (mantissa * Math.pow(10, exponent));
    }

    protected int unsignedBytesToInt(byte b0, byte b1)
    {
        return ((unsignedByteToInt(b0) << 8) + unsignedByteToInt(b1));
    }

    protected int unsignedBytesToIntRev(byte b1, byte b0)
    {
        return ((unsignedByteToInt(b0) << 8) + unsignedByteToInt(b1));
    }

    protected long convertBytesToLong(byte[] data)
    {
        if (data.length != 8)
        {
            return 0;
        }

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(data);
        buffer.flip();

        return buffer.getLong();
    }

    //endregion Conversion helper
}
