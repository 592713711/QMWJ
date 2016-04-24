package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.HeartData;

import java.util.ArrayList;

/**
 * 心跳数据响应
 * Created by zsg on 2016/4/20.
 */
public class HeartDataResponse extends Response{
    public ArrayList<HeartData> datas;
    public long from_time;        //查询数据起始的时间
    public long to_timie;         //查询数据终止的时间
    public HeartDataResponse(){
        this.tag= MessageTag.HEARTDATA_RES;
    }
}
