package com.ld.qmwj.model;

import com.baidu.mapapi.model.LatLng;

/**
 * 路线信息
 * Created by zsg on 2016/5/2.
 */
public class RouteWay {
    public LatLng startLocation;        //起点
    public LatLng endLocation;          //终点
    public int way_type;                 //路线类型  驾车 步行 公交
    public int routePos;            //第几条路线
    public String endAddress;
}
