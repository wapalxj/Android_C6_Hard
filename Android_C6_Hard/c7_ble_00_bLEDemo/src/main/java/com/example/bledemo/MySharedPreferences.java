package com.example.bledemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MySharedPreferences {
	private static Context mContext;
	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;

	private final String PREFERENCE_NAME =MySharedPreferences.class.getName() ;

	private static class MySharedPreferencesHold {
		/**
		 * 鍗曚緥瀵硅薄瀹炰緥
		 */
		static final MySharedPreferences INSTANCE = new MySharedPreferences();
	}

	public static MySharedPreferences getInstance(Context context) {
		mContext = context;
		return MySharedPreferencesHold.INSTANCE;
	}

	/**
	 * private鐨勬瀯閫犲嚱鏁扮敤浜庨伩鍏嶅鐣岀洿鎺ヤ娇鐢╪ew鏉ュ疄渚嬪寲瀵硅薄
	 */
	private MySharedPreferences() {
		preferences = mContext.getSharedPreferences(PREFERENCE_NAME, 0);
		editor = preferences.edit();
	}

	/**
	 * readResolve鏂规硶搴斿鍗曚緥瀵硅薄琚簭鍒楀寲鏃跺��
	 */
	private Object readResolve() {
		return getInstance(mContext);
	}

	 
	
	public void saveDeviceMacAddress(String mac){
		
		editor.putString("MACDEVICE", mac);
		editor.commit();
	}
	 
	
	public String getDeviceMacAddress(){
		return preferences.getString("MACDEVICE", "");
	}
	
}



