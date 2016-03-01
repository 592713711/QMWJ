package com.ld.qmwj.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 储存消息的表
 * Created by zsg on 2016/2/21.
 */
public class MessageDao {
    private static final String COL_MONITOR_ID = "monitor_id";      // 被监护人/监护人的id
    private static final String COL_MESSAGE = "message";            //消息内容
    private static final String COL_TYPE = "type";       //消息类型   0:to  1:from  2：系统提醒
    private static final String COL_ISREAD = "isRead";                   //是否已读  0:未读  1：已读
    private static final String COL_TIME = "time";      //时间


    private DBHelper helper;
    private SQLiteDatabase mDb;

    public MessageDao(Context context) {
        helper = new DBHelper(context);
        mDb = helper.getWritableDatabase();
    }

    /**
     *为每个关联者创建一个表  表名为 _userid
     * @param userId
     */
    private void createTable(String userId) {
        mDb.execSQL("CREATE table IF NOT EXISTS _" + userId
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " //
                + COL_MONITOR_ID + " integer, " //
                + COL_MESSAGE + " text, "//
                + COL_TYPE + " integer ,"//
                + COL_MESSAGE + " text , " //
                + COL_ISREAD + " text , " //
                + COL_TIME + " text ); ");//
    }
}
