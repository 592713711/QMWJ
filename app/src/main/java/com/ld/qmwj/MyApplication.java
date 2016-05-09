package com.ld.qmwj;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ld.qmwj.client.ClientService;
import com.ld.qmwj.dao.AlarmDao;
import com.ld.qmwj.dao.AuthDao;
import com.ld.qmwj.dao.BlacklistDao;
import com.ld.qmwj.dao.CacheDao;
import com.ld.qmwj.dao.CallPhoneDao;
import com.ld.qmwj.dao.HeartDao;
import com.ld.qmwj.dao.LinkManDao;
import com.ld.qmwj.dao.MessageDao;
import com.ld.qmwj.dao.RelateDao;
import com.ld.qmwj.dao.RouteDao;
import com.ld.qmwj.dao.SmsDao;
import com.ld.qmwj.util.ContactUtil;
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
    private RouteDao routeDao;
    private AuthDao authDao;
    private CacheDao cacheDao;
    private CallPhoneDao callPhoneDao;
    private ContactUtil contactUtil;
    private LinkManDao linkManDao;
    private MessageDao messageDao;
    private SmsDao smsDao;
    private HeartDao heartDao;
    private BlacklistDao blacklistDao;
    private AlarmDao alarmDao;

    public static final String SP_FILE_NAME = "user_sp";

    public synchronized static MyApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //比活动创建时启动服务要先执行
        // 注册科大讯飞服务
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=572efe27");
        startService(new Intent(this, ClientService.class));
        mApplication = this;
    }


    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();
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
            sendMsgUtil = new SendMessageUtil();
        return sendMsgUtil;
    }

    public synchronized RelateDao getRelateDao() {
        if (relateDao == null)
            relateDao = new RelateDao(this);
        return relateDao;
    }

    public synchronized RouteDao getRouteDao() {
        if (routeDao == null)
            routeDao = new RouteDao(this);
        return routeDao;
    }

    public synchronized AuthDao getAuthDao() {
        if (authDao == null)
            authDao = new AuthDao(this);
        return authDao;
    }


    public synchronized CacheDao getCacheDao() {
        if (cacheDao == null)
            cacheDao = new CacheDao(this);
        return cacheDao;
    }

    public synchronized CallPhoneDao getCallPhoneDao() {
        if (callPhoneDao == null)
            callPhoneDao = new CallPhoneDao(this);
        return callPhoneDao;
    }

    public synchronized ContactUtil getContactUtil() {
        if (contactUtil == null)
            contactUtil = new ContactUtil(this);
        return contactUtil;
    }


    public synchronized LinkManDao getLinkManDao() {
        if (linkManDao == null)
            linkManDao = new LinkManDao(this);
        return linkManDao;
    }

    public synchronized MessageDao getMessageDao() {
        if (messageDao == null)
            messageDao = new MessageDao(this);
        return messageDao;
    }

    public synchronized SmsDao getSmsDao() {
        if (smsDao == null)
            smsDao = new SmsDao(this);
        return smsDao;
    }

    public synchronized HeartDao getHeartDao() {
        if (heartDao == null)
            heartDao = new HeartDao(this);
        return heartDao;
    }

    public synchronized BlacklistDao getBlacklistDao() {
        if (blacklistDao == null)
            blacklistDao = new BlacklistDao(this);
        return blacklistDao;
    }

    public synchronized AlarmDao getAlarmtDao() {
        if (alarmDao == null)
            alarmDao = new AlarmDao(this);
        return alarmDao;
    }


}
