package com.ld.qmwj.client;

import android.util.Log;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.AuthMessage;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.ChatRequest;
import com.ld.qmwj.message.response.LoginResponse;
import com.ld.qmwj.message.response.RefreshListRes;
import com.ld.qmwj.message.response.Response;
import com.ld.qmwj.model.chatmessage.SimpleMsg;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.SharePreferenceUtil;

import org.greenrobot.eventbus.EventBus;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by zsg on 2016/2/15.
 */
public class MsgHandle {
    private Gson gson;
    public static MsgHandle msgHandle;
    public ChannelHandlerContext channel;     //与服务器连接的通道


    // private Handler handler;          //主线程handle 用于分发消息
    private MsgHandle_Control msgHandle_control;
    private MsgHandle_Under msgHandle_under;

    private MsgHandle() {
        gson = new Gson();
        msgHandle_control = new MsgHandle_Control(gson);
        msgHandle_under = new MsgHandle_Under(gson);

    }

    public static MsgHandle getInstance() {
        if (msgHandle == null) {
            msgHandle = new MsgHandle();
        }
        return msgHandle;
    }


    /**
     * 处理服务器发来的请求
     */
    public void handleMsg(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        Log.d(Config.TAG, msgJson);
        switch (message.tag) {
            case MessageTag.LOGIN_RES:
                //登陆响应
                handleLogin(msgJson);
                break;
            case MessageTag.HEART_MSG:
                //发送心跳消息
                // Log.d(Config.TAG, "收到心跳消息");
                MyApplication.getInstance().getSendMsgUtil().sendHeartMessage();
                break;
            case MessageTag.LOCATION_REQ:
                //位置请求
                msgHandle_under.handleLocation(msgJson);
                break;
            case MessageTag.LOCATION_RES:
                //位置响应
                msgHandle_control.handleLocation(msgJson);
                break;
            case MessageTag.OLDWAY_REQ:
            case MessageTag.OLDWAY_REQ_ALL:
                msgHandle_under.handleOldWay(msgJson);
                break;
            case MessageTag.OLDWAY_RES:
                msgHandle_control.handleOldWay(msgJson);
                break;
            case MessageTag.ONLINE:
            case MessageTag.NOT_ONLINE:
                handleState(message);
                break;
            case MessageTag.CALLPHONE_RES:
                msgHandle_control.handleCallPhone(msgJson);
                break;
            case MessageTag.LINKMAN_REQ:
                msgHandle_under.handleLinkMan(msgJson);
                break;
            case MessageTag.LINKMAN_RES:
                msgHandle_control.handleLinkMan(msgJson);
                break;
            case MessageTag.CHAT_REQ:
                handleChat(msgJson);
                break;
            case MessageTag.SMS_RES:
                msgHandle_control.handleSms(msgJson);
                break;
            case MessageTag.BANDSTATE_REQ:      //手环状态请求
                msgHandle_under.handleBandState(msgJson);
                break;
            case MessageTag.BANDSTATE_RES:      //手环状态响应
                msgHandle_control.handleBandState(msgJson);
                break;
            case MessageTag.DOHEART_REQ:    //测量心率请求
                msgHandle_under.handleDoHeart(msgJson);
                break;
            case MessageTag.DOHEART_RES:    //测量心率响应
                msgHandle_control.handleDoHeart(msgJson);
                break;
            case MessageTag.HEARTDATA_REQ:  //心跳数据请求
                msgHandle_under.handleHeartData(msgJson);
                break;
            case MessageTag.HEARTDATA_RES:  //心跳数据响应
                msgHandle_control.handleHeartData(msgJson);
                break;
            case MessageTag.AUTH_MSG:       //权限消息
                handleAuth(msgJson);
                break;
            case MessageTag.ROUTEWAY_REQ:   //收到监护方发来的地图路线消息
                msgHandle_under.handleRouteWay(msgJson);
                break;
            case MessageTag.ADDBLACKPHONE_REQ:  //收到监护方发来的添加黑名单消息
                msgHandle_under.handleAddBlackPhone(msgJson);
                break;
            case MessageTag.DELETEBLACKPHONE_REQ:  //收到监护方发来的删除黑名单消息
                msgHandle_under.handleDeleteBlackPhone(msgJson);
                break;
            case MessageTag.ADDLINKMAN_REQ: //收到监护方发来的添加联系人消息
            case MessageTag.DELETELINKMAN_REQ:  //删除联系人
            case MessageTag.UPDATELINKMAN_REQ:  //修改联系人
                msgHandle_under.handleAlterLinkMan(msgJson);
                break;
            case MessageTag.ADDALARM_REQ:       //收到监护方添加闹钟
            case MessageTag.DELETEALARM_REQ:    //删除闹钟
            case MessageTag.UPDATEALARM_REQ:    //更新闹钟
                msgHandle_under.handleAlterAlarm(msgJson);
                break;

        }
    }


    /**
     * 处理权限消息
     *
     * @param msgJson
     */
    private void handleAuth(String msgJson) {
        AuthMessage authMessage = gson.fromJson(msgJson, AuthMessage.class);
        //将权限写入权限表
        MyApplication.getInstance().getAuthDao().initAuth(authMessage.from_id, authMessage.authority);
        EventBus.getDefault().post(HandlerUtil.AUTH_MSG);
    }

    /**
     * 处理文本聊天信息
     *
     * @param msgJson
     */
    private void handleChat(String msgJson) {
        ChatRequest request = gson.fromJson(msgJson, ChatRequest.class);
        SimpleMsg simpleMsg = new SimpleMsg();
        simpleMsg.is_coming = Config.FROM_MSG;
        simpleMsg.time = request.time;
        simpleMsg.msg = request.msg;
        MyApplication.getInstance().getMessageDao().addMessage(request.from_id, simpleMsg, simpleMsg.msg);

        EventBus.getDefault().post(HandlerUtil.CHAT_UPDATE);

    }

    /**
     * 处理关系客户端上下线
     *
     * @param message
     */
    private void handleState(Response message) {
        MyApplication.getInstance().getRelateDao().changeRelateState(message.from_id, message.tag);
        EventBus.getDefault().post(HandlerUtil.STATE_RESPONSE);
    }


    /**
     * 处理登陆
     *
     * @param msgJson
     */
    private void handleLogin(String msgJson) {
        LoginResponse loginRes = gson.fromJson(msgJson, LoginResponse.class);
        int tag;        //消息标识
        if (loginRes.isSuccess) {
            //将监护列表写入数据库中
            RefreshListRes refreshListRes = loginRes.refreshListRes;
            SharePreferenceUtil spUtil = MyApplication.getInstance().getSpUtil();
            spUtil.writeUser(loginRes.user);
            if (refreshListRes != null) {
                if (loginRes.user != null) {
                    //正常登陆
                    MyApplication.getInstance().getRelateDao().addList(refreshListRes.monitorList);
                    if (spUtil.getUser().status == Config.GUARDIAN_STATUS) {
                        //当前用户时监护方
                        //MyApplication.getInstance().getAuthDao().initAuth(refreshListRes.monitorList);
                    } else {
                        //当前用户为被监护方 将自己的权限写入
                        MyApplication.getInstance().getAuthDao().initAuth(spUtil.getUser().id);
                    }
                } else {
                    //自动登录
                    // MyApplication.getInstance().getRelateDao().addList(refreshListRes.monitorList);
                    MyApplication.getInstance().getRelateDao().updateState(refreshListRes.monitorList);
                }
            }
            tag = HandlerUtil.LOGIN_SUCCESS;

            //发送缓存表消息
            MyApplication.getInstance().getSendMsgUtil().sendCache();
        } else {
            tag = HandlerUtil.LOGIN_FAIL;
        }

        EventBus.getDefault().post(tag);

    }


}
