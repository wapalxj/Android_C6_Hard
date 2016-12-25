package com.c7_ble_01_proj_fangdiuqi;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class MusicPlayer implements OnCompletionListener {
	private Vibrator mVibrator;

	int repeatTotalCount = 1;// 总次数

	int playedCount;// 已播放次数

	// MediaPlayer player= MediaPlayer.create(this,
	// Uri.parse("content://media/internal/audio/media/92"));
	// player.start();

	private MediaPlayer mediaPlayer;

	Context mContext;

	private OnPlayStateListener onPlayStateListener;

	public MusicPlayer(Context mContext) {
		super();
		this.mContext = mContext;
		this.mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
	}

	public void playWithTimesAndPath(int times, String path, boolean needVibrate) {
		this.repeatTotalCount = times;
		int duration = playWithPath(path);
		if (needVibrate) {
			duration = repeatTotalCount * duration;
			duration = duration / 1000;
			playVibrate(duration);
		}

		playedCount = 0;
	}

	public void playBird() {
		String path = "android.resource://" + mContext.getPackageName() + "/"
				+ R.raw.bird;

		playWithPath(path);

	}
	
	
	public void playDog(){
		String path = "android.resource://" + mContext.getPackageName() + "/"
				+ R.raw.dog;

		playWithTimesAndPath(4, path, false);
	}

	public void stopPlay() {
		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();
		mediaPlayer.reset();
		stopVibrate();
	}

	public void setOnPlayStateListener(OnPlayStateListener onPlayStateListener) {
		this.onPlayStateListener = onPlayStateListener;
	}

	public int playWithPath(String path) {
		stopPlay();
		try {
			mediaPlayer.setDataSource(mContext, Uri.parse(path));
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int duration = mediaPlayer.getDuration();

		if (duration < 1000) {
			duration = 1000;
		}

		mediaPlayer.start();

		return duration;
	}

	// 时间长度，秒
	public void playVibrate(int duration) {
		duration=100;
		stopVibrate();
		mVibrator = (Vibrator) mContext
				.getSystemService(Service.VIBRATOR_SERVICE);
		int length = duration * 2;
		long[] pattern = new long[length];
		for (int i = 0; i < length; i++) {
			if (i % 2 == 0) {
				pattern[i] = 200;
			} else {
				pattern[i] = 800;
			}
		}
		Log.e("", " playVibrate");
		mVibrator.vibrate(pattern, -1);
	}

	public void stopVibrate() {
		if (mVibrator != null)
			mVibrator.cancel();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		playedCount++;
		if (playedCount < repeatTotalCount) {
			mediaPlayer.start();
		} else {
			mediaPlayer.reset();
			if (onPlayStateListener != null) {
				onPlayStateListener.onCompletion(this);
			}
		}
	}

	public interface OnPlayStateListener {
		public void onCompletion(MusicPlayer musicPlayer);

	}

}
