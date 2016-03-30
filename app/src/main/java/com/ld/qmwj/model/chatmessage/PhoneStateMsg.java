package com.ld.qmwj.model.chatmessage;

import com.ld.qmwj.Config;

/**
 * 电话监控消息
 * Created by zsg on 2016/3/24.
 */
public class PhoneStateMsg extends ChatMessage{
    public String phoneNum;         //电话号码
    public String phonename;
    public int call_Type;            // 通话类型
    public PhoneStateMsg(){
        this.msg_type= Config.CALL_MSG;
    }

    @Override
    public String toString() {

        return "ChatMessage{" +
                "msg_type=" + msg_type +
                ", is_coming=" + is_coming +
                ", time=" + time +
                '}'+"PhoneStateMsg{" +
                "phoneNum='" + phoneNum + '\'' +
                ", phonename='" + phonename + '\'' +
                ", call_Type=" + call_Type +
                '}';
    }
}
