package control.phone.health;

import android.annotation.TargetApi;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.WaitDialog;
import com.ld.qmwj.view.BandConnectView;
import com.ld.qmwj.view.CircleHintView;
import com.ld.qmwj.view.NumberView;
import com.readystatesoftware.systembartint.SystemBarTintManager;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 手环主界面
 */
public class MiBandActivity extends AppCompatActivity implements OnClickListener {

    private RelativeLayout headLayout;
    private RelativeLayout contentLayout;
    private Button band_btn;        //手环绑定解除按钮
    private CircleHintView circleHintView;
    private ImageView battery_icon;
    private BandConnectView connecting_image;
    private NumberView hintText;
    private ProgressBar progressBar;        //请求等待条
    private Monitor monitor;
    private BandState bandState;
    private Gson gson;



    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HandlerUtil.REQUEST_ERROR) {
                progressBar.setVisibility(View.GONE);
                if (MsgHandle.getInstance().channel == null)
                    Toast.makeText(MyApplication.getInstance(), "当前网路问题，无法更新数据", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyApplication.getInstance(), "未能更新数据，请联系客服", Toast.LENGTH_SHORT).show();
                hintText.setText("更新手环状态失败");
                band_btn.setText("更新状态");
                hintText.setTextSize(19);
                band_btn.setEnabled(true);

                initWindow(R.color.unbindbg);
                headLayout.setBackgroundResource(R.color.unbindbg);
                contentLayout.setBackgroundResource(R.color.unbindbg);
            }
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_band2);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        gson = new Gson();
        initView();
        getBandState();
    }


    private void initView() {
        headLayout = (RelativeLayout) findViewById(R.id.head);
        contentLayout = (RelativeLayout) findViewById(R.id.mi_content);
        circleHintView = (CircleHintView) findViewById(R.id.circlehintview);
        battery_icon = (ImageView) findViewById(R.id.battery_icon);
        band_btn = (Button) findViewById(R.id.band_btn);
        hintText = (NumberView) findViewById(R.id.hintText);
        connecting_image = (BandConnectView) findViewById(R.id.connecting_icon);
        progressBar= (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        ImageButton heart_btn = (ImageButton) findViewById(R.id.heart_btn);
        heart_btn.setOnClickListener(this);

        initWindow(R.color.unbindbg);
        headLayout.setBackgroundResource(R.color.unbindbg);
        contentLayout.setBackgroundResource(R.color.unbindbg);

    }

    /**
     * 得到对方手环状态
     */
    private void getBandState() {
        connecting_image.setVisibility(View.GONE);
        battery_icon.setVisibility(View.GONE);
        hintText.setText("更新中");
        band_btn.setText("更新状态中");
        band_btn.setEnabled(false);
        hintText.setTextSize(25);

        // 向对方请求当前手环状态
        Request request = new Request();
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.tag = MessageTag.BANDSTATE_REQ;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(request));

        progressBar.setVisibility(View.VISIBLE);

        handler.sendEmptyMessageDelayed(HandlerUtil.REQUEST_ERROR, 8000);

    }



    /**
     *  更新手环状态点击
     * @param v
     */
    public void doBand(View v) {
        getBandState();
    }


    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.STATE_RESPONSE) {
            //得到对方状态响应
            monitor = MyApplication.getInstance().getRelateDao().getMonitorById(monitor.id);
            if (monitor.state == Config.NOT_ONLINE_STATE) {
                hintText.setText("对方未连接");
                hintText.setTextSize(21);
                Toast.makeText(this, "对方网络问题，未能更新手环信息", Toast.LENGTH_SHORT).show();
                handler.removeMessages(HandlerUtil.REQUEST_ERROR);
                progressBar.setVisibility(View.GONE);
                band_btn.setText("更新状态");
                band_btn.setEnabled(true);


                initWindow(R.color.unbindbg);
                headLayout.setBackgroundResource(R.color.unbindbg);
                contentLayout.setBackgroundResource(R.color.unbindbg);
            }
        }
    }

    /**
     * 收到手环状态响应
     *
     * @param bandState
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveBandState(BandState bandState) {
        this.bandState = bandState;
        handler.removeMessages(HandlerUtil.REQUEST_ERROR);
        progressBar.setVisibility(View.GONE);
        band_btn.setText("更新状态");
        band_btn.setEnabled(true);
        updateBand();

    }

    private void updateBand() {
        battery_icon.setVisibility(View.GONE);
        connecting_image.setVisibility(View.GONE);
        hintText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        band_btn.setEnabled(true);
        if(bandState!=null){
            if(bandState.state==BandState.UNBIND){
                hintText.setTextSize(19);
                hintText.setText("手环未绑定");
                circleHintView.setLevel(0);
                initWindow(R.color.unbindbg);
                headLayout.setBackgroundResource(R.color.unbindbg);
                contentLayout.setBackgroundResource(R.color.unbindbg);
            }else if(bandState.state==BandState.UNCONNECT){
                //手环未连接
                hintText.setText("手环未连接");
                band_btn.setText("更新状态");
                hintText.setTextSize(19);
                circleHintView.setLevel(0);
                initWindow(R.color.unbindbg);
                headLayout.setBackgroundResource(R.color.unbindbg);
                contentLayout.setBackgroundResource(R.color.unbindbg);
            }else if(bandState.state==BandState.CONNECT){
                //手环已连接
                hintText.setText("");
                //显示电量
                if (bandState.batteryInfo != null) {
                    hintText.setTextSize(50);
                    hintText.isAnim=true;
                    hintText.showNumberWithAnimation(bandState.batteryInfo.getLevel());

                    // 更改电量外圆
                    circleHintView.isAnim=true;
                    circleHintView.LevelToAnimator(bandState.batteryInfo.getLevel());
                }
                battery_icon.setVisibility(View.VISIBLE);

                initWindow(R.color.bindbg);
                headLayout.setBackgroundResource(R.color.bindbg);
                contentLayout.setBackgroundResource(R.color.bindbg);
            }
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
     * 按钮点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.heart_btn:
                Intent intent=new Intent(this,HeartActivity.class);
                intent.putExtra("monitor",monitor);
                intent.putExtra("bandstate",bandState);
                startActivity(intent);
                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void doQuit(View v) {
        finish();
    }
}
