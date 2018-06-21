package com.ti.ti_oad;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.ti.ti_oad.BluetoothLEController.BluetoothLEDevice;
import com.ti.ti_oad.BluetoothLEController.BluetoothLEManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.ti.ti_oad.TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientProgrammingAbortedByUser;

/**
 * Created by ole on 01/12/2017.
 */

public class TIOADEoadClient {

  private final static String TAG = TIOADEoadClient.class.getSimpleName();

  private TIOADEoadImageReader fileReader;
  private Context context;
  private TIOADEoadClientProgressCallback progressCallback;
  private BluetoothGattCharacteristic imageNotifyChar;
  private BluetoothGattCharacteristic blockRequestChar;
  private BluetoothGattCharacteristic imageCountChar;
  private BluetoothGattCharacteristic imageStatusChar;
  private BluetoothGattCharacteristic imageControlChar;

  private TIOADEoadDefinitions.oadStatusEnumeration status;
  private TIOADEoadDefinitions.oadStatusEnumeration internalState;

  BluetoothLEDevice oadDevice;
  byte[] TIOADEoadDeviceID;
  int myMTU;

  private int TIOADEoadBlockSize = 0;
  private int TIOADEoadTotalBlocks = 0;



  public TIOADEoadClient(Context context) {
    this.context = context;
  }

  public Runnable oadThread = new Runnable() {
    @Override
    public void run() {
        Log.d(TAG,"OAD Thread started !");
        internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientInitializing;

        oadDevice.myCB = cb;
        oadDevice.connectDevice();

        while(internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientReady) {
          sleepWait(200);
        }

      sleepWait(5000);

      //Update MTU first
      oadDevice.g.requestMtu(255);

      sleepWait(2000);

      oadDevice.g.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
      sleepWait(2000);

      new Thread(new Runnable() {
        @Override
        public void run() {
          boolean deviceReady = setCharacteristicsAndCheckEOAD();
          if (deviceReady) {
            sendEoadBlockSizeRequest();
          }
        }
      }).start();

      internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientBlockSizeRequestSent;

      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientGotBlockSizeResponse) {
        sleepWait(200);
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          sendEoadReadDeviceID();
        }
      }).start();

      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientDeviceTypeRequestResponse) {
        sleepWait(200);
      }
    }
  };


  public Runnable oadProgramThread = new Runnable() {
    @Override
    public void run() {
      new Thread(new Runnable() {
        @Override
        public void run() {
          sendHeaderToDevice();
        }
      }).start();

      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientHeaderOK) {
        sleepWait(200);
      }

      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientHeaderOK);
      }

      new Thread(new Runnable() {
        @Override
        public void run() {
          sleepWait(200);
          sendEoadStartOADProcessCmd();
        }
      }).start();

      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransfer) {
        sleepWait(200);
      }


      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransferOK) {
        sleepWait(200);
        if (internalState == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientProgrammingAbortedByUser) {
          return;
        }
      }
      if (progressCallback != null) {
        progressCallback.oadProgressUpdate(100,TIOADEoadTotalBlocks);
      }
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientEnableOADImageCommandSent);
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          sendEoadEnableImageCmd();
        }
      }).start();
      while (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteFeedbackOK) {
        sleepWait(200);
      }

      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteFeedbackOK);
      }
    }
  };

  public void sleepWait(int milliSeconds) {
    try {
      Thread.sleep(milliSeconds);
    }
    catch (InterruptedException e) {
      Log.d(TAG,"Interrupted while in sleepWait !");
    }
  }


  public boolean initializeTIOADEoadProgrammingOnDevice(BluetoothDevice dev,TIOADEoadClientProgressCallback callback) {
    //End here, we cannot do anything before we get a connect to a device, and then we continue...

    this.progressCallback = callback;
    oadDevice = new BluetoothLEDevice(dev,this.context);

    new Thread(oadThread).start();

    return true;
  }


  public void start(String filename) {
    //Start programming directly
    fileReader = new TIOADEoadImageReader(filename,this.context);

    //We now have the chip id and can check it against the image file to see if this can be programmed ...
    boolean imageCanBeProgrammed = true;
    byte[] fileImageInfo = fileReader.imageHeader.TIOADEoadImageIdentificationValue;
    byte[] oadClientImageInfo = TIOADEoadDefinitions.oadImageInfoFromChipType(TIOADEoadDeviceID);
    for (int ii = 0; ii < 8; ii++) {
      if (fileImageInfo[ii] != oadClientImageInfo[ii]) imageCanBeProgrammed = false;
    }
    if (!imageCanBeProgrammed) {
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientFileIsNotForDevice);
      }
      //Cannot continue
      return;
    }
    new Thread(oadProgramThread).start();
    return;
  }


  public void start(Uri filename) {
    //Start programming directly
    fileReader = new TIOADEoadImageReader(filename,this.context);

    //We now have the chip id and can check it against the image file to see if this can be programmed ...
    boolean imageCanBeProgrammed = true;
    byte[] fileImageInfo = fileReader.imageHeader.TIOADEoadImageIdentificationValue;
    byte[] oadClientImageInfo = TIOADEoadDefinitions.oadImageInfoFromChipType(TIOADEoadDeviceID);
    for (int ii = 0; ii < 8; ii++) {
      if (fileImageInfo[ii] != oadClientImageInfo[ii]) imageCanBeProgrammed = false;
    }
    if (!imageCanBeProgrammed) {
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientFileIsNotForDevice);
      }
      //Cannot continue
      return;
    }
    new Thread(oadProgramThread).start();
    return;
  }

  public void abortProgramming() {
    internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientProgrammingAbortedByUser;
  }


  public void sendHeaderToDevice() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        oadDevice.writeCharacteristicAsync(imageNotifyChar,fileReader.getHeaderForImageNotify());
      }
    }).start();
  }


  public void sendEoadDataBlock(long block) {
        int dataSize = TIOADEoadBlockSize - 4;
        int readSize = dataSize;
        if (fileReader.getRawImageData().length - ((block) * dataSize) < dataSize) {
          readSize = fileReader.getRawImageData().length - ((int)(block) * dataSize);
        }
        if (block == (TIOADEoadTotalBlocks)) {
          Log.d(TAG,"Last block has been sent");
          internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransferOK;
          return;
        }
        byte[] buffer = new byte[readSize + 4];
        for (int ii = 0; ii < 4; ii++) {
          buffer[ii] = TIOADEoadDefinitions.GET_BYTE_FROM_UINT32(block, ii);
        }
        Log.d(TAG,"Block: " + block + " srcPos: " + (int)(dataSize * block) + " dstPos: " + 4 + " readSize: " + readSize);
        System.arraycopy(fileReader.getRawImageData(),(int)(dataSize * block),buffer,4,readSize);
        oadDevice.writeCharacteristicAsync(blockRequestChar,buffer);
        Log.d(TAG,"Sent Block " + block + " With Data:" + TIOADEoadDefinitions.BytetohexString(buffer));
        if (progressCallback != null) {
          progressCallback.oadProgressUpdate((float)block/(float)TIOADEoadTotalBlocks * (float)100, (int)block);
          progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransfer);
        }

  }

  public void sendEoadReadDeviceID() {
    byte cmd = TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_DEVICE_TYPE_CMD;
    oadDevice.writeCharacteristicAsync(imageControlChar,cmd);
  }

  public void sendEoadEnableImageCmd() {
    byte cmd = TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_ENABLE_OAD_IMAGE;
    oadDevice.writeCharacteristicAsync(imageControlChar,cmd);
  }

  public void sendEoadStartOADProcessCmd () {
    byte cmd = TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_START_OAD_PROCESS;
    TIOADEoadTotalBlocks = this.fileReader.getRawImageData().length / (TIOADEoadBlockSize - 4);
    if ((this.fileReader.getRawImageData().length - (TIOADEoadTotalBlocks * (TIOADEoadBlockSize - 4))) > 0) {
      TIOADEoadTotalBlocks++;
    }
    oadDevice.writeCharacteristicAsync(imageControlChar,cmd);
  }

  public void sendEoadBlockSizeRequest() {
    byte cmd = TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_GET_BLOCK_SIZE;
    oadDevice.writeCharacteristicAsync(imageControlChar,cmd);
  }

  public void handleEoadBlockSizeResponse(byte[] resp) {
    if (resp[0] != TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_GET_BLOCK_SIZE) {
      return;
    }
    TIOADEoadBlockSize = TIOADEoadDefinitions.BUILD_UINT16(resp[2],resp[1]);
    Log.d(TAG,"Block Size is : " + TIOADEoadBlockSize);
    internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientGotBlockSizeResponse;
  }


  public void handleOADControlPointMessage(final byte[] response) {
    switch(response[0]) {
      case TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_DEVICE_TYPE_CMD:
        internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientDeviceTypeRequestResponse;
        TIOADEoadDeviceID = new byte[4];
        for (int ii = 0; ii < 4; ii++) TIOADEoadDeviceID[ii] = response[ii + 1];
        Log.d(TAG,"Device ID: "  + TIOADEoadDefinitions.oadChipTypePrettyPrint(TIOADEoadDeviceID));
        if (TIOADEoadDefinitions.oadChipTypePrettyPrint(TIOADEoadDeviceID).equals("CC1352P")) {
          //Special case for the CC1352P's because they can have two different RF layouts
          if (progressCallback != null) {
            progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientChipIsCC1352PShowWarningAboutLayouts);
          }
        }
        if (progressCallback != null) {
          progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientReady);
        }
        break;
      case TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_GET_BLOCK_SIZE:
        handleEoadBlockSizeResponse(response);
        //We should now be ready to actually run an OAD programming
        break;
      case TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_ENABLE_OAD_IMAGE:
        if (response[1] == 0x00) {
          internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteFeedbackOK;
        }
        break;
      case TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_IMAGE_BLOCK_WRITE_CHAR_RESPONSE:
        if (response[1] == 0x00) {
          if (internalState == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientProgrammingAbortedByUser) {
            oadDevice.g.disconnect();
            oadDevice.g.close();
            if (progressCallback != null) {
              progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientProgrammingAbortedByUser);
            }
            return;
          }
          internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransfer;
          new Thread(new Runnable() {
            @Override
            public void run() {
              long block = TIOADEoadDefinitions.BUILD_UINT32(response[5],response[4],response[3],response[2]);
              sendEoadDataBlock(block);
            }
          },"OAD Block Send Thread").start();
        }
        else if (response[1] == 0x0e) {
          internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransferOK;
          return;
        }
        else {
          if (progressCallback != null) {
            progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransferFailed);
          }
        }
        break;
      case TIOADEoadDefinitions.TI_OAD_CONTROL_POINT_CMD_START_OAD_PROCESS:
        break;
      default:
        Log.d(TAG,"Unknown Control Point Response: " + TIOADEoadDefinitions.BytetohexString(response));
        break;
    }
  }




  public boolean setCharacteristicsAndCheckEOAD() {
    for (BluetoothGattService service : oadDevice.g.getServices()) {
      if (service.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_SERVICE)) {
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
          if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_BLOCK_REQUEST)) {
            blockRequestChar = characteristic;
            oadDevice.setCharacteristicNotificationSync(blockRequestChar,true);
          } else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_CONTROL)) {
            imageControlChar = characteristic;
            oadDevice.setCharacteristicNotificationSync(imageControlChar,true);
          } else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_NOTIFY)) {
            imageNotifyChar = characteristic;
            oadDevice.setCharacteristicNotificationSync(imageNotifyChar,true);
          } else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_COUNT)) {
            imageCountChar = characteristic;
            if (progressCallback != null) {
              progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADWrongVersion);
            }
            return false;
          } else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_STATUS)) {
            imageStatusChar = characteristic;
            if (progressCallback != null) {
              progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADWrongVersion);
            }
            return false;
          }
        }
        if ((blockRequestChar != null) && (imageNotifyChar != null) && (imageControlChar != null)) {
          return true;
        }
      }
    }
    if (progressCallback != null) {
      progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientOADServiceMissingOnPeripheral);
    }
    return false;
  }


  public int getMTU() {
    return myMTU;
  }

  public int getTIOADEoadBlockSize() {
    return TIOADEoadBlockSize;
  }

  public int getTIOADEoadTotalBlocks() {
    return TIOADEoadTotalBlocks;
  }

  public byte[] getTIOADEoadDeviceID() { return TIOADEoadDeviceID; }

  BluetoothLEDevice.BluetoothLEDeviceCB cb = new BluetoothLEDevice.BluetoothLEDeviceCB() {
    @Override
    public void waitingForConnect(BluetoothLEDevice dev, int milliSecondsLeft, int retry) {
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientDeviceConnecting);
      }
    }

    @Override
    public void waitingForDiscovery(BluetoothLEDevice dev, int milliSecondsLeft, int retry) {
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientDeviceDiscovering);
      }
    }

    @Override
    public void deviceReady(BluetoothLEDevice dev) {
      Log.d(TAG,"Device is ready !");
      if (progressCallback != null) {
        progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientDeviceMTUSet);
      }
      internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientReady;

    }

    @Override
    public void deviceFailed(BluetoothLEDevice dev) {

    }

    @Override
    public void deviceConnectTimedOut(BluetoothLEDevice dev) {

    }

    @Override
    public void deviceDiscoveryTimedOut(BluetoothLEDevice dev) {

    }

    @Override
    public void didUpdateCharacteristicData(BluetoothLEDevice dev, final BluetoothGattCharacteristic characteristic) {
      Log.d(TAG,"Characteristic: " + characteristic.getUuid().toString() + " Value: " + TIOADEoadDefinitions.BytetohexString(characteristic.getValue()));
      if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_CONTROL)) {
        handleOADControlPointMessage(characteristic.getValue());
      }
      else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_NOTIFY)) {
        if (characteristic.getValue()[0] == 0x00) {
          status = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientHeaderOK;
          internalState = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientHeaderOK;
        }
        else {
          status = TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientHeaderFailed;
          Log.d(TAG,"Failed when sending header, cannot continue");
        }
        if (progressCallback != null) {
          progressCallback.oadStatusUpdate(status);
        }
      }
      else if (characteristic.getUuid().toString().equalsIgnoreCase(TIOADEoadDefinitions.TI_OAD_IMAGE_BLOCK_REQUEST)) {


      }
    }

    @Override
    public void didReadCharacteristicData(BluetoothLEDevice dev, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void didUpdateCharacteristicNotification(BluetoothLEDevice dev, BluetoothGattCharacteristic characteristic) {
      Log.d(TAG,"didUpdateCharacteristicNotification: " + characteristic.getUuid().toString());
    }

    @Override
    public void didUpdateCharacteristicIndication(BluetoothLEDevice dev) {

    }

    @Override
    public void didWriteCharacteristicData(BluetoothLEDevice dev, BluetoothGattCharacteristic characteristic) {
      Log.d(TAG,"didWriteCharacteristic: " + characteristic.getUuid().toString() + " Value: " + TIOADEoadDefinitions.BytetohexString(characteristic.getValue()));
    }

    @Override
    public void deviceDidDisconnect(BluetoothLEDevice dev) {
      if (progressCallback != null) {
        if (internalState != TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientImageTransfer) {
          progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteDeviceDisconnectedPositive);
        }
        else {
          progressCallback.oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteDeviceDisconnectedDuringProgramming);
        }
      }
    }

    @Override
    public void mtuValueChanged(int mtu) {
      myMTU = mtu;
    }
  };



}
