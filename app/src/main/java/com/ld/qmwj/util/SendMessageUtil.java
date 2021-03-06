package com.ld.qmwj.util;

import android.util.Log;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.HeartMessage;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.LoginRequest;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.User;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * 用来发送消息给服务器
 * Created by zsg on 2016/2/16.
 */
public class SendMessageUtil {
    private Gson gson;
    private User user;
    SharePreferenceUtil spUtil;
    String heartMessage;

    public SendMessageUtil() {
        gson = new Gson();
        spUtil = MyApplication.getInstance().getSpUtil();
        heartMessage = gson.toJson(new HeartMessage())+"#";

    }

    /**
     * 发送登陆消息 若用户名和密码都为null  则为自动登录
     *
     * @param username
     * @param password
     */
    public void sendLoginRequest(String username, String password) {
        LoginRequest loginRequest = new LoginRequest();
        //查找sp中的用户信息
        user = spUtil.getUser();
        if (username == null && password == null) {
            //自动登录
            loginRequest.isAuto = true;
            if (user != null)
                loginRequest.from_id = user.id;
            else        //尚未登陆
                return;
        } else {
            loginRequest.username = username;
            loginRequest.password = password;
        }

        sendMessageToServer(gson.toJson(loginRequest));
    }

    /**
     * 发送请求位置请求
     */
    public void sendLocationRequest(int user_id,int monitor_id){
        Request request=new Request();
        request.tag= MessageTag.LOCATION_REQ;
        request.from_id=user_id;
        request.into_id=monitor_id;
        sendMessageToServer(gson.toJson(request));
    }

    /**
     * 发送心跳消息
     */
    public void sendHeartMessage() {
        ByteBuf msgbuf = Unpooled.copiedBuffer(heartMessage.getBytes());
        MsgHandle.getInstance().channel.writeAndFlush(msgbuf);
    }

    /**
     * 发送实时消息到服务器
     */
    public void sendMessageToServer(String msg){
        Log.d(Config.TAG, "发送:"+msg);
        msg+="#";
        ByteBuf msgbuf = Unpooled.copiedBuffer(msg.getBytes());
        if (MsgHandle.getInstance().channel != null) {
            MsgHandle.getInstance().channel.writeAndFlush(msgbuf);
        }
    }

    /**
     * 发送非实时消息到服务器
     * @param msg
     */
    public void sendCacheMessageToServer(String msg){
        Log.d(Config.TAG, "发送非及时消息:"+msg);
        String msg2=msg+"#";
        ByteBuf msgbuf = Unpooled.copiedBuffer(msg2.getBytes());
        if (MsgHandle.getInstance().channel != null) {
            MsgHandle.getInstance().channel.writeAndFlush(msgbuf);
        }else{
            //放入缓存表中
            Log.d(Config.TAG,"未连接服务器，放入缓存表中");
            MyApplication.getInstance().getCacheDao().insertCacheMsg(msg);
        }
    }

    /**
     * 发送缓存表的信息
     */
    public void sendCache(){

        if(MyApplication.getInstance().getSpUtil().getUser()!=null) {
            //已登录
            ArrayList<String> msgs = MyApplication.getInstance().getCacheDao().getAllCacheMsg();
            for (String s : msgs) {
                Log.d(Config.TAG, "发送缓存消息：" + s);
                sendCacheMessageToServer(s);
            }
        }

    }
}
