package com.ld.qmwj.util;



import com.baidu.mapapi.model.LatLng;
import com.ld.qmwj.Config;


import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Created by zzc on 2016/1/19.
 */
public class WayUtil {

    public WayUtil(){

    }

    /**
     * 计算两点之间距离
     * @param start
     * @param end
     * @return 米
     */
    public static double getDistance(LatLng start,LatLng end){
        double lat1 = (Math.PI/180)*start.latitude;
        double lat2 = (Math.PI/180)*end.latitude;

        double lon1 = (Math.PI/180)*start.longitude;
        double lon2 = (Math.PI/180)*end.longitude;


        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;

        return d*1000;
    }

    //得到当前时间的日期 用于命名表名
    public static String getDateStr(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat format=new SimpleDateFormat("yyyy_MM_dd");
        String s=format.format(date);
       // Log.d(Config.TAG,"日期："+s);
        return s;
    }
}
