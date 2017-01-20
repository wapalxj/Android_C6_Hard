package byd.com.c7_ble_02_byd_btkey;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码模板
 */
public class ActivityModel extends AppCompatActivity {
    BluetoothManager mManager;
    BluetoothAdapter mBluetoothAdapter;
    ListView mListView;
    List<BluetoothDevice> devices;
    DeviceAdapter mDeviceAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        init();
        initView();
    }

    private void initView() {
        mListView= (ListView) findViewById(R.id.deviceList);
        devices=new ArrayList<>();
        mDeviceAdapter=new DeviceAdapter(devices,this);
        mListView.setAdapter(mDeviceAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan:

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
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "当前设备不支持BLE", Toast.LENGTH_LONG).show();
        }
    }
}
