package control.phone.health;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.view.HeartView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import under_control.home.miband.HeartDataActivity;
import under_control.home.miband.HeartRcAdapter;
import under_control.home.service.FuctionService;

public class HeartActivity extends AppCompatActivity implements HeartView.StopRingAnim, MyRecycleViewItemListener {

    private Monitor monitor;
    private BandState bandState;

    private HeartView heartView;
    private CheckBox heart_btn;

    private TextView heart_data;
    private TextView data_hint1;
    private TextView data_hint2;
    private TextView data_errorhint;

    private RecyclerView recyclerView;
    private HeartRcAdapter adapter;
    private ArrayList<HeartData> datas;
    private Gson gson;

    public static int STOPANIM = 1;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        bandState = (BandState) getIntent().getSerializableExtra("bandstate");
        gson = new Gson();
        initWindow();
        initView();
        initData();

    }


    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycleview_list);

        heartView = (HeartView) findViewById(R.id.ringView);
        heartView.initStopRingAnim(this);
        heart_btn = (CheckBox) findViewById(R.id.startHeartBeatTest);
        heart_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    doHeart();
                } else {
                    heart_btn.setText("测量心率");
                    heart_btn.setEnabled(true);
                }
            }
        });


        heart_data = (TextView) findViewById(R.id.heart_data_textview);
        data_hint1 = (TextView) findViewById(R.id.heart_data_textview_hint1);
        data_hint2 = (TextView) findViewById(R.id.heart_data_textview_hint2);
        data_errorhint = (TextView) findViewById(R.id.heart_data_textview_errorhint);
        data_errorhint.setVisibility(View.GONE);
    }


    private void initData() {
        datas = MyApplication.getInstance().getHeartDao().getHeartDataList(
                monitor.id);
        adapter = new HeartRcAdapter(this);
        adapter.updateData(datas);
        adapter.setItemOnClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        data_hint1.setText("上次测量结果");
        if (datas.size() > 0) {
            HeartData lastData = datas.get(0);
            heart_data.setText(lastData.data + "");
        } else {
            heart_data.setText("无");
        }
    }


    /**
     * 更新列表数据
     */
    public void updateData() {
        datas = MyApplication.getInstance().getHeartDao().getHeartDataList(
                monitor.id);
        adapter.updateData(datas);
    }

    /**
     * 测试心跳  发送心跳请求到服务器
     */
    public void doHeart() {
        if (bandState==null||bandState.state != BandState.CONNECT) {
            Toast.makeText(HeartActivity.this, "手环未连接", Toast.LENGTH_SHORT).show();
            heart_btn.setChecked(false);
            return;
        }
        //测量心跳
        heartView.startAnim();
        heart_btn.setText("测量中");
        heart_btn.setEnabled(false);
        hintDataView();

        //发送测试心跳请求
        Request request = new Request();
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.tag = MessageTag.DOHEART_REQ;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(request));

    }


    public void hintDataView() {
        heart_data.setVisibility(View.GONE);
        data_hint1.setVisibility(View.GONE);
        data_hint2.setVisibility(View.GONE);
        data_errorhint.setVisibility(View.GONE);
    }


    public void showDataView() {
        heart_data.setVisibility(View.VISIBLE);
        data_hint1.setVisibility(View.VISIBLE);
        data_hint2.setVisibility(View.VISIBLE);
        data_errorhint.setVisibility(View.GONE);
    }


    /**
     * //收到手环状态响应
     *
     * @param heartData
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveHeartData(HeartData heartData) {
        Log.d(Config.TAG, "测量心率: " + heartData);
        if (heartData.data == 0)
            heartData.data = 30;

        //显示心跳数据


        heartView.stopAnim();
        heart_btn.setChecked(false);
        heart_data.setText(heartData.data + "");
        data_hint1.setText("本次测量结果");
        showDataView();
        updateData();


    }

    /**
     * 收到心跳测试数据
     *
     * @param bandState
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveBandState(BandState bandState) {
        this.bandState = bandState;
        if(bandState.state!=BandState.CONNECT){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    heartView.stopAnim();
                    heart_btn.setChecked(false);
                    data_errorhint.setText("对方手环已断开连接");
                    data_errorhint.setVisibility(View.VISIBLE);
                }
            }, 3000);

        }

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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (monitor.state == Config.NOT_ONLINE_STATE) {
                        heartView.stopAnim();
                        heart_btn.setChecked(false);
                        hintDataView();
                        data_errorhint.setText("对方未连接");
                        data_errorhint.setVisibility(View.VISIBLE);
                        Toast.makeText(HeartActivity.this,"对方未连接",Toast.LENGTH_SHORT).show();
                    }
                }
            },5000);

        }
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
        tintManager.setStatusBarTintResource(R.color.red);//通知栏所需颜色

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
     * 圆环动画结束 回调方法
     * 执行此方法意味着在动画结束后还没有收到对方返回的心率数据
     */
    @Override
    public void onStopAnim() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (MsgHandle.getInstance().channel == null) {
                    Toast.makeText(MyApplication.getInstance(), "当前网路问题，无法连接服务器", Toast.LENGTH_SHORT).show();
                    data_errorhint.setText("请检查网络");
                } else {
                    data_errorhint.setText("测量失败");
                }
                data_errorhint.setVisibility(View.VISIBLE);
                heart_btn.setChecked(false);

            }
        });

    }

    /**
     * 视图最后一项“更多”点击监听器
     *
     * @param v
     * @param position
     */
    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(this, Control_HeartDataActivity.class);
        intent.putExtra("monitor_id", monitor.id);
        startActivity(intent);
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
}
