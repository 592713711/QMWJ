package control.home;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.util.HandlerUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import control.map.MapFragment;
import control.msg.MsgFragment;
import control.phone.PhoneFragment;

/**
 * 被监护方详细页面
 */
public class MonitorActivity extends AppCompatActivity {
    private Monitor monitor;
    private MapFragment mapFragment;
    private MsgFragment msgFragment;
    private PhoneFragment phoneFragment;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    private RelativeLayout hintLayout;      //提示布局
    private TextView hintMessage;           //提示内容
    private TextView title_text;            //标题
    private BottomNavigationBar bottomNavigationBar;        //底部导航


    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initView();
        initBottom();
        //订阅事件
        EventBus.getDefault().register(this);
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

    private void initView() {
        title_text = (TextView) findViewById(R.id.title);
        hintLayout = (RelativeLayout) findViewById(R.id.hint);
        hintMessage = (TextView) findViewById(R.id.hintmsg);
        updateHint();


        mapFragment = new MapFragment(monitor, this);
        msgFragment = new MsgFragment(monitor, this);
        phoneFragment = new PhoneFragment(monitor, this);
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.add(R.id.content, mapFragment);
        transaction.add(R.id.content, msgFragment);
        transaction.add(R.id.content, phoneFragment);
        transaction.commit();

        changeFragment(0);


    }


    private Boolean isStart = false;         //该活动是否在显示

    @Override
    protected void onStart() {
        super.onStart();
        isStart = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStart = false;
    }

    /**
     * 根据选择的按钮 切换显示的碎片
     *
     * @param position
     */
    private void changeFragment(int position) {
        transaction = fm.beginTransaction();
        transaction.hide(msgFragment);
        transaction.hide(mapFragment);
        transaction.hide(phoneFragment);
        switch (position) {
            case 0:
                title_text.setText(monitor.remark_name);
                transaction.show(msgFragment);
                transaction.commit();
                break;
            case 1:
                title_text.setText("位 置");
                transaction.show(mapFragment);
                transaction.commit();
                break;
            case 2:
                title_text.setText("管 理");
                transaction.show(phoneFragment);
                transaction.commit();
                break;
        }
    }

    public long lastShowUnlinetime = 0;       //最后一次显示不在线的时间

    /**
     * 控制提示框的显示与消失
     */
    public void updateHint() {
        //先判断是否已经连接上了服务器
        if (MsgHandle.getInstance().channel != null) {
            hintLayout.setVisibility(View.GONE);
        } else if (MsgHandle.getInstance().channel == null) {
            hintMessage.setText("与服务器断开连接，请检查网络");
            hintLayout.setVisibility(View.VISIBLE);
            return;
        }

        //判断对方是否在线
        if (monitor.state == Config.ONLINE_STATE) {
            hintLayout.setVisibility(View.GONE);
        } else if (monitor.state == Config.NOT_ONLINE_STATE) {
            hintMessage.setText("对方未连接");
            hintLayout.setVisibility(View.VISIBLE);
            if (mapFragment != null && mapFragment.waitDialog != null)
                //mapFragment.waitDialog.dismiss();
            if (isStart) {
                long nowtime = System.currentTimeMillis();
                if (nowtime - lastShowUnlinetime > 3000)
                    Toast.makeText(this, "对方网络问题，未能获得数据", Toast.LENGTH_SHORT).show();
                lastShowUnlinetime = nowtime;
            }
            return;

        }
    }

    public void doQuit(View v) {
        finish();
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

    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.STATE_RESPONSE) {
            //更新对方状态
            monitor = MyApplication.getInstance().getRelateDao().getMonitorById(monitor.id);
            updateHint();
        } else if (tag == HandlerUtil.CONNECT_SUC || tag == HandlerUtil.CONNECT_FAIL) {
            updateHint();
        }
    }

    /**
     * 点击路线信息 进入地图显示路线
     *
     * @param mapWayMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMapWay(MapWayMsg mapWayMsg) {
        bottomNavigationBar.selectTab(1);
        mapFragment.showMapWay(mapWayMsg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除订阅事件
        EventBus.getDefault().unregister(this);
    }


}
