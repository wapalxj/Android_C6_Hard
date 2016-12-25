package byd.com.c8_ble_01_l_central;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    BluetoothManager mManager;
    BluetoothAdapter mBluetoothAdapter;
    ListView mListView;
    List<BluetoothDevice> mDevices;
    DeviceAdapter mDeviceAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        initView();
        init();
    }

    private void initView() {
        tv= (TextView) findViewById(R.id.tv);
        mListView= (ListView) findViewById(R.id.deviceList);
        mDevices =new ArrayList<>();
        mDeviceAdapter=new DeviceAdapter(mDevices,this);
        mListView.setAdapter(mDeviceAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    ScanCallback mScanCallback=new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device=result.getDevice();
            if(!mDevices.contains(device)){
                mDevices.add(device);
            }
            mDeviceAdapter.notifyDataSetChanged();
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            tv.setText("扫描失败"+errorCode);
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan:
                if(mBluetoothLeScanner==null){
                    mBluetoothLeScanner=mBluetoothAdapter.getBluetoothLeScanner();
                }
                Toast.makeText(this, "扫描", Toast.LENGTH_LONG).show();
                mBluetoothLeScanner.startScan(mScanCallback);
                break;
            case 0:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void init(){
        if (Build.VERSION.SDK_INT>=23)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        10);
            }
        }
        mManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=mManager.getAdapter();
        if(mBluetoothAdapter==null){
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            tv.setText("当前设备不支持BLE");
            Toast.makeText(this, "当前设备不支持BLE", Toast.LENGTH_LONG).show();
        }

//        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(intent, 11);
        mBluetoothAdapter.enable();
    }
}
