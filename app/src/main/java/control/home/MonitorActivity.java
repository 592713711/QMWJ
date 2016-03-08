package control.home;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
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
    private RadioButton msg_btn;
    private RadioButton phone_btn;
    private RadioButton map_btn;
    private RadioGroup radioGroup;
    private MapFragment mapFragment;
    private MsgFragment msgFragment;
    private PhoneFragment phoneFragment;
    FragmentManager fm;
    FragmentTransaction transaction;

    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initView();
        //订阅事件
        EventBus.getDefault().register(this);
    }

    private void initView() {
        msg_btn = (RadioButton) findViewById(R.id.msg_btn);
        map_btn = (RadioButton) findViewById(R.id.map_btn);
        phone_btn = (RadioButton) findViewById(R.id.phone_btn);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeFragment(checkedId);
            }
        });

        mapFragment = new MapFragment(monitor, this);
        msgFragment = new MsgFragment(monitor);
        phoneFragment = new PhoneFragment(monitor);
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.add(R.id.content, mapFragment);
        transaction.add(R.id.content, msgFragment);
        transaction.add(R.id.content, phoneFragment);
        transaction.commit();

        changeFragment(R.id.msg_btn);


    }


    /**
     * 根据选择的按钮 切换显示的碎片
     *
     * @param checkedId
     */
    private void changeFragment(int checkedId) {
        transaction = fm.beginTransaction();
        transaction.hide(msgFragment);
        transaction.hide(mapFragment);
        transaction.hide(phoneFragment);
        switch (checkedId) {
            case R.id.msg_btn:
                transaction.show(msgFragment);
                transaction.commit();
                break;
            case R.id.map_btn:
                transaction.show(mapFragment);
                transaction.commit();
                break;
            case R.id.phone_btn:
                transaction.show(phoneFragment);
                transaction.commit();
                break;
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
        if (tag == HandlerUtil.LOCATION_RESPONSE) {
            //收到位置响应
            MyLocation location = MyApplication.getInstance().getRelateDao().getLocation(monitor.id);
            handler.removeMessages(HandlerUtil.REQUEST_ERROR);
            mapFragment.handler.removeMessages(HandlerUtil.REQUEST_ERROR);
            mapFragment.waitDialog.dismiss();
            Toast.makeText(MonitorActivity.this, "更新了对方位置", Toast.LENGTH_SHORT).show();
            mapFragment.updateLocation(location);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除订阅事件
        EventBus.getDefault().unregister(this);
    }


}
