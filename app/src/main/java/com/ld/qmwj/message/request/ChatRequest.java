package com.ld.qmwj.message.request;

import com.ld.qmwj.Config;
import com.ld.qmwj.message.MessageTag;

/**
 * 携带聊天信息的请求
 * Created by zsg on 2016/3/26.
 */
public class ChatRequest extends Request{
    public long time;       //发送时间
    public String msg;      //发送的文本内容
    public ChatRequest(){
        this.tag= MessageTag.CHAT_REQ;
    }
}
