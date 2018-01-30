package com.ti.ti_oad.BluetoothLEController;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by ole on 17/03/2017.
 */

public class BluetoothLEDevice {

    static final String TAG = BluetoothLEDevice.class.getSimpleName();
    public static final int MAX_RETRIES = 4;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int DISCOVERY_TIMEOUT = 10000;

    public BluetoothDevice d;
    public BluetoothGatt g;
    public BluetoothManager m;
    public ScanResult sR;
    public List<BluetoothGattService> services;
    public List<BluetoothGattCharacteristic> chars;
    Context c;
    public boolean isConnected;
    public boolean isDiscovered;
    public BluetoothLEDeviceCB myCB;
    public boolean needsBroadcastScreen;
    public boolean shouldReconnect;
    BluetoothLEDevice mThis;
    BluetoothLEDeviceDebugVariables dVars;
    ArrayList<BluetoothLETransaction> deviceTransactions;
    BluetoothLETransaction currentTransaction;
    Thread TransactionHandlerThread;
    boolean stopTransactionHandler = false;
    int currentConnectionPriority = BluetoothGatt.CONNECTION_PRIORITY_BALANCED;

    public BluetoothLEDevice(BluetoothDevice device,Context context) {
        d = device;
        c = context;
        dVars = new BluetoothLEDeviceDebugVariables();
        chars = new ArrayList<BluetoothGattCharacteristic>();
        deviceTransactions = new ArrayList<BluetoothLETransaction>();
        currentTransaction = null;
        mThis = this;
    }

    public void connectDevice() {
        dVars.connectionCalls++;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 3) {
            Log.d(TAG,"Connect called from : " + stackTraceElements[3].getClassName() + " " + stackTraceElements[3].getMethodName());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int ii = 0; (ii < MAX_RETRIES + 1) && !isConnected; ii++) {
                    int timeout = CONNECTION_TIMEOUT;
                    g = d.connectGatt(c,false,BluetoothLEDeviceCB);
                    while (!isConnected) {
                        try {
                            Thread.sleep(1, 0);
                        } catch (Exception e) {
                            Log.d(TAG, "Interrupted while waiting for connect");
                        }
                        timeout--;
                        if (myCB != null) {
                            if ((timeout % 250) == 0) myCB.waitingForConnect(mThis, timeout, ii);
                        }
                        if (timeout < 0) {
                        /* Error handling */
                            if (g != null) {
                                Log.d(TAG, "Timeout while connecting");
                                g.disconnect();
                                if (ii == MAX_RETRIES) {
                                    if (myCB != null) myCB.deviceConnectTimedOut(mThis);
                                    return;
                                }
                                break;
                            }
                        }
                    }
                }
                for (int ii = 0; (ii < MAX_RETRIES + 1) && !isDiscovered; ii++) {
                    int timeout = DISCOVERY_TIMEOUT;
                    while (!isDiscovered) {
                        try {
                            Thread.sleep(1, 0);
                        } catch (Exception e) {
                            Log.d(TAG, "Interrupted while waiting for service discovery");
                        }
                        timeout--;
                        if (myCB != null) {
                            if ((timeout % 250) == 0)myCB.waitingForDiscovery(mThis, timeout, ii);
                        }
                        if (timeout < 0) {
                        /* Error handling */
                            if (g != null) {
                                Log.d(TAG, "Timeout while discovering services");
                                g.disconnect();
                                if (ii == MAX_RETRIES) {
                                    if (myCB != null) myCB.deviceDiscoveryTimedOut(mThis);
                                    return;
                                }
                                g = d.connectGatt(c,false,BluetoothLEDeviceCB);
                                break;
                            }
                        }
                    }
                }
            }
        }).start();
    }
    public void disconnectDevice() {
        dVars.disconnectionCalls++;
        stopTransactionHandler = true;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 3) {
                Log.d(TAG,"Disconnect called from : " + stackTraceElements[3].getClassName() + " " + stackTraceElements[3].getMethodName());
        }
        if (g != null) {
            //TODO: Add timeout
            g.disconnect();
        }
        else {
            Log.d(TAG,"FAILURE !!!! Device did not have a BluetoothGatt when isConnected = true !");
            //This is a very bad limbo, because here we do not have a handle to Bluetooth GATT anymore ...
            try {
                Log.d(TAG, "Current state is: " + this.m.getConnectionState(this.d, BluetoothGatt.GATT));
            }
            catch (NullPointerException e) {
                Log.d(TAG,"Not able to read state, device was already null !");
            }
        }
    }

    public boolean requestMTUChange(int mtu) {
        return g.requestMtu(mtu);
    }

    public int writeCharacteristicAsync(BluetoothGattCharacteristic characteristic, byte[] val) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.WRITE_SYNC,val);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20,0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int writeCharacteristicAsync(BluetoothGattCharacteristic characteristic, byte val) {
        byte[] vals = new byte[1];
        vals[0] = val;
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.WRITE_SYNC,vals);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20,0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int readCharacteristicAsync(BluetoothGattCharacteristic characteristic) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.READ_SYNC, null);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20, 0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int setCharacteristicNotificationAsync(BluetoothGattCharacteristic characteristic, boolean enable) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, (enable) ?
                BluetoothLETransaction.BluetoothLETransactionType.ENABLE_NOTIFICATION_ASYNC :
                BluetoothLETransaction.BluetoothLETransactionType.DISABLE_NOTIFICATION_ASYNC,null);
        this.deviceTransactions.add(trans);

        return BluetoothGatt.GATT_SUCCESS;
    }

    public int setCharacteristicNotificationSync(BluetoothGattCharacteristic characteristic, boolean enable) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, (enable) ?
                BluetoothLETransaction.BluetoothLETransactionType.ENABLE_NOTIFICATION_SYNC :
                BluetoothLETransaction.BluetoothLETransactionType.DISABLE_NOTIFICATION_SYNC,null);
        this.deviceTransactions.add(trans);

        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20, 0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int writeCharacteristicSync(BluetoothGattCharacteristic characteristic, byte[] val) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.WRITE_SYNC,val);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20,0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int writeCharacteristicSync(BluetoothGattCharacteristic characteristic, byte val) {
        byte[] vals = new byte[1];
        vals[0] = val;
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.WRITE_SYNC,vals);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20,0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
    public int readCharacteristicSync(BluetoothGattCharacteristic characteristic) {
        BluetoothLETransaction trans = new BluetoothLETransaction(this,characteristic, BluetoothLETransaction.BluetoothLETransactionType.READ_SYNC, null);
        this.deviceTransactions.add(trans);
        while (!trans.transactionFinished) {
            try {
                Thread.sleep(20, 0);
            }
            catch (InterruptedException e) {

            }
        }
        return BluetoothGatt.GATT_SUCCESS;
    }

    BluetoothGattCallback BluetoothLEDeviceCB = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG,"Device " + gatt.getDevice().getAddress().toString() + " CONNECTED");
                isConnected = true;
                if (refreshDeviceCache()) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {

                    }
                }

                boolean startDiscoveryOK = gatt.discoverServices();
                if (mThis.g == null) {
                    mThis.g = gatt;
                    Log.d(TAG,"Did not have BluetoothGatt Property set correctly !");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTING) {
                Log.d(TAG,"Device " + gatt.getDevice().getAddress().toString() + " DISCONNECTING");
                isConnected = false;
                isDiscovered = false;
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                isConnected = false;
                isDiscovered = false;
                Log.d(TAG,"Device " + gatt.getDevice().getAddress().toString() + " DISCONNECTED");
                if (shouldReconnect) {
                    connectDevice();
                    isConnected = false;
                }
                else {
                    gatt.close();
                    mThis.myCB.deviceDidDisconnect(mThis);
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG,"Device " + gatt.getDevice().getAddress().toString() + " SERVICES DISCOVERED" + "Status" + status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"Device " + gatt.getDevice().getAddress().toString() + "Service Discovery FAILED !");
                return;
            }
            services = gatt.getServices();
            for (BluetoothGattService serv : services) {
                for (BluetoothGattCharacteristic characteristic : serv.getCharacteristics()) {
                    chars.add(characteristic);
                }
            }
            PrintAllServicesAndCharacteristics();
            TransactionHandlerThread = new Thread(deviceTransactionHandler);
            TransactionHandlerThread.start();
            Log.d(TAG,"Transaction Handler Thread : " + TransactionHandlerThread.toString());
            isDiscovered = true;
            if (myCB != null) myCB.deviceReady(mThis);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (currentTransaction != null) {
                if (currentTransaction.transactionFinished != true) {
                    currentTransaction.transactionFinished = true;
                    deviceTransactions.remove(currentTransaction);
                    currentTransaction = null;
                }
            }
            Log.d(TAG,"onCharacteristicRead: Read " + characteristic.getUuid().toString());
            if (myCB != null) {
                myCB.didReadCharacteristicData(mThis,characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (currentTransaction != null) {
                if (currentTransaction.transactionFinished != true) {
                    currentTransaction.transactionFinished = true;
                    deviceTransactions.remove(currentTransaction);
                    currentTransaction = null;
                }
            }
            Log.d(TAG,"onCharacteristicWrite: Wrote to " + characteristic.getUuid().toString());
            if (myCB != null) {
                myCB.didWriteCharacteristicData(mThis,characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (myCB != null) {
                myCB.didUpdateCharacteristicData(mThis,characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (currentTransaction != null) {
                if (currentTransaction.transactionFinished != true) {
                    currentTransaction.transactionFinished = true;
                    deviceTransactions.remove(currentTransaction);
                    currentTransaction = null;
                }
            }
            Log.d(TAG,"onDescriptorWrite: Wrote to " + descriptor.getCharacteristic().getUuid().toString());
            if (myCB != null) {
                myCB.didUpdateCharacteristicNotification(mThis,descriptor.getCharacteristic());
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG,"onMtuChanged: Got new MTU setting : MTU = " + mtu + "status = " + status);
            super.onMtuChanged(gatt, mtu, status);
            myCB.mtuValueChanged(mtu);
        }
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.d(TAG,"onPhyUpdate : New TX PHY: " +
                    ((txPhy == BluetoothDevice.PHY_LE_2M) ? "2M" :
                            (txPhy == BluetoothDevice.PHY_LE_1M) ? "1M" :
                                    (txPhy == BluetoothDevice.PHY_LE_CODED) ? "Coded" : "Unknown"));
            Log.d(TAG,"onPhyUpdate : New RX PHY: " +
                    ((rxPhy == BluetoothDevice.PHY_LE_2M) ? "2M" :
                            (rxPhy == BluetoothDevice.PHY_LE_1M) ? "1M" :
                                    (rxPhy == BluetoothDevice.PHY_LE_CODED) ? "Coded" : "Unknown"));
            Log.d(TAG,"onPhyRead : Status :" + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.d(TAG,"onPhyRead : New TX PHY: " +
                    ((txPhy == BluetoothDevice.PHY_LE_2M) ? "2M" :
                            (txPhy == BluetoothDevice.PHY_LE_1M) ? "1M" :
                                    (txPhy == BluetoothDevice.PHY_LE_CODED) ? "Coded" : "Unknown"));
            Log.d(TAG,"onPhyRead : New RX PHY: " +
                    ((rxPhy == BluetoothDevice.PHY_LE_2M) ? "2M" :
                            (rxPhy == BluetoothDevice.PHY_LE_1M) ? "1M" :
                                    (rxPhy == BluetoothDevice.PHY_LE_CODED) ? "Coded" : "Unknown"));
            Log.d(TAG,"onPhyRead : Status :" + status);
        }
    };


    public interface BluetoothLEDeviceCB {
        void waitingForConnect(BluetoothLEDevice dev, final int milliSecondsLeft, final int retry);
        void waitingForDiscovery(BluetoothLEDevice dev, final int milliSecondsLeft, final int retry);
        void deviceReady(BluetoothLEDevice dev);
        void deviceFailed(BluetoothLEDevice dev);
        void deviceConnectTimedOut(BluetoothLEDevice dev);
        void deviceDiscoveryTimedOut(BluetoothLEDevice dev);
        void didUpdateCharacteristicData(BluetoothLEDevice dev,BluetoothGattCharacteristic characteristic);
        void didReadCharacteristicData(BluetoothLEDevice dev,BluetoothGattCharacteristic characteristic);
        void didUpdateCharacteristicNotification(BluetoothLEDevice dev,BluetoothGattCharacteristic characteristic);
        void didUpdateCharacteristicIndication(BluetoothLEDevice dev);
        void didWriteCharacteristicData(BluetoothLEDevice dev,BluetoothGattCharacteristic characteristic);
        void deviceDidDisconnect(BluetoothLEDevice dev);
        void mtuValueChanged(int mtu);
    }

    private class BluetoothLEDeviceDebugVariables {
        public int connectionCalls;
        public int disconnectionCalls;
        public int reads;
        public int writes;
    }
    public void PrintAllServicesAndCharacteristics() {
        int serviceId = 0;
        for (BluetoothGattService serv : services) {
            Log.d(TAG,"Service[" + serviceId + "] : " + serv.getUuid().toString());
            int charId = 0;
            for (BluetoothGattCharacteristic characteristic : serv.getCharacteristics()) {
                Log.d(TAG,"    Characteristic[" + charId + "] : " + characteristic.getUuid().toString());
                charId++;
            }
            serviceId++;
        }
    }

    public Runnable deviceTransactionHandler = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"deviceTransactionHandler started for device : " + mThis.d.getAddress().toString());
            while (!stopTransactionHandler) {

                if (currentTransaction != null) {
                    /* We are running a transaction that still isn't finished, check for timeout */
                    long transDate = currentTransaction.transactionStartDate.getTime();
                    long curDate = new Date().getTime();
                    if (Math.abs(transDate - curDate) > 5000) {
                        Log.d(TAG,"Transaction has used more than " + (Math.abs(transDate - curDate) / 1000) + " seconds to complete !");
                        /* TODO: Handle error here */
                        currentTransaction = null;
                        continue;
                    }
                }
                else if (deviceTransactions.size() > 0) {
                    /* We have a pending transaction, process it */
                    currentTransaction = deviceTransactions.get(0);
                    currentTransaction.transactionStartDate = new Date();
                    if (!commitTransactionToBT(currentTransaction)) {
                        currentTransaction = null;
                        /* TODO: Needs to warn application that things went sour ! */
                        continue;
                    }
                }
                try {
                    Thread.sleep(100,0);
                }
                catch (InterruptedException e) {
                    Log.d(TAG,"deviceTransactionHandler: interrupted while running");
                }
            }
        }
    };

    public boolean commitTransactionToBT(BluetoothLETransaction trans) {
        boolean success = false;
        if (trans.characteristic == null) return success;
        switch (trans.transactionType) {
            case ENABLE_NOTIFICATION_ASYNC:
            case ENABLE_NOTIFICATION_SYNC:
                success = g.setCharacteristicNotification(trans.characteristic,true);
                BluetoothGattDescriptor clientConfigChar = trans.characteristic
                        .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (clientConfigChar == null) {
                    Log.d(TAG,"Set Notification failed for :" + trans.characteristic.getUuid().toString());
                    success = true;
                    break;
                }
                clientConfigChar.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                final BluetoothGattDescriptor bgClientconfigChar = clientConfigChar;
                mThis.g.writeDescriptor(bgClientconfigChar);
                success = true;
                break;
            case DISABLE_NOTIFICATION_ASYNC:
            case DISABLE_NOTIFICATION_SYNC:
                success = g.setCharacteristicNotification(trans.characteristic,false);
                clientConfigChar = trans.characteristic
                        .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                clientConfigChar.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                g.writeDescriptor(clientConfigChar);
                success = true;
                break;
            case READ_ASYNC:
            case READ_SYNC:
                success = g.readCharacteristic(trans.characteristic);
                break;
            case WRITE_ASYNC:
            case WRITE_SYNC:
                trans.characteristic.setValue(trans.dat);
                success = g.writeCharacteristic(trans.characteristic);
                break;
        }
        return success;
    }

    public int getCurrentConnectionPriority() {
        return this.currentConnectionPriority;
    }
    public boolean setCurrentConnectionPriority(int prio) {
        if (this.g.requestConnectionPriority(prio)) {
            this.currentConnectionPriority = prio;
            return true;
        }
        else return false;
    }

    public boolean refreshDeviceCache() {
       return false;
    }
}
