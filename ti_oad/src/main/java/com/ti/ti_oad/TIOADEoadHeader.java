package com.ti.ti_oad;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ole on 10/11/2017.
 */

public class TIOADEoadHeader {

  public static final String TAG = TIOADEoadHeader.class.getSimpleName();

  public static class TIOADEoadSegmentInformation {

    public byte   TIOADSegmentType;
    public int    TIOADWirelessTechnology;
    public byte   TIOADReserved;
    public long   TIOADPayloadLength;
    public byte[] SegmentData;

    public boolean isContigous() {
      return false;
    }
    public boolean isBoundary() {
      return false;
    }

    public void printSegmentInformation(TIOADEoadSegmentInformation segmentInformation) {
      Log.d(TAG,"Segment information :");
      Log.d(TAG,"Segment Type: " + segmentInformation.TIOADSegmentType + " (" +
              ((segmentInformation.TIOADSegmentType == TIOADEoadDefinitions.TI_OAD_EOAD_SEGMENT_TYPE_BOUNDARY_INFO) ? "Boundary Info" :
                      (segmentInformation.TIOADSegmentType == TIOADEoadDefinitions.TI_OAD_EOAD_SEGMENT_TYPE_CONTIGUOUS_INFO) ? "Contiguous Info" :
                              "Unknown Type") + ") ");
      Log.d(TAG,"Segment Wireless Standard: " + TIOADEoadHeader.WirelessStdToString(segmentInformation.TIOADWirelessTechnology));

      if (segmentInformation.isBoundary()) {
        //Do boundary segment specifics here.
        TIOADEoadBoundaryInformation boundaryInformation = (TIOADEoadBoundaryInformation) segmentInformation;
        Log.d(TAG,"Stack Entry Address (32-bit): " + String.format("0x%08x",boundaryInformation.TIOADBoundaryStackEntryAddress));
        Log.d(TAG,"ICall Stack 0 Address (32-bit): " + String.format("0x%08x",boundaryInformation.TIOADBoundaryIcallStack0Address));
        Log.d(TAG,"RAM Start Address (32-bit): " + String.format("0x%08x",boundaryInformation.TIOADBoundaryRamStartAddress));
        Log.d(TAG,"RAM End Address (32-bit): " + String.format("0x%08x",boundaryInformation.TIOADBoundaryRamEndAddress));
      }
      else if (segmentInformation.isContigous()) {
        //Do Contigous segment specifics here.
        TIOADEoadContiguosImageInformation contiguosImageInformation = (TIOADEoadContiguosImageInformation) segmentInformation;
        Log.d(TAG,"Contiguous image information :");
        Log.d(TAG,"Stack Entry Address (32-bit): " + String.format("0x%8x",contiguosImageInformation.TIOADStackEntryAddress));
      }

    }
  }

  public static class TIOADEoadContiguosImageInformation extends TIOADEoadSegmentInformation {
    public long                         TIOADStackEntryAddress;

    @Override
    public boolean isContigous() {
      return true;
    }
  }

  public static class TIOADEoadBoundaryInformation extends TIOADEoadSegmentInformation {
    public long                         TIOADBoundaryStackEntryAddress;
    public long                         TIOADBoundaryIcallStack0Address;
    public long                         TIOADBoundaryRamStartAddress;
    public long                         TIOADBoundaryRamEndAddress;

    @Override
    public boolean isBoundary() {
      return true;
    }
  }



  public byte[] TIOADEoadImageIdentificationValue;
  public long   TIOADEoadImageCRC32;
  public byte   TIOADEoadBIMVersion;
  public byte   TIOADEoadImageHeaderVersion;
  public int    TIOADEoadImageWirelessTechnology;
  public byte[] TIOADEoadImageInformation;
  public long   TIOADEoadImageValidation;
  public long   TIOADEoadImageLength;
  public long   TIOADEoadProgramEntryAddress;
  public byte[] TIOADEoadImageSoftwareVersion;
  public long   TIOADEoadImageEndAddress;
  public int    TIOADEoadImageHeaderLength;
  public int    TIOADEoadReserved;


  public ArrayList<TIOADEoadSegmentInformation> segments;
  public byte[] rawData;

  /*! \brief
   *
   *  @param rawData Raw bytestream of the TI OAD bin file
   *  @return Returns completely parsed file with all sections in file filled up.
   *
   */
  public TIOADEoadHeader(byte[] rawData) {
    this.rawData = rawData;
    /* Important to allocate memory for the all the byte vectors */
    this.TIOADEoadImageIdentificationValue = new byte[8];
    this.TIOADEoadImageInformation = new byte[4];
    this.TIOADEoadImageSoftwareVersion = new byte[4];

  }

  public boolean validateImage() {
    if (this.rawData == null) return false;
    int position = 0;
    //Image identification first
    System.arraycopy(rawData,
            position,
            this.TIOADEoadImageIdentificationValue,
            0,
            this.TIOADEoadImageIdentificationValue.length);
    position += 8;
    //Image CRC next
    this.TIOADEoadImageCRC32 = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    this.TIOADEoadBIMVersion = rawData[position++];
    this.TIOADEoadImageHeaderVersion = rawData[position++];
    this.TIOADEoadImageWirelessTechnology = TIOADEoadDefinitions.BUILD_UINT16(rawData[position + 1],
            rawData[position]);
    position += 2;
    System.arraycopy(rawData,
            position,
            this.TIOADEoadImageInformation,
            0,
            this.TIOADEoadImageInformation.length);
    position += 4;
    this.TIOADEoadImageValidation = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    this.TIOADEoadImageLength = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    this.TIOADEoadProgramEntryAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    System.arraycopy(rawData,
            position,
            this.TIOADEoadImageSoftwareVersion,
            0,
            this.TIOADEoadImageSoftwareVersion.length);
    position += 4;
    this.TIOADEoadImageEndAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    this.TIOADEoadImageHeaderLength = TIOADEoadDefinitions.BUILD_UINT16(rawData[position + 1],
            rawData[position]);
    position += 2;
    this.TIOADEoadReserved = TIOADEoadDefinitions.BUILD_UINT16(rawData[position + 1],
            rawData[position]);
    position += 2;

    //This is the final of the fields in the main header, our position should now be 44
    //We start now parsing the rest of the image, and it should start with a segment info
    //segment with a segment info type we know. If not, abort parsing because we cannot continue

    this.segments = new ArrayList<TIOADEoadSegmentInformation>();

    TIOADEoadSegmentInformation segmentInformation = new TIOADEoadSegmentInformation();

    segmentInformation.TIOADSegmentType = rawData[position++];
    segmentInformation.TIOADWirelessTechnology = TIOADEoadDefinitions.BUILD_UINT16(rawData[position + 1],
            rawData[position]);
    position += 2;
    segmentInformation.TIOADReserved = rawData[position++];
    segmentInformation.TIOADPayloadLength = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position +=4;

    switch (segmentInformation.TIOADSegmentType) {
      case TIOADEoadDefinitions.TI_OAD_EOAD_SEGMENT_TYPE_BOUNDARY_INFO:
        TIOADEoadBoundaryInformation boundaryInformation = new TIOADEoadBoundaryInformation();
        boundaryInformation.TIOADSegmentType = segmentInformation.TIOADSegmentType;
        boundaryInformation.TIOADWirelessTechnology = segmentInformation.TIOADWirelessTechnology;
        boundaryInformation.TIOADReserved = segmentInformation.TIOADReserved;
        boundaryInformation.TIOADPayloadLength = segmentInformation.TIOADPayloadLength;
        this.addBoundaryInformation(boundaryInformation,rawData,position);
        break;
      case TIOADEoadDefinitions.TI_OAD_EOAD_SEGMENT_TYPE_CONTIGUOUS_INFO:
        TIOADEoadContiguosImageInformation contiguosImageInformation = new TIOADEoadContiguosImageInformation();
        contiguosImageInformation.TIOADSegmentType = segmentInformation.TIOADSegmentType;
        contiguosImageInformation.TIOADWirelessTechnology = segmentInformation.TIOADWirelessTechnology;
        contiguosImageInformation.TIOADReserved = segmentInformation.TIOADReserved;
        contiguosImageInformation.TIOADPayloadLength = segmentInformation.TIOADPayloadLength;
        this.addContigousInformation(contiguosImageInformation,rawData,position);
        break;
      default:
        //We must abort here, because we do not know what type this is and
        //it might be garbage so payload length is wrong, but we could possibly
        //check it against remaining length of rawdata ?
        break;
    }

    this.segments.add(segmentInformation);

    return true;
  }

  public void printHeader(TIOADEoadHeader header) {
    Log.d(TAG,"Enhanced OAD Header");
    Log.d(TAG,"Image Information : " + String.format("%c,%c,%c,%c,%c,%c,%c,%c",
              header.TIOADEoadImageIdentificationValue[0],
              header.TIOADEoadImageIdentificationValue[1],
              header.TIOADEoadImageIdentificationValue[2],
              header.TIOADEoadImageIdentificationValue[3],
              header.TIOADEoadImageIdentificationValue[4],
              header.TIOADEoadImageIdentificationValue[5],
              header.TIOADEoadImageIdentificationValue[6],
              header.TIOADEoadImageIdentificationValue[7]));
    Log.d(TAG,"Image CRC32 : " + String.format("0x%08X",header.TIOADEoadImageCRC32));
    Log.d(TAG,"Image BIM version : " + header.TIOADEoadBIMVersion);
    Log.d(TAG,"Image Image Header Version : " + header.TIOADEoadImageHeaderVersion);
    Log.d(TAG,"Image Wireless Standard : " + WirelessStdToString(header.TIOADEoadImageWirelessTechnology));
    Log.d(TAG,"Image Information : " + String.format("%d(0x%02x),%d(0x%02x),%d(0x%02x),%d(0x%02x)",
            header.TIOADEoadImageInformation[0],
            header.TIOADEoadImageInformation[0],
            header.TIOADEoadImageInformation[1],
            header.TIOADEoadImageInformation[1],
            header.TIOADEoadImageInformation[2],
            header.TIOADEoadImageInformation[2],
            header.TIOADEoadImageInformation[3],
            header.TIOADEoadImageInformation[3]));
    Log.d(TAG,"Image Validation : " + String.format("%d(0x%08X)",header.TIOADEoadImageValidation,header.TIOADEoadImageValidation));
    Log.d(TAG,"Image Length : " + String.format("%d(0x%08X) Bytes",header.TIOADEoadImageLength,header.TIOADEoadImageLength));
    Log.d(TAG,"Program Entry Address : " + String.format("0x%08X",header.TIOADEoadProgramEntryAddress));
    Log.d(TAG,"Image Software Version : " + String.format("%c(0x%02X),%c(0x%02X),%c(0x%02X),%c(0x%02X)",
            TIOADEoadImageSoftwareVersion[0],
            TIOADEoadImageSoftwareVersion[0],
            TIOADEoadImageSoftwareVersion[1],
            TIOADEoadImageSoftwareVersion[1],
            TIOADEoadImageSoftwareVersion[2],
            TIOADEoadImageSoftwareVersion[2],
            TIOADEoadImageSoftwareVersion[3],
            TIOADEoadImageSoftwareVersion[3]
            ));
    Log.d(TAG,"Image End Address : " + String.format("0x%08X",header.TIOADEoadImageEndAddress));
    Log.d(TAG,"Image Header Length : " + String.format("%d(0x%08X) Bytes",header.TIOADEoadImageHeaderLength,header.TIOADEoadImageHeaderLength));
    Log.d(TAG,"Image Reserved : " + String.format("%d(0x%04X)",header.TIOADEoadReserved,header.TIOADEoadReserved));
  }

  boolean addBoundaryInformation(TIOADEoadBoundaryInformation boundaryInformation,
                                 byte rawData[],
                                 int position) {
    boundaryInformation.TIOADBoundaryStackEntryAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    boundaryInformation.TIOADBoundaryIcallStack0Address = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    boundaryInformation.TIOADBoundaryRamStartAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;
    boundaryInformation.TIOADBoundaryRamEndAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;

    return true;
  }
  boolean addContigousInformation(TIOADEoadContiguosImageInformation contiguosImageInformation,
                                  byte rawData[],
                                  int position) {
    contiguosImageInformation.TIOADStackEntryAddress = TIOADEoadDefinitions.BUILD_UINT32(rawData[position + 3],
            rawData[position + 2],
            rawData[position + 1],
            rawData[position]);
    position += 4;

    return true;
  }




  public static String WirelessStdToString(int wirelessStd) {
    String returnVal = "";
    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_BLE)
                  != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_BLE) ? " BLE " : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_RF4CE)
                  != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_RF4CE) ? "RF4CE" : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_802_15_4_2_POINT_FOUR)
            != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_802_15_4_2_POINT_FOUR) ? "802.15.4 (2.4GHz)" : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_802_15_4_SUB_ONE)
            != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_802_15_4_SUB_ONE) ? "802.15.4 (Sub-One)" : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_EASY_LINK)
            != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_EASY_LINK) ? "Easy Link" : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_THREAD)
            != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_THREAD) ? "Thread" : "";

    returnVal += ((wirelessStd & TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_ZIGBEE)
            != TIOADEoadDefinitions.TI_OAD_EOAD_WIRELESS_STD_ZIGBEE) ? "ZigBee" : "";

    return returnVal;

  }

}

