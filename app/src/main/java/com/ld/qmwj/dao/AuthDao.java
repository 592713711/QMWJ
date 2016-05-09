package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.model.Authority;
import com.ld.qmwj.model.LocationRange;
import com.ld.qmwj.model.Monitor;

import java.util.ArrayList;

/**
 * 操作权限数据库
 * Created by zsg on 2016/3/15.
 */
public class AuthDao {
    //列名
    private static final String TABLE_NAME = "authority";      //表名
    public static final String COL_MONITOR_ID = "monitor_id";    //权限对象id
    public static final String COL_LOCATION_AUTH = "location_auth";    //位置权限
    public static final String COL_LOCATION_RANGE1 = "location_range1";    //位置范围
    public static final String COL_LOCATION_RANGE2 = "location_range2";    //位置范围
    public static final String COL_LOCATION_RANGE3 = "location_range3";    //位置范围
    public static final String COL_SMS_AUTH = "sms_auth";      //短信权限
    public static final String COL_CALL_AUTH = "call_auth";        //通话
    public static final String COL_BLUETOOTH_AUTH = "bluetooth_auth";  //蓝牙随行
    public static final String COL_HEALTH_AUTH = "health_auth";        //健康
    public static final String COL_CLOCK_AUTH = "clock_auth";          //闹钟
    public static final String COL_SOUND_AUTH = "sound_auth";          //声音
    public static final String COL_CLOSE_AUTH = "close_auth";          //关机
    public static final String COL_LINKMAN_AUTH = "Linkman_auth";          //联系人

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s integer,%s text,%s text,%s text,%s integer,%s integer,%s integer,%s integer,%s integer,%s integer,%s integer,%s integer)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_LOCATION_AUTH,
            COL_LOCATION_RANGE1,
            COL_LOCATION_RANGE2,
            COL_LOCATION_RANGE3,
            COL_SMS_AUTH,
            COL_CALL_AUTH,
            COL_BLUETOOTH_AUTH,
            COL_HEALTH_AUTH,
            COL_CLOCK_AUTH,
            COL_SOUND_AUTH,
            COL_CLOSE_AUTH,
            COL_LINKMAN_AUTH
    );

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    private SQLiteDatabase db;
    private DBHelper helper;

    public AuthDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 初始化关系方权限
     *
     * @param id
     */
    public void initAuth(int id) {
        //清除所有数据
        db.delete(TABLE_NAME, null, null);
        ContentValues values = new ContentValues();
        values.put(COL_MONITOR_ID, id);
        db.insert(TABLE_NAME, null, values);
    }

    /**
     * 初始化关系方权限
     */
    public void initAuth(int id, Authority authority) {
        //清除该id数据
        db.delete(TABLE_NAME, COL_MONITOR_ID + "=" + id, null);
        ContentValues values = new ContentValues();
        values.put(COL_MONITOR_ID, id);
        values.put(COL_LOCATION_AUTH, authority.location_auth);
        if (authority.loation_range1 != null)
            values.put(COL_LOCATION_RANGE1, authority.loation_range1);
        if (authority.loation_range2 != null)
            values.put(COL_LOCATION_RANGE2, authority.loation_range2);
        if (authority.loation_range3 != null)
            values.put(COL_LOCATION_RANGE3, authority.loation_range3);
        values.put(COL_CALL_AUTH, authority.call_auth);
        values.put(COL_SMS_AUTH, authority.sms_auth);
        db.insert(TABLE_NAME, null, values);
    }


    /**
     * 更新用户权限
     *
     * @param col_authName 权限列名
     * @param auth         权限  允许
     * @param monitor_id
     */
    public void updateAuth(String col_authName, int auth, int monitor_id) {
        ContentValues values = new ContentValues();
        values.put(col_authName, auth);
        db.update(TABLE_NAME, values, COL_MONITOR_ID + "=" + monitor_id, null);
    }

    /**
     * 得到 指定用户 的指定权限
     *
     * @param col_authName
     * @param monitor_id
     * @return
     */
    public int getAuthById(String col_authName, int monitor_id) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{col_authName}, COL_MONITOR_ID + "=" + monitor_id, null, null, null, null);
        int auth = Config.AUTH_OPEN;
        if (cursor.moveToNext()) {
            auth = cursor.getInt(0);
        }
        cursor.close();
        return auth;
    }


    /**
     * 更新安全区域
     *
     * @param monitor_id
     * @param locationRange
     */
    public void updateLacationRange(int monitor_id, LocationRange locationRange) {
        String col_range = COL_LOCATION_RANGE1;
        switch (locationRange.rang_pos) {
            case 0:
                col_range = COL_LOCATION_RANGE1;
                break;
            case 1:
                col_range = COL_LOCATION_RANGE2;
                break;
            case 2:
                col_range = COL_LOCATION_RANGE3;
                break;
        }
        ContentValues values = new ContentValues();
        values.put(col_range, MyApplication.getInstance().getGson().toJson(locationRange));
        db.update(TABLE_NAME, values, COL_MONITOR_ID + "=" + monitor_id, null);
    }

    public ArrayList<LocationRange> getLocationRanges(int monitor_id) {
        ArrayList<LocationRange> datas = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCATION_RANGE1, COL_LOCATION_RANGE2, COL_LOCATION_RANGE3},
                COL_MONITOR_ID + "=" + monitor_id, null, null, null, null);
        if (cursor.moveToNext()) {
            if (cursor.getString(0) != null) {
                LocationRange locationRange = MyApplication.getInstance().getGson().fromJson(cursor.getString(0), LocationRange.class);
                datas.add(locationRange);
            }

            if (cursor.getString(1) != null) {
                LocationRange locationRange = MyApplication.getInstance().getGson().fromJson(cursor.getString(1), LocationRange.class);
                datas.add(locationRange);
            }

            if (cursor.getString(2) != null) {
                LocationRange locationRange = MyApplication.getInstance().getGson().fromJson(cursor.getString(2), LocationRange.class);
                datas.add(locationRange);
            }
        }

        return datas;

    }

    public int getEmptyPosition(int monitor_id) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCATION_RANGE1, COL_LOCATION_RANGE2, COL_LOCATION_RANGE3},
                COL_MONITOR_ID + "=" + monitor_id, null, null, null, null);
        if (cursor.moveToNext()) {
            if (cursor.getString(0) == null) {
                return 0;
            }

            if (cursor.getString(1) == null) {
                return 1;
            }

            if (cursor.getString(2) == null) {
                return 2;
            }

        }
        return -1;
    }


    /**
     * 删除指定位置的安全信息
     *
     * @param id
     * @param rang_pos
     */
    public void removeLocationRange(int id, int rang_pos) {
        ContentValues values = new ContentValues();
        String col_range = COL_LOCATION_RANGE1;
        switch (rang_pos) {
            case 0:
                col_range = COL_LOCATION_RANGE1;
                break;
            case 1:
                col_range = COL_LOCATION_RANGE2;
                break;
            case 2:
                col_range = COL_LOCATION_RANGE3;
                break;
        }
        String sql = "update " + TABLE_NAME + " set " + col_range + "=null where monitor_id=" + id;
        db.execSQL(sql);
    }
}
