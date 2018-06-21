package com.ti.ti_oad;

import android.util.Log;

import java.util.Formatter;

/**
 * Created by ole on 24/11/2017.
 */

public class TIOADEoadDefinitions {
  /*! Main TI OAD Service UUID */
  public static String          TI_OAD_SERVICE              = "f000ffc0-0451-4000-b000-000000000000";
  /*! Characteristic used to identify and approve a new image */
  public static String          TI_OAD_IMAGE_NOTIFY         = "f000ffc1-0451-4000-b000-000000000000";
  /*! Characteristic used to send the actual image data to the client */
  public static String          TI_OAD_IMAGE_BLOCK_REQUEST  = "f000ffc2-0451-4000-b000-000000000000";
  /*! Characteristic for image count (Legacy, not used on TI Enhanced OAD and not available on EOAD devices */
  public static String          TI_OAD_IMAGE_COUNT          = "f000ffc3-0451-4000-b000-000000000000";
  /*! Characteristic for image status (Legacy, not used on TI Enhanced OAD and not available on EOAD devices */
  public static String          TI_OAD_IMAGE_STATUS         = "f000ffc4-0451-4000-b000-000000000000";
  /*! Characteristic for OAD control point, only on EOAD devices and controls all aspects of EOAD */
  public static String          TI_OAD_IMAGE_CONTROL        = "f000ffc5-0451-4000-b000-000000000000";


  public static byte[] TI_OAD_IMG_INFO_CC2640R2 = new byte[] {'O','A','D',' ','I','M','G',' '};
  public static byte[] TI_OAD_IMG_INFO_CC26X2R1 = new byte[] {'C','C','2','6','x','2','R','1'};
  public static byte[] TI_OAD_IMG_INFO_CC13XR1 = new byte[] {'C','C','1','3','x','2','R','1'};


  public final static byte   TI_OAD_CONTROL_POINT_CMD_GET_BLOCK_SIZE                   = 0x01;

  public final static byte   TI_OAD_CONTROL_POINT_CMD_START_OAD_PROCESS                = 0x03;
  public final static byte   TI_OAD_CONTROL_POINT_CMD_ENABLE_OAD_IMAGE                 = 0x04;

  public final static byte   TI_OAD_CONTROL_POINT_CMD_DEVICE_TYPE_CMD                  = 0x10;

  public final static byte   TI_OAD_CONTROL_POINT_CMD_IMAGE_BLOCK_WRITE_CHAR_RESPONSE  = 0x12;



  public static byte  TI_OAD_IMAGE_IDENTIFY_PACKAGE_LEN                          = 22;



  public final static byte  TI_OAD_EOAD_SEGMENT_TYPE_BOUNDARY_INFO               = 0x00;
  public final static byte  TI_OAD_EOAD_SEGMENT_TYPE_CONTIGUOUS_INFO             = 0x01;

  public static int   TI_OAD_EOAD_SEGMENT_TYPE_WIRELESS_STD_OFF                   = 1;
  public static int   TI_OAD_EOAD_SEGMENT_TYPE_PAYLOAD_LEN_OFF                    = 4;


  public static int   TI_OAD_EOAD_WIRELESS_STD_BLE                                = 0x01;
  public static int   TI_OAD_EOAD_WIRELESS_STD_802_15_4_SUB_ONE                   = 0x02;
  public static int   TI_OAD_EOAD_WIRELESS_STD_802_15_4_2_POINT_FOUR              = 0x04;
  public static int   TI_OAD_EOAD_WIRELESS_STD_ZIGBEE                             = 0x08;
  public static int   TI_OAD_EOAD_WIRELESS_STD_RF4CE                              = 0x10;
  public static int   TI_OAD_EOAD_WIRELESS_STD_THREAD                             = 0x20;
  public static int   TI_OAD_EOAD_WIRELESS_STD_EASY_LINK                          = 0x40;


  public static int   TI_OAD_EOAD_IMAGE_COPY_STATUS_NO_ACTION_NEEDED              = 0xFF;
  public static int   TI_OAD_EOAD_IMAGE_COPY_STATUS_IMAGE_TO_BE_COPIED            = 0xFE;
  public static int   TI_OAD_EOAD_IMAGE_COPY_STATUS_IMAGE_COPIED                  = 0xFC;

  public static int   TI_OAD_EOAD_IMAGE_CRC_STATUS_INVALID                        = 0x00;
  public static int   TI_OAD_EOAD_IMAGE_CRC_STATUS_VALID                          = 0x02;
  public static int   TI_OAD_EOAD_IMAGE_CRC_STATUS_NOT_CALCULATED_YET             = 0x03;

  public static int   TI_OAD_EOAD_IMAGE_TYPE_PERSISTANT_APP                       = 0x00;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_APP                                  = 0x01;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_STACK                                = 0x02;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_APP_STACK_MERGED                     = 0x03;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_NETWORK_PROCESSOR                    = 0x04;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_BLE_FACTORY_IMAGE                    = 0x05;
  public static int   TI_OAD_EOAD_IMAGE_TYPE_BIM                                  = 0x06;


  public static int   TI_OAD_EOAD_IMAGE_HEADER_LEN                                = 44;
  public static int   TI_OAD_EOAD_SEGMENT_INFO_LEN                                = 8;


  public static long BUILD_UINT32(byte a, byte b, byte c, byte d) {
    long al,bl,cl,dl,result;
    al = (((long)(a & 0xFF)<< 24) & 0x00000000FF000000);
    bl = (((long)(b & 0xFF)<< 16) & 0x0000000000FF0000);
    cl = (((long)(c & 0xFF)<< 8) &  0x000000000000FF00);
    dl = (((long)d & 0xFF)      &  0x00000000000000FF);
    result = (long)((al | bl | cl | dl) & 0x00000000FFFFFFFF);
    return result;
  }

  public static byte GET_BYTE_FROM_UINT32(long uint32,int whichByte) {
    switch (whichByte) {
      case 0:
        return (byte)(uint32 & 0xFF);
      case 1:
        return (byte)((uint32 >> 8) & 0xff);
      case 2:
        return (byte)((uint32 >> 16) & 0xff);
      case 3:
        return (byte)((uint32 >> 24) & 0xff);
      default:
        return 0x00;
    }
  }

  public static int BUILD_UINT16(byte a, byte b) {
    return (int)((((int)(a & 0xFF) << 8) & 0x0000FF00) | ((int)(b & 0x000000FF)));
  }

  public static String BytetohexString(byte[] b) {
    if (b == null) return "NULL";
    StringBuilder sb = new StringBuilder(b.length * (2 + 1));
    Formatter formatter = new Formatter(sb);

    for (int i = 0; i < b.length; i++) {
      if (i < b.length - 1)
        formatter.format("%02X:", b[i]);
      else
        formatter.format("%02X", b[i]);

    }
    formatter.close();

    return sb.toString();
  }

  public static byte GET_HIGH_BYTE_FROM_UINT16(int val) {
    return (byte)((val & 0xff00) >> 8);
  }
  public static byte GET_LOW_BYTE_FROM_UINT16(int val) {
    return (byte)(val & 0xff);
  }

  public enum oadStatusEnumeration {
    tiOADClientDeviceConnecting,
    tiOADClientDeviceDiscovering,
    tiOADClientConnectionParametersChanged,
    tiOADClientDeviceMTUSet,
    tiOADClientInitializing,
    tiOADClientPeripheralConnected,
    tiOADClientOADServiceMissingOnPeripheral,
    tiOADClientOADCharacteristicMissingOnPeripheral,
    tiOADClientOADWrongVersion,
    tiOADClientReady,
    tiOADClientFileIsNotForDevice,
    tiOADClientDeviceTypeRequestResponse,
    tiOADClientBlockSizeRequestSent,
    tiOADClientGotBlockSizeResponse,
    tiOADClientHeaderSent,
    tiOADClientHeaderOK,
    tiOADClientHeaderFailed,
    tiOADClientOADProcessStartCommandSent,
    tiOADClientImageTransfer,
    tiOADClientImageTransferFailed,
    tiOADClientImageTransferOK,
    tiOADClientEnableOADImageCommandSent,
    tiOADClientCompleteFeedbackOK,
    tiOADClientCompleteFeedbackFailed,
    tiOADClientCompleteDeviceDisconnectedPositive,
    tiOADClientCompleteDeviceDisconnectedDuringProgramming,
    tiOADClientProgrammingAbortedByUser,
    tiOADClientChipIsCC1352PShowWarningAboutLayouts,
  }

  public enum oadChipType {
    tiOADChipTypeCC1310,
    tiOADChipTypeCC1350,
    tiOADChipTypeCC2620,
    tiOADChipTypeCC2630,
    tiOADChipTypeCC2640,
    tiOADChipTypeCC2650,
    tiOADChipTypeCustomOne,
    tiOADChipTypeCustomTwo,
    tiOADChipTypeCC2640R2,
    tiOADChipTypeCC2642,
    tiOADChipTypeCC2644,
    tiOADChipTypeCC2652,
    tiOADChipTypeCC1312,
    tiOADChipTypeCC1352,
    tiOADChipTypeCC1352P,
  }

  public enum oadChipFamily {
    tiOADChipFamilyCC26x0,
    tiOADChipFamilyCC13x0,
    tiOADChipFamilyCC26x1,
    tiOADChipFamilyCC26x0R2,
    tiOADChipFamilyCC13x2_CC26x2,
  }

  static public String oadImageIdentificationPrettyPrint(byte[] imageId) {
    boolean match = true;
    for (int ii = 0; ii < 8; ii++) {
      if (imageId[ii] != TI_OAD_IMG_INFO_CC2640R2[ii]) match = false;
    }
    if (match) return "CC2640R2";
    match = true;
    for (int ii = 0; ii < 8; ii++) {
      if (imageId[ii] != TI_OAD_IMG_INFO_CC26X2R1[ii]) match = false;
    }
    if (match) return "CC26X2R";
    match = true;
    for (int ii = 0; ii < 8; ii++) {
      if (imageId[ii] != TI_OAD_IMG_INFO_CC13XR1[ii]) match = false;
    }
    if (match) return "CC13X2R";

    return "UNKNOWN";
  }

  static public byte[] oadImageInfoFromChipType (byte[] chipTypeVector) {
    oadChipType chipType = oadChipType.values()[chipTypeVector[0]];
    switch (chipType) {
      case tiOADChipTypeCC2640R2:
        return TI_OAD_IMG_INFO_CC2640R2;
      case tiOADChipTypeCC2642:
      case tiOADChipTypeCC2652:
        return TI_OAD_IMG_INFO_CC26X2R1;
      case tiOADChipTypeCC1352:
      case tiOADChipTypeCC1352P:
        return TI_OAD_IMG_INFO_CC13XR1;
      default:
        return new byte[8];
    }
  }

  static public String oadChipTypePrettyPrint(byte[] chipTypeVector) {
    oadChipType chipType = oadChipType.values()[chipTypeVector[0]];
    //oadChipFamily chipFamily = chipType[1];

    switch (chipType) {
      case tiOADChipTypeCC1310:
        return "CC1310";
      case tiOADChipTypeCC1312:
        return "CC1312";
      case tiOADChipTypeCC1350:
        return "CC1350";
      case tiOADChipTypeCC1352:
        return "CC1352";
      case tiOADChipTypeCC1352P:
        return "CC1352P";
      case tiOADChipTypeCC2620:
        return "CC2620";
      case tiOADChipTypeCC2630:
        return "CC2630";
      case tiOADChipTypeCC2640:
        return "CC2640";
      case tiOADChipTypeCC2640R2:
        return "CC2640R2";
      case tiOADChipTypeCC2642:
        return "CC2642";
      case tiOADChipTypeCC2644:
        return "CC2644";
      case tiOADChipTypeCC2650:
        return "CC2650";
      case tiOADChipTypeCC2652:
        return "CC2652";
      case tiOADChipTypeCustomOne:
      case tiOADChipTypeCustomTwo:
        return "Custom";
    }
    return "Unknown";
  }


  static public String oadStatusEnumerationGetDescriptiveString(oadStatusEnumeration status) {
    switch (status) {
      case tiOADClientDeviceConnecting:
        return "TI EOAD Client is connecting !";
      case tiOADClientDeviceDiscovering:
        return "TI EOAD Client is discovering services !";
      case tiOADClientConnectionParametersChanged:
        return "TI EOAD Client waiting for connection parameter change";
      case tiOADClientDeviceMTUSet:
        return "TI EOAD Client waiting for MTU Update";
      case tiOADClientInitializing:
        return "TI EOAD Client is initializing !";
      case tiOADClientPeripheralConnected:
        return "Connected to peripheral";
      case tiOADClientOADServiceMissingOnPeripheral:
        return "EOAD service is missing on peripheral, cannot continue !";
      case tiOADClientOADCharacteristicMissingOnPeripheral:
        return "Found EOAD service, but it`s missing some characteristics !";
      case tiOADClientOADWrongVersion:
        return "OAD on peripheral has the wrong version !";
      case tiOADClientReady:
        return "EOAD Client is ready for programming";
      case tiOADClientBlockSizeRequestSent:
        return "EOAD Client sent block size request to peripheral";
      case tiOADClientGotBlockSizeResponse:
        return "EOAD Client received block size response from peripheral";
      case tiOADClientHeaderSent:
        return "EOAD Client sent image header to peripheral";
      case tiOADClientHeaderOK:
        return "EOAD Client header was accepted by peripheral";
      case tiOADClientHeaderFailed:
        return "EOAD Client header was rejected by peripheral, cannot continue !";
      case tiOADClientOADProcessStartCommandSent:
        return "Sent start command to peripheral";
      case tiOADClientImageTransfer:
        return "EOAD Image is transfering";
      case tiOADClientImageTransferFailed:
        return "EOAD Image transfer failed, cannot continue !";
      case tiOADClientImageTransferOK:
        return "EOAD Image transfer completed OK";
      case tiOADClientEnableOADImageCommandSent:
        return "EOAD Image Enable command sent";
      case tiOADClientCompleteFeedbackOK:
        return "EOAD Image Enable OK, device is rebooting on new image !";
      case tiOADClientCompleteFeedbackFailed:
        return "EOAD Image Enable FAILED, device continuing on old image !";
      case tiOADClientFileIsNotForDevice:
        return "EOAD Image is not for this device, cannot continue";
      case tiOADClientDeviceTypeRequestResponse:
        return "EOAD Image device type response received";
      case tiOADClientCompleteDeviceDisconnectedPositive:
        return "TI EOAD Client disconnected after successfull programming !";
      case tiOADClientCompleteDeviceDisconnectedDuringProgramming:
        return "TI EOAD Client disconnected during image transfer, please move closer and try again !";
      case tiOADClientProgrammingAbortedByUser:
        return "Programming aborted by user !";
      default:
        return "Unknown states";
    }
  }

}
