package com.ld.qmwj.model;

/**
 * Created by zsg on 2016/3/3.
 */
public class MyLocation {
    public double latitude=0;        //纬度
    public double longitude=0;       //进度
    public double   time=0;            //时间

    public MyLocation(double mlat,double mlong,double mtime){
        this.latitude=mlat;
        this.longitude=mlong;
        this.time=mtime;
    }

    @Override
    public String toString() {
        return "MyLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", time=" + time +
                '}';
    }
}
