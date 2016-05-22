package under_control.home.miband;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.view.HeartView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;

import java.util.ArrayList;

import under_control.home.service.FunctionService;

public class HeartActivity extends AppCompatActivity implements View.OnClickListener, HeartView.StopRingAnim, MyRecycleViewItemListener {

    private HeartView heartView;
    private CheckBox heart_btn;
    private MiBand miBand;

    private TextView heart_data;
    private TextView data_hint1;
    private TextView data_hint2;
    private TextView data_errorhint;

    private RecyclerView recyclerView;
    private HeartRcAdapter adapter;
    private ArrayList<HeartData> datas;

    public static int STOPANIM = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STOPANIM) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);
        initWindow();
        initView();
        initData();
        updateMiBand();

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
                    Log.d(Config.TAG, "选中");
                    if (FunctionService.isConnectBand != MiBand.CONNECTED) {
                        Toast.makeText(HeartActivity.this, "手环未连接", Toast.LENGTH_SHORT).show();
                        heart_btn.setChecked(false);
                        return;
                    }
                    //测量心跳
                    heartView.startAnim();
                    heart_btn.setText("测量中");
                    heart_btn.setEnabled(false);
                    hintDataView();
                    doHeart();
                } else {
                    Log.d(Config.TAG, "未选中");
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
                MyApplication.getInstance().getSpUtil().getUser().id);
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

    public void updateMiBand() {
        miBand = FunctionService.miBand;
    }

    /**
     * 更新列表数据
     */
    public void updateData(){
        datas = MyApplication.getInstance().getHeartDao().getHeartDataList(
                MyApplication.getInstance().getSpUtil().getUser().id);
        adapter.updateData(datas);
    }

    /**
     * 测试心跳  必须先设置userinfo
     */
    public void doHeart() {
        //设置心跳监听器
        miBand.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate) {
                Log.d(Config.TAG, "heart rate: " + heartRate);
                if(heartRate==0)
                    heartRate=30;
                HeartData heartData = new HeartData();
                heartData.data = heartRate;
                heartData.time = System.currentTimeMillis();
                //保存心跳数据
                MyApplication.getInstance().getHeartDao().insertHeartData(
                        MyApplication.getInstance().getSpUtil().getUser().id,
                        heartData
                );
                //显示心跳数据


                final int finalHeartRateTemp = heartRate;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        heartView.stopAnim();
                        heart_btn.setChecked(false);
                        heart_data.setText(finalHeartRateTemp + "");
                        data_hint1.setText("本次测量结果");
                        showDataView();
                        updateData();
                    }
                });


            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                miBand.startHeartRateScan();
            }
        }, 1000);

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

    @Override
    public void onClick(View v) {

    }

    /**
     * 圆环动画结束 回调方法
     * 执行此方法意味着在动画结束后还没有得到心率数据
     */
    @Override
    public void onStopAnim() {
        handler.post(new Runnable() {
            @Override
            public void run() {
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
        Intent intent = new Intent(this, HeartDataActivity.class);
        intent.putExtra("monitor_id", MyApplication.getInstance().getSpUtil().getUser().id);
        startActivity(intent);
    }

    public void doQuit(View v) {
        finish();
    }

}
