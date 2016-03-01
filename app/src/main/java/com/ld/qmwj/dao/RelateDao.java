package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ld.qmwj.Config;
import com.ld.qmwj.model.Monitor;

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
    private static final String COL_ICON = "icon";          //头像id
    private static final String[] COL_ALL = {COL_MONITOR_ID, COL_MONITOR_NAME, COL_REMARK_NAME, COL_ICON};
    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s text,%s text,%s integer)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_MONITOR_NAME,
            COL_REMARK_NAME,
            COL_ICON
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
            values.put(COL_ICON,0);
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
            m.username=cursor.getString(1);
            m.remark_name=cursor.getString(2);
            m.icon=cursor.getInt(3);
            list.add(m);

        }
        cursor.close();
        return list;
    }



}
