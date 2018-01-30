package com.ti.ti_oad.BluetoothLEController;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Date;

/**
 * Created by ole on 28/03/2017.
 */

public class BluetoothLETransaction {
    public static final String TAG = BluetoothLETransaction.class.getSimpleName();
    public enum BluetoothLETransactionType {   READ_ASYNC, READ_SYNC, WRITE_ASYNC, WRITE_SYNC,
                                    ENABLE_NOTIFICATION_ASYNC, ENABLE_NOTIFICATION_SYNC,
                                    DISABLE_NOTIFICATION_ASYNC,DISABLE_NOTIFICATION_SYNC,
                                    ENABLE_INDICATION_ASYNC,ENABLE_INDICATION_SYNC,
                                    DISABLE_INDICATION_ASYNC,DISABLE_INDICATION_SYNC }

    public BluetoothLEDevice dev;
    public BluetoothGattCharacteristic characteristic;
    public BluetoothLETransactionType transactionType;
    public byte[] dat;
    public Date transactionStartDate;
    public boolean transactionFinished;

    public BluetoothLETransaction(BluetoothLEDevice d, BluetoothGattCharacteristic c,BluetoothLETransactionType t, byte[] data) {
        dev = d;
        characteristic = c;
        transactionType = t;
        dat = data;
    }
}
