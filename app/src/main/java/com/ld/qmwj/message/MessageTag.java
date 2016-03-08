package com.ld.qmwj.message;

/**
 * 消息标识
 * @author zsg
 *
 */
public class MessageTag {

    //心跳包标识
    public static final int	HEART_MSG=0;

    //登陆请求和响应标识
    public static final int LOGIN_REQ = 1;
    public static final int LOGIN_RES = 2;

    //刷新列表标识
    public static final int REFRESHLIST_REQ=3;
    public static final int REFRESHLIST_RES=4;

    //请求位置标识
    public static final int LOCATION_REQ=5;
    public static final int LOCATION_RES=6;


}

