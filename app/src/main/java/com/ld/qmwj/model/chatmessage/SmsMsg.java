package com.ld.qmwj.model.chatmessage;

import com.ld.qmwj.Config;

/**
 * Created by zsg on 2016/3/24.
 */
public class SmsMsg extends ChatMessage{
    public String smsNum;       //短信号码
    public String smsName;      //短信备注名
    public int smsType;         //短信类型    1接收     2发送
    public SmsMsg(){
        this.msg_type= Config.SMS_MSG;
    }
}
