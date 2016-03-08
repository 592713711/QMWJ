package com.ld.qmwj.client;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.util.HandlerUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 控制方消息处理
 * Created by zsg on 2016/3/4.
 */
public class MsgHandle_Control {
    private Gson gson;
    public MsgHandle_Control(Gson gson){
        this.gson=gson;

    }

    /**
     * 控制方接受到位置响应
     * @param msgJson
     */
    public void handleLocation(String msgJson) {
        Log.d(Config.TAG, "收到位置响应" + msgJson);
        LocationRes locationRes=gson.fromJson(msgJson, LocationRes.class);
        //将位置信息写入表中
        MyApplication.getInstance().getRelateDao().insertLocation(locationRes.from_id,locationRes.myLocation);
        EventBus.getDefault().post(HandlerUtil.LOCATION_RESPONSE);

    }
}
