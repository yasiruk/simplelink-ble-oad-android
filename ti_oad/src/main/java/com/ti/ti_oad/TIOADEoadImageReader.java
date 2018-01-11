package com.ti.ti_oad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ole on 10/11/2017.
 */

public class TIOADEoadImageReader {

  private final String TAG = TIOADEoadImageReader.class.getSimpleName();

  private byte[] rawImageData;
  public TIOADEoadHeader imageHeader;
  private ArrayList <TIOADEoadHeader.TIOADEoadSegmentInformation> imageSegments;
  private Context context;

  public TIOADEoadImageReader(String assetFileName, Context context) {
    this.imageSegments = new ArrayList<>();
    this.context = context;
    this.TIOADToadLoadImage(assetFileName);
  }

  public void TIOADToadLoadImage(String assetFileName) {
    AssetManager aMan = this.context.getAssets();

    try {
      InputStream inputStream = aMan.open(assetFileName);
      rawImageData = new byte[inputStream.available()];
      int len = inputStream.read(rawImageData);
      Log.d(TAG,"Read " + len + " bytes from asset file");
      this.imageHeader = new TIOADEoadHeader(rawImageData);
      this.imageHeader.validateImage();
    }
    catch (IOException e) {
      Log.d(TAG,"Could not read input file");
    }
  }
}

