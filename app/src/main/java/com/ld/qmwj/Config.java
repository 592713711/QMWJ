package com.ld.qmwj;


public interface Config {
    public final static String serverIP="192.168.191.1";
  //  public final static String serverIP="172.30.18.211";      //服务器地址
    public final static int port=10245;       //服务器端口
    public final static String TAG="QMWJ";

    // 身份标识
    public final static int GUARDIAN_STATUS = 0;	//监护
    public final static int NOT_GUARDIAN_STATUS = 1;	//被监护

    public final static int ONLINE_STATE=0;         //对方在线
    public final static int NOT_ONLINE_STATE=1;     //对方不在线

    public final static int AUTH_OPEN=0;        //权限开启
    public final static int AUTH_CLOSE=1;       //权限关闭

    //电话监控
    public final static int CALL_INTO=1;        //来电
    public final static int CALL_GOTO=2;        //拨出
    public final static int CALL_MISS=3;        //未接

    //短信监控
    public final static int SMS_INTO=1;     //接收短信
    public final static int SMS_GOTO=2;     //发送短信

    //是否已读
    public final static int MSG_UNREAD=0;       //未读
    public final static int MSG_READ=1;         //已读

    //消息  来自还是发送
    public final static int TO_MSG=0;         //发送消息
    public final static int FROM_MSG=1;       //对方来自消息    （聊天）

    //消息类型
    public final static int CHAT_MSG=0;
    public final static int WARN_MSG=1;       //对方警告消息     （超出警告区）
    public final static int CALL_MSG=2;       //监听电话提醒
    public final static int SMS_MSG=3;        //监听短信提醒
    public final static int MAPWAY_MSG=4;     //地图路线提醒




}
