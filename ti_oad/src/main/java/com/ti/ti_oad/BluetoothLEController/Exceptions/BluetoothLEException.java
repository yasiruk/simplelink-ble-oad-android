package com.ti.ti_oad.BluetoothLEController.Exceptions;

/**
 * Created by ole on 21/03/2017.
 */

public class BluetoothLEException extends RuntimeException {
    public BluetoothLEException() { super(); }
    public BluetoothLEException(String message) { super(message); }
    public BluetoothLEException(String message, Throwable cause) { super(message,cause); }
    public BluetoothLEException(Throwable cause) { super(cause); }
}
