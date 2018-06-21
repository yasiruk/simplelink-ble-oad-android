package com.ti.ti_oad.BluetoothLEController.Exceptions;

/**
 * Created by ole on 21/03/2017.
 */

public class BluetoothLEConnectTimedOutException extends BluetoothLEException {
    public BluetoothLEConnectTimedOutException() { super(); }
    public BluetoothLEConnectTimedOutException(String message) { super(message); }
    public BluetoothLEConnectTimedOutException(String message, Throwable cause) { super(message,cause); }
    public BluetoothLEConnectTimedOutException(Throwable cause) { super(cause); }
}
