package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ld.qmwj.model.HeartData;

import java.util.ArrayList;

/**
 * 心率记录数据库
 * Created by zsg on 2016/4/11.
 */
public class HeartDao {

    private static final String TABLE_NAME = "heartdata";      //表名
    public static final String COL_MONITOR_ID = "monitor_id";    //对象id
    public static final String COL_DATA = "heartdata";    //心跳数据
    public static final String COL_TIME = "time";    //测试时间  储存为Integer 读取时用Long读取  sqlite  integer可以存储64位


    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s integer,%s integer)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_DATA,
            COL_TIME
    );


    public static final String[] ALLCOL = {COL_DATA, COL_TIME};

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    private SQLiteDatabase db;
    private DBHelper helper;

    public HeartDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 插入心跳数据
     *
     * @param id
     * @param data
     */
    public void insertHeartData(int id, HeartData data) {
        ContentValues values = new ContentValues();
        values.put(COL_MONITOR_ID, id);
        values.put(COL_DATA, data.data);
        values.put(COL_TIME, data.time);
        db.insert(TABLE_NAME, null, values);
    }

    /**
     * 读取最近10次心跳数据
     *
     * @param id
     * @return
     */
    public ArrayList<HeartData> getHeartDataList(int id) {
        ArrayList<HeartData> list = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id, null, null, null, COL_TIME + " desc", "0,10");
        while (cursor.moveToNext()) {
            HeartData heartData = new HeartData();
            heartData.data = cursor.getInt(0);
            heartData.time = cursor.getLong(1);
            list.add(heartData);
        }

        return list;
    }

    /**
     * 得到所有数据
     *
     * @param id
     * @return
     */
    public ArrayList<HeartData> getAllHeartDataList(int id) {
        ArrayList<HeartData> list = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id, null, null, null, COL_TIME + " asc");
        while (cursor.moveToNext()) {
            HeartData heartData = new HeartData();
            heartData.data = cursor.getInt(0);
            heartData.time = cursor.getLong(1);
            list.add(heartData);
        }

        return list;
    }

    /**
     * 得到指定日期的所有数据   时间段  （当日凌晨 到 次日凌晨）
     *
     * @param id
     * @return
     */
    public ArrayList<HeartData> getAllHeartDataListByTime(int id, long fromtime, long totime) {
        ArrayList<HeartData> list = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, ALLCOL, COL_MONITOR_ID + "=" + id + " and " + COL_TIME + ">" + fromtime + " and " + COL_TIME + "<" + totime, null, null, null, COL_TIME + " asc");
        while (cursor.moveToNext()) {
            HeartData heartData = new HeartData();
            heartData.data = cursor.getInt(0);
            heartData.time = cursor.getLong(1);
            list.add(heartData);
        }

        return list;
    }


    /**
     * 更新心跳数据
     * 先时间删除区间中的数据 再将数据添加进去
     */
    public void updateHeartData(int id,ArrayList<HeartData> datas,long from_time,long to_time){
        //删除区间的数据
        deleteHeartData(id,from_time,to_time);

        for(HeartData data:datas){
            ContentValues values=new ContentValues();
            values.put(COL_MONITOR_ID, id);
            values.put(COL_DATA, data.data);
            values.put(COL_TIME, data.time);
            db.insert(TABLE_NAME, null, values);
        }
    }


    /**
     * 根据时间区间删除数据
     * @param from_time
     * @param to_time
     */
    public void deleteHeartData(int id,long from_time,long to_time){
        db.delete(TABLE_NAME,COL_MONITOR_ID + "=" + id + " and " + COL_TIME + ">" + from_time + " and " + COL_TIME + "<" + to_time,null);
    }

}
