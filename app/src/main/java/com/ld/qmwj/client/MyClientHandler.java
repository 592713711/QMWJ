package com.ld.qmwj.client;

import android.util.Log;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MyClientHandler extends SimpleChannelInboundHandler<String> {

    public MyClientHandler() {

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Log.d(LoginActivity.TAG, "接收服务端信息");
        String body = (String) msg;
        MsgHandle.getInstance().handleMsg(body);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(Config.TAG, "连接上服务器");
        super.channelActive(ctx);
        MsgHandle.getInstance().channel = ctx;
        MyApplication.getInstance().getSendMsgUtil().sendLoginRequest(null,null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(Config.TAG, "与服务器断开连接服务器");
        super.channelInactive(ctx);
        MsgHandle.getInstance().channel = null;

        //重新连接服务器
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                MyClient.doConnect();
            }
        }, 2, TimeUnit.SECONDS);
        ctx.close();
    }

}