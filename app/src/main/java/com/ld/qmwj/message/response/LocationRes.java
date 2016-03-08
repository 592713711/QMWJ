package com.ld.qmwj.message.response;


import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.MyLocation;

/**
 * 响应对方位置
 * Created by zsg on 2016/3/3.
 */
public class LocationRes extends Request {
    public MyLocation myLocation;

    public LocationRes() {
        tag = MessageTag.LOCATION_RES;
    }
}
