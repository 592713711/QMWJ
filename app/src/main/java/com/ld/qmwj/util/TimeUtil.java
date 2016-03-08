package com.ld.qmwj.util;

import android.util.Log;

import com.ld.qmwj.Config;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 处理时间工具类
 * Created by zsg on 2016/3/5.
 */
public class TimeUtil {
    public static String getTimeStr(long oldTime, long currenttime) {

        long time = (currenttime - oldTime) / 1000;
        Log.d(Config.TAG,currenttime+"  "+oldTime+"时差："+time);
        if (time >= 0 && time < 60) {
            return "刚才";
        } else if (time >= 60 && time < 3600) {
            return time / 60 + "分钟前";
        } else if (time >= 3600 && time < 3600 * 24) {
            return time / 3600 + "小时前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(oldTime);
        }
    }
}
