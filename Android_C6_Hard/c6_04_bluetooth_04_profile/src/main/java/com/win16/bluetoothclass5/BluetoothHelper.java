package com.win16.bluetoothclass5;

import android.content.Context;
import android.media.AudioManager;


/** 
 * @Description: 
 * @author yingjie.lin 
 * @date 2014年10月17日 上午11:00:02
 */

public class BluetoothHelper extends BluetoothHeadsetUtils {
	private final static String TAG = BluetoothHelper.class.getSimpleName();
	Context mContext;
	int mCallvol;
//	int mMediaVol;
	AudioManager mAudioManager;
	public BluetoothHelper(Context context) {
		super(context);
		mContext = context;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mCallvol = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
//		mMediaVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	public void onHeadsetDisconnected() {
		mAudioManager.setBluetoothScoOn(false);
	}

	@Override
	public void onHeadsetConnected() {
		mAudioManager.setBluetoothScoOn(true); // 打开SCO
//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	}

	@Override
	public void onScoAudioDisconnected() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mCallvol, 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMediaVol, 0);
	}

	@Override
	public void onScoAudioConnected() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
		//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	}

}
