package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

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
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueTooth extends BroadcastReceiver
{
    private static final String LOGTAG = BlueTooth.class.getSimpleName();

    private static BlueTooth instance;

    public static BlueTooth getInstance(Context context)
    {
        if (instance == null) instance = new BlueTooth(context);

        return instance;
    }

    protected final Context context;
    protected final BluetoothAdapter bta;

    protected String modelName;
    protected String macAddress;

    protected BluetoothGatt currentGatt;
    protected BluetoothGattCharacteristic currentControl;
    protected boolean currentConnectState;

    protected final ArrayList<GattAction> gattSchedule = new ArrayList<>();
    protected BlueToothConnectCallback connectCallback;

    private static final String SCALE_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String SCALE_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private static final String BPM_SERVICE = "00001810-0000-1000-8000-00805f9b34fb";
    private static final String BPM_CHARACTERISTIC_MEASURE = "00002a35-0000-1000-8000-00805f9b34fb";
    private static final String BPM_CHARACTERISTIC_FEATURE = "00002a49-0000-1000-8000-00805f9b34fb";
    private static final String BPM_CHARACTERISTIC_INTERMEDIATE = "00002a36-0000-1000-8000-00805f9b34fb";

    private static final String NOTIFY_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private final ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private BluetoothDevice currentDevice;

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

    @SuppressLint("NewApi")
    public void connect()
    {
        BluetoothDevice device = bta.getRemoteDevice(this.macAddress);

        Log.d(LOGTAG,"connect: device=" + device);

        currentGatt = device.connectGatt(context, true, gattCallback);
    }

    @SuppressLint("NewApi")
    public void setConnectCallback(BlueToothConnectCallback callback)
    {
        connectCallback = callback;

        if (currentConnectState)
        {
            connectCallback.onBluetoothConnect(currentGatt.getDevice());
        }
        else
        {
            connectCallback.onBluetoothDisconnect(currentGatt.getDevice());
        }
    }

    public boolean isDiscovering;

    public boolean discoverLE;
    public boolean discoverClassic;
    public boolean discoverScales;
    public boolean discoverBPMs;
    public int discoverBGJobs;
    public BlueToothDiscoverCallback discoverCallback;

    public void discoverBPMs(BlueToothDiscoverCallback callback)
    {
        if (isDiscovering) return;
        isDiscovering = true;

        IntentFilter filter;

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(this, filter);

        discoverLE = true;
        discoverClassic = false;
        discoverBPMs = true;
        discoverCallback = callback;

        devices.clear();
        bta.startDiscovery();
        isDiscovering = true;
    }

    public void discoverScales(BlueToothDiscoverCallback callback)
    {
        if (isDiscovering) return;
        isDiscovering = true;

        IntentFilter filter;

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(this, filter);

        discoverLE = true;
        discoverClassic = false;
        discoverScales = true;
        discoverCallback = callback;

        devices.clear();
        bta.startDiscovery();
    }

    @Override
    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_UUID.equals(action))
        {
            Log.d(LOGTAG, "onReceive: found UUID");

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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

            if ((--discoverBGJobs == 0) && (! isDiscovering) && (discoverCallback != null))
            {
                discoverCallback.onDiscoverFinished();
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

            devices.add(device);

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

            isDiscovering = false;

            if ((discoverBGJobs == 0) && (discoverCallback != null))
            {
                discoverCallback.onDiscoverFinished();
            }

            context.unregisterReceiver(this);
        }
    }

    protected class GattAction
    {
        static final int MODE_INDICATE = 1;
        static final int MODE_NOTIFY = 2;
        static final int MODE_WRITE = 3;
        static final int MODE_READ = 4;

        BluetoothGattCharacteristic characteristic;
        BluetoothGatt gatt;
        byte[] data;
        int mode;
    }

    @SuppressLint("NewApi")
    protected void fireNext()
    {
        if (gattSchedule.size() == 0) return;

        GattAction ga = gattSchedule.remove(0);

        boolean executed = true;

        if (ga.mode == GattAction.MODE_READ)
        {
            executed = ga.gatt.readCharacteristic(ga.characteristic);
        }

        if (ga.mode == GattAction.MODE_WRITE)
        {
            Log.d(LOGTAG,"fireNext: " + StaticUtils.hexBytesToString(ga.data));
            ga.characteristic.setValue(ga.data);
            executed = ga.gatt.writeCharacteristic(ga.characteristic);
        }

        if (ga.mode == GattAction.MODE_NOTIFY)
        {
            ga.gatt.setCharacteristicNotification(ga.characteristic, true);

            UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
            BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            executed = ga.gatt.writeDescriptor(descriptor);
        }

        if (ga.mode == GattAction.MODE_INDICATE)
        {
            ga.gatt.setCharacteristicNotification(ga.characteristic, true);

            UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
            BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            executed = ga.gatt.writeDescriptor(descriptor);
        }
    }

    @SuppressLint("NewApi")
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback()
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

                if (connectCallback != null) connectCallback.onBluetoothConnect(gatt.getDevice());

                if (isDiscovering) gatt.discoverServices();
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

                if (service.getUuid().toString().equals(SCALE_SERVICE))
                {
                    Log.d(LOGTAG,"Found scale service=" + gatt.getDevice().getName());
                }

                if (service.getUuid().toString().equals(BPM_SERVICE))
                {
                    Log.d(LOGTAG,"Found BPM service=" + gatt.getDevice().getName());
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

                        if (characteristic.getUuid().toString().equals(SCALE_CHARACTERISTIC) &&
                                descriptor.getUuid().toString().equals(NOTIFY_DESCRIPTOR))
                        {
                            currentGatt = gatt;
                            currentControl = characteristic;

                            Log.d(LOGTAG,"Found compatible scale=" + gatt.getDevice().getName());

                            if (discoverScales && (discoverCallback != null))
                            {
                                discoverCallback.onDeviceDiscovered(gatt.getDevice());
                            }
                        }

                        if (characteristic.getUuid().toString().equals(BPM_CHARACTERISTIC_MEASURE) &&
                                descriptor.getUuid().toString().equals(NOTIFY_DESCRIPTOR))
                        {
                            currentGatt = gatt;
                            currentControl = characteristic;

                            Log.d(LOGTAG,"Found compatible bpm=" + gatt.getDevice().getName());

                            if (discoverBPMs && (discoverCallback != null))
                            {
                                discoverCallback.onDeviceDiscovered(gatt.getDevice());
                            }
                        }
                    }
                }
            }

            if ((--discoverBGJobs == 0) && (! isDiscovering) && (discoverCallback != null))
            {
                discoverCallback.onDiscoverFinished();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d(LOGTAG, "onCharacteristicChanged=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicChanged="
                    + StaticUtils.hexBytesToString(characteristic.getValue()));

            parseResponse(characteristic.getValue());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicRead=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicRead=" + status
                    + "=" + StaticUtils.hexBytesToString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicWrite=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicWrite=" + status
                    + "=" + StaticUtils.hexBytesToString(characteristic.getValue()));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorRead=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorRead=" + status
                    + "=" + StaticUtils.hexBytesToString(descriptor.getValue()));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorWrite=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorWrite=" + status
                    + "=" + StaticUtils.hexBytesToString(descriptor.getValue()));

            fireNext();
        }
    };

    protected void parseResponse(byte[] resp)
    {
        Log.d(LOGTAG,"parseResponse: Please implement this method in derived class.");
    }

    private String getDeviceTypeString(int devtype)
    {
        String pstr = "";

        if ((devtype & BluetoothDevice.DEVICE_TYPE_CLASSIC) > 0) pstr += "CLASSIC ";
        if ((devtype & BluetoothDevice.DEVICE_TYPE_DUAL) > 0) pstr += "DUAL ";
        if ((devtype & BluetoothDevice.DEVICE_TYPE_LE) > 0) pstr += "LE ";
        if ((devtype & BluetoothDevice.DEVICE_TYPE_UNKNOWN) > 0) pstr += "UNKNOWN ";

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
        public void onDiscoverStarted();
        public void onDiscoverFinished();
        public void onDeviceDiscovered(BluetoothDevice device);
    }

    public interface BlueToothConnectCallback
    {
        public void onBluetoothConnect(BluetoothDevice device);
        public void onBluetoothDisconnect(BluetoothDevice device);
    }
}
