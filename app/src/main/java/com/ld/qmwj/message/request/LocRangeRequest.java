package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.LocationRange;

/**
 * 安全范围更改请求
 * Created by zsg on 2016/4/28.
 */
public class LocRangeRequest extends Request{
    public LocationRange locationRange;
    public LocRangeRequest(){
        this.tag= MessageTag.LOCATIONRANGE_REQ;
    }
}
