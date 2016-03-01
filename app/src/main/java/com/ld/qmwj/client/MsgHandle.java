package com.ld.qmwj.client;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.response.LoginResponse;
import com.ld.qmwj.message.response.RefreshListRes;
import com.ld.qmwj.message.response.Response;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.SharePreferenceUtil;

import java.util.HashMap;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by zsg on 2016/2/15.
 */
public class MsgHandle {
    private Gson gson;
    public static MsgHandle msgHandle;
    public ChannelHandlerContext channel;     //与服务器连接的通道

    private Handler handler;          //主线程handle 用于分发消息

    private MsgHandle() {
        gson = new Gson();

    }

    public static MsgHandle getInstance() {
        if (msgHandle == null) {
            msgHandle = new MsgHandle();
        }
        return msgHandle;
    }

    public void initHandle(Handler handler) {
        this.handler = handler;
    }

    /**
     * 处理服务器发来的请求
     */
    public void handleMsg(String msgJson) {
        Response message = gson.fromJson(msgJson, Response.class);
        Log.d(Config.TAG,msgJson);
        switch (message.tag) {
            case MessageTag.LOGIN_RES:
                //登陆请求
                handleLogin(msgJson);
                break;
            case MessageTag.HEART_MSG:
                //发送心跳消息
                Log.d(Config.TAG,"收到心跳消息");
                MyApplication.getInstance().getSendMsgUtil().sendHeartMessage();
                break;
        }
    }

    /**
     * 处理登陆
     * @param msgJson
     */
    private void handleLogin(String msgJson) {
        LoginResponse loginRes = gson.fromJson(msgJson, LoginResponse.class);
        Message msg = Message.obtain();
        if (loginRes.isSuccess) {
            //将监护列表写入数据库中
            RefreshListRes refreshListRes=loginRes.refreshListRes;
            if(refreshListRes!=null){
                MyApplication.getInstance().getRelateDao().addList(refreshListRes.monitorList);
            }
            msg.what = HandlerUtil.LOGIN_SUCCESS;
            SharePreferenceUtil spUtil = MyApplication.getInstance().getSpUtil();
            spUtil.writeUser(loginRes.user);
        } else {
            msg.what = HandlerUtil.LOGIN_FAIL;
        }

        if(handler!=null)
            handler.sendMessage(msg);
    }


}
