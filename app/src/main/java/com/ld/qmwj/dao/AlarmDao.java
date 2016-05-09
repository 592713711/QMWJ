package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ld.qmwj.model.Alarm;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/5/6.
 */
public class AlarmDao {
    //列名
    private static final String TABLE_NAME = "alarm";      //表名
    public static final String COL_MONITOR_ID = "monitor_id";    //对象id
    public static final String COL_ALARMTIME = "alarm_time";    //闹钟时间
    public static final String COL_ALARMHINT = "alarm_hint";    //闹钟备注
    public static final String COL_OPEN = "isopen";   //开关  0关闭，1开启
    public static final String COL_POSITION = "position";     //闹钟位置 为了区别各个闹钟主键

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s integer,%s text,%s integer,%s integer PRIMARY KEY)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_ALARMTIME,
            COL_ALARMHINT,
            COL_OPEN,
            COL_POSITION
    );

    public static final String[] ALLCOL = {COL_ALARMTIME, COL_ALARMHINT, COL_OPEN, COL_POSITION};

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    private SQLiteDatabase db;
    private DBHelper helper;

    public AlarmDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 插入闹钟
     *
     * @param id
     * @param alarm
     */
    public void insertAlarm(int id, Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COL_MONITOR_ID, id);
        values.put(COL_ALARMTIME, alarm.alarm_time);
        values.put(COL_ALARMHINT, alarm.alarm_hint);
        values.put(COL_OPEN, alarm.isopen);
        db.insert(TABLE_NAME, null, values);
    }

    public void insertAlarm2(int id, Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COL_MONITOR_ID, id);
        values.put(COL_ALARMTIME, alarm.alarm_time);
        values.put(COL_ALARMHINT, alarm.alarm_hint);
        values.put(COL_OPEN, alarm.isopen);
        values.put(COL_POSITION, alarm.position);
        db.insert(TABLE_NAME, null, values);
    }

    public ArrayList<Alarm> getAlarmList(int id) {
        ArrayList<Alarm> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id, null, null, null, null);
        while (cursor.moveToNext()) {
            Alarm alarm = new Alarm();
            alarm.alarm_time = cursor.getLong(0);
            alarm.alarm_hint = cursor.getString(1);
            alarm.isopen = cursor.getInt(2);
            alarm.position = cursor.getInt(3);
            list.add(alarm);
        }
        return list;
    }

    public ArrayList<Alarm> getOpenAlarmList(int id) {
        ArrayList<Alarm> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id + " and " + COL_OPEN + "=" + Alarm.ON, null, null, null, null);
        while (cursor.moveToNext()) {
            Alarm alarm = new Alarm();
            alarm.alarm_time = cursor.getLong(0);
            alarm.alarm_hint = cursor.getString(1);
            alarm.isopen = cursor.getInt(2);
            alarm.position = cursor.getInt(3);
            list.add(alarm);
        }
        return list;
    }


    public void deleteAlarm(int id, Alarm alarm) {
        db.delete(TABLE_NAME, COL_POSITION + "=" + alarm.position, null);
    }

    public void updateAlarm(int id, Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COL_ALARMTIME, alarm.alarm_time);
        values.put(COL_ALARMHINT, alarm.alarm_hint);
        values.put(COL_OPEN, alarm.isopen);
        db.update(TABLE_NAME, values, COL_POSITION + "=" + alarm.position, null);
    }

    /**
     * 得到最新添加的闹钟
     *
     * @param id
     */
    public Alarm getAlarmLast(int id) {
        Alarm alarm = new Alarm();
        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id, null, null, null, COL_POSITION + " desc");
        if (cursor.moveToNext()) {
            alarm.alarm_time = cursor.getLong(0);
            alarm.alarm_hint = cursor.getString(1);
            alarm.isopen = cursor.getInt(2);
            alarm.position = cursor.getInt(3);
        }
        return alarm;
    }
}
