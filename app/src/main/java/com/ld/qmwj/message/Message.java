package com.ld.qmwj.message;

/**
 * Created by zsg on 2016/2/12.
 * 所有传递消息的父类
 */

public abstract class Message {
    public int tag;
    public int from_id;		//消息来源的用户id
    public int into_id;		//消息要到达的用户id		//消息给服务器时 为-1
}
