package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ld.qmwj.model.BlackPhone;

import java.util.ArrayList;

/**
 * 黑名单表
 * Created by zsg on 2016/5/4.
 */
public class BlacklistDao {
    //列名
    private static final String TABLE_NAME = "blacklist";      //表名
    public static final String COL_MONITOR_ID = "monitor_id";    //对象id
    public static final String COL_BLACKPHONE = "black_phone";    //黑名单电话
    public static final String COL_ADDRESS = "address";    //黑名单归属地

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s text,%s text)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_BLACKPHONE,
            COL_ADDRESS
    );

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    public static final String[] ALLCOL = {COL_BLACKPHONE,COL_ADDRESS};

    private SQLiteDatabase db;
    private DBHelper helper;

    public BlacklistDao(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public void insertBlackPhone(int id, BlackPhone blackPhone) {
        ContentValues values=new ContentValues();
        values.put(COL_MONITOR_ID,id);
        values.put(COL_BLACKPHONE,blackPhone.phonenum);
        values.put(COL_ADDRESS,blackPhone.address);
        db.insert(TABLE_NAME,null,values);
    }

    public ArrayList<BlackPhone> getBlackPhone(int id){
        ArrayList<BlackPhone> list=new ArrayList<>();
        Cursor cursor=db.query(TABLE_NAME,ALLCOL,COL_MONITOR_ID+"="+id,null,null,null,null);
        while(cursor.moveToNext()){
            BlackPhone blackPhone=new BlackPhone();
            blackPhone.phonenum=cursor.getString(0);
            blackPhone.address=cursor.getString(1);
            list.add(blackPhone);
        }
        return list;
    }

    /**
     * 判断指定号码是否已经添加了
     */
    public boolean isAlreadyadd(int id,String phone){
        boolean b=false;
        Cursor cursor=db.query(TABLE_NAME,ALLCOL,COL_MONITOR_ID+"="+id+" and "+COL_BLACKPHONE+"="+"'"+phone+"'",null,null,null,null);
        if(cursor.moveToNext()){
            b=true;
        }
        return b;
    }


    /**
     * 删除指定黑名单号码
     */
    public void deleteBlackPhone(int id,String phone){
        db.delete(TABLE_NAME,COL_MONITOR_ID+"="+id+" and "+COL_BLACKPHONE+"="+"'"+phone+"'",null);
    }

}
