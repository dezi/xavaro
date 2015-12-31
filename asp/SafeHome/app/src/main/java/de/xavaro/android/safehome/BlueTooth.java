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

    private static final ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private static final String SCALE_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String SCALE_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private final Context context;

    private final ArrayList<GattAction> gattSchedule = new ArrayList<>();

    private String globaldevicename;
    private BluetoothGatt globalgatt;
    private BluetoothGattCharacteristic globalcontrol;

    public BlueTooth(Context context)
    {
        this.context = context;

        IntentFilter filter;

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(this, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(this, filter);

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        bta.startDiscovery();
    }

    private class GattAction
    {
        static final int MODE_NOTIFY = 1;
        static final int MODE_WRITE = 1;
        static final int MODE_READ = 1;

        BluetoothGattCharacteristic characteristic;
        BluetoothGatt gatt;
        byte[] data;
        int mode;
    }

    @SuppressLint("NewApi")
    private void fireNext()
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
            if (ga.data != null) ga.characteristic.setValue(ga.data);
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
    }

    @Override
    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_UUID.equals(action))
        {
            Log.d(LOGTAG, "onReceive found UUID");

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

            Log.d(LOGTAG, "------------------------------" + device.getName());

            if (uuidExtra == null)
            {
                Log.e(LOGTAG, "UUID = null");

                return;
            }

            for (Parcelable ep : uuidExtra)
            {
                Log.d(LOGTAG, "UUID records : " + ep.toString());
            }
        }

        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.d(LOGTAG, "onReceive Found"
                    + ":" + device.getAddress()
                    + "=" + device.getType()
                    + "=" + device.getName()
                    + "=" + device.getBondState());

                /*
                Log.d(LOGTAG, "DEVICE_TYPE_CLASSIC=" + BluetoothDevice.DEVICE_TYPE_CLASSIC);
                Log.d(LOGTAG, "DEVICE_TYPE_DUAL=" + BluetoothDevice.DEVICE_TYPE_DUAL);
                Log.d(LOGTAG, "DEVICE_TYPE_LE=" + BluetoothDevice.DEVICE_TYPE_LE);
                Log.d(LOGTAG, "DEVICE_TYPE_UNKNOWN=" + BluetoothDevice.DEVICE_TYPE_UNKNOWN);
                */

            if (device.getName().contains("SAN"))
            {
                devices.add(device);

                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            }
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
        {
            Log.d(LOGTAG, "onReceive started");
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
        {
            Log.d(LOGTAG, "onReceive finished");

            for (BluetoothDevice device : devices)
            {

                if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC)
                {
                    Log.d(LOGTAG, "onReceive fetchUuidsWithSdp=" + device.getName());
                    device.fetchUuidsWithSdp();
                }

                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE)
                {
                    Log.d(LOGTAG, "onReceive connectGatt=" + device.getName());

                    globalgatt = device.connectGatt(context, true, gattCallback);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.i(LOGTAG, "onConnectionStateChange [" + newState + "]");

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.i(LOGTAG, "onConnectionStateChange: device connected");

                gatt.discoverServices();
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.i(LOGTAG, "onConnectionStateChange: device disconnected");
            }
        }

        private String getProps(int prop)
        {
            String pstr = "";

            if ((prop & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0) pstr += "BROADCAST ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0) pstr += "EXTENDED_PROPS ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) pstr += "INDICATE ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) pstr += "NOTIFY ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_READ) > 0) pstr += "READ ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) pstr += "WRITE ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) pstr += "SIGNED_WRITE ";
            if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) pstr += "WRITE_NO_RESPONSE ";

            return pstr;
        }

        private String getPerms(int perms)
        {
            String pstr = "";

            if ((perms & BluetoothGattCharacteristic.PERMISSION_READ) > 0) pstr += "READ ";
            if ((perms & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) > 0) pstr += "READ_ENCRYPTED ";
            if ((perms & BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) pstr += "WRITE ";
            if ((perms & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) > 0) pstr += "WRITE_ENCRYPTED ";

            return pstr;
        }

        private void sendAcknowledgement(byte[] resp)
        {
            byte[] data = new byte[ 5 ];

                /*
                if (getConnectedScaleName().equalsIgnoreCase(BleConstants.GS485) ||
                        getConnectedScaleName().equalsIgnoreCase(BleConstants.SANITAS_SBF70) ||
                        getConnectedScaleName().equalsIgnoreCase(BleConstants.BF710))
                {
                */
            data[ 0 ] = (byte) 0xe7;
                /*
                }
                else
                {
                    data[ 0 ] = (byte) 0xf7;
                }
                */

            data[ 1 ] = (byte) 0xf1;
            data[ 2 ] = (byte) resp[ 1 ];
            data[ 3 ] = (byte) resp[ 2 ];
            data[ 4 ] = (byte) resp[ 3 ];

            globalcontrol.setValue(data);
            globalgatt.writeCharacteristic(globalcontrol);
        }

        private void parseResponse(byte[] resp)
        {
            Log.d(LOGTAG,"parseResponse: " + StaticUtils.hexBytesToString(resp));
            Log.d(LOGTAG,"parseResponse: " + resp[ 0 ] + ":" + resp[ 1 ]);

            if ((resp[ 0 ] == (byte) 0xe7) && (resp[ 1 ] == (byte) 0x58))
            {
                //
                // Gewicht....
                //

                Log.d(LOGTAG,"parseResponse: Gewicht");
            }

            // E7 59 03 01 01 00 00 00 00 00 00 00 65
            Log.d(LOGTAG,"parseResponse: other");

            sendAcknowledgement(resp);
        }

        @Override
        public void onServicesDiscovered (BluetoothGatt gatt, int status)
        {
            Log.i(LOGTAG, "onServicesDiscovered");

            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService service : services)
            {
                Log.d(LOGTAG, "serv=" + service.getUuid());

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics)
                {
                    Log.d(LOGTAG, "     chara=" + characteristic.getUuid() + ":" + getProps(characteristic.getProperties()));

                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

                    for (BluetoothGattDescriptor descriptor : descriptors)
                    {
                        Log.d(LOGTAG, "          descs=" + descriptor.getUuid());

                        if (characteristic.getUuid().toString().equals(SCALE_CHARACTERISTIC) &&
                                descriptor.getUuid().toString().equals(NOTIFY_DESCRIPTOR))
                        {
                            globalcontrol = characteristic;

                            Log.d(LOGTAG,"Found compatible scale=" + gatt.getDevice().getName());
                        }
                    }
                }
            }

            if (globalcontrol != null)
            {
                GattAction ga;

                ga = new GattAction();

                ga.gatt = gatt;
                ga.mode = GattAction.MODE_NOTIFY;
                ga.characteristic = globalcontrol;

                gattSchedule.add(ga);

                byte[] data = new byte[ 10 ];
                data[ 0 ] = -25;
                data[ 1 ] = 69;

                ga = new GattAction();

                ga.gatt = gatt;
                ga.mode = GattAction.MODE_WRITE;
                ga.data = data;
                ga.characteristic = globalcontrol;

                gattSchedule.add(ga);

                fireNext();
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

            fireNext();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(LOGTAG, "onCharacteristicWrite=" + characteristic.getUuid());
            Log.d(LOGTAG, "onCharacteristicWrite=" + status
                    + "=" + StaticUtils.hexBytesToString(characteristic.getValue()));

            fireNext();
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(LOGTAG, "onDescriptorRead=" + descriptor.getUuid());
            Log.d(LOGTAG, "onDescriptorRead=" + status
                    + "=" + StaticUtils.hexBytesToString(descriptor.getValue()));

            fireNext();
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
}
