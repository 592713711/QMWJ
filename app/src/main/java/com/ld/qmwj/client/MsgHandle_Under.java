package com.ld.qmwj.client;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.dao.BlacklistDao;
import com.ld.qmwj.message.Message;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.AddBlackPhoneReq;
import com.ld.qmwj.message.request.AlterAlarmRequest;
import com.ld.qmwj.message.request.AlterLinkManRequset;
import com.ld.qmwj.message.request.DeleteBlackPhoneReq;
import com.ld.qmwj.message.request.HeartDataRequest;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.message.request.RouteWayRequest;
import com.ld.qmwj.message.response.BandStateRes;
import com.ld.qmwj.message.response.DoHeartRes;
import com.ld.qmwj.message.response.HeartDataResponse;
import com.ld.qmwj.message.response.LinkManResponse;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.message.response.OldWayResponse;
import com.ld.qmwj.message.response.OpenVoiceResponse;
import com.ld.qmwj.message.response.Response;
import com.ld.qmwj.model.Alarm;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.Contacts;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.model.RouteWay;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.util.ContactUtil;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.WayUtil;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import under_control.home.HomeActivity_under;
import under_control.home.service.FunctionService;
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
            ArrayList<LatLng> latLngs = MyApplication.getInstance().getRouteDao().getRoute(
                    MyApplication.getInstance().getSpUtil().getUser().id,
                    WayUtil.getDateStr()
            );
            response.wayList.put(WayUtil.getDateStr(), latLngs);
            response.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
            response.into_id = message.from_id;
            Log.d(Config.TAG, "发送路线响应" + gson.toJson(response));
            MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));

        } else if (message.tag == MessageTag.OLDWAY_REQ_ALL) {

        }
    }

    /**
     * 处理 得到联系人请求
     *
     * @param msgJson
     */
    public void handleLinkMan(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        LinkManResponse response = new LinkManResponse();
        response.list = MyApplication.getInstance().getContactUtil().getContactsList();
        response.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        response.into_id = message.from_id;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));


    }

    /**
     * 处理手环状态请求
     *
     * @param msgJson
     */
    public void handleBandState(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        BandState bandState = new BandState();
        BluetoothDevice device = MyApplication.getInstance().getSpUtil().getDevice();
        if (device == null) {
            bandState.state = BandState.UNBIND;    //未绑定
        } else {
            if (FunctionService.isConnectBand == MiBand.CONNECTED) {
                bandState.state = BandState.CONNECT;      //已连接
                bandState.batteryInfo = FunctionService.info;
            } else {
                bandState.state = BandState.UNCONNECT;    //未连接
            }
        }

        BandStateRes bandStateRes = new BandStateRes();
        bandStateRes.bandState = bandState;
        bandStateRes.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        bandStateRes.into_id = message.from_id;

        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(bandStateRes));
    }

    /**
     * 处理测量心率请求
     *
     * @param msgJson
     */
    public void handleDoHeart(String msgJson) {
        final Response message = gson.fromJson(msgJson, Response.class);

        if (FunctionService.isConnectBand != MiBand.CONNECTED) {
            //如果手环未连接  则响应对方手环状态
            handleBandState(msgJson);
            return;
        }

        MiBand miBand = FunctionService.miBand;
        Log.d(Config.TAG, "测量心率");
        miBand.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate) {
                Log.d(Config.TAG, "heart rate: " + heartRate);
                if (heartRate == 0)
                    heartRate = 30;
                HeartData heartData = new HeartData();
                heartData.data = heartRate;
                heartData.time = System.currentTimeMillis();
                //保存心跳数据
                MyApplication.getInstance().getHeartDao().insertHeartData(
                        MyApplication.getInstance().getSpUtil().getUser().id,
                        heartData
                );

                //发送心跳数据
                DoHeartRes doHeartRes = new DoHeartRes();
                doHeartRes.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
                doHeartRes.into_id = message.from_id;
                doHeartRes.heartData = heartData;

                MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(doHeartRes));
            }
        });

        miBand.startHeartRateScan();
    }

    /**
     * 处理监护方发来心跳数据请求
     *
     * @param msgJson
     */
    public void handleHeartData(String msgJson) {
        HeartDataRequest request = gson.fromJson(msgJson, HeartDataRequest.class);
        ArrayList<HeartData> datas = MyApplication.getInstance().getHeartDao().getAllHeartDataListByTime(
                request.into_id, request.from_time, request.to_timie);
        HeartDataResponse response = new HeartDataResponse();
        response.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        response.into_id = request.from_id;
        response.from_time = request.from_time;
        response.to_timie = request.to_timie;
        response.datas = datas;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(response));
    }

    /**
     * 处理监护方发来的路线消息
     *
     * @param msgJson
     */
    public void handleRouteWay(String msgJson) {
        RouteWayRequest request = gson.fromJson(msgJson, RouteWayRequest.class);
        RouteWay routeWay = request.routeWay;
        //将信息放入消息表中
        MapWayMsg mapWayMsg = new MapWayMsg();
        mapWayMsg.routeWay = routeWay;
        String msg = MyApplication.getInstance().getGson().toJson(mapWayMsg);
        mapWayMsg.is_coming = Config.FROM_MSG;
        //短信时间和现在时间对比，若比现在时间还大，则变为现在时间
        if (request.time > System.currentTimeMillis()) {
            mapWayMsg.time = System.currentTimeMillis();
        } else {
            mapWayMsg.time = request.time;
        }

        MyApplication.getInstance().getMessageDao().addMessage(
                request.from_id, mapWayMsg, msg);
        EventBus.getDefault().post(HandlerUtil.CHAT_UPDATE);

    }

    /**
     * 添加黑名单
     *
     * @param msgJson
     */
    public void handleAddBlackPhone(String msgJson) {
        AddBlackPhoneReq request = gson.fromJson(msgJson, AddBlackPhoneReq.class);
        BlacklistDao dao = MyApplication.getInstance().getBlacklistDao();
        if (!dao.isAlreadyadd(request.into_id, request.blackPhone.phonenum))
            dao.insertBlackPhone(
                    request.into_id, request.blackPhone
            );
        Log.d(Config.TAG, "添加黑名单成功");
    }

    /**
     * 删除黑名单
     *
     * @param msgJson
     */
    public void handleDeleteBlackPhone(String msgJson) {
        DeleteBlackPhoneReq request = gson.fromJson(msgJson, DeleteBlackPhoneReq.class);
        MyApplication.getInstance().getBlacklistDao().deleteBlackPhone(request.into_id, request.blackPhone.phonenum);
        Log.d(Config.TAG, "删除黑名单成功");
    }

    /**
     * 处理操作联系请求
     *
     * @param msgJson
     */
    public void handleAlterLinkMan(String msgJson) {
        AlterLinkManRequset requset = gson.fromJson(msgJson, AlterLinkManRequset.class);
        Contacts contacts = requset.contacts;
        if (requset.tag == MessageTag.ADDLINKMAN_REQ) {
            //添加联系人
            ContactUtil.addContact(MyApplication.getInstance(), contacts.getName(), contacts.getPhonenum());
            Log.d(Config.TAG, "添加了一条联系人");
        } else if (requset.tag == MessageTag.DELETELINKMAN_REQ) {
            //删除联系人
            ContactUtil.delContact(MyApplication.getInstance(), contacts.getName(), contacts.getPhonenum());
            Log.d(Config.TAG, "删除了一条联系人");
        } else if (requset.tag == MessageTag.UPDATELINKMAN_REQ) {
            Contacts oldcontact = requset.oldcontacts;
            ContactUtil.updatePhoneContact(MyApplication.getInstance(),
                    oldcontact.getName(), oldcontact.getPhonenum(), contacts.getName(), contacts.getPhonenum());
            Log.d(Config.TAG, "修改了一条联系人");
        }
    }


    /**
     * 处理操作闹钟请求
     *
     * @param msgJson
     */
    public void handleAlterAlarm(String msgJson) {
        AlterAlarmRequest request = gson.fromJson(msgJson, AlterAlarmRequest.class);
        Alarm alarm = request.alarm;
        if (request.tag == MessageTag.ADDALARM_REQ) {
            MyApplication.getInstance().getAlarmtDao().insertAlarm2(
                    MyApplication.getInstance().getSpUtil().getUser().id,
                    alarm
            );
            Log.d(Config.TAG, "添加了一条闹钟"+alarm.position);
        } else if (request.tag == MessageTag.DELETEALARM_REQ) {
            MyApplication.getInstance().getAlarmtDao().deleteAlarm(MyApplication.getInstance().getSpUtil().getUser().id,
                    alarm);
            Log.d(Config.TAG, "删除了一条闹钟"+alarm.position);
        } else if (request.tag == MessageTag.UPDATEALARM_REQ) {
            MyApplication.getInstance().getAlarmtDao().updateAlarm(MyApplication.getInstance().getSpUtil().getUser().id,
                    alarm);
            Log.d(Config.TAG, "更新了一条闹钟"+alarm.position);
        }

        EventBus.getDefault().post(request);
    }

    public void handleOpenVoice(String msgJson) {
        //开启语音录制

        OpenVoiceResponse response=gson.fromJson(msgJson,OpenVoiceResponse.class);
        MyApplication.getInstance().getAudioWrapper().setServerPort(response.port);
        Log.d(Config.TAG,"port:"+response.port);
        MyApplication.getInstance().getAudioWrapper().stopRecord();
        Log.d(Config.TAG, "开启语音监听");
        MyApplication.getInstance().getAudioWrapper().startRecord();
    }
}
