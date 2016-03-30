package com.ld.qmwj.client;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.message.response.LinkManResponse;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.message.response.OldWayResponse;
import com.ld.qmwj.message.response.Response;
import com.ld.qmwj.model.Contacts;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.util.WayUtil;

import java.util.ArrayList;

import under_control.home.service.LocationService;

/**
 * 被控制方的消息处理
 * Created by zsg on 2016/3/4.
 */
public class MsgHandle_Under {
    private Gson gson;

    public MsgHandle_Under(Gson gson) {
        this.gson = gson;

    }

    /**
     * 被控制方收到位置请求
     * 响应自己当前位置
     *
     * @param msgJson
     */
    public void handleLocation(String msgJson) {
        if (LocationService.bdLocation == null)
            return;
        Request request = gson.fromJson(msgJson, Request.class);
        LocationRes locationRes = new LocationRes();
        locationRes.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        locationRes.into_id = request.from_id;
        MyLocation location = new MyLocation(LocationService.bdLocation.getLatitude(),
                LocationService.bdLocation.getLongitude(), System.currentTimeMillis());
        locationRes.myLocation = location;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(locationRes));
    }


    /**
     * 处理位置请求
     *
     * @param msgJson
     */
    public void handleOldWay(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        OldWayResponse response = new OldWayResponse();
        if (message.tag == MessageTag.OLDWAY_REQ) {
            //请求当天数据
            ArrayList<LatLng> latLngs= MyApplication.getInstance().getRouteDao().getRoute(
                    MyApplication.getInstance().getSpUtil().getUser().id,
                    WayUtil.getDateStr()
            );
            response.wayList.put(WayUtil.getDateStr(),latLngs);
            response.from_id= MyApplication.getInstance().getSpUtil().getUser().id;
            response.into_id=message.from_id;
            Log.d(Config.TAG, "发送路线响应" + gson.toJson(response));
            MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));

        } else if (message.tag == MessageTag.OLDWAY_REQ_ALL) {

        }
    }

    /**
     * 处理 得到联系人请求
     * @param msgJson
     */
    public void handleLinkMan(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        LinkManResponse response=new LinkManResponse();
        response.list=MyApplication.getInstance().getContactUtil().getContactsList();
        response.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
        response.into_id=message.from_id;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));


    }
}
