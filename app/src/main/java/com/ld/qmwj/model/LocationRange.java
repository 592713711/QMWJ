package com.ld.qmwj.model;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsg on 2016/4/24.
 */
public class LocationRange implements Serializable{
    public String location_name;        //地址
    public String location_remark;      //地址备注
    public LatLng latLng;   //坐标
    public int range;       // 范围
    public ArrayList<LatLng> points;
    public int rang_pos;    //当前安全区域的位置  最多有3个

    @Override
    public String toString() {
        return "LocationRange{" +
                "location_name='" + location_name + '\'' +
                ", location_remark='" + location_remark + '\'' +
                ", latLng=" + latLng +
                ", range=" + range +
                ", rang_pos=" + rang_pos +
                '}';
    }
}
