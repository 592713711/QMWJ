package com.ld.qmwj.model;

import java.io.Serializable;

/**
 * Created by zsg on 2016/5/6.
 */
public class Alarm implements Serializable{
    public String alarm_hint;
    public long alarm_time;
    public int isopen;
    public int position;

    public static final int ON=1;
    public static final int OFF=0;
}
