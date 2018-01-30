package com.ti.ti_oad_example;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ti.ti_oad.TIOADEoadClient;
import com.ti.ti_oad.TIOADEoadClientProgressCallback;
import com.ti.ti_oad.TIOADEoadDefinitions;
import com.ti.ti_oad.TIOADEoadHeader;
import com.ti.ti_oad.TIOADEoadImageReader;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TIOADExampleActivity extends AppCompatActivity {
  static private final String TAG = TIOADExampleActivity.class.getSimpleName();
  static public final int SCAN_PERMISSIONS_CODE = 1;
  TIOADExampleActivity mThis;
  ArrayList<BluetoothDevice> mDeviceList;
  TableLayout mDeviceListTable;
  BluetoothLeScanner scanner;
  TIOADEoadClient client;
  ProgressBar oadProgressBar;
  TextView oadState;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mThis = this;
    mDeviceList = new ArrayList<BluetoothDevice>();
    setContentView(R.layout.activity_tioad_example);

    TIOADEoadImageReader reader = new TIOADEoadImageReader(
            "ble5_project_zero_cc26x2r1lp_app_FlashROM_Release_oad.bin",
            getApplicationContext());
    reader.imageHeader.printHeader(reader.imageHeader);
    for (TIOADEoadHeader.TIOADEoadSegmentInformation s : reader.imageHeader.segments) {
      s.printSegmentInformation(s);
    }

    Button startScanButton = (Button)findViewById(R.id.start_scan_button);
    startScanButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startScanForDevices();
      }
    });

    mDeviceListTable = (TableLayout)findViewById(R.id.device_table);

    oadProgressBar = (ProgressBar)findViewById(R.id.oad_progress);
    oadProgressBar.setMax(100);

    oadState = (TextView)findViewById(R.id.oad_status);


    Log.d(TAG,"Requesting permission");
    int permissionCheck = ContextCompat.checkSelfPermission(mThis, Manifest.permission.ACCESS_FINE_LOCATION);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mThis,Manifest.permission.ACCESS_FINE_LOCATION)) {

      }
      else {
        ActivityCompat.requestPermissions(mThis,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},SCAN_PERMISSIONS_CODE);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }


  public void startScanForDevices() {
    BluetoothManager m = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter adapt = m.getAdapter();

    scanner = adapt.getBluetoothLeScanner();
    scanner.startScan(bleScanCallback);

  }

  private ScanCallback bleScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      if (!mDeviceList.contains(result.getDevice())) {
        Log.d(TAG,"Found new device: " + result.getDevice().getName() + " (" + result.getDevice().getAddress() + ")");
        mDeviceList.add(result.getDevice());
        LayoutInflater li = LayoutInflater.from(mThis);
        final View deviceTR = li.inflate(R.layout.tr_device_row,null, false);
        deviceTR.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.d(TAG,"Cell clicked: " + mDeviceListTable.indexOfChild(deviceTR));
            mDeviceListTable.setVisibility(View.INVISIBLE);
            scanner.stopScan(bleScanCallback);
            deviceWasClicked(mDeviceListTable.indexOfChild(deviceTR));
          }
        });

        ImageView devIcon = (ImageView)deviceTR.findViewById(R.id.device_icon);
        devIcon.setImageResource(R.mipmap.device_unknown);
        TextView devName = (TextView)deviceTR.findViewById(R.id.device_name);
        TextView devAddress = (TextView)deviceTR.findViewById(R.id.device_address);
        devName.setText(((result.getDevice().getName()) == null) ? "No localname" : result.getDevice().getName());
        devAddress.setText(result.getDevice().getAddress());
        mDeviceListTable.addView(deviceTR);
      }
    }

    @Override
    public void onScanFailed(int errorCode) {
      super.onScanFailed(errorCode);
    }
  };

  public void deviceWasClicked(int index) {
    BluetoothDevice dev = mDeviceList.get(index);
    client = new TIOADEoadClient(mThis,"ble5_project_zero_cc26x2r1lp_app_FlashROM_Release_oad.bin");

    client.initializeTIOADEoadProgrammingOnDevice(dev, new TIOADEoadClientProgressCallback() {
      @Override
      public void oadProgressUpdate(float percent) {
        Log.d(TAG,"Progress update : " + percent + "%");
        oadProgressBar.setProgress((int)percent);
      }

      @Override
      public void oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration status) {
        Log.d(TAG,"OAD Status update : " + status);
        switch (status) {
          case tiOADClientOADServiceMissingOnPeripheral:
          case tiOADClientOADWrongVersion:
          case tiOADClientOADCharacteristicMissingOnPeripheral:
            //showAlert(status);
            break;
          case tiOADClientReady:
            //showStart(status);
            break;


        }
      }
    });
  }

  public void showStart(final TIOADEoadDefinitions.oadStatusEnumeration status) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        AlertDialog.Builder aD = new AlertDialog.Builder(mThis);

        aD.setTitle("Start programming ?")
                .setIcon(R.mipmap.device_unknown)
                .setMessage("Device is ready to start programming !")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    client.start();
                  }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    mDeviceListTable.setVisibility(View.VISIBLE);
                  }
                }).show();
      }
    });
  }

  public void showAlert(final TIOADEoadDefinitions.oadStatusEnumeration status) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        AlertDialog.Builder aD = new AlertDialog.Builder(mThis);

        aD.setTitle("Error")
                .setIcon(R.mipmap.device_unknown)
                .setMessage("Error : " + status + " caught")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    mDeviceListTable.setVisibility(View.VISIBLE);
                  }
                }).show();
      }
    });
  }
}




