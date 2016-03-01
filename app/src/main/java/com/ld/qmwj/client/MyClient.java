package com.ld.qmwj.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by zsg on 2015/11/21.
 */
public class MyClient implements Config {
    private static Bootstrap bootstrap;
    private static ChannelFutureListener channelFutureListener = null;

    public MyClient() {

    }


    // 初始化客户端
    public static void initClient() {

        NioEventLoopGroup group = new NioEventLoopGroup();

        // Client服务启动器 3.x的ClientBootstrap
        // 改为Bootstrap，且构造函数变化很大，这里用无参构造。
        bootstrap = new Bootstrap();
        // 指定EventLoopGroup
        bootstrap.group(group);
        // 指定channel类型
        bootstrap.channel(NioSocketChannel.class);
        // 指定Handler
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 创建分隔符缓冲对象
                ByteBuf delimiter = Unpooled.copiedBuffer("#"
                        .getBytes());
                // 当达到最大长度仍没找到分隔符 就抛出异常
                ch.pipeline().addLast(
                        new DelimiterBasedFrameDecoder(10000, true, false, delimiter));
                // 将消息转化成字符串对象 下面的到的消息就不用转化了
                //解码
                ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                ch.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));
                ch.pipeline().addLast(new MyClientHandler());
            }
        });
        //设置TCP协议的属性
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);

        channelFutureListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                //  Log.d(Config.TAG, "isDone:" + f.isDone() + "     isSuccess:" + f.isSuccess() +
                //          "     cause" + f.cause() + "        isCancelled" + f.isCancelled());

                if (f.isSuccess()) {
                    Log.d(Config.TAG, "重新连接服务器成功");

                } else {
                    Log.d(Config.TAG, "重新连接服务器失败");
                    //  3秒后重新连接
                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 3, TimeUnit.SECONDS);
                }
            }
        };

    }

    //  连接到Socket服务端
    public static void doConnect() {
        Log.d(TAG, "doConnect");
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(
                    serverIP, port));
            future.addListener(channelFutureListener);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "关闭连接");
        }

    }


}
