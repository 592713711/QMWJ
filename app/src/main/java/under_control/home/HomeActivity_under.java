package under_control.home;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.CursorLoader;
import android.content.Intent;

import android.database.Cursor;
import android.os.Build;

import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.dao.AuthDao;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.PhoneUtils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import control.msg.MsgFragment;
import under_control.home.service.FuctionService;
import under_control.home.service.LocationService;

public class HomeActivity_under extends AppCompatActivity {
    MyApplication myApplication;
    User user;
    private BottomNavigationBar bottomNavigationBar;        //底部导航
    private MsgFragment msgFragment;
    private PhoneFragment phoneFragment;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    private TextView title_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        initWindow();
        myApplication=MyApplication.getInstance();
        initView();
        initData();
        //开启服务和广播
        openService();
        initBottom();


    }

    private void initView() {
        title_text= (TextView) findViewById(R.id.title);
    }

    /**
     * 初始化底部导航栏
     */
    private void initBottom() {

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom);


        bottomNavigationBar
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);

        bottomNavigationBar
                .setMode(BottomNavigationBar.MODE_DEFAULT);

        bottomNavigationBar
                .setActiveColor("#29b6f6")
                .setInActiveColor("#a0a0a0")
                .setBarBackgroundColor("#fafafa");

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.msg_btn_1, "信息"))
                .addItem(new BottomNavigationItem(R.drawable.map_btn_1, "定位"))
                .addItem(new BottomNavigationItem(R.drawable.phone_btn_1, "手机"))
                .initialise();


        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                changeFragment(position);

            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
            }
        });
    }

    private void initData() {
        user=myApplication.getSpUtil().getUser();

       // mapFragment = new MapFragment(monitor, this);
        ArrayList<Monitor> monitors=MyApplication.getInstance().getRelateDao().getList();
        msgFragment = new MsgFragment(monitors.get(0),this);
        phoneFragment=new PhoneFragment(this);
       // phoneFragment = new PhoneFragment(monitor, this);
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
      //  transaction.add(R.id.content, mapFragment);
        transaction.add(R.id.content, msgFragment);
        transaction.add(R.id.content, phoneFragment);
        transaction.commit();

        changeFragment(0);
    }

    /**
     * 根据选择的按钮 切换显示的碎片
     *
     * @param position
     */
    private void changeFragment(int position) {
        transaction = fm.beginTransaction();
        transaction.hide(msgFragment);
       // transaction.hide(mapFragment);
        transaction.hide(phoneFragment);
        switch (position) {
            case 0:
                title_text.setText("消息");
                transaction.show(msgFragment);
                transaction.commit();
                break;
            case 2:
                title_text.setText("功能");
                transaction.show(phoneFragment);
                transaction.commit();
                break;
        }
    }

    private void openService() {
        AuthDao authDao=myApplication.getAuthDao();

        //位置服务
        int loction_auth=authDao.getAuthById(AuthDao.COL_LOCATION_AUTH,user.id);
        if(loction_auth== Config.AUTH_OPEN){
            //开启服务
            //判断服务是否开启
            if(!isServiceRunning("under_control.home.service.LocationService")){
                //若服务未开启  就开启服务
                Log.d(Config.TAG,"服务未开启  执行开启位置服务");
                Intent intent=new Intent(this,LocationService.class);
                startService(intent);
            }
        }else if(loction_auth==Config.AUTH_CLOSE){
            //关闭服务
            if(isServiceRunning("under_control.home.service.LocationService")){
                //若服务已开启  就关闭服务
                Log.d(Config.TAG,"服务已开启  关闭位置");
                Intent intent=new Intent(this,LocationService.class);
                stopService(intent);
            }
        }

        // 启动功能服务
        if(!isServiceRunning("under_control.home.service.FuctionService")){
            //若服务未开启  就开启服务
            Log.d(Config.TAG,"服务未开启  执行开启功能服务");
            Intent intent=new Intent(this,FuctionService.class);
            startService(intent);
        }


    }

    /**
     * 判断服务是否开启
     * @return
     */
    private boolean isServiceRunning(String servicename) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (servicename.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    /**
     * 初始化通知栏颜色
     */
    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.statusbar_bg);//通知栏所需颜色

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


}
