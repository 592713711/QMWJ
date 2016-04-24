package control.map.LiShiLuxian;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.dao.RouteDao;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.SharePreferenceUtil;
import com.ld.qmwj.util.WaitDialog;
import com.ld.qmwj.util.WayUtil;
import com.ld.qmwj.view.calendar.CalendarCard;
import com.ld.qmwj.view.calendar.CalenderDialog;
import com.ld.qmwj.view.calendar.CustomDate;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {
    private Monitor monitor;
    private Gson gson;
    MapView mMapView = null;
    BaiduMap mBaidumap;
    TextView dateText;

    ArrayList<LatLng> routeData;
    RouteDao routeDao;

    CheckBox checkBox;           //日期对话框开关
    private CalenderDialog calenderDialog;  //日期对话框
    private WaitDialog waitDialog;      //等待对话框
    private String selectDate = WayUtil.getDateStr();   //当前选择日期

    private Handler handler = new Handler() {
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
        setContentView(R.layout.activity_route);

        initWindow();
        initData();

        doRequestWay();
    }

    private void initData() {
        routeDao = MyApplication.getInstance().getRouteDao();
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        gson = new Gson();
        waitDialog = new WaitDialog();
        initRoute();
        initMap();
        showRoute();
        initview();
        //订阅事件
        EventBus.getDefault().register(this);
    }

    /**
     * 请求历史路线
     */
    private void doRequestWay() {
        boolean isExist = routeDao.tabbleIsExist(monitor.id);
        Request request = new Request();
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        if (!isExist) {
            //表不存在 说明 第一次请求  创建表并请求对方30天的数据
            routeDao.createTable(monitor.id);
            request.tag = MessageTag.OLDWAY_REQ_ALL;
            waitDialog.setMsg("第一次请求可能较慢，请稍等");
        } else {
            //请求对方当天的数据
            request.tag = MessageTag.OLDWAY_REQ;
            waitDialog.setMsg("正在更新数据，请稍等");
        }

        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(gson.toJson(request));
        //等待对话框
        waitDialog.show(getSupportFragmentManager(), "request_wait");
        waitDialog.setCancelable(false);

        handler.sendEmptyMessageDelayed(HandlerUtil.REQUEST_ERROR, 10000);
    }


    private void initview() {
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //显示日期选择器
                    calenderDialog.show();
                }
            }
        });

        calenderDialog = new CalenderDialog(this, R.style.dialog, cellClickListener);
        calenderDialog.initDialog(this);
        //监听对话框隐藏时
        calenderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                checkBox.setChecked(false);
            }
        });

        dateText = (TextView) findViewById(R.id.date_text);
        dateText.setText(WayUtil.getDateStr().replace("_", "/"));

    }

    private void showRoute() {

        if (routeData.size() < 2) {
            Toast.makeText(this, "暂无当天路线数据", Toast.LENGTH_SHORT).show();
            return;
        }

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(routeData.get(0));   //设置位置（经度和纬度）
        mBaidumap.animateMapStatus(msu);      //使用动画的方式更新地图  不是瞬间跳到当前的位置 而是有个动画移动的效果
        //绘制历史路线图
        OverlayOptions ooPolyline = new PolylineOptions().width(8)
                .color(Color.BLUE).points(routeData);
        mBaidumap.addOverlay(ooPolyline);


    }

    private void initRoute() {

        routeData = routeDao.getRoute(monitor.id, selectDate);
    }

    private void initMap() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();
        //设置缩放等级 zoomTo
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaidumap.setMapStatus(msu);        //设置地图状态
        mMapView.removeViewAt(2);           //隐藏地图上比例尺
    }

    /**
     * 日期点击监听器
     */
    CalendarCard.OnCellClickListener cellClickListener = new CalendarCard.OnCellClickListener() {
        @Override
        public void clickDate(CustomDate date) {

            Log.d(Config.TAG, "点击：" + date.toString());
            dateText.setText(date.toString().replace("_", "/"));
            selectDate = date.toString();
            updateRoute();
            calenderDialog.dismiss();
        }

        @Override
        public void changeDate(CustomDate date) {
            if (calenderDialog != null)
                calenderDialog.monthText.setText(date.month + "月");
        }
    };

    public void updateRoute() {
        mBaidumap.clear();
        initRoute();
        showRoute();
    }


    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.OLDWAY_UPDATE) {
            updateRoute();
            waitDialog.dismiss();
            handler.removeMessages(HandlerUtil.REQUEST_ERROR);
            Toast.makeText(this, "数据以更新", Toast.LENGTH_SHORT).show();
            Log.d(Config.TAG, "更新历史路线");
        } else if (tag == HandlerUtil.STATE_RESPONSE) {
            //更新对方状态
            monitor = MyApplication.getInstance().getRelateDao().getMonitorById(monitor.id);
            if (monitor.state == Config.NOT_ONLINE_STATE) {
                waitDialog.dismiss();
                handler.removeMessages(HandlerUtil.REQUEST_ERROR);
                Toast.makeText(this, "对方网络问题，未能更新数据", Toast.LENGTH_SHORT).show();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除订阅事件
        EventBus.getDefault().unregister(this);
    }
}
