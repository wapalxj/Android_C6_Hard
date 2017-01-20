package byd.com.c7_ble_02_byd_btkey;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView mTv;
    BluetoothManager mManager;
    BluetoothAdapter mBluetoothAdapter;
    ListView mListView;
    List<BluetoothDevice> devices;
    DeviceAdapter mDeviceAdapter;
    BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic controlCharacteristic;
    BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void initView() {
        mTv= (TextView) findViewById(R.id.tv);
        mListView= (ListView) findViewById(R.id.deviceList);
        devices=new ArrayList<>();
        mDeviceAdapter=new DeviceAdapter(devices,this);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothDevice=devices.get(position);
                mBluetoothGatt=mBluetoothDevice.connectGatt(MainActivity.this,false,bluetoothGattCallback);
            }
        });
       }


    /**
     * 连接回调：同样是异步回调
     */
    private BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                //获取服务成功
                List<BluetoothGattService> services=mBluetoothGatt.getServices();
                for (BluetoothGattService service : services) {
//                    Log.e("service-UUID","service-UUID : "+service.getUuid().toString());
                    List<BluetoothGattCharacteristic> characteristics=service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic: characteristics){
                        //notify过程
                        //判断是否等于谋值(例如设备控制手机震动值)
                        if(characteristic.getUuid().toString().equals("0000ffe5-0000-1000-8000-00805f9b34fb")){
                            Log.e("Discovered-UUID","charatoristic-UUID : "+service.getUuid().toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "特征值已找到", Toast.LENGTH_SHORT).show();
                                }
                            });
                            controlCharacteristic=characteristic;
                            //打开通知功能
                            enableNotification(true,controlCharacteristic);
                        }else {
                            enableNotification(true,characteristic);
                        }
                    }
                }
            }
        }

        /**
         * /**
         * 连接状态改变回调
         *
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (newState) {
                        case BluetoothGatt.STATE_CONNECTED:
                            //已连接
                            mTv.setText("已连接");
                            //获取service
                            mBluetoothGatt.discoverServices();
                            //停止扫描
                            mBluetoothAdapter.stopLeScan(mScanCallback);
                            break;
                        case BluetoothGatt.STATE_CONNECTING:
                            //不可靠的API:可能不会调用？
                            //连接中
                            mTv.setText("连接中");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTED:
                            //已断开连接
                            mTv.setText("已断开");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTING:
                            //正在断开
                            mTv.setText("正在断开");
                            break;
                        default:
                            break;
                    }
                }
            });

        }

        /**
         * notify回调函数
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if(characteristic.getUuid().toString().equals(controlCharacteristic.getUuid().toString())){
                final byte[] values=characteristic.getValue();
                Log.e("onCharacteristicChanged","notify: "+ bytes2HexString(values));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTv.setText("获取到："+bytes2HexString(values));
                    }
                });
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!devices.contains(device)){
                        devices.add(device);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
            });

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan:
                mBluetoothAdapter.startLeScan(mScanCallback);
                break;
            case R.id.write:
                byte [][]datas={
                        new byte[]{0x01},
                        new byte[]{0x02},
                        new byte[]{0x03},
                        new byte[]{0x04},
                        new byte[]{0x05},
                        new byte[]{0x06},
                        new byte[]{0x07},
                        new byte[]{0x08},
                        new byte[]{0x09},
                        new byte[]{0x0A},
                        new byte[]{0x0B},
                        new byte[]{0x0C},
                        new byte[]{0x0D},
                        new byte[]{0x0E},
                        new byte[]{0x0F},
                };
                Random random=new Random();
                if(mBluetoothGatt !=null && controlCharacteristic!=null){
                    controlCharacteristic.setValue(datas[random.nextInt(datas.length-1)]);
                    Boolean write=mBluetoothGatt.writeCharacteristic(controlCharacteristic);
                    Log.e("write","write成功");
                }else {
                    Toast.makeText(this, "write失败", Toast.LENGTH_LONG).show();
                    Log.e("write","write失败");
                }
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
            mTv.setText("当前设备不支持蓝牙");
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
        }else {
            if(!mBluetoothAdapter.isEnabled()){
                mBluetoothAdapter.enable();
            }
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            mTv.setText("当前设备不支持BLE");
            Toast.makeText(this, "当前设备不支持BLE", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 打开通知功能
     * @param enable
     * @param characteristic
     * @return
     */
    private boolean enableNotification(boolean enable,
                                       BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null){
            Log.e("enableNotification","notify: Gatt");
            return false;
        }

        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)){
            Log.e("enableNotification","notify: setNotification");
            return false;
        }

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(UUID
                        .fromString("00002901-0000-1000-8000-00805f9b34fb"));
        if (clientConfig == null){
            Log.e("enableNotification","notify: clientConfig==null");
            return false;
        }


        if (enable) {
            clientConfig
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig
                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            Log.e("enableNotification","notify: DISABLE_NOTIFICATION_VALUE");
        }
        return mBluetoothGatt.writeDescriptor(clientConfig);
    }


    /**
     * byte数组转换成String
     */
    private final byte[] hex = "0123456789ABCDEF".getBytes();
    private String bytes2HexString(byte[] b) {
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[i] & 0x0f];
        }
        return new String(buff);
    }
}

