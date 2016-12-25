package com.neurosky.ble;

import java.util.List;

import com.neurosky.chrisblecentral.R;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class BLECentralActivity extends Activity implements
		BluetoothAdapter.LeScanCallback {

	private BluetoothAdapter btAdapter;
	private BluetoothGatt gatt;
	private List<BluetoothGattService> serviceList;
	private List<BluetoothGattCharacteristic> characterList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();

		this.setContentView(R.layout.ble_central);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		btAdapter.stopLeScan(this);
		super.onStop();
	}

	public void onButtonClicked(View v) {
		Log.d("Chris", "onButtonClicked");
		btAdapter.startLeScan(this);
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		btAdapter.stopLeScan(this);
		gatt = device.connectGatt(this, true, gattCallback);

		Log.d("Chris", "Device Name:" + device.getName());
		Toast.makeText(this,""+ device.getName()+", "+device.getAddress(),Toast.LENGTH_SHORT).show();
	}

	private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			Log.d("Chris", "onConnectionStateChange");
			switch (newState) {
			case BluetoothProfile.STATE_CONNECTED:
				Log.d("Chris", "STATE_CONNECTED");
				gatt.discoverServices();

				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				Log.d("Chris", "STATE_DISCONNECTED");
				break;
			case BluetoothProfile.STATE_CONNECTING:
				Log.d("Chris", "STATE_CONNECTING");
				break;
			case BluetoothProfile.STATE_DISCONNECTING:
				Log.d("Chris", "STATE_DISCONNECTING");
				break;
			}
			super.onConnectionStateChange(gatt, status, newState);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d("Chris", "onServicesDiscovered");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				serviceList = gatt.getServices();
				for (int i = 0; i < serviceList.size(); i++) {
					BluetoothGattService theService = serviceList.get(i);
					Log.d("Chris", "ServiceName:" + theService.getUuid());

					characterList = theService.getCharacteristics();
					for (int j = 0; j < characterList.size(); j++) {
						Log.d("Chris",
								"---CharacterName:"
										+ characterList.get(j).getUuid());
					}
				}
			}
			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d("Chris", "onCharacteristicRead");
			super.onCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d("Chris", "onCharacteristicWrite");
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.d("Chris", "onCharacteristicChanged");
			super.onCharacteristicChanged(gatt, characteristic);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.d("Chris", "onDescriptorRead");
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.d("Chris", "onDescriptorWrite");
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			Log.d("Chris", "onReliableWriteCompleted");
			super.onReliableWriteCompleted(gatt, status);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			Log.d("Chris", "onReadRemoteRssi");
			super.onReadRemoteRssi(gatt, rssi, status);
		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
