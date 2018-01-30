package com.ti.ti_oad.BluetoothLEController;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ti.ble.BluetoothLEController.Exceptions.BluetoothLEBluetoothEnableTimeoutException;
import com.ti.ble.BluetoothLEController.Exceptions.BluetoothLEPermissionException;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by ole on 17/03/2017.
 */

public class BluetoothLEManager {
    static public final int SCAN_PERMISSIONS_CODE = 1;
    static public final int SCAN_TERMINATE_TIMEOUT = 10;
    static public final int BT_ENABLE_TIMEOUT = 5;

    static final String TAG = BluetoothLEManager.class.getSimpleName();
    private static BluetoothLEManager mThis;
    Context c;
    BluetoothManager m;
    BluetoothAdapter adapter; //= BluetoothAdapter.getDefaultAdapter();
    Thread scanThread;
    List<BluetoothLEDevice> deviceList;
    public BluetoothLEManagerCB managerCB;
    boolean stopScan = false;

    public BluetoothLEManager(Context con) {
        this.c = con;
        this.deviceList = new ArrayList<BluetoothLEDevice>();
        this.m = (BluetoothManager)c.getSystemService(Context.BLUETOOTH_SERVICE);
        this.adapter = this.m.getAdapter();
    }

    public static BluetoothLEManager getInstance(Context c) {
        if (mThis == null) {
            mThis = new BluetoothLEManager(c);
        }
        return mThis;
    }

    public int checkPermission() {
        return ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void prepareForScanForDevices (Activity a) {
        /* No reason to check for permission to fine location if we do not have BLE features on
           the phone
         */
        BluetoothAdapter adapt = this.m.getAdapter();
        if (adapt == null) {
            /* Send error back to application */
            return;
        }
        else {
            if (!adapt.isEnabled()) {
                adapt.enable();
                int timeout = 50;
                while (!adapt.isEnabled()) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        Log.d(TAG,"prepareForScanForDevices: Interrupted while sleeping when " +
                                "waiting for bluetooth adapter enable !");
                    }
                    timeout--;
                    if (timeout < 0) {
                        /* Send error back to application */
                        return;
                    }
                }
            }
        }
        /* When we get here we have a valid adapter and also it is enabled, we can continue */

        int permissionCheck = ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(a,Manifest.permission.ACCESS_FINE_LOCATION)) {

            }
            else {
                ActivityCompat.requestPermissions(a,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},SCAN_PERMISSIONS_CODE);
            }
        }

    }

    public void scanForDevices() {
        /* No reason to check for permission to fine location if we do not have BLE features on
        the phone
         */
        BluetoothAdapter adapt = this.m.getAdapter();
        if (adapt == null) {
            /* Send error back to application */
            return;
        }
        else {
            if (!adapt.isEnabled()) {
                adapt.enable();
                int timeout = 50;
                while (!adapt.isEnabled()) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        Log.d(TAG,"prepareForScanForDevices: Interrupted while sleeping when " +
                                "waiting for bluetooth adapter enable !");
                    }
                    timeout--;
                    if (timeout < 0) {
                        /* Send error back to application */
                        throw new BluetoothLEBluetoothEnableTimeoutException("Timed out after waiting for 5 seconds for bluetooth enable");
                    }
                }
            }
        }
        /* When we get here we have a valid adapter and also it is enabled, we can continue */

        int permissionCheck = ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            /* Send error back to application */
            throw new BluetoothLEPermissionException("Permission denied");
        }
        scanForDevices(PackageManager.PERMISSION_GRANTED);
    }

    public void scanForDevices (int grantResult) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 3) {
            Log.d(TAG,"scanForDevices from : " + stackTraceElements[3].getClassName() + " " + stackTraceElements[3].getMethodName());
        }
        if (grantResult == PackageManager.PERMISSION_DENIED) {
            /* Send error back to application */
            throw new BluetoothLEPermissionException("Permission denied");
        }
        /* Everything is peachy we can run scan */
        if (this.scanThread != null) {
            if (this.scanThread.getState() == Thread.State.TERMINATED) {
                //Terminated, we can start it again.
                this.stopScan = false;
                this.scanThread = new Thread(scanRoutine);
                this.scanThread.start();
            }
            else {
                this.stopScan();
                int timeout = SCAN_TERMINATE_TIMEOUT;
                while (this.scanThread.getState() != Thread.State.TERMINATED) {
                    try {
                        Thread.sleep(50,0);
                    }
                    catch (InterruptedException e) {

                    }
                    timeout--;
                    if (timeout < 0) {
                        Log.d(TAG,"Timeout while waiting for scanThread to die ...");
                        return;
                    }
                }
                Log.d(TAG,"Scan thread stopped, restarting");
                this.deviceList = new ArrayList<BluetoothLEDevice>();
                this.stopScan = false;
                this.scanThread = new Thread(scanRoutine);
                this.scanThread.start();
            }
        }
        else {
            this.scanThread = new Thread(scanRoutine);
            this.scanThread.start();
        }
    }
    public interface BluetoothLEManagerCB {
            void deviceFound(BluetoothLEDevice device);
    }
    public void restartBluetooth(Context con, final Activity a) {
        final BluetoothAdapter mBtAdapter = this.m.getAdapter();
        final ProgressDialog dialog = new ProgressDialog(con);
        dialog.setTitle("Restarting Bluetooth");
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mBtAdapter.isEnabled()) {
                        mBtAdapter.disable();
                        for (int ii = 0; ii < 10; ii++) {
                            Thread.sleep(200, 0);
                            final int iii = ii;
                            a.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setProgress(iii * 5);
                                }
                            });
                        }
                    }
                    mBtAdapter.enable();
                    for (int ii = 0; ii < 10; ii++) {
                        Thread.sleep(250, 0);
                        final int iii = ii;
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setProgress(50 + (iii * 5));
                            }
                        });
                    }
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            a.refreshTable();
                        }
                    });
                    dialog.dismiss();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void stopScan() {
        stopScan = true;
    }
    public BluetoothLEDevice deviceForBluetoothDev(BluetoothDevice dev) {
        for (int ii = 0; ii < deviceList.size();ii++) {
            BluetoothLEDevice myDev = deviceList.get(ii);
            if (myDev.d.getAddress().toString().equalsIgnoreCase(dev.getAddress().toString())) {
                return myDev;
            }
        }
        BluetoothLEDevice newDevice = new BluetoothLEDevice(dev,this.c);
        newDevice.sR = new ScanResult(dev,null,0,0);
        deviceList.add(newDevice);
        Log.d(TAG,"Did not find deviceForBluetoothDev, but added a new device instead to the list");
        return newDevice;
    }
    public boolean deviceInList(BluetoothDevice dev) {
        for (int ii = 0; ii < deviceList.size(); ii++) {
            BluetoothLEDevice myDev = deviceList.get(ii);
            if (myDev.d.getAddress().equalsIgnoreCase(dev.getAddress())) {
                return true;
            }
        }
        return false;
    }
    Runnable scanRoutine = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"scanRoutine started");
            BluetoothAdapter adapt = mThis.m.getAdapter();
            ScanSettings scanSettings = new ScanSettings.Builder().build();
            ArrayList<ScanFilter> scanFilter = new ArrayList<ScanFilter>(2);

            if (!adapt.isEnabled()) {
                adapt.enable();
                int timeout = BT_ENABLE_TIMEOUT * 10;
                while (!adapt.isEnabled()) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        Log.d(TAG,"scanRoutine: Interrupted while sleeping when " +
                                "waiting for bluetooth adapter enable !");
                    }
                    timeout--;
                    if (timeout < 0) {
                        /* Send error back to application */
                        return;
                    }
                }
            }

            BluetoothLeScanner scanner = adapt.getBluetoothLeScanner();
            ScanCallback sCB = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (!deviceInList(result.getDevice())) {
                        BluetoothLEDevice dev = new BluetoothLEDevice(result.getDevice(), c);
                        dev.m = m;
                        dev.sR = result;
                        deviceList.add(dev);
                        Log.d(TAG,"" + result.getDevice().getAddress().toString() + " - Added to device list");
                        mThis.managerCB.deviceFound(dev);
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    Log.d(TAG,"onBatchScanResults");
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.d(TAG,"onScanFailed");
                }
            };
            scanner.startScan(scanFilter, scanSettings, sCB);
            while (!stopScan) {
                /* Busy waiting here */
                try {
                    Thread.sleep(500,0);
                }
                catch (InterruptedException e) {
                    Log.d(TAG,"Interrupted");
                }
            }
            if (adapt.isEnabled()) scanner.stopScan(sCB);
            Log.d(TAG,"scanRoutine stopped");
        }
    };
}
