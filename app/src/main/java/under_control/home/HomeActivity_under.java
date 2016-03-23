package under_control.home;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.dao.AuthDao;
import com.ld.qmwj.model.Contacts;
import com.ld.qmwj.model.User;

import java.lang.reflect.Array;
import java.util.ArrayList;

import under_control.home.service.FuctionService;
import under_control.home.service.LocationService;

public class HomeActivity_under extends AppCompatActivity {
    MyApplication myApplication;
    User user;
    private BottomNavigationBar bottomNavigationBar;        //底部导航
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        myApplication=MyApplication.getInstance();
        initData();
        //开启服务和广播
        openService();
        initBottom();

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

}
