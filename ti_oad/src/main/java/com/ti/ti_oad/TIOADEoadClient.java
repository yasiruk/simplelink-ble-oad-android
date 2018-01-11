package com.ti.ti_oad;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

/**
 * Created by ole on 01/12/2017.
 */

public class TIOADEoadClient {

  private final static String TAG = TIOADEoadClient.class.getSimpleName();

  private TIOADEoadImageReader fileReader;
  private Context context;
  private BluetoothGatt gatt;
  private TIOADEoadClientProgressCallback progressCallback;
  private BluetoothGattCharacteristic imageNotifyChar;
  private BluetoothGattCharacteristic blockRequestChar;
  private BluetoothGattCharacteristic imageCountChar;
  private BluetoothGattCharacteristic imageStatusChar;
  private BluetoothGattCharacteristic imageControlChar;

  private TIOADEoadDefinitions.oadStatusEnumeration status;



  public TIOADEoadClient(Context context, String assetFileName) {
    this.context = context;
    fileReader = new TIOADEoadImageReader(assetFileName,this.context);
  }




  public boolean startTIOADEoadProgrammingOnDevice(BluetoothDevice dev,TIOADEoadClientProgressCallback callback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      //Here we have the possibility to connect using a PHY preference, use it
      gatt = dev.connectGatt(context,false,TIOADEoadBTCallback,BluetoothDevice.TRANSPORT_LE,BluetoothDevice.PHY_LE_2M | BluetoothDevice.PHY_LE_1M);
      progressCallback = callback;
      status = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientInitializing;
      return true;
    }
    else {
      gatt = dev.connectGatt(context,false,TIOADEoadBTCallback);
      progressCallback = callback;
      status = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientInitializing;
      return true;
    }

    //End here, we cannot do anything before we get a connect to a device, and then we continue...
  }

  BluetoothGattCallback TIOADEoadBTCallback = new BluetoothGattCallback() {
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

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      switch (newState) {
        case BluetoothGatt.STATE_CONNECTED:
          gatt.discoverServices();
          break;
        case BluetoothGatt.STATE_DISCONNECTED:
          gatt.close();
          break;
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      super.onServicesDiscovered(gatt, status);
      if (status == BluetoothGatt.GATT_SUCCESS) {
        //Try to find the TI EOAD Service
        BluetoothGattService oadService = gatt.getService(UUID.fromString(TIOADEoadDefinitions.TI_OAD_SERVICE));
        if (oadService == null) {
          //Send error to the callback functions
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADServiceMissingOnPeripheral);
          gatt.disconnect();
          return;
        }
        imageControlChar = oadService.getCharacteristic(UUID.fromString(TIOADEoadDefinitions.TI_OAD_IMAGE_CONTROL));
        if (imageControlChar == null) {
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADWrongVersion);
          gatt.disconnect();
          return;
        }
        imageCountChar = oadService.getCharacteristic(UUID.fromString(TIOADEoadDefinitions.TI_OAD_IMAGE_COUNT));
        if (imageCountChar != null) {
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADWrongVersion);
          gatt.disconnect();
          return;
        }
        imageNotifyChar = oadService.getCharacteristic(UUID.fromString(TIOADEoadDefinitions.TI_OAD_IMAGE_NOTIFY));
        if (imageNotifyChar == null) {
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADCharacteristicMissingOnPeripheral);
          gatt.disconnect();
          return;
        }
        imageStatusChar = oadService.getCharacteristic(UUID.fromString(TIOADEoadDefinitions.TI_OAD_IMAGE_STATUS));
        if (imageStatusChar != null) {
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADWrongVersion);
          gatt.disconnect();
          return;
        }
        blockRequestChar = oadService.getCharacteristic(UUID.fromString(TIOADEoadDefinitions.TI_OAD_IMAGE_BLOCK_REQUEST));
        if (blockRequestChar == null) {
          if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADCharacteristicMissingOnPeripheral);
          gatt.disconnect();
          return;
        }
        if (progressCallback != null) progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientReady);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          gatt.readPhy();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          gatt.requestMtu(247);
        }
      }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      super.onCharacteristicChanged(gatt, characteristic);
      if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_NOTIFY)) {
        //Got back info from image notify, check the status and report back
      }
      else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_CONTROL)) {
        //Got message from image control point
        switch (status) {
          case tiOADClientHeaderSent:
            break;
          case tiOADClientReady:
            break;
          case tiOADClientImageTransfer:
            break;
        }
      }
      else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_BLOCK_REQUEST)) {
        switch (status) {
          case tiOADClientImageTransfer:
            break;
          case tiOADClientReady:
            break;
        }
      }

    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      super.onDescriptorWrite(gatt, descriptor, status);
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
      super.onMtuChanged(gatt, mtu, status);
      Log.d(TAG,"MTU Changed to : " + mtu + " Bytes " + "Status: " + status);
    }
  };

}
