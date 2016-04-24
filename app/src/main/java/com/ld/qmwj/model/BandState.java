package com.ld.qmwj.model;

import com.zhaoxiaodan.miband.model.BatteryInfo;

import java.io.Serializable;

/**
 * 手环状态
 * Created by zsg on 2016/4/17.
 */
public class BandState implements Serializable {
    public int state;
    public static int UNBIND=1;     //未绑定
    public static int UNCONNECT=2;  //未连接
    public static int CONNECT=3;    //已连接

    public BatteryInfo batteryInfo;     //电池信息
}
