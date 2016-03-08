package com.ld.qmwj;


public interface Config {
    public final static String serverIP="192.168.191.1";      //服务器地址
    public final static int port=10245;       //服务器端口
    public final static String TAG="QMWJ";

    // 身份标识
    public final static int GUARDIAN_STATUS = 0;	//监护
    public final static int NOT_GUARDIAN_STATUS = 1;	//被监护
}
