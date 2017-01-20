package com.example.bledemo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

	TextView tv_status;

	List<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();

	MusicPlayer player;

	BluetoothAdapter bluetoothAdapter;

	BluetoothDevice bluetoothDevice;

	BluetoothGatt bluetoothGatt;

	BluetoothGattCharacteristic controlCharacteristicl,notifyCharacteristic,batteryCharacteristic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		player = new MusicPlayer(this);
		tv_status = (TextView) findViewById(R.id.tv_status);

		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();

	}

	public void scanAction(View v) {
		devicelist.clear();

		bluetoothAdapter.startLeScan(mScanCallback);

		showDeviceListDialog();
	}

	public void ringAction(View v) {

		// 让设备发出声音

		if (controlCharacteristicl != null && bluetoothGatt != null) {

			controlCharacteristicl.setValue(new byte[] { 0x01 });

			bluetoothGatt.writeCharacteristic(controlCharacteristicl);
		}

	}

	public void stopRingAction(View v) {
		if (controlCharacteristicl != null && bluetoothGatt != null) {

			controlCharacteristicl.setValue(new byte[] { 0x00 });

			bluetoothGatt.writeCharacteristic(controlCharacteristicl);
		}
	}

	private LeScanCallback mScanCallback = new LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi,
				byte[] values) {

			Log.e("", " name:" + bluetoothDevice.getName() + "  mac:"
					+ bluetoothDevice.getAddress());

			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					if (!devicelist.contains(bluetoothDevice)) {
						devicelist.add(bluetoothDevice);
						adapter.notifyDataSetChanged();
					}
				}
			});

		}
	};

	Dialog dialog;

	private void showDeviceListDialog() {
		Button btn_dialgo_cancle = null;
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.dialog_scan_device, null);
		dialog = new Dialog(this, R.style.MyDialog);
		// ContentView
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.show();
		btn_dialgo_cancle = (Button) view
				.findViewById(R.id.btn_dialog_scan_cancle);
		ListView listView = (ListView) view.findViewById(R.id.listview_device);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				dialog.dismiss();
				bluetoothAdapter.stopLeScan(mScanCallback);
				bluetoothDevice = devicelist.get(position);
				bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this,
						false, gattCallback);

			}
		});
		btn_dialgo_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				devicelist.clear();
				dialog.dismiss();

			}
		});
	}

	private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
		 
			if(characteristic.getUuid().toString().equals(notifyCharacteristic.getUuid().toString())){
				Log.e("", " 收到震动命令："+ Bytes2HexString(characteristic.getValue()) );
				
				byte[] values= characteristic.getValue();
				
				if( values[0]==0x11){
					player.playVibrate(10);
				}else if(values[0]==0x10){
					player.stopVibrate();
				}
				
				
			}
			
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
			
			if(status==BluetoothGatt.GATT_SUCCESS){
				
				if(characteristic.getUuid().toString().equals(batteryCharacteristic.getUuid().toString())){
					
					Log.e("", "获取到电量："+characteristic.getValue()[0]);
					
				}
				
			}
			
			
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				final int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(gatt, status, newState);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (newState) {
					case BluetoothGatt.STATE_CONNECTED:
						tv_status.setText("已连接");

						bluetoothGatt.discoverServices();// 寻找服务

						break;
					case BluetoothGatt.STATE_CONNECTING:
						tv_status.setText("连接中");
						break;
					case BluetoothGatt.STATE_DISCONNECTED:
						tv_status.setText("已断开");

						break;
					case BluetoothGatt.STATE_DISCONNECTING:
						tv_status.setText("正在断开");
						break;
					default:
						break;
					}
				}
			});

		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			super.onReadRemoteRssi(gatt, rssi, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onReliableWriteCompleted(gatt, status);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);

			if (status == BluetoothGatt.GATT_SUCCESS) {

				List<BluetoothGattService> services = bluetoothGatt
						.getServices();
				for (BluetoothGattService bluetoothGattService : services) {
					Log.e("", " server:"
							+ bluetoothGattService.getUuid().toString());

					List<BluetoothGattCharacteristic> characteristics = bluetoothGattService
							.getCharacteristics();
					for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
						Log.e("", " charac:"
								+ bluetoothGattCharacteristic.getUuid()
										.toString());

						if (bluetoothGattCharacteristic.getUuid().toString()
								.equals("00002a08-0000-1000-8000-00805f9b34fb")) {
							controlCharacteristicl = bluetoothGattCharacteristic;
						}else if(bluetoothGattCharacteristic.getUuid().toString()
								.equals("0000ffe1-0000-1000-8000-00805f9b34fb")){
							notifyCharacteristic = bluetoothGattCharacteristic;
							
							enableNotification(true, notifyCharacteristic);
							
						}else if(bluetoothGattCharacteristic.getUuid().toString()
								.equals("00002a19-0000-1000-8000-00805f9b34fb")){
							
							batteryCharacteristic=bluetoothGattCharacteristic;
							
							
							bluetoothGatt.readCharacteristic(batteryCharacteristic);
							
							
							
						}
					}

				}

			}

		}

	};

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

	
	
	private final byte[] hex = "0123456789ABCDEF".getBytes();

	// 从字节数组到十六进制字符串转�?
	private String Bytes2HexString(byte[] b) {
		byte[] buff = new byte[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
			buff[2 * i + 1] = hex[b[i] & 0x0f];
		}
		return new String(buff);
	}
}
