package com.ld.qmwj.model.chatmessage;

/**
 * 用于显示在信息列表消息的所有父类
 * Created by zsg on 2016/3/24.
 */
public class ChatMessage {
    public int msg_type;       // 消息类型  1警告  2短信  3电话
    public int is_coming;      // 0:to发送  1:from收到              不写入message字段中（json）
    public long time;          // 消息时间(是对方发送信息的时间)      不写入message字段中（json）

    @Override
    public String toString() {
        return "ChatMessage{" +
                "msg_type=" + msg_type +
                ", is_coming=" + is_coming +
                ", time=" + time +
                '}';
    }
}
