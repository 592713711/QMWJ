package com.ld.qmwj.model.chatmessage;

import com.ld.qmwj.Config;

/**
 * Created by zsg on 2016/5/10.
 */
public class RecordMsg extends ChatMessage{
    public String filename;     //文件名
    public int duration;        //文件时长
    public RecordMsg(){
        this.msg_type= Config.RECORD_MSG;
    }
}
