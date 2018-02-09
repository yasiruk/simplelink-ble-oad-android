package com.ti.ti_oad;

/**
 * Created by ole on 01/12/2017.
 */

public interface TIOADEoadClientProgressCallback {

  void oadProgressUpdate(float percent, int currentBlock);
  void oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration status);

}
