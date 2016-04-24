package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.HeartData;

/**
 * 测试心跳响应
 * Created by zsg on 2016/4/18.
 */
public class DoHeartRes extends Response{
    public HeartData heartData;
    public DoHeartRes(){
        this.tag= MessageTag.DOHEART_RES;
    }
}
