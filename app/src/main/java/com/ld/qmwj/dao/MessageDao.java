package com.ld.qmwj.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.model.chatmessage.ChatMessage;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.model.chatmessage.PhoneStateMsg;
import com.ld.qmwj.model.chatmessage.SimpleMsg;
import com.ld.qmwj.model.chatmessage.SmsMsg;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 储存消息的表
 * 储存的message是不消息子类不包括父类的JSON字符串，不包括聊天消息（聊天消息直接储存聊天内容）
 * Created by zsg on 2016/2/21.
 */
public class MessageDao {
    private static final String TABLE_NAME = "monitor_msg";      //表名
    private static final String COL_MONITOR_ID = "monitor_id";      // 被监护人/监护人的id
    private static final String COL_MESSAGE = "message";            //消息内容
    private static final String COL_IS_COMING = "is_coming";    //0:to发送  1:from来自
    private static final String COL_TYPE = "type";              //消息类型  1警告  2短信  3电话
    private static final String COL_ISREAD = "isRead";              //是否已读  0:未读  1：已读
    private static final String COL_TIME = "time";      //时间
    private Gson gson;

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer,%s text,%s integer,%s text,%s text,%s text)",
            TABLE_NAME,
            COL_MONITOR_ID,
            COL_MESSAGE,
            COL_IS_COMING,
            COL_TYPE,
            COL_ISREAD,
            COL_TIME
    );

    public static final String[] ALL_COL={COL_MESSAGE,COL_IS_COMING,COL_TYPE,COL_TIME};


    private DBHelper helper;
    private SQLiteDatabase mDb;

    public MessageDao(Context context) {
        helper = new DBHelper(context);
        mDb = helper.getWritableDatabase();
        gson= MyApplication.getInstance().getGson();
    }

    /**
     *
     * @param monitorId
     * @param chatMessage
     * @param msg  对应消息的json字符串
     */
    public void addMessage(int monitorId,ChatMessage chatMessage,String msg){
        ContentValues values=new ContentValues();
        values.put(COL_MONITOR_ID, monitorId);
        values.put(COL_IS_COMING, chatMessage.is_coming);
        if(chatMessage.is_coming==Config.FROM_MSG)
            values.put(COL_ISREAD,Config.MSG_UNREAD);
        else
            values.put(COL_ISREAD,Config.MSG_READ);
        values.put(COL_TIME,chatMessage.time+"");
        values.put(COL_MESSAGE,msg);
        values.put(COL_TYPE,chatMessage.msg_type);
        mDb.insert(TABLE_NAME, null, values);
        Log.d(Config.TAG,"插入数据成功");

    }

    /**
     * 查询指定id的记录
     * @param monitorId
     * @param currentPage   查询的页数
     * @param pageSize      每页大小
     * @return
     */
    public ArrayList<ChatMessage>  getMsgById(int monitorId, int currentPage, int pageSize){
        ArrayList<ChatMessage> list=new ArrayList<>();
        int start = (currentPage - 1) * pageSize;
        //  Limit  从 A条数据开始加载B条数据
        Cursor cursor=mDb.query(TABLE_NAME, ALL_COL, COL_MONITOR_ID + "=" + monitorId, null, null, null
                , COL_TIME + " desc",start+","+pageSize);
        while(cursor.moveToNext()){
            String msg=cursor.getString(0);
            int isComing=cursor.getInt(1);
            int msg_type=cursor.getInt(2);
            long time=Long.parseLong(cursor.getString(3));
            ChatMessage chatMessage=null;

            //根据类型创建消息对象
            switch (msg_type){
                case Config.CHAT_MSG:
                    SimpleMsg simpleMsg=new SimpleMsg();
                    simpleMsg.msg=msg;
                    chatMessage=simpleMsg;
                    break;
                case Config.CALL_MSG:
                    PhoneStateMsg phoneStateMsg=gson.fromJson(msg,PhoneStateMsg.class);
                    chatMessage=phoneStateMsg;
                    break;
                case Config.SMS_MSG:
                    SmsMsg smsMsg=gson.fromJson(msg,SmsMsg.class);
                    chatMessage=smsMsg;
                    break;
                case Config.MAPWAY_MSG:
                    MapWayMsg mapWayMsg=gson.fromJson(msg,MapWayMsg.class);
                    chatMessage=mapWayMsg;
                    break;
                case Config.WARN_MSG:
                    break;

            }
            chatMessage.is_coming=isComing;
            chatMessage.time=time;
            list.add(chatMessage);
        }

        //按原顺序的反向排序
        Collections.reverse(list);
        return list;
    }


    /**
     * 得到单个关系方 未读消息个数
     * @param monitorId
     * @return
     */
    private int getUnreadedMsgsCountByUserId(int monitorId) {
        //查找该id未读消息的个数
        String sql = "select count(*) as count from " + TABLE_NAME + " where "
                + COL_IS_COMING + " = 1 and "+COL_MONITOR_ID+"="+monitorId+" and "+COL_ISREAD+"=0";
        Cursor c = mDb.rawQuery(sql, null);
        int count = 0;
        if (c.moveToNext())
            count = c.getInt(c.getColumnIndex("count"));
        c.close();
        return count;
    }

    /**
     * 更新已读标志位
     */
    public void updateReaded(int monitorId)
    {
        ContentValues values=new ContentValues();
        values.put(COL_ISREAD, Config.MSG_READ);
        mDb.update(TABLE_NAME, values
                , COL_MONITOR_ID + "=" + monitorId + "and " + COL_ISREAD + "=" + Config.MSG_UNREAD, null);

    }

}
