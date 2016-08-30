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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

@SuppressWarnings("WeakerAccess")
public abstract class BlueTooth extends BroadcastReceiver
{
    private static final String LOGTAG = BlueTooth.class.getSimpleName();

    protected final boolean needlog = true;

    protected final Context context;
    protected final BluetoothAdapter bta;

    protected String deviceName;
    protected String macAddress;

    protected BluetoothGatt currentGatt;
    protected boolean currentConnectState;

    public BluetoothGattCharacteristic currentPrimary;
    public BluetoothGattCharacteristic currentSecondary;
    public BluetoothGattCharacteristic currentControl;

    protected BlueToothDiscoverCallback discoverCallback;
    protected BlueToothConnectCallback connectCallback;
    protected BlueToothDataCallback dataCallback;

    private static final String NOTIFY_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public BlueTooth(Context context)
    {
        this.context = context;

        gattHandler = new Handler(context.getMainLooper());

        this.bta = BluetoothAdapter.getDefaultAdapter();
    }

    public BlueTooth(Context context, String deviceTag)
    {
        this(context);

        if (deviceTag != null)
        {
            String[] parts = deviceTag.split(" => ");

            deviceName = parts[ 0 ];

            if (parts.length == 2)
            {
                macAddress = parts[ 1 ];
            }
        }
    }

    public void close()
    {
        if (currentGatt != null)
        {
            currentGatt.disconnect();
            currentGatt.close();
            currentGatt = null;
        }
    }

    public final Runnable connectRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "connect: device=" + deviceName + " => " + macAddress);

            BluetoothDevice device = bta.getRemoteDevice(macAddress);

            //if (isSpecialBonding()) unpairDevice(device);

            currentGatt = device.connectGatt(Simple.getAppContext(), true, gattCallback);
        }
    };

    public void setConnectCallback(BlueToothConnectCallback callback)
    {
        connectCallback = callback;

        if (currentConnectState)
        {
            connectCallback.onBluetoothConnect(deviceName);
        }
    }

    public void setDataCallback(BlueToothDataCallback callback)
    {
        dataCallback = callback;
    }

    //region Device discovery

    protected final Handler discoverHandler = new Handler();

    public boolean isDiscovering;
    public int discoverBGJobs;

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

        discoverHandler.postDelayed(abortDiscoveryRunnable, 30000);
    }

    public void finishDiscovery()
    {
        Log.d(LOGTAG,"finishDiscovery");

        isDiscovering = false;

        discoverHandler.removeCallbacks(abortDiscoveryRunnable);

        if (bta.isDiscovering()) bta.cancelDiscovery();

        if (discoverCallback != null)
        {
            discoverCallback.onDiscoverFinished();
            discoverCallback = null;
        }
    }

    public final Runnable abortDiscoveryRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            finishDiscovery();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
        {
            Log.d(LOGTAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }

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

            if (isDiscovering && (discoverBGJobs == 0)) finishDiscovery();

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

            if (((device.getType() == BluetoothDevice.DEVICE_TYPE_LE) ||
                    (device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL)))
            {
                deviceName = device.getName();
                macAddress = device.getAddress();

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

            if (isDiscovering && (discoverBGJobs == 0)) finishDiscovery();

            context.unregisterReceiver(this);
        }
    }

    //endregion Device discovery

    //region Device communication handling

    protected final Handler gattHandler;
    protected final ArrayList<GattAction> gattSchedule = new ArrayList<>();

    public static class GattAction
    {
        public static final int MODE_READ = 1;
        public static final int MODE_WRITE = 2;
        public static final int MODE_NOTIFY = 3;
        public static final int MODE_INDICATE = 4;
        public static final int MODE_DISCONNECT = 5;
        public static final int MODE_NOACTIONREQUIRED = 6;

        public GattAction()
        {
        }

        public GattAction(int mode)
        {
            this.mode = mode;
        }

        public GattAction(BluetoothGattCharacteristic characteristic)
        {
            this.mode = MODE_NOACTIONREQUIRED;
            this.characteristic = characteristic;

            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
            {
                this.mode = MODE_NOTIFY;
            }

            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)
            {
                this.mode = MODE_INDICATE;
            }
        }

        public GattAction(int mode, BluetoothGattCharacteristic characteristic)
        {
            this.mode = mode;
            this.characteristic = characteristic;
        }


        public GattAction(byte[] data)
        {
            this.mode = MODE_WRITE;
            this.data = data;
        }

        int mode;

        BluetoothGattCharacteristic characteristic;

        byte[] data;
    }

    protected boolean noFireOnWrite;

    protected void fireNext(boolean delayed)
    {
        fireNext(delayed ? 300 : 100);
    }

    protected void fireNext(int delay)
    {
        if (fireNextPending) return;
        if (gattSchedule.size() == 0) return;

        if (delay > 0)
        {
            synchronized (fireNextRunnable)
            {
                fireNextPending = true;

                gattHandler.postDelayed(fireNextRunnable, delay);
            }

            return;
        }

        fireNextRunnable.run();
    }

    private boolean fireNextPending;

    private final Runnable fireNextRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                synchronized (fireNextRunnable)
                {
                    fireNextPending = false;

                    if (gattSchedule.size() == 0) return;

                    GattAction ga = gattSchedule.remove(0);

                    if (ga.mode == GattAction.MODE_NOACTIONREQUIRED)
                    {
                        fireNext(false);
                        return;
                    }

                    if (ga.characteristic == null) ga.characteristic = currentPrimary;
                    if (ga.mode == 0) ga.mode = GattAction.MODE_WRITE;

                    if (ga.mode == GattAction.MODE_READ)
                    {
                        Log.d(LOGTAG, "fireNext: read:" + ga.characteristic.getUuid());
                        currentGatt.readCharacteristic(ga.characteristic);
                    }

                    if (ga.mode == GattAction.MODE_WRITE)
                    {
                        Log.d(LOGTAG, "fireNext: write:" + ga.characteristic.getUuid() + "=" + Simple.getHexBytesToString(ga.data));
                        ga.characteristic.setValue(ga.data);
                        currentGatt.writeCharacteristic(ga.characteristic);
                    }

                    if (ga.mode == GattAction.MODE_NOTIFY)
                    {
                        Log.d(LOGTAG, "fireNext: notify:" + ga.characteristic.getUuid());
                        currentGatt.setCharacteristicNotification(ga.characteristic, true);

                        UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
                        BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        currentGatt.writeDescriptor(descriptor);
                    }

                    if (ga.mode == GattAction.MODE_INDICATE)
                    {
                        Log.d(LOGTAG, "fireNext: indicate:" + ga.characteristic.getUuid());
                        currentGatt.setCharacteristicNotification(ga.characteristic, true);

                        UUID descuuid = UUID.fromString(NOTIFY_DESCRIPTOR);
                        BluetoothGattDescriptor descriptor = ga.characteristic.getDescriptor(descuuid);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        currentGatt.writeDescriptor(descriptor);
                    }

                    if (ga.mode == GattAction.MODE_DISCONNECT)
                    {
                        currentGatt.disconnect();
                    }
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);

                gattSchedule.clear();
            }
        }
    };

    //endregion Device communication handling

    //region Gatt callback handler

    private void pairDevice(BluetoothDevice device)
    {
        Log.d(LOGTAG, "pairDevice: =================" + deviceName);

        try
        {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private void unpairDevice(BluetoothDevice device)
    {
        Log.d(LOGTAG, "unpairDevice: =================" + deviceName);

        try
        {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @SuppressWarnings("unused")
    private final Runnable runCreateBonding = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "runCreateBonding: " + deviceName);

            final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(bondingReceiver, filter);

            if (currentGatt != null) pairDevice(currentGatt.getDevice());
        }
    };

    private final Runnable runDiscoverServices = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "runDiscoverServices: " + deviceName);

            currentGatt.discoverServices();
        }
    };

    private final Runnable runEnableAndSync = new Runnable()
    {
        @Override
        public void run()
        {
            discoveredDevice();

            Log.d(LOGTAG, "runEnableAndSync: " + deviceName);

            enableDevice();
            syncSequence();
        }
    };

    private final BroadcastReceiver bondingReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int nbs = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int obs = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            Log.d(LOGTAG, "Bond state changed for: " + device.getName() + obs + " => " + nbs);

            if (nbs == BluetoothDevice.BOND_BONDED)
            {
                context.unregisterReceiver(this);

                if (currentPrimary == null)
                {
                    Log.d(LOGTAG, "bondingReceiver: discoverServices after bonding: " + device.getName());

                    gattHandler.postDelayed(runDiscoverServices, 100);
                }
                else
                {
                    Log.d(LOGTAG, "bondingReceiver: enableDevice after bonding: " + device.getName());

                    gattHandler.postDelayed(runEnableAndSync, 100);
                }
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.d(LOGTAG, "onConnectionStateChange [" + newState + "] = " + status);

            String devicetag = deviceName + " => " + gatt.getDevice().getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d(LOGTAG, "onConnectionStateChange: device " + devicetag + " connected");

                currentGatt = gatt;
                currentConnectState = true;

                Log.d(LOGTAG, "onConnectionStateChange===================="
                        + ":" + currentGatt.getDevice().getAddress()
                        + "=" + currentGatt.getDevice().getType()
                        + "=" + currentGatt.getDevice().getName()
                        + "=" + currentGatt.getDevice().getBondState());

                if (isSpecialBonding() && (currentGatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDED))
                {
                    Log.d(LOGTAG, "onConnectionStateChange==================== bonding!!!!!!!!!!!");

                    pairDevice(currentGatt.getDevice());
                }
                else
                {
                    gattHandler.postDelayed(runDiscoverServices, 100);
                }
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d(LOGTAG, "onConnectionStateChange: device " + devicetag + " disconnected");

                gattSchedule.clear();
                currentConnectState = false;
                currentGatt.close();
                currentGatt = null;

                if (connectCallback != null) connectCallback.onBluetoothDisconnect(deviceName);

                gattHandler.post(connectRunnable);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.d(LOGTAG, "onServicesDiscovered=" + (currentGatt == gatt));

            List<BluetoothGattService> gattServices = gatt.getServices();

            for (BluetoothGattService service : gattServices)
            {
                //noinspection ConstantConditions
                if (needlog) Log.d(LOGTAG, "serv=" + service.getUuid());

                if (isCompatibleService(service))
                {
                    Log.d(LOGTAG, "Found service=" + deviceName + " " + service.getUuid());
                }

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics)
                {
                    //noinspection ConstantConditions
                    if (needlog)
                    {
                        String props = getPropsString(characteristic.getProperties());
                        Log.d(LOGTAG, "  chara=" + characteristic.getUuid() + ":" + props);

                        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

                        for (BluetoothGattDescriptor descriptor : descriptors)
                        {
                            Log.d(LOGTAG, "    descs=" + descriptor.getUuid());
                        }
                    }

                    String uuid = characteristic.getUuid().toString();

                    if (isCompatiblePrimary(characteristic))
                    {
                        currentPrimary = characteristic;
                        Log.d(LOGTAG, "Found primary=" + deviceName + " " + uuid);
                    }

                    if (isCompatibleSecondary(characteristic))
                    {
                        currentSecondary = characteristic;
                        Log.d(LOGTAG, "Found secondary=" + deviceName + " " + uuid);
                    }

                    if (isCompatibleControl(characteristic))
                    {
                        currentControl = characteristic;
                        Log.d(LOGTAG, "Found control=" + deviceName + " " + uuid);
                    }
                }
            }

            if (isDiscovering)
            {
                if ((discoverCallback != null) && (currentPrimary != null))
                {
                    Log.d(LOGTAG,"onServicesDiscovered Found=" + deviceName);

                    discoverCallback.onDeviceDiscovered(gatt.getDevice());
                }

                if (--discoverBGJobs == 0) finishDiscovery();
            }
            else
            {
                Log.d(LOGTAG,"onServicesDiscovered special:"
                        + isSpecialBonding()
                        + " => "
                        + currentGatt.getDevice().getBondState());

                Log.d(LOGTAG,"onServicesDiscovered ===================================:" + currentPrimary);

                if (currentPrimary != null) gattHandler.postDelayed(runEnableAndSync, 0);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicWrite=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicWrite=" + status
                    + "=" + Simple.getHexBytesToString(characteristic.getValue()));

            //
            // A change notification might come.
            //

            if (! noFireOnWrite) fireNext(true);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorWrite=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorWrite=" + status
                    + "=" + Simple.getHexBytesToString(descriptor.getValue()));

            fireNext(500);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            Log.d(LOGTAG, "onCharacteristicChanged=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicChanged="
                    + Simple.getHexBytesToString(characteristic.getValue()));

            parseResponse(characteristic.getValue(), characteristic);

            fireNext(true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicRead=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicRead=" + status
                    + "=" + Simple.getHexBytesToString(characteristic.getValue()));

            if (status == 0)
            {
                parseResponse(characteristic.getValue(), characteristic);
            }

            fireNext(true);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorRead=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorRead=" + status
                    + "=" + Simple.getHexBytesToString(descriptor.getValue()));
        }
    };

    //endregion Gatt callback handler

    //region Derived class abstract stuff

    protected abstract boolean isCompatibleService(BluetoothGattService service);
    protected abstract boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic);
    protected abstract boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic);
    protected abstract boolean isCompatibleControl(BluetoothGattCharacteristic characteristic);

    protected boolean isSpecialBonding()
    {
        return false;
    }

    //
    // This method is called as soon, as the device is connected
    // to the gatt service. This allows the subclass to fire an early
    // connection callback as we like a fast haptic feedback to
    // the user when he activates a device. Method must also
    // set the connect state.
    //

    protected void discoveredDevice()
    {
        if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);
    }

    //
    // This method is called when the services have been discovered
    // and the device is about to be enabled. Used to initialize
    // notifiers and other stuff and also to fire a late connection
    // callback, if enabling the device is successful.
    //

    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice:" + deviceName);

        //
        // Enable notifications and indications on standard
        // charactaristics if they exist.
        //

        if (currentControl != null) gattSchedule.add(new GattAction(currentControl));
        if (currentPrimary != null) gattSchedule.add(new GattAction(currentPrimary));
        if (currentSecondary != null) gattSchedule.add(new GattAction(currentSecondary));
    }

    protected abstract void syncSequence();
    protected abstract void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic);

    public abstract void sendCommand(JSONObject command);

    //endregion Derived class abstract stuff

    //region Mask formatter helper

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

    //endregion Mask formatter helper

    //region Callback interfaces

    public interface BlueToothDiscoverCallback
    {
        void onDiscoverStarted();
        void onDeviceDiscovered(BluetoothDevice device);
        void onDiscoverFinished();
    }

    public void callOnBluetoothFakeConnect()
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeConnect(deviceName);
    }

    public void callOnBluetoothFakeDisconnect()
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeDisconnect(deviceName);
    }

    public interface BlueToothConnectCallback
    {
        void onBluetoothConnect(String deviceName);
        void onBluetoothDisconnect(String deviceName);
        void onBluetoothFakeConnect(String deviceName);
        void onBluetoothFakeDisconnect(String deviceName);
    }

    public interface BlueToothDataCallback
    {
        void onBluetoothReceivedData(String deviceName, JSONObject data);
    }

    public interface BlueToothPhysicalDevice
    {
        boolean isCompatibleService(BluetoothGattService service);
        boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic);
        boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic);
        boolean isCompatibleControl(BluetoothGattCharacteristic characteristic);

        void enableDevice();
        void syncSequence();
        void sendCommand(JSONObject command);
        void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic);
    }

    //endregion Callback interfaces

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
        if (data.length != 8)  return 0;

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(data);
        buffer.flip();

        return buffer.getLong();
    }

    public byte[] convertLongToBytes(long value)
    {
        byte[] data = new byte[ 8 ];

        for (int i = 0; i < 8; i++)
        {
            data[ i ] = (byte) ((int) (value >> ((7 - i) * 8)));
        }

        return data;
    }

    public byte[] convertIntToBytes(int value)
    {
        byte[] data = new byte[ 8 ];

        for (int i = 0; i < 4; i++)
        {
            data[ i ] = (byte) (value >> ((3 - i) * 8));
        }

        return data;
    }

    public static int convertBytesToInt(byte[] data)
    {
        if (data.length != 4) return 0;

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(data);
        buffer.flip();

        return buffer.getInt();
    }

    public static long getTimeStampInMilliSeconds(int timeStampInSeconds)
    {
        return ((long) timeStampInSeconds) * 1000;
    }

    public static int getTimeStampInSeconds(long timeStampInMilliSeconds)
    {
        return (int) (timeStampInMilliSeconds / 1000);
    }

    //endregion Conversion helper
}
