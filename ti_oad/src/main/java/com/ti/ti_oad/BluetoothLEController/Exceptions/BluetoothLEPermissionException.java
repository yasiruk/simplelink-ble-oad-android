package com.ti.ti_oad.BluetoothLEController.Exceptions;

/**
 * Created by ole on 21/03/2017.
 */

public class BluetoothLEPermissionException extends BluetoothLEException {
    public BluetoothLEPermissionException() { super(); }
    public BluetoothLEPermissionException(String message) { super(message); }
    public BluetoothLEPermissionException(String message, Throwable cause) { super(message,cause); }
    public BluetoothLEPermissionException(Throwable cause) { super(cause); }
}
