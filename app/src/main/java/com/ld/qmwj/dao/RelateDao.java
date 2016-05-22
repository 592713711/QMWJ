package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.ld.qmwj.Config;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;

import java.util.ArrayList;

/**
 * 操作被监护人/监护人的信息
 * Created by zsg on 2016/2/21.
 */
public class RelateDao {
    private static final String TABLE_NAME = "relate";      //表名
    private static final String COL_MONITOR_ID = "monitor_id";      // 被监护人/监护人的id
    private static final String COL_MONITOR_NAME = "Monitor_name";          //
    private static final String COL_REMARK_NAME = "remark_name";       //备注名
    private static final String COL_IDENTIFY = "identify";       //用户相对于该Monitor的身份   0被监护方  1 主监护方  2 副监护
    private static final String COL_ICON = "icon";          //头像id
    private static final String COL_LATITUDE = "latitude";          //最后一次定位纬度
    private static final String COL_LONGITUDE = "longitude";          //最后一次定位经度
    private static final String COL_TIME = "time";          //最后一次定位时间
    private static final String COL_STATE = "state";          //状态  在线 0  离线 1
    private static final String COL_BANDSTATE = "bandstate";  //手环状态
    private static final String COL_SAFE = "safe";      //位置安全
    private static final String COL_LOCATION_MSG = "location_msg";      //地理位置信息
    private static final String COL_HEART_RATE = "heart_rate";      //最后一次心跳信息

    private static final String[] COL_ALL = {COL_MONITOR_ID, COL_MONITOR_NAME, COL_REMARK_NAME, COL_ICON, COL_STATE, COL_IDENTIFY,COL_BANDSTATE,COL_SAFE,COL_HEART_RATE,COL_LOCATION_MSG};
    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s text,%s integer,%s text,%s integer,%s text default '0',%s text default '0',%s text default '0',%s integer,%s integer,%s integer,%s integer,%s text)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_MONITOR_NAME,
            COL_IDENTIFY,
            COL_REMARK_NAME,
            COL_ICON,
            COL_LATITUDE,
            COL_LONGITUDE,
            COL_TIME,
            COL_STATE,
            COL_BANDSTATE,
            COL_SAFE,
            COL_HEART_RATE,
            COL_LOCATION_MSG
    );

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );


    private DBHelper helper;
    private SQLiteDatabase db;

    public RelateDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 将列表存入数据库
     */
    public void addList(ArrayList<Monitor> monitorList) {

        if (monitorList == null)
            return;
        //删除表中所有数据
        db.delete(TABLE_NAME, null, null);
        for (int i = 0; i < monitorList.size(); i++) {
            Monitor monitor = monitorList.get(i);
            ContentValues values = new ContentValues();
            values.put(COL_MONITOR_ID, monitor.id);
            values.put(COL_MONITOR_NAME, monitor.username);
            values.put(COL_REMARK_NAME, monitor.remark_name);
            values.put(COL_IDENTIFY, monitor.identify);
            values.put(COL_ICON, 0);
            values.put(COL_STATE, monitor.state);
            Log.d(Config.TAG, "插入数据:" + monitor.toString());
            db.insert(TABLE_NAME, null, values);
        }
    }


    /**
     * 得到所有列表
     */
    public ArrayList<Monitor> getList() {
        ArrayList<Monitor> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, COL_ALL, null, null, null, null, null);
        if (cursor.moveToNext()) {
            Monitor m = new Monitor();
            m.id = cursor.getInt(0);
            m.username = cursor.getString(1);
            m.remark_name = cursor.getString(2);
            m.icon = cursor.getInt(3);
            m.state = cursor.getInt(4);
            m.identify = cursor.getInt(5);
            m.bandstate=cursor.getInt(6);
            m.safe=cursor.getInt(7);
            m.heart_rate=cursor.getInt(8);
            m.location_msg=cursor.getString(9);
            list.add(m);

        }
        cursor.close();
        return list;
    }

    /**
     * 插入指定用户位置
     */
    public void insertLocation(int monitor_id, MyLocation location) {
        String mlat = Double.toString(location.latitude);
        String mlong = Double.toString(location.longitude);
        String mtime = Double.toString(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.put(COL_LATITUDE, mlat);
        values.put(COL_LONGITUDE, mlong);
        values.put(COL_TIME, mtime);
        db.update(TABLE_NAME, values, COL_MONITOR_ID + "=" + monitor_id, null);
    }

    public Monitor getMonitorById(int id) {
        Monitor m = new Monitor();
        Cursor cursor = db.query(TABLE_NAME, COL_ALL, COL_MONITOR_ID + "=" + id, null, null, null, null);
        if (cursor.moveToNext()) {
            m.id = cursor.getInt(0);
            m.username = cursor.getString(1);
            m.remark_name = cursor.getString(2);
            m.icon = cursor.getInt(3);
            m.state = cursor.getInt(4);
            m.identify = cursor.getInt(5);
            m.identify = cursor.getInt(5);
            m.bandstate=cursor.getInt(6);
            m.safe=cursor.getInt(7);
            m.heart_rate=cursor.getInt(8);
            m.location_msg=cursor.getString(9);
        }
        cursor.close();
        return m;
    }

    public MyLocation getLocation(int monitor_id) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LATITUDE, COL_LONGITUDE, COL_TIME}, COL_MONITOR_ID + "=" + monitor_id, null, null, null, null);
        double d1 = 0;
        double d2 = 2;
        double l3 = 0;
        if (cursor.moveToNext()) {
            String mlat = cursor.getString(0);
            String mlong = cursor.getString(1);
            String mtime = cursor.getString(2);
            d1 = Double.parseDouble(mlat);
            d2 = Double.parseDouble(mlong);
            l3 = Double.parseDouble(mtime);
        }

        MyLocation location = new MyLocation(d1, d2, l3);

        return location;
    }

    /**
     * 更新关系方所有状态
     *
     * @param monitorList
     */
    public void updateState(ArrayList<Monitor> monitorList) {
        if (monitorList == null)
            return;

        for (int i = 0; i < monitorList.size(); i++) {
            Monitor monitor = monitorList.get(i);
            ContentValues values = new ContentValues();
            values.put(COL_STATE, monitor.state);
            Log.d(Config.TAG, "更新关系方状态:" + monitor.toString());
            db.update(TABLE_NAME, values, COL_MONITOR_ID + "=" + monitor.id, null);
        }
    }

    /**
     * 更改关系客户端状态
     */
    public void changeRelateState(int id, int tag) {
        int state = 0;
        if (tag == MessageTag.ONLINE)
            state = Config.ONLINE_STATE;
        else
            state = Config.NOT_ONLINE_STATE;
        ContentValues values = new ContentValues();
        values.put(COL_STATE, state);
        db.update(TABLE_NAME, values, COL_MONITOR_ID + "=" + id, null);
    }

    /**
     * 改变被监护放的手办状态信息
     * @param id
     * @param bandState
     */
    public void alterBandState(int id, BandState bandState) {
        ContentValues values=new ContentValues();
        values.put(COL_BANDSTATE,bandState.state);
        db.update(TABLE_NAME,values,COL_MONITOR_ID+"="+id,null);

    }

    /**
     * 添加心跳信息
     * @param id
     * @param heartData
     */
    public void updateHeart(int id, HeartData heartData) {
        ContentValues values=new ContentValues();
        values.put(COL_HEART_RATE,heartData.data);
        db.update(TABLE_NAME,values,COL_MONITOR_ID+"="+id,null);
    }

    /**
     * 修改位置安全信息
     * @param id
     * @param safe
     */
    public void updateSafe(int id, int safe) {
        ContentValues values=new ContentValues();
        values.put(COL_SAFE,safe);
        db.update(TABLE_NAME,values,COL_MONITOR_ID+"="+id,null);
    }

    public void updateLocationMessage(int id, ReverseGeoCodeResult.AddressComponent mComponent) {
        ContentValues values=new ContentValues();
        values.put(COL_LOCATION_MSG,mComponent.city+mComponent.street);
        db.update(TABLE_NAME,values,COL_MONITOR_ID+"="+id,null);
    }
}
