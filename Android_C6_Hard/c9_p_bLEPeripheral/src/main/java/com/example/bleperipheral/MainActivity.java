package com.example.bleperipheral;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	
	public static final String TAG="bleperipheral";
	private  static boolean D = true;
	
	public static final String HEART_RATE_SERVICE="0000180d-0000-1000-8000-00805f9b34fb";
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
	private  static Context 	mContext;
	private Button mStartButton;
	private Button mStopButton;
	TextView isSupportP;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		init();
	}
	private void initView(){
		mContext = this;
		isSupportP= (TextView) findViewById(R.id.isSupport);
		mStartButton = (Button) findViewById(R.id.start);
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 0), createAdvertiseData(), mAdvertiseCallback);
				if(D){
					Toast.makeText(mContext, "Start Advertising", Toast.LENGTH_LONG).show();
					Log.e(TAG,"Start Advertising");
				}
			}
		});

		mStopButton = (Button) findViewById(R.id.stop);
		mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopAdvertise() ;
				if(D){
					Toast.makeText(mContext, "Stop Advertising", Toast.LENGTH_LONG).show();
					Log.e(TAG,"Stop Advertising");
				}
			}
		});
	}
	private void init(){
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
			finish();
		}
		
		final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		
		if(mBluetoothAdapter !=null){
//			finish();
			isSupportP.setText("支持BLE，\n");
		}

		if(!mBluetoothAdapter.isMultipleAdvertisementSupported()){
			Toast.makeText(this, "the device not support peripheral", Toast.LENGTH_SHORT	).show();
			Log.e(TAG, "the device not support peripheral");
			isSupportP.setText(isSupportP.getText().toString()+"不支持peripheral\n");
		}else{
			mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
		}

//		if(mBluetoothLeAdvertiser == null){
//		检测支持peripheral，和上一个if作用相同
//			isSupportP.setText(isSupportP.getText().toString()+",不支持peripheral");
//		}
	}
	
	 /** create AdvertiseSettings */
		 public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
			 AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
			 mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
			 mSettingsbuilder.setConnectable(connectable);
			 mSettingsbuilder.setTimeout(timeoutMillis);
			 mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
			 AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
				if(mAdvertiseSettings == null){
					if(D){
						Toast.makeText(mContext, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
						Log.e(TAG,"mAdvertiseSettings == null");
					}
				}
			return mAdvertiseSettings;
		 }
	 public static AdvertiseData createAdvertiseData(){		 
		 	AdvertiseData.Builder    mDataBuilder = new AdvertiseData.Builder();
			mDataBuilder.addServiceUuid(ParcelUuid.fromString(HEART_RATE_SERVICE));
			AdvertiseData mAdvertiseData = mDataBuilder.build();
			if(mAdvertiseData==null){
				if(D){
					Toast.makeText(mContext, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
					Log.e(TAG,"mAdvertiseSettings == null");
				}
			}
			
			return mAdvertiseData;
	 }
	 
	 private void stopAdvertise() {
			 if (mBluetoothLeAdvertiser != null) {
				 mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
				 mBluetoothLeAdvertiser = null;
			 }
		 }
	private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			super.onStartSuccess(settingsInEffect);
			if (settingsInEffect != null) {
				Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel()	 + " mode=" + settingsInEffect.getMode()
						+ " timeout=" + settingsInEffect.getTimeout());
			} else {
				Log.e(TAG, "onStartSuccess, settingInEffect is null");
			}
			Log.e(TAG,"onStartSuccess settingsInEffect" + settingsInEffect);

		}

		@Override
		public void onStartFailure(int errorCode) {
			super.onStartFailure(errorCode);
			if(D) 	Log.e(TAG,"onStartFailure errorCode" + errorCode);

			if(errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE){
				if(D){
					Toast.makeText(mContext, R.string.advertise_failed_data_too_large, Toast.LENGTH_LONG).show();
					Log.e(TAG,"Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
				}
			}else if(errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
				if(D){
					Toast.makeText(mContext, R.string.advertise_failed_too_many_advertises, Toast.LENGTH_LONG).show();
					Log.e(TAG,"Failed to start advertising because no advertising instance is available.");
				}
			}else if(errorCode == ADVERTISE_FAILED_ALREADY_STARTED){
				if(D){
					Toast.makeText(mContext, R.string.advertise_failed_already_started, Toast.LENGTH_LONG).show();
					Log.e(TAG,"Failed to start advertising as the advertising is already started");
				}
			}else if(errorCode == ADVERTISE_FAILED_INTERNAL_ERROR){
				if(D){
					Toast.makeText(mContext, R.string.advertise_failed_internal_error, Toast.LENGTH_LONG).show();
					Log.e(TAG,"Operation failed due to an internal error");
				}
			}else if(errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED){
				if(D){
					Toast.makeText(mContext, R.string.advertise_failed_feature_unsupported, Toast.LENGTH_LONG).show();
					Log.e(TAG,"This feature is not supported on this platform");
				}
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
