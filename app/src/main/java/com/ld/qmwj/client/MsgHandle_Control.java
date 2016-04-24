package com.ld.qmwj.client;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.response.BandStateRes;
import com.ld.qmwj.message.response.CallPhoneRes;
import com.ld.qmwj.message.response.DoHeartRes;
import com.ld.qmwj.message.response.HeartDataResponse;
import com.ld.qmwj.message.response.LinkManResponse;
import com.ld.qmwj.message.response.LocationRes;
import com.ld.qmwj.message.response.OldWayResponse;
import com.ld.qmwj.message.response.SmsResponse;
import com.ld.qmwj.model.PhoneState;
import com.ld.qmwj.model.Sms;
import com.ld.qmwj.model.chatmessage.PhoneStateMsg;
import com.ld.qmwj.model.chatmessage.SmsMsg;
import com.ld.qmwj.util.HandlerUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 控制方消息处理
 * Created by zsg on 2016/3/4.
 */
public class MsgHandle_Control {
    private Gson gson;

    public MsgHandle_Control(Gson gson) {
        this.gson = gson;

    }

    /**
     * 控制方接受到位置响应
     *
     * @param msgJson
     */
    public void handleLocation(String msgJson) {
        Log.d(Config.TAG, "收到位置响应" + msgJson);
        LocationRes locationRes = gson.fromJson(msgJson, LocationRes.class);
        //将位置信息写入表中
        MyApplication.getInstance().getRelateDao().insertLocation(locationRes.from_id, locationRes.myLocation);
        EventBus.getDefault().post(HandlerUtil.LOCATION_RESPONSE);

    }


    /**
     * 处理位置响应
     *
     * @param msgJson
     */
    public void handleOldWay(String msgJson) {
        Log.d(Config.TAG, "收到路线响应" + msgJson);
        OldWayResponse response = gson.fromJson(msgJson, OldWayResponse.class);
        Map<String, ArrayList<LatLng>> map = response.wayList;
        Set<String> keys = map.keySet();
        for (String key : keys) {
            MyApplication.getInstance().getRouteDao().addRouteList(
                    response.from_id,
                    key, map.get(key));
        }

        //发送 更新历史路线到主线程
        EventBus.getDefault().post(HandlerUtil.OLDWAY_UPDATE);

    }

    /**
     * 处理 被监护方通话记录消息
     */
    public void handleCallPhone(String msgJson) {
        CallPhoneRes callPhoneRes = gson.fromJson(msgJson, CallPhoneRes.class);
        PhoneState phoneState=callPhoneRes.phoneState;

        //短信时间和现在时间对比，若比现在时间还大，则变为现在时间
        if(phoneState.getEndTime()>System.currentTimeMillis()){
            phoneState.setEndTime(System.currentTimeMillis());
        }

        //将通话记录放入通话表中
        MyApplication.getInstance().getCallPhoneDao().insertCallPhone(callPhoneRes.from_id, phoneState);
        //放入信息表中
        PhoneStateMsg phoneStateMsg=new PhoneStateMsg();
        phoneStateMsg.phoneNum=phoneState.getPhonenum();
        phoneStateMsg.phonename=phoneState.getName();
        phoneStateMsg.call_Type=phoneState.getType();
        String msg=gson.toJson(phoneStateMsg);
        phoneStateMsg.is_coming=Config.FROM_MSG;
        phoneStateMsg.time=phoneState.getEndTime();
        MyApplication.getInstance().getMessageDao().addMessage(
                callPhoneRes.from_id,phoneStateMsg,msg);
        //发送收到通话记录事件
        EventBus.getDefault().post(HandlerUtil.CALLPHONE_RESPONSE);
    }


    /**
     * 处理联系人响应
     *
     * @param msgJson
     */
    public void handleLinkMan(String msgJson) {
        LinkManResponse response = gson.fromJson(msgJson, LinkManResponse.class);
        MyApplication.getInstance().getLinkManDao().insertList(response.from_id, response.list);
        EventBus.getDefault().post(HandlerUtil.LINKMAN_RESPONSE);
    }

    public void handleSms(String msgJson) {
        SmsResponse smsResponse=gson.fromJson(msgJson,SmsResponse.class);
        Sms sms=smsResponse.sms;


        //短信时间和现在时间对比，若比现在时间还大，则变为现在时间
        if(sms.getDate()>System.currentTimeMillis()){
            sms.setDate(System.currentTimeMillis());
        }

        //放入信息表中
        MyApplication.getInstance().getSmsDao().insertSms(smsResponse.from_id, sms);
        //放入信息表
        SmsMsg smsMsg=new SmsMsg();
        smsMsg.smsNum=sms.getPhoneNumber();
        smsMsg.smsName=sms.getName();
        smsMsg.smsType=sms.getType();
        String msg=gson.toJson(smsMsg);
        smsMsg.is_coming=Config.FROM_MSG;
        smsMsg.time=sms.getDate();


        MyApplication.getInstance().getMessageDao().addMessage(
                smsResponse.from_id, smsMsg, msg);
        //发送收到通话记录事件
        EventBus.getDefault().post(HandlerUtil.SMSSTATE_RESPONSE);
    }

    /**
     * 处理手环状态响应
     * @param msgJson
     */
    public void handleBandState(String msgJson) {
        BandStateRes bandStateRes=gson.fromJson(msgJson,BandStateRes.class);
        EventBus.getDefault().post(bandStateRes.bandState);
    }

    /**
     * 处理心跳请求响应
     * @param msgJson
     */
    public void handleDoHeart(String msgJson) {
        DoHeartRes doHeartRes=gson.fromJson(msgJson,DoHeartRes.class);
        //保存心跳数据
        MyApplication.getInstance().getHeartDao().insertHeartData(
                doHeartRes.from_id,
                doHeartRes.heartData
        );
        EventBus.getDefault().post(doHeartRes.heartData);
    }

    public void handleHeartData(String msgJson) {
        HeartDataResponse response=gson.fromJson(msgJson,HeartDataResponse.class);
        MyApplication.getInstance().getHeartDao().updateHeartData(
                response.from_id,response.datas,response.from_time,response.to_timie
        );

        EventBus.getDefault().post(HandlerUtil.UPDATEHEARTDATA);
    }
}
