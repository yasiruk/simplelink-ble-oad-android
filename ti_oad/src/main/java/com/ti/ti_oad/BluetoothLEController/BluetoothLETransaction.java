package com.ti.ti_oad.BluetoothLEController;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Date;

/*
 Copyright 2018 Texas Instruments

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
