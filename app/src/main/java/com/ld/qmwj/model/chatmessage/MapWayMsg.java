package com.ld.qmwj.model.chatmessage;

import com.baidu.mapapi.model.LatLng;
import com.ld.qmwj.Config;

/**
 * 地图路线消息
 * Created by zsg on 2016/3/24.
 */
public class MapWayMsg extends ChatMessage{
    public LatLng startLocation;        //起点
    public LatLng endLocation;          //终点
    public MapWayMsg(){
        this.msg_type= Config.MAPWAY_MSG;
    }
}
