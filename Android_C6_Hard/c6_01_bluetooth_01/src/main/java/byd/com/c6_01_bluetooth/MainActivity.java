package byd.com.c6_01_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private BluetoothController mController=new BluetoothController();
    private Toast mToast;
    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //收听关闭监听蓝牙状态
            int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    showToast("STATE_OFF：蓝牙已经关闭");
                    break;
                case BluetoothAdapter.STATE_ON:
                    showToast("STATE_ON：蓝牙已经开启");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    showToast("STATE_TURNING_ON:蓝牙正在开启");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    showToast("STATE_TURNING_OFF：蓝牙正在关闭");
                    break;
                 default:
                     showToast("位置STATE");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter =new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver,filter);
    }

    public void isSupportBlueTooth(View view){
        boolean ret=mController.isSupportBlueTooth();
        showToast("support Bluetooth ? "+ret);
    }

    public void isBlueToothEnable(View view){
        boolean ret=mController.getBlueToothStatus();
        showToast("bluetooth enable ? "+ret);
    }

    public void turnOnBlueTooth(View view){
        mController.turnOnBlueTooth(this,0);
    }

    public void turnOffBlueTooth(View view){
        mController.turnOffBlueTooth();
    }

    private void showToast(String text){
        if(mToast == null){
            mToast=Toast.makeText(this,text,Toast.LENGTH_SHORT);
        }else {
            mToast.setText(text);
        }
        mToast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(resultCode==Activity.RESULT_OK){
                //打开成功,返回值-1
                showToast("打开蓝牙成功,"+resultCode);
            }else if(resultCode==Activity.RESULT_CANCELED){
                //打开失败,返回值0
                showToast("打开蓝牙失败,"+resultCode);
            }

        }else {
            showToast("打开蓝牙失败,"+requestCode);
        }
    }
}
