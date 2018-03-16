package com.ti.ti_oad;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

public class TIOADEoadImageReader {

  private final String TAG = TIOADEoadImageReader.class.getSimpleName();

  private byte[] rawImageData;
  public TIOADEoadHeader imageHeader;
  private ArrayList <TIOADEoadHeader.TIOADEoadSegmentInformation> imageSegments;
  private Context context;

  public TIOADEoadImageReader(Uri filename, Context context) {
    this.imageSegments = new ArrayList<>();
    this.context = context;
    this.TIOADToadLoadImageFromDevice(filename);
  }

  public void TIOADToadLoadImage(String assetFilename) {
    AssetManager aMan = this.context.getAssets();

    try {
      InputStream inputStream = aMan.open(assetFilename);
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
  public void TIOADToadLoadImageFromDevice(Uri filename) {
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(filename);
      rawImageData = new byte[inputStream.available()];
      int len = inputStream.read(rawImageData);
      Log.d(TAG,"Read " + len + " bytes from file");
      this.imageHeader = new TIOADEoadHeader(rawImageData);
      this.imageHeader.validateImage();
    }
    catch (IOException e) {
      Log.d(TAG,"Could not read input file");
    }
  }

  public byte[] getRawImageData() {
    return rawImageData;
  }

  public byte[] getHeaderForImageNotify() {
    byte[] imageNotifyHeader = new byte[22];
    int position = 0;
    //0
    System.arraycopy(imageHeader.TIOADEoadImageIdentificationValue,0,imageNotifyHeader,position,imageHeader.TIOADEoadImageIdentificationValue.length);
    position += imageHeader.TIOADEoadImageIdentificationValue.length;
    //7
    imageNotifyHeader[position++] = imageHeader.TIOADEoadBIMVersion;
    //8
    imageNotifyHeader[position++] = imageHeader.TIOADEoadImageHeaderVersion;
    //9
    System.arraycopy(imageHeader.TIOADEoadImageInformation,0,imageNotifyHeader,position,imageHeader.TIOADEoadImageInformation.length);
    position += imageHeader.TIOADEoadImageInformation.length;
    //13
    for (int ii = 0; ii < 4; ii++) {
      imageNotifyHeader[position++] = TIOADEoadDefinitions.GET_BYTE_FROM_UINT32(imageHeader.TIOADEoadImageLength, ii);
    }
    //17
    System.arraycopy(imageHeader.TIOADEoadImageSoftwareVersion,0,imageNotifyHeader,position,imageHeader.TIOADEoadImageSoftwareVersion.length);
    position += imageHeader.TIOADEoadImageSoftwareVersion.length;
    //21
    return imageNotifyHeader;
  }


}

