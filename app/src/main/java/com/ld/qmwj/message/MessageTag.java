package com.ld.qmwj.message;

/**
 * 消息标识
 *
 * @author zsg
 */
public class MessageTag {

    //心跳包标识
    public static final int HEART_MSG = 0;

    //登陆请求和响应标识
    public static final int LOGIN_REQ = 1;
    public static final int LOGIN_RES = 2;

    //刷新列表标识
    public static final int REFRESHLIST_REQ = 3;
    public static final int REFRESHLIST_RES = 4;

    //请求位置标识
    public static final int LOCATION_REQ = 5;
    public static final int LOCATION_RES = 6;

    // 用户在线/离线标识
    public static final int ONLINE = 7;
    public static final int NOT_ONLINE = 8;

    //请求历史路线标识
    public static final int OLDWAY_REQ_ALL = 9;
    public static final int OLDWAY_REQ = 10;
    public static final int OLDWAY_RES = 11;

    //通话记录响应
    public static final int CALLPHONE_RES = 12;


    //请求对方所有联系人
    public static final int LINKMAN_REQ = 13;
    public static final int LINKMAN_RES = 14;


    //聊天信息
    public static final int CHAT_REQ = 16;

    //短信响应
    public static final int SMS_RES = 17;

    //请求手环状态
    public static final int BANDSTATE_REQ = 18;
    //手环状态响应
    public static final int BANDSTATE_RES = 19;
    //测试心跳请求
    public static final int DOHEART_REQ = 20;
    //测试心跳响应
    public static final int DOHEART_RES = 21;
    //心跳数据请求
    public static final int HEARTDATA_REQ = 22;
    //心跳数据响应
    public static final int HEARTDATA_RES = 23;

    //权限
    public static final int AUTH_MSG = 24;
    public static final int AUTH_CHANGE = 25;
    //安全范围更改请求
    public static final int LOCATIONRANGE_REQ = 26;
    //删除安全区域请求
    public static final int REMOVERANGE_REQ = 27;

    //路线消息请求
    public static final int ROUTEWAY_REQ = 28;

    //添加黑名单请求
    public static final int ADDBLACKPHONE_REQ = 29;
    //删除黑名单请求
    public static final int DELETEBLACKPHONE_REQ = 30;

    //添加联系人
    public static final int ADDLINKMAN_REQ = 15;
    //删除联系人
    public static final int DELETELINKMAN_REQ=31;
    //修改联系人
    public static final int UPDATELINKMAN_REQ=32;

    //添加闹钟
    public static final int ADDALARM_REQ = 33;
    //删除闹钟
    public static final int DELETEALARM_REQ = 34;
    //更新闹钟
    public static final int UPDATEALARM_REQ = 35;

}

