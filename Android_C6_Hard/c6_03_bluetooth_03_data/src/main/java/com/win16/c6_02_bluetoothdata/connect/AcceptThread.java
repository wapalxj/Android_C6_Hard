package com.win16.c6_02_bluetoothdata.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Rex on 2015/5/30.
 * //服务器端使用的
 */
public class AcceptThread extends Thread {
    private static final String NAME = "BlueToothClass";
    //基础常量，必须是这个值
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);

    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;//主要用于UI更新
    private ConnectedThread mConnectedThread;//新建线程用于数据传输

    public AcceptThread(BluetoothAdapter adapter, Handler handler) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        mBluetoothAdapter = adapter;
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            //socket的创建
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                //监听客户端连接进来，并且启动等待UI
                mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
                //进行监听
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                //监听到有客户端请求连接
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                    mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        //本DEMO中简单起见：只支持同时处理一个连接
        if( mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
        //开启线程读取数据
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) { }
    }

    public void sendData(byte[] data) {
        if( mConnectedThread!=null){
            mConnectedThread.write(data);
        }
    }
}