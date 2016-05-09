package com.ld.qmwj.message.request;

import com.ld.qmwj.model.Alarm;

/**
 * 操作闹钟请求
 * Created by zsg on 2016/5/7.
 */
public class AlterAlarmRequest extends Request {
    public Alarm alarm;
    public AlterAlarmRequest(int tag) {
        this.tag = tag;
    }
}
