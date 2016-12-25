package byd.com.c6_01_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * Created by Administrator on 2016/12/10.
 */

public class BluetoothController {
    private BluetoothAdapter mAdapter;
    public BluetoothController(){
        mAdapter=BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 是否支持蓝牙
     * @return
     */
    public boolean isSupportBlueTooth(){
        if(mAdapter!=null){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 判断当前蓝牙关闭状态
     */
    public boolean getBlueToothStatus(){
        assert (mAdapter!=null);
        return mAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     * @param activity
     * @param requestCode
     */
    public void turnOnBlueTooth(Activity activity,int requestCode){
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,requestCode);

        //由系统去调用打开蓝牙，为强制打开蓝牙，不推荐这样
        //不需要弹对话框？
//        mAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public void turnOffBlueTooth(){
        mAdapter.disable();
    }
}
