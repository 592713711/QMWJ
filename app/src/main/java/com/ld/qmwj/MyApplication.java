package com.ld.qmwj;

import android.app.Application;
import android.content.Intent;

import com.google.gson.Gson;
import com.ld.qmwj.client.ClientService;
import com.ld.qmwj.dao.RelateDao;
import com.ld.qmwj.util.SendMessageUtil;
import com.ld.qmwj.util.SharePreferenceUtil;

/**
 * 程序的Application
 * Created by zzc on 2015/11/24.
 */
public class MyApplication extends Application {

    private static MyApplication mApplication;
    private SharePreferenceUtil mSpUtil;
    private SendMessageUtil sendMsgUtil;
    private Gson mGson;
    private RelateDao relateDao;

    public static final String SP_FILE_NAME = "user_sp";

    public synchronized static MyApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //比活动创建时启动服务要先执行
        startService(new Intent(this, ClientService.class));
        mApplication = this;
    }

    public synchronized SharePreferenceUtil getSpUtil() {

        if (mSpUtil == null)
            mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
        return mSpUtil;
    }

    public synchronized Gson getGson() {
        if (mGson == null)
            mGson = new Gson();
        return mGson;
    }

    public synchronized SendMessageUtil getSendMsgUtil() {
        if (sendMsgUtil == null)
           sendMsgUtil=new SendMessageUtil();
        return sendMsgUtil;
    }

    public synchronized RelateDao getRelateDao() {
        if (relateDao == null)
            relateDao=new RelateDao(this);
        return relateDao;
    }

}
