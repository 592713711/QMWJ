package under_control.home.miband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.view.BandConnectView;
import com.ld.qmwj.view.CircleHintView;
import com.ld.qmwj.view.NumberView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.NotifyListener;
import com.zhaoxiaodan.miband.model.BatteryInfo;
import com.zhaoxiaodan.miband.model.UserInfo;
import com.zhaoxiaodan.miband.model.VibrationMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import under_control.home.service.FunctionService;

/**
 * 手环主界面
 */
public class MiBandActivity extends AppCompatActivity implements OnClickListener{

    private RelativeLayout headLayout;
    private RelativeLayout contentLayout;
    private Button band_btn;        //手环绑定解除按钮
    private CircleHintView circleHintView;
    private ImageView battery_icon;
    private BandConnectView connecting_image;
    private MiBand miBand;
    private int flag;           // 1：未绑定（按钮：绑定手环）   2：未连接（按钮：连接手环）   3：已连接（按钮：断开连接）
    private NumberView hintText;
    private BluetoothDevice device;
    private BatteryInfo info;       //设备电池信息


    public static final int UNBIND_FLAG = 1;
    public static final int UNCONN_FLAG = 2;
    public static final int CONN_FLAG = 3;

    public Handler handler = new Handler() {

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_band);
        EventBus.getDefault().register(this);

        initView();
        updateMiBand();
    }


    private void initView() {
        headLayout = (RelativeLayout) findViewById(R.id.head);
        contentLayout = (RelativeLayout) findViewById(R.id.mi_content);
        circleHintView = (CircleHintView) findViewById(R.id.circlehintview);
        battery_icon = (ImageView) findViewById(R.id.battery_icon);
        band_btn = (Button) findViewById(R.id.band_btn);
        hintText = (NumberView) findViewById(R.id.hintText);
        connecting_image = (BandConnectView) findViewById(R.id.connecting_icon);

        ImageButton heart_btn= (ImageButton) findViewById(R.id.heart_btn);
        heart_btn.setOnClickListener(this);

        ImageButton search_btn= (ImageButton) findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);

    }

    /**
     * 更新手环状态
     */
    private void updateMiBand() {
        miBand = FunctionService.miBand;
        //判断是否已经绑定
        device = MyApplication.getInstance().getSpUtil().getDevice();
        battery_icon.setVisibility(View.GONE);
        connecting_image.setVisibility(View.GONE);
        hintText.setVisibility(View.VISIBLE);
        band_btn.setEnabled(true);
        connecting_image.stopAnim();
        if (device == null) {
            //手环未绑定
            flag = UNBIND_FLAG;
            hintText.setText("手环尚未绑定");
            hintText.setTextSize(19);
            band_btn.setText("绑定手环");
            circleHintView.setLevel(0);
            initWindow(R.color.unbindbg);
            headLayout.setBackgroundResource(R.color.unbindbg);
            contentLayout.setBackgroundResource(R.color.unbindbg);
        } else {
            //判断是否已经连接
            int isConnect = FunctionService.isConnectBand;
            if (isConnect == MiBand.CONNECTED) {
                //手环已连接
                flag = CONN_FLAG;
                info = FunctionService.info;
                FunctionService.getBattery();
                hintText.setText("");
                //显示电量
                if (info != null) {
                    hintText.setTextSize(50);
                    hintText.isAnim=true;
                    hintText.showNumberWithAnimation(info.getLevel());

                    // 更改电量外圆
                    circleHintView.isAnim=true;
                    circleHintView.LevelToAnimator(info.getLevel());
                }
                band_btn.setText("断开连接");
                initWindow(R.color.bindbg);
                battery_icon.setVisibility(View.VISIBLE);
                headLayout.setBackgroundResource(R.color.bindbg);
                contentLayout.setBackgroundResource(R.color.bindbg);
            } else if (isConnect == MiBand.UNCONNECT) {
                //手环未连接
                flag = UNCONN_FLAG;
                hintText.setText("手环未连接");
                hintText.setTextSize(19);
                band_btn.setText("连接手环");
                circleHintView.setLevel(0);
                initWindow(R.color.unbindbg);
                headLayout.setBackgroundResource(R.color.unbindbg);
                contentLayout.setBackgroundResource(R.color.unbindbg);
            } else if (isConnect == MiBand.CONNECTING) {
                //手环连接中
                connecting_image.setVisibility(View.VISIBLE);
                connecting_image.startAnim();
                band_btn.setText("连接中...");
                band_btn.setEnabled(false);
                hintText.setVisibility(View.GONE);

            }
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        updateMiBand();
    }

    public void doBand(View v) {
        if (flag == UNBIND_FLAG) {
            //启动绑定手环活动
            Intent intent = new Intent(this, BindBandActivity.class);
            startActivity(intent);
        } else if (flag == UNCONN_FLAG) {
            //连接手环
            BluetoothDevice device = MyApplication.getInstance().getSpUtil().getDevice();
            doConnect(device);
            updateMiBand();
        } else if (flag == CONN_FLAG) {
            //断开连接
            FunctionService.miBand = new MiBand(MyApplication.getInstance());
            FunctionService.isConnectBand = MiBand.UNCONNECT;
            EventBus.getDefault().post(HandlerUtil.DISCONNECTBAND);
            circleHintView.isAnim=false;
            hintText.isAnim=false;
        }
    }




    /**
     * 连接小米手环
     */
    public void doConnect(final BluetoothDevice device) {

        //获取本地蓝牙适配器
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(Config.TAG, "BluetoothAdapter is null");
            return;
        }


        //判断蓝牙是否开启
        if (!adapter.isEnabled()) {
            Intent openIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getInstance().startActivity(openIntent);
            return;
        }


        FunctionService.isConnectBand = miBand.CONNECTING;
        miBand.connect(device, new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                Log.d(Config.TAG, "连接成功!!!");
                FunctionService.isConnectBand = MiBand.CONNECTED;
                getBattery();
                miBand.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        Log.d(Config.TAG, "手环连接断开!!!");
                        EventBus.getDefault().post(HandlerUtil.DISCONNECTBAND);
                        FunctionService.isConnectBand = MiBand.UNCONNECT;
                    }

                });

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //延时发送  不然  会和请求电池信息起冲突
                        //给手环设置用户信息   当 设置的 userid 跟之前一样时 手环无反应
                        User user = MyApplication.getInstance().getSpUtil().getUser();
                        UserInfo userInfo = new UserInfo(user.id, 1, 32, 180, 55, user.username, 0);
                        miBand.setUserInfo(userInfo);
                        EventBus.getDefault().post(HandlerUtil.CONNECTBAND);
                    }
                }, 2000);

            }

            @Override
            public void onFail(int errorCode, String msg) {
                FunctionService.isConnectBand = MiBand.UNCONNECT;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MiBandActivity.this, "连接失败，请将手环放于手机附近", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(Config.TAG, "连接失败, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }


    /**
     * 读取电池信息
     */
    public void getBattery() {
        miBand.getBatteryInfo(new ActionCallback() {

            @Override
            public void onSuccess(final Object data) {
                FunctionService.info = (BatteryInfo) data;
                Log.d(Config.TAG, "电池信息" + info.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        updateMiBand();
                        //hintText.setText(info.getLevel()+"%");

                    }
                });

            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(Config.TAG, "getBatteryInfo fail");
            }
        });
    }


    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.CONNECTBAND || tag == HandlerUtil.DISCONNECTBAND) {
            updateMiBand();
        }
    }


    /**
     * 初始化通知栏颜色
     */
    private void initWindow(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(color);//通知栏所需颜色

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
     *  按钮点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.heart_btn:
                Intent intent=new Intent(this,HeartActivity.class);
                startActivity(intent);
                break;
            case R.id.search_btn:
                doSearch();
                break;
        }

    }

    /**
     *  查找手环  震动
     */
    private void doSearch() {
        //震动2次， 三颗led亮
        miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);
        //震动2次, 没有led亮
        //miband.startVibration(VibrationMode.VIBRATION_WITHOUT_LED);
        //震动10次, 中间led亮蓝色
        //miband.startVibration(VibrationMode.VIBRATION_10_TIMES_WITH_LED);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
