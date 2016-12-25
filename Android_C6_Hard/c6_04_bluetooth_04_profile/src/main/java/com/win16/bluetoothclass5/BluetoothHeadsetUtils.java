package com.win16.bluetoothclass5;

import java.util.List;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;


/**
 * @Description:
 * @author yingjie.lin
 * @date 2014年10月17日 上午10:56:47
 */

public abstract class BluetoothHeadsetUtils {
	private Context mContext;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothHeadset mBluetoothHeadset;
	private BluetoothDevice mConnectedHeadset;

	private AudioManager mAudioManager;

	private boolean mIsOnHeadsetSco;
	private boolean mIsStarted;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public BluetoothHeadsetUtils(Context context) {
		mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Call this to start BluetoothHeadsetUtils functionalities.
	 * 
	 * @return The return value of startBluetooth() or startBluetooth11()
	 */
	public boolean start() {
		if (mBluetoothAdapter.isEnabled() == false){
			mIsStarted = false;
			return mIsStarted;
		}
		if (!mIsStarted) {
			mIsStarted = true;

			mIsStarted = startBluetooth();
		}
		return mIsStarted;
	}

	/**
	 * Should call this on onResume or onDestroy. Unregister broadcast receivers
	 * and stop Sco audio connection and cancel count down.
	 */
	public void stop() {
		if (mIsStarted) {
			mIsStarted = false;

			stopBluetooth();

		}
	}

	/**
	 * 
	 * @return true if audio is connected through headset.
	 */
	public boolean isOnHeadsetSco() {
		return mIsOnHeadsetSco;
	}

	public abstract void onHeadsetDisconnected();

	public abstract void onHeadsetConnected();

	public abstract void onScoAudioDisconnected();

	public abstract void onScoAudioConnected();


	/**
	 * Register a headset profile listener
	 * 
	 * @return false if device does not support bluetooth or current platform
	 *         does not supports use of SCO for off call or error in getting
	 *         profile proxy.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private boolean startBluetooth() {

		// Device support bluetooth
		if (mBluetoothAdapter != null) {
			if (mAudioManager.isBluetoothScoAvailableOffCall()) {
				// All the detection and audio connection are done in
				// mHeadsetProfileListener---结果实现
				if (mBluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener, BluetoothProfile.HEADSET)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * API >= 11 Unregister broadcast receivers and stop Sco audio connection
	 * and cancel count down.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void stopBluetooth() {

		if (mBluetoothHeadset != null) {
			// Need to call stopVoiceRecognition here when the app
			// change orientation or close with headset still turns on.
			mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);
			mContext.unregisterReceiver(mHeadsetBroadcastReceiver);
			mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
			mBluetoothHeadset = null;
		}
	}





	/**
	 * API >= 11 Check for already connected headset and if so start audio
	 * connection. Register for broadcast of headset and Sco audio connection
	 * states.
	 */
	private BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener() {

		/**
		 * This method is never called, even when we closeProfileProxy on
		 * onPause. When or will it ever be called???
		 */
		@Override
		public void onServiceDisconnected(int profile) {
			stopBluetooth();
		}

		@SuppressWarnings("synthetic-access")
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			//设备连接上的时候
			// mBluetoothHeadset is just a headset profile,
			// it does not represent a headset device.
			mBluetoothHeadset = (BluetoothHeadset) proxy;

			// If a headset is connected before this application starts,
			// ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
			// So we need to check for already connected headset.
			List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
			if (devices.size() > 0) {
				// Only one headset can be connected at a time,
				//同一时间只能有一个设备(耳机)连接上，所以get(0)
				// so the connected headset is at index 0.
				mConnectedHeadset = devices.get(0);

				onHeadsetConnected();

			}

			// During the active life time of the app, a user may turn on and
			// off the headset.
			// 运行期间可能被打断，所以收听广播
			// So register for broadcast of connection states.
			mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
			// Calling startVoiceRecognition does not result in immediate audio
			// connection.
			// So register for broadcast of audio connection states. This
			// broadcast will
			// only be sent if startVoiceRecognition returns true.
			mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
		}
	};

	/**
	 * API >= 11 Handle headset and Sco audio connection states.
	 */
	private BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver() {

		@SuppressWarnings("synthetic-access")
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int state;
			if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
				state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
				if (state == BluetoothHeadset.STATE_CONNECTED) {
					//状态:还是连接状态
					mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					// Calling startVoiceRecognition always returns false here,
					// that why a count down timer is implemented to call
					// startVoiceRecognition in the onTick.


					// override this if you want to do other thing when the
					// device is connected.
					onHeadsetConnected();

				} else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
					//状态:断开状态
					mConnectedHeadset = null;

					// override this if you want to do other thing when the
					// device is disconnected.
					onHeadsetDisconnected();

				}
			} else // audio
			{
				state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
				if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
					// override this if you want to do other thing when headset
					// audio is connected.
					onScoAudioConnected();
				} else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
					mIsOnHeadsetSco = false;

					// override this if you want to do other thing when headset
					// audio is disconnected.
					onScoAudioDisconnected();

				}
			}
		}
	};
}
