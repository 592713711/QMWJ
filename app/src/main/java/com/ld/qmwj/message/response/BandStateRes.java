package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.BandState;

/**
 * 手环状态响应
 * Created by zsg on 2016/4/18.
 */
public class BandStateRes extends Response{
    public BandState bandState;
    public BandStateRes(){
        this.tag= MessageTag.BANDSTATE_RES;
    }
}
