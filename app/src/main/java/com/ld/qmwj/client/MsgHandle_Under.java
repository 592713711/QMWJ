package com.ld.qmwj.client;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.HeartDataRequest;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.message.response.BandStateRes;
import com.ld.qmwj.message.response.DoHeartRes;
import com.ld.qmwj.message.response.HeartDataResponse;
import com.ld.qmwj.message.response.LinkManResponse;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.message.response.OldWayResponse;
import com.ld.qmwj.message.response.Response;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.Contacts;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.util.WayUtil;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;

import java.util.ArrayList;

import under_control.home.service.FuctionService;
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

    /**
     * 处理手环状态请求
     * @param msgJson
     */
    public void handleBandState(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        BandState bandState=new BandState();
        BluetoothDevice device = MyApplication.getInstance().getSpUtil().getDevice();
        if (device == null) {
           bandState.state=BandState.UNBIND;    //未绑定
        }else {
            if(FuctionService.isConnectBand== MiBand.CONNECTED){
                bandState.state=BandState.CONNECT;      //已连接
                bandState.batteryInfo=FuctionService.info;
            }else {
                bandState.state=BandState.UNCONNECT;    //未连接
            }
        }

        BandStateRes bandStateRes=new BandStateRes();
        bandStateRes.bandState=bandState;
        bandStateRes.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
        bandStateRes.into_id=message.from_id;

        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(bandStateRes));
    }

    /**
     * 处理测量心率请求
     * @param msgJson
     */
    public void handleDoHeart(String msgJson) {
        final Response message = gson.fromJson(msgJson, Response.class);

        if(FuctionService.isConnectBand!=MiBand.CONNECTED){
            //如果手环未连接  则响应对方手环状态
            handleBandState(msgJson);
            return;
        }

        MiBand miBand=FuctionService.miBand;
        Log.d(Config.TAG,"测量心率");
        miBand.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate) {
                Log.d(Config.TAG, "heart rate: " + heartRate);
                if(heartRate==0)
                    heartRate=30;
                HeartData heartData = new HeartData();
                heartData.data = heartRate;
                heartData.time = System.currentTimeMillis();
                //保存心跳数据
                MyApplication.getInstance().getHeartDao().insertHeartData(
                        MyApplication.getInstance().getSpUtil().getUser().id,
                        heartData
                );

                //发送心跳数据
                DoHeartRes doHeartRes=new DoHeartRes();
                doHeartRes.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
                doHeartRes.into_id=message.from_id;
                doHeartRes.heartData=heartData;

                MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(doHeartRes));
            }
        });

        miBand.startHeartRateScan();
    }

    public void handleHeartData(String msgJson) {
        HeartDataRequest request=gson.fromJson(msgJson,HeartDataRequest.class);
        ArrayList<HeartData> datas=MyApplication.getInstance().getHeartDao().getAllHeartDataListByTime(
                request.into_id, request.from_time, request.to_timie);
        HeartDataResponse response=new HeartDataResponse();
        response.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
        response.into_id=request.from_id;
        response.from_time=request.from_time;
        response.to_timie=request.to_timie;
        response.datas=datas;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));
    }
}
