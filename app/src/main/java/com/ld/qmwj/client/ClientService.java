package com.ld.qmwj.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ld.qmwj.Config;
import com.ld.qmwj.client.MyClient;

/**
 * 用来启动客户端的服务
 */
public class ClientService extends Service implements Config {
    public ClientService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();


        //开启线程（服务是工作在主线程上） 用来启动客户端与服务器的连接
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MyClient.initClient();
                MyClient.doConnect();
            }
        });
        thread.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
