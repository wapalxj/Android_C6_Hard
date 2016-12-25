package com.c7_ble_01_proj_fangdiuqi;

import android.Manifest;
import android.app.Dialog;
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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView tv_support;
    TextView tv_status;
    MusicPlayer player;
    private Dialog mDialog;
    List<BluetoothDevice> devicelist=new ArrayList<>();
    private BluetoothManager mBm;
    private BluetoothAdapter mAdapter;
    private  BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic controlCharacteristic;//用于write发送控制指令给remote设备
    BluetoothGattCharacteristic notifyCharacteristic;//用于notify接收remote设备的指令
    BluetoothGattCharacteristic batteryCharacteristic;//用于read读取Charactoristic
    String macAddr;

    MySharedPreferences mySharedPreferences;

    Handler mHander=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
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

        if(check()){
            tv_support.setText(tv_support.getText().toString()+"是");
        }else {
            tv_support.setText(tv_support.getText().toString()+"否");
        }

        mBm= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter=mBm.getAdapter();

        //APP打开自动重新连接上次设备
        mySharedPreferences=MySharedPreferences.getInstance(this);
        macAddr=mySharedPreferences.getDeviceMacAddress();
        if(macAddr.equals("")){
            //用户第一次使用本APP，还没有连接过设备

        }else {
            //扫描
            //延迟一秒执行扫描，给予充分的mAdapter初始化准备
            mHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.startLeScan(mScanCallback);
                }
            },1000);

        }
    }

    public void initView() {
        tv_support= (TextView) findViewById(R.id.tv_support);
        player = new MusicPlayer(this);
    }

    /**
     * 扫描蓝牙设备
     */
    public void scanAction(View view){
//        devices.clear();
        mAdapter.startLeScan(mScanCallback);
        showDeviceListDialog();
    }

    /**
     * 扫描回调：LeScanCallback是异步回调
     */
    private BluetoothAdapter.LeScanCallback mScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                /**
                 *
                 * @param bluetoothDevice---扫描到的设备
                 * @param rssi----蓝牙信号强度
                 * @param values---广播信号
                 */
                @Override
                public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] values) {
                    //扫描到设备时，回调此方法
                    if(bluetoothDevice!=null){
                        //数据展示
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!devicelist.contains(bluetoothDevice)){
                                    devicelist.add(bluetoothDevice);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                        //重连：比对MAC地址
                        if(bluetoothDevice.getAddress().equals(macAddr)){
                            bluetoothGatt=bluetoothDevice.connectGatt(MainActivity.this,false,bluetoothGattCallback);
                        }
                    }


                }
            };

    /**
     * 让设备响铃
     * @param view
     */
    public void ringAction(View view){
        if(bluetoothGatt !=null && controlCharacteristic!=null){
            controlCharacteristic.setValue(new byte[]{0x01});
            bluetoothGatt.writeCharacteristic(controlCharacteristic);
        }
    }

    /**
     * 停止响铃
     * @param view
     */
    public void stopRingAction(View view){
        if(bluetoothGatt !=null && controlCharacteristic!=null){
            controlCharacteristic.setValue(new byte[]{0x00});
            bluetoothGatt.writeCharacteristic(controlCharacteristic);
        }
    }

    private void showDeviceListDialog() {
        Button btn_dialgo_cancle = null;
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.dialog_scan_device, null);
        mDialog = new Dialog(this, R.style.MyDialog);
        // ContentView
        mDialog.setContentView(view);
        mDialog.setCancelable(false);
        mDialog.show();
        btn_dialgo_cancle = (Button) view
                .findViewById(R.id.btn_dialog_scan_cancle);
        ListView listView = (ListView) view.findViewById(R.id.listview_device);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //点击进行连接
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                BluetoothDevice device=devicelist.get(position);
                //记录设备的MAC地址,用于重新连接
                macAddr=device.getAddress();
                //保存MAC之地到SP
                mySharedPreferences.saveDeviceMacAddress(macAddr);

                bluetoothGatt=device.connectGatt(MainActivity.this,
                        false,bluetoothGattCallback);
                //停止扫描
                mAdapter.stopLeScan(mScanCallback);
                mDialog.dismiss();
            }
        });
        btn_dialgo_cancle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }


    //防止误报：延迟检测
    Runnable delayCheckRunable=new Runnable() {
        @Override
        public void run() {
            if(isConnected){
                //5秒内自动重新连接上，说明为误报,不进行报警
            }else {
                //真的断线
                player.playDog();
            }
        }
    };
    boolean isConnected;

    /**
     * 连接回调：同样是异步回调
     */
    private BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {

        /**
         * /**
         * 连接状态改变回调
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
                    switch (newState){
                        case BluetoothGatt.STATE_CONNECTED:
                            //已连接
                            isConnected=true;
                            tv_status.setText("已连接");
                            //获取service
                            bluetoothGatt.discoverServices();
                            //停止扫描
                            mAdapter.stopLeScan(mScanCallback);
                            break;
                        case BluetoothGatt.STATE_CONNECTING:
                            //不可靠的API:可能不会调用？
                            //连接中
                            tv_status.setText("连接中");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTED:
                            //已断开连接
                            isConnected=false;
                            tv_status.setText("已断开");
                            //断线之后的操作
                            //狗叫声
                            //防止误报：延迟检测
                            mHander.removeCallbacks(delayCheckRunable);//防止执行多次
                            mHander.postDelayed(delayCheckRunable,5000);

//                            bluetoothGatt.connect();//重连，但是效率非常低
                            //重连:再次进行扫描
                            mAdapter.stopLeScan(mScanCallback);
                            break;
                        case BluetoothGatt.STATE_DISCONNECTING:
                            //正在断开
                            tv_status.setText("正在断开");
                            break;
                        default:
                            break;
                    }
                }
            });

        }

        /**
         * service获取回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                //获取服务成功
                List<BluetoothGattService> services=bluetoothGatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.e("service-UUID","service-UUID : "+service.getUuid().toString());
                    List<BluetoothGattCharacteristic> charatoristics=service.getCharacteristics();
                    for (BluetoothGattCharacteristic charatoristic: charatoristics){
                        Log.e("charatoristic-UUID","charatoristic-UUID : "+service.getUuid().toString());

                        //判断是否等于谋值(手机控制设备---响铃值)
                        if(charatoristic.getUuid().toString().equals("xxxxxxx")){
                            controlCharacteristic=charatoristic;
                        }

                        //notify过程
                        //判断是否等于谋值(例如设备控制手机震动值)
                        if(charatoristic.getUuid().toString().equals("xxxxxxx")){
                            notifyCharacteristic=charatoristic;
                            //打开通知功能
                            enableNotification(true,notifyCharacteristic);
                        }
                        //read---例如读取目标设备电量
                        if(charatoristic.getUuid().toString().equals("xxxxxxx")){
                            batteryCharacteristic=charatoristic;
                            //read
                            bluetoothGatt.readCharacteristic(batteryCharacteristic);
                        }
                    }
                }
            }
        }

        /**
         * //read---例如读取目标设备电量
         * 读取成功回调
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(characteristic.getUuid().toString().equals(batteryCharacteristic.getUuid().toString())){
                Log.e("read---获取到","获取到: "+characteristic.getValue()[0]);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        /**
         * notify回调函数
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getUuid().toString().equals(notifyCharacteristic.getUuid().toString())){
                Log.e("onCharacteristicChanged","notify: "+ bytes2HexString(notifyCharacteristic.getValue()));
                byte[] values=characteristic.getValue();
                if(values[0]==0x11){
                    Log.e("notify 0x11","手机进行震动");
                    player.playVibrate(10);//震动
                }else if(values[0]==0x00){
                    Log.e("notify 0x10","手机进行震动");
                    player.stopVibrate();//停止震动
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    /**
     * 是否支持BLE
     */
    private boolean check() {
        boolean re;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            re=false;
        } else {
            re=true;
        }
        return re;
    }

    /**
     * 打开通知功能
     * @param enable
     * @param characteristic
     * @return
     */
    private boolean enableNotification(boolean enable,
                                       BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null || characteristic == null)
            return false;
        if (!bluetoothGatt
                .setCharacteristicNotification(characteristic, enable))
            return false;
        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(UUID
                        .fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (clientConfig == null)
            return false;

        if (enable) {
            clientConfig
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig
                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return bluetoothGatt.writeDescriptor(clientConfig);
    }



    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {

            return devicelist.size();
        }

        @Override
        public long getItemId(int arg0) {

            return 0;
        }

        @Override
        public Object getItem(int arg0) {

            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_item_scan, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_device = (TextView) convertView
                        .findViewById(R.id.tv_device);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv_device.setText(devicelist.get(position).getName()
                    + "  " + devicelist.get(position).getAddress());
            return convertView;
        }

        class ViewHolder {
            TextView tv_device;
        }
    };

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
