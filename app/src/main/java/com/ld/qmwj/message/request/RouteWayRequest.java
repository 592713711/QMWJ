package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.RouteWay;

/**
 * 路线消息请求
 * Created by zsg on 2016/5/2.
 */
public class RouteWayRequest extends Request{
    public long time;
    public RouteWay routeWay;
    public RouteWayRequest(){
        this.tag= MessageTag.ROUTEWAY_REQ;
    }
}
