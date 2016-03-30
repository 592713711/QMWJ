package com.ld.qmwj.model.chatmessage;

import com.ld.qmwj.Config;

/**
 * 普通聊天消息
 * Created by zsg on 2016/3/24.
 */
public class SimpleMsg extends ChatMessage{
    public String msg;     //消息内容
    public SimpleMsg(){
        this.msg_type= Config.CHAT_MSG;
    }
}
