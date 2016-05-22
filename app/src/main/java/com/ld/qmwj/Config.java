package com.ld.qmwj;


public interface Config {
    //public final static String serverIP = "192.168.191.1";        //寝室服务器
    //public final static String serverIP = "192.168.232.157";      //实验室服务器地址
    public final static String serverIP = "119.29.143.22";          //腾讯云服务器
    public final static int port = 10245;       //服务器端口
    public final static String TAG = "QMWJ";

    //客户端  实时语音通信 端口号、
    public final static int CLIENT_RECORD_PORT = 11288;

    // 身份标识
    public final static int GUARDIAN_STATUS = 0;    //监护
    public final static int NOT_GUARDIAN_STATUS = 1;    //被监护

    //用户user相对于monitor的身份
    public final static int UNDER_GUARDIAN = 0;     //被监护
    public final static int MAIN_GUARDIAN = 1;    //主监护
    public final static int VICE_GUARDIAN_STATUS = 2;    //副监护

    public final static int ONLINE_STATE = 0;         //对方在线
    public final static int NOT_ONLINE_STATE = 1;     //对方不在线

    public final static int AUTH_OPEN = 0;        //权限开启
    public final static int AUTH_CLOSE = 1;       //权限关闭

    //电话监控
    public final static int CALL_INTO = 1;        //来电
    public final static int CALL_GOTO = 2;        //拨出
    public final static int CALL_MISS = 3;        //未接

    //短信监控
    public final static int SMS_INTO = 1;     //接收短信
    public final static int SMS_GOTO = 2;     //发送短信

    //是否已读
    public final static int MSG_UNREAD = 0;       //未读
    public final static int MSG_READ = 1;         //已读

    //消息  来自还是发送
    public final static int TO_MSG = 0;         //发送消息
    public final static int FROM_MSG = 1;       //对方来自消息    （聊天）

    //消息类型
    public final static int CHAT_MSG = 0;       //普通文字聊天信息
    public final static int WARN_MSG = 1;       //对方警告消息     （超出警告区）
    public final static int CALL_MSG = 2;       //监听电话提醒
    public final static int SMS_MSG = 3;        //监听短信提醒
    public final static int MAPWAY_MSG = 4;     //地图路线提醒
    public final static int RECORD_MSG = 5;     //语音聊天信息

    //安全区域范围
    public final static int MINRANG = 50;
    public final static int MAXRANG = 2000;

    //路线类型
    public final static int ROUTE_WALK = 0;     //步行
    public final static int ROUTE_DRIVE = 1;    //驾车
    public final static int ROUTE_BUS = 2;      //换乘

    //判断指令
    public static final String CONTACTS = "Contacts";
    //添加黑名单
    public static final String DOADD = "doAdd";
    //传递数据时用户名
    public static final String CONTACTS_NAME = "contactsName";
    //传递数据的手机号码
    public static final String CONTACTS_NUM = "contactsNum";
    //新建联系人
    public static final String DONEW = "doNew";
    //更新联系人
    public static final String DOUPDATE = "doUpdate";

    public static final int WEIZHI_LOCATION=0;      //未设置安全区域
    public static final int SAFE_LOCATION=1;
    public static final int UNSAFE_LOCATION=2;

}
