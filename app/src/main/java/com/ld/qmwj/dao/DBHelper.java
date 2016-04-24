package com.ld.qmwj.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ld.qmwj.Config;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "qmwj.db";

    public DBHelper(Context context) {
        super(context, DBNAME, null, 10);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RelateDao.SQL_CREATE_TABLE);
        db.execSQL(AuthDao.SQL_CREATE_TABLE);
        db.execSQL(CacheDao.SQL_CREATE_TABLE);
        db.execSQL(CallPhoneDao.SQL_CREATE_TABLE);
        db.execSQL(MessageDao.SQL_CREATE_TABLE);
        db.execSQL(LinkManDao.SQL_CREATE_TABLE);
        db.execSQL(SmsDao.SQL_CREATE_TABLE);
        db.execSQL(HeartDao.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SmsDao.SQL_DROP_TABLE);
        //db.execSQL(RelateDao.SQL_DROP_TABLE);
        //db.execSQL(HeartDao.SQL_DROP_TABLE);
       //db.execSQL(AuthDao.SQL_DROP_TABLE);
        onCreate(db);
    }
}
