package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.HeartData;

/**
 * 心跳数据请求
 * Created by zsg on 2016/4/20.
 */
public class HeartDataRequest extends  Request{
    public long from_time;        //查询数据起始的时间
    public long to_timie;         //查询数据终止的时间
    public HeartDataRequest(){
        this.tag= MessageTag.HEARTDATA_REQ;
    }
}
