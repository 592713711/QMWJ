package com.ld.qmwj;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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

import java.io.File;
import java.net.DatagramSocket;
import java.net.SocketException;

import control.phone.audio.AudioWrapper;

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
    private String recordPath;
    public BMapManager mBMapManager = null;

    private AudioWrapper audioWrapper;
    private DatagramSocket socket;

    private GeoCoder geoCoder;
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
        recordPath=android.os.Environment.getExternalStorageDirectory()+"/QMWG_Cache";
        //检查文件夹是否存在
        File file = new File(recordPath);
        if (!file.exists())
            file.mkdirs();
        recordPath+="/";

        initEngineManager(this);

    }
    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Log.d(Config.TAG, "授权错误");
            } else {
                Log.d(Config.TAG, "授权成功");
            }
        }
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(this, "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d(Config.TAG, "initEngineManager");
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

    public synchronized AudioWrapper getAudioWrapper() {
        if (audioWrapper == null)
            audioWrapper = new AudioWrapper();
        return audioWrapper;
    }

    /**
     * 得到外部储存语音的地址
     * @return
     */
    public String getRecordPath(){
        return recordPath;
    }

    public DatagramSocket getSocket(){
        if(socket==null){
            try {
                socket = new DatagramSocket(Config.CLIENT_RECORD_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return socket;
    }


}
