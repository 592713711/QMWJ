package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;

/**
 * Created by zsg on 2016/5/16.
 */
public class OpenVoiceResponse extends Response {
    public int port;       //服务器端口号

    public OpenVoiceResponse() {
        this.tag = MessageTag.OPEN_VOICE_RES;
    }
}
