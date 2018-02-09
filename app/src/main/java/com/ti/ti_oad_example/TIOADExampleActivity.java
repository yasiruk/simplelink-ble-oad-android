package com.ti.ti_oad_example;

import android.Manifest;
import android.app.Activity;
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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
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

import static com.ti.ti_oad.TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteFeedbackOK;

public class TIOADExampleActivity extends AppCompatActivity {
  static private final String TAG = TIOADExampleActivity.class.getSimpleName();
  static private final String oadFileName = "ble5_project_zero_cc26x2r1lp_app_FlashROM_Release_oad.bin";

  private static final int READ_REQUEST_CODE = 42;

  static public final int SCAN_PERMISSIONS_CODE = 1;
  TIOADExampleActivity mThis;
  ArrayList<BluetoothDevice> mDeviceList;
  TableLayout mDeviceListTable;
  BluetoothLeScanner scanner;
  TIOADEoadClient client;
  ProgressBar oadProgressBar;
  TextView oadState;
  Uri fileURL;


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      if (data != null) {
        fileURL = data.getData();
        Log.d(TAG,"File selected: " + fileURL.toString());
        TextView oadImageFile = (TextView)findViewById(R.id.oad_image_filename);
        String filename = fileURL.getPath();
          int cut = filename.lastIndexOf('/');
          if (cut != -1) {
              filename = filename.substring(cut + 1);
          }
        oadImageFile.setText(filename);
        client.start(fileURL);
        TIOADEoadImageReader reader = new TIOADEoadImageReader(fileURL,this);

        TextView oadInfo = (TextView)findViewById(R.id.oad_image_info);
        oadInfo.setText(reader.imageHeader.getHeaderInfo(reader.imageHeader));

        TextView oadMTUSize = (TextView)findViewById(R.id.oad_mtu_size);
        TextView oadBlockSize = (TextView)findViewById(R.id.oad_block_size);



        oadMTUSize.setText(String.format("%d",client.getMTU()));
        oadBlockSize.setText(String.format("%d",client.getTIOADEoadBlockSize()));


      }
    }
  }






  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mThis = this;
    mDeviceList = new ArrayList<BluetoothDevice>();
    setContentView(R.layout.activity_tioad_example);
    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
    //oadProgressBar.getProgressDrawable().setColorFilter(
    //        Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

    oadState = (TextView)findViewById(R.id.oad_status);

    TextView oadImageFile = (TextView)findViewById(R.id.oad_image_filename);
    oadImageFile.setText("");

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

    mDeviceList = new ArrayList<BluetoothDevice>();
    mDeviceListTable.removeAllViews();

    mDeviceListTable.setVisibility(View.VISIBLE);

    scanner = adapt.getBluetoothLeScanner();
    if (scanner != null) {
        scanner.startScan(bleScanCallback);
    }
    else {
        AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
        builder.setTitle("Error");
        builder.setMessage("Cannot start scan, might be that Bluetooth is turned off");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        }).create().show();
    }
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
        TextView devRSSI = (TextView)deviceTR.findViewById(R.id.device_rssi);
        devRSSI.setText(String.format("%d dBm",result.getRssi()));
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
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            oadProgressBar.setIndeterminate(true);
        }
    });

    client = new TIOADEoadClient(mThis);

    client.initializeTIOADEoadProgrammingOnDevice(dev, new TIOADEoadClientProgressCallback() {
      @Override
      public void oadProgressUpdate(final float percent, final int currentBlock) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Progress update : " + percent + "%");
                oadProgressBar.setIndeterminate(false);
                oadProgressBar.setProgress((int)percent);
                TextView oadCurrentBlock = (TextView) findViewById(R.id.oad_current_block);
                oadCurrentBlock.setText(String.format("%d",currentBlock));
                TextView oadTotalBlocks = (TextView) findViewById(R.id.oad_total_blocks);
                oadTotalBlocks.setText(String.format("%d",client.getTIOADEoadTotalBlocks()));
            }
        });
      }

      @Override
      public void oadStatusUpdate(TIOADEoadDefinitions.oadStatusEnumeration status) {
        Log.d(TAG,"OAD Status update : " + status);
        final TIOADEoadDefinitions.oadStatusEnumeration finalStatus = status;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            oadState.setText(TIOADEoadDefinitions.oadStatusEnumerationGetDescriptiveString(finalStatus));
          }
        });
        if (finalStatus == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientReady) {
          showFileSelector();
          //client.start("ble5_project_zero_cc26x2r1lp_app_FlashROM_Release_oad.bin");
        }
        if (finalStatus == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientFileIsNotForDevice) {
          showAlert(finalStatus);
        }
        if (finalStatus == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteFeedbackOK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder aD = new AlertDialog.Builder(mThis);
                    aD.setTitle("Success !");
                    aD.setMessage("OAD Upgrade complete, device is now resetting");
                    aD.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    }).create().show();
                }
            });
        }
        if (finalStatus == TIOADEoadDefinitions.oadStatusEnumeration.tiOADClientCompleteDeviceDisconnectedDuringProgramming) {
            showAlert(finalStatus);
        }
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
                .setMessage("Error : " + TIOADEoadDefinitions.oadStatusEnumerationGetDescriptiveString(status))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    mDeviceListTable.setVisibility(View.VISIBLE);
                  }
                }).show();
      }
    });
  }

  public void showFileSelector() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
          AlertDialog.Builder aD = new AlertDialog.Builder(mThis);
          aD.setTitle("Select Image");
          aD.setMessage("Device is now ready to program, please select file in the next dialog shown after this dialog.");
          aD.setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                  // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                  // browser.
                  Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                  // Filter to only show results that can be "opened", such as a
                  // file (as opposed to a list of contacts or timezones)
                  //intent.addCategory(Intent.CAT);

                  // Filter to show only images, using the image MIME data type.
                  // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                  // To search for all documents available via installed storage providers,
                  // it would be "*/*".
                  intent.setType("application/octet-stream");

                  startActivityForResult(intent, READ_REQUEST_CODE);
              }
          }).create().show();
      }
    });
  }

}




