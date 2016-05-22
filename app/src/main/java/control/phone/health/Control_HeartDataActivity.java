package control.phone.health;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mikephil.charting.data.LineData;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.request.HeartDataRequest;

import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.TimeUtil;
import com.ld.qmwj.util.WaitDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import under_control.home.miband.HeartDataActivity;

/**
 * 根据日期显示心跳数据
 */
public class Control_HeartDataActivity extends HeartDataActivity {
    private WaitDialog waitDialog;
    private Gson gson;
    private ImageButton refresh_btn;
    private CoordinatorLayout coordinatorLayout;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerUtil.REQUEST_ERROR) {
                waitDialog.dismiss();
                if (MsgHandle.getInstance().channel == null)
                    Toast.makeText(MyApplication.getInstance(), "当前网路问题，无法更新数据", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyApplication.getInstance(), "未能更新数据，请联系客服", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initLineChart();
        String date = TimeUtil.getNowDateStr();
        long from_time = TimeUtil.timeStrToLong(date);
        long to_timie = TimeUtil.timeStrToLong2(date);
        doRequestHeartData(from_time, to_timie,"更新当天数据中");      //yyyy-MM-dd
    }

    @Override
    protected void initData() {
        datas = new ArrayList<>();
        waitDialog = new WaitDialog();
        gson = new Gson();

        refresh_btn = (ImageButton) findViewById(R.id.refresh_btn);
        refresh_btn.setVisibility(View.VISIBLE);
        refresh_btn.setOnClickListener(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

    }


    /**
     * 向对方请求当天心跳数据
     */
    private void doRequestHeartData(long from_time, long to_time,String hint) {

        HeartDataRequest request = new HeartDataRequest();
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = id;
        request.from_time = from_time;
        request.to_timie = to_time;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(request));


        waitDialog.setMsg(hint);
        //等待对话框
        waitDialog.show(getSupportFragmentManager(), "request_wait");
        waitDialog.setCancelable(false);

        handler.sendEmptyMessageDelayed(HandlerUtil.REQUEST_ERROR, 10000);
    }


    @Override
    protected void updateData() {
        super.initData();       //调用父类  从数据库中得到最新数据
        Log.d(Config.TAG, "数组长度：" + datas.size());
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        if (datas.size() == 0)
            mChart.clear();

        animateAdd();
    }

    /**
     * 收到心跳更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveHeartDatas(Integer tag) {
        if (tag == HandlerUtil.UPDATEHEARTDATA) {
            Log.d(Config.TAG, "更新心跳数据 ");
            waitDialog.dismiss();
            handler.removeMessages(HandlerUtil.REQUEST_ERROR);
            Toast.makeText(this, "数据更新成功", Toast.LENGTH_SHORT).show();
            updateData();
        } else if (tag == HandlerUtil.STATE_RESPONSE) {
            //得到对方状态响应
            Monitor monitor = MyApplication.getInstance().getRelateDao().getMonitorById(id);
            if (monitor.state == Config.NOT_ONLINE_STATE) {
                waitDialog.dismiss();
                handler.removeMessages(HandlerUtil.REQUEST_ERROR);
                Toast.makeText(this, "对方未连接,数据更新失败", Toast.LENGTH_SHORT).show();
            }


        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.refresh_btn) {
            doUpdateAll();

        }
    }


    /**
     * 更新7天内所有数据
     */
    private void doUpdateAll() {

        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "更新7天内所有数据，可能会消耗少量流量", Snackbar.LENGTH_SHORT);
        snackbar.setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                snackbar.dismiss();
                //请求7天数据
                String nowDate = TimeUtil.getNowDateStr();
                long from_time = TimeUtil.timeStrToLong_agoseven(nowDate);
                long to_time = TimeUtil.timeStrToLong2(nowDate);
                doRequestHeartData(from_time, to_time, "更新数据中");

            }
        });
        snackbar.show();


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
