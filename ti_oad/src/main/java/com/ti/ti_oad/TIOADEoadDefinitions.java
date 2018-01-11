package com.ti.ti_oad;

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




  public static byte   TI_OAD_CONTROL_POINT_CMD_GET_BLOCK_SIZE                   = 0x01;

  public static byte   TI_OAD_CONTROL_POINT_CMD_START_OAD_PROCESS                = 0x03;
  public static byte   TI_OAD_CONTROL_POINT_CMD_ENABLE_OAD_IMAGE                 = 0x04;

  public static byte   TI_OAD_CONTROL_POINT_CMD_IMAGE_BLOCK_WRITE_CHAR_RESPONSE  = 0x12;



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

  public static int BUILD_UINT16(byte a, byte b) {
    return (int)((((int)(a & 0xFF) << 8) & 0x0000FF00) | ((int)(b & 0x000000FF)));
  }

  public static byte GET_HIGH_BYTE_FROM_UINT16(int val) {
    return (byte)((val & 0xff00) >> 8);
  }
  public static byte GET_LOW_BYTE_FROM_UINT16(int val) {
    return (byte)(val & 0xff);
  }

  public enum oadStatusEnumeration {
    tiOADClientInitializing,
    tiOADClientPeripheralConnected,
    tiOADClientOADServiceMissingOnPeripheral,
    tiOADClientOADCharacteristicMissingOnPeripheral,
    tiOADClientOADWrongVersion,
    tiOADClientReady,
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
    tiOADClientCompleteFeedbackFailed
  }

}
