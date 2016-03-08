package com.ld.qmwj.client;

import com.google.gson.Gson;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.model.MyLocation;

import under_control.home.LocationService;

/**
 * 被控制方的消息处理
 * Created by zsg on 2016/3/4.
 */
public class MsgHandle_Under {
    private Gson gson;
    public MsgHandle_Under(Gson gson){
        this.gson=gson;

    }

    /**
     * 被控制方收到位置请求
     * 响应自己当前位置
     * @param msgJson
     */
    public void handleLocation(String msgJson) {
        if(LocationService.bdLocation==null)
            return;
        Request request=gson.fromJson(msgJson,Request.class);
        LocationRes locationRes=new LocationRes();
        locationRes.from_id= MyApplication.getInstance().getSpUtil().getUser().id;
        locationRes.into_id=request.from_id;
        MyLocation location=new MyLocation(LocationService.bdLocation.getLatitude(),
                LocationService.bdLocation.getLongitude(),System.currentTimeMillis());
        locationRes.myLocation=location;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(locationRes));
    }
}
