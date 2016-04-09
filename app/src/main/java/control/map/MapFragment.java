package control.map;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.TimeUtil;
import com.ld.qmwj.util.WaitDialog;

import control.home.MonitorActivity;
import control.map.LiShiLuxian.RouteActivity;
import control.map.overlayutil.DrivingRouteOverlay;
import control.map.overlayutil.TransitRouteOverlay;
import control.map.overlayutil.WalkingRouteOverlay;

/**
 * 地图显示的碎片
 */
public class MapFragment extends Fragment implements View.OnClickListener {

    private Monitor monitor;
    private MonitorActivity monitorActivity;
    private MapView mMapView = null;
    private BaiduMap mBaidumap;

    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;


    //private double mCurrentlatitude=28.138352;         //对方当前位置的纬度
    // private double mCurrentlongitude=113.000691;        //对方当前经度
    private MyLocation mLocation;           //被监护方位置
    private LatLng currentLocation;         //当前用户位置

    private BitmapDescriptor mLocationIcon; //定位的图标
    private float mCurrentx;             //当前位置 x方向的值
    private MyLocationConfiguration.LocationMode mLocationModel;         //定位模式

    private String city = "广德";       //对方当前位置的城市
    int way_type = 0;         //路线类型  0步行 1小车 2公交
    LatLng selectLatLng = null;       //选择的坐标点
    private boolean isSelect = false;     //判断是否是点击地图的标志


    //相关控件
    CheckBox more_btn;
    RelativeLayout head_layout;
    RelativeLayout bottom_layout;
    RelativeLayout bottom_layout2;
    RelativeLayout more_menu;
    RadioGroup radioGroup;
    TextView location_message;      //标记地点位置信息
    TextView location_time;         //对方位置信息时间
    TextView location_message2;     //对方位置信息
    EditText address_input;
    public WaitDialog waitDialog;      //等待对话框
    Dialog inputDialog;    //输入对话框

    //标记相关
    BitmapDescriptor mMarkerIcon;
    Marker marker;

    //路线查询相关
    RoutePlanSearch routePlanSearch;// 路径规划搜索接口

    //反编译
    GeoCoder geoCoder;      // 创建地理编码检索实例

    //传感器
    MyOrientationListener myOrientationListener;        //自定义方向传感器监听器 内部已经实现了传感器的初始化


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerUtil.SEAECH_ERROR) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }
                Toast.makeText(MyApplication.getInstance(), "查找路线，请检查网络", Toast.LENGTH_SHORT).show();
            } else if (msg.what == HandlerUtil.REQUEST_ERROR) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }

                if (MsgHandle.getInstance().channel == null)
                    Toast.makeText(MyApplication.getInstance(), "当前网路问题，无法得到位置", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyApplication.getInstance(), "未能得到位置，请联系客服", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public MapFragment(Monitor monitor, MonitorActivity activity) {
        this.monitor = monitor;
        this.monitorActivity = activity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(MyApplication.getInstance());
        initGeoCoder();
        initData();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initView(view);
        centerToMyPosition();

        return view;
    }

    private void initData() {
        //初始化位置定位
        mLocationClient = new LocationClient(MyApplication.getInstance());
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //初始化定位配置
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");       //设置坐标类型
        option.setIsNeedAddress(true);      //设置true后可以得到位置的地址
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);

        //方向传感器  需要手动开启和关闭
        myOrientationListener = new MyOrientationListener(MyApplication.getInstance());
        //设置方向传感器回调方法
        myOrientationListener.setOnOrientationListener(onOrientationListener);

        mLocationModel = MyLocationConfiguration.LocationMode.NORMAL;
        waitDialog = new WaitDialog();

        //初始化对话框
        LayoutInflater inflater = monitorActivity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(monitorActivity);
        View view = inflater.inflate(R.layout.dialog_input, null);
        address_input = (EditText) view.findViewById(R.id.address_input);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                geoCoder.geocode(new GeoCodeOption()
                        .city(city)
                        .address(address_input.getText().toString()));
            }
        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(view);
        inputDialog = builder.create();

        //初始化标记物
        mMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.maker);
        //初始化定位的图标
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //缩放的比例，缩放是很难按准备的比例进行缩放的，其值表明缩放的倍数，SDK中建议其值是2的指数值,值越大会导致图片不清晰
        opts.inSampleSize = 1;
        Bitmap bmp = null;
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.location_icon);
        mLocationIcon = BitmapDescriptorFactory.fromBitmap(bmp);

        //初始化路线
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch
                .setOnGetRoutePlanResultListener(routePlanResultListener);

        //先加载数据库中的位置
        mLocation = MyApplication.getInstance().getRelateDao().getLocation(monitor.id);


    }


    private void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();
        //设置缩放等级 zoomTo
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaidumap.setMapStatus(msu);        //设置地图状态
        mBaidumap.setMyLocationEnabled(true);
        mMapView.removeViewAt(2);           //隐藏地图上比例尺

        radioGroup = (RadioGroup) view.findViewById(R.id.way_type);
        location_message = (TextView) view.findViewById(R.id.location_text);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //选项发生改变
                switch (checkedId) {
                    case R.id.walk_radio:
                        way_type = 0;
                        break;
                    case R.id.bus_radio:
                        way_type = 2;
                        break;
                    case R.id.car_radio:
                        way_type = 1;
                        break;
                }
                //判断是点击地图还是到当前位置
                if (isSelect)
                    searchWay(selectLatLng);
                else
                    searchWay(currentLocation);
            }
        });

        //更多菜单监听
        more_btn = (CheckBox) view.findViewById(R.id.more_btn);
        more_menu = (RelativeLayout) view.findViewById(R.id.more_menu);
        more_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    more_menu.setVisibility(View.VISIBLE);
                } else {
                    more_menu.setVisibility(View.GONE);
                }
            }
        });

        head_layout = (RelativeLayout) view.findViewById(R.id.head);
        bottom_layout = (RelativeLayout) view.findViewById(R.id.bottom);
        bottom_layout2 = (RelativeLayout) view.findViewById(R.id.bottom2);
        head_layout.setVisibility(View.GONE);
        bottom_layout.setVisibility(View.GONE);

        location_time = (TextView) view.findViewById(R.id.location_time);
        location_message2 = (TextView) view.findViewById(R.id.location_text2);

        ImageButton mark_btn = (ImageButton) view.findViewById(R.id.mark_btn);
        mark_btn.setOnClickListener(this);

        ImageButton search_btn = (ImageButton) view.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);

        ImageButton back_btn = (ImageButton) view.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        ImageButton refresh_btn = (ImageButton) view.findViewById(R.id.refresh_btn);
        refresh_btn.setOnClickListener(this);

        ImageButton way_btn = (ImageButton) view.findViewById(R.id.way_btn);
        way_btn.setOnClickListener(this);

        ImageButton goto_btn = (ImageButton) view.findViewById(R.id.goto_btn);
        goto_btn.setOnClickListener(this);

        ImageButton protect_btn = (ImageButton) view.findViewById(R.id.protect_btn);
        protect_btn.setOnClickListener(this);


    }


    /**
     * 初始换地理编译和反编译
     */
    private void initGeoCoder() {
        geoCoder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数     通过坐标得到 地理信息
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                String msg = null;
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    msg = "抱歉，未能找到结果";
                }
                if (bottom_layout.getVisibility() == View.VISIBLE) {
                    if (msg != null) {
                        location_message.setText(msg);
                        return;
                    }
                    location_message.setText("标记位置：" + result.getAddress());

                }

                if (bottom_layout2.getVisibility() == View.VISIBLE) {
                    if (msg != null) {
                        location_message2.setText(msg);
                        return;
                    }
                    location_message2.setText(result.getAddress());

                }

            }

            // 地理编码查询结果回调函数  通过地理信息得到坐标
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                }
                selectLatLng = result.getLocation();
                searchWay(selectLatLng);
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);


        // 释放地理编码检索实例
        // geoCoder.destroy();
    }


    /**
     * 将地图移动到当前位置的中心点
     */
    private void centerToMyPosition() {
        mBaidumap.clear();
        if (mLocation.latitude == 0 && mLocation.longitude == 0)
            return;
        MyLocationData data = new MyLocationData.Builder()
                .direction(mCurrentx)
                .latitude(mLocation.latitude)
                .longitude(mLocation.longitude)
                .build();

        // 设置当前位置定位数据
        mBaidumap.setMyLocationData(data);
        //设置当前位置的图标
        MyLocationConfiguration config = new MyLocationConfiguration(
                mLocationModel,       //设置定位模式
                true, mLocationIcon);
        mBaidumap.setMyLocationConfigeration(config);

        //LatLng latLng = new LatLng(28.138451,113.000645);
        LatLng latLng = new LatLng(mLocation.latitude, mLocation.longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);   //设置位置（经度和纬度）
        mBaidumap.animateMapStatus(msu);      //使用动画的方式更新地图  不是瞬间跳到当前的位置 而是有个动画移动的效果

        //更新底部显示的位置信息
        // 反地理编码  通过坐标查位置
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        //显示时间
        location_time.setText(TimeUtil.getTimeStr((long) mLocation.time, System.currentTimeMillis()) + "他(她)在：");
    }

    public void requestPosition() {
        //请求对方
        MyApplication.getInstance().getSendMsgUtil().sendLocationRequest(
                MyApplication.getInstance().getSpUtil().getUser().id, monitor.id);

        //等待对话框
        waitDialog.setMsg("请求位置中...");
        waitDialog.show(monitorActivity.getSupportFragmentManager(), "WAIT_DIALOG");
        waitDialog.setCancelable(false);
        handler.sendEmptyMessageDelayed(HandlerUtil.REQUEST_ERROR, 10000);
    }

    public MyOrientationListener.OnOrientationListener onOrientationListener = new MyOrientationListener.OnOrientationListener() {
        @Override
        public void onOrientationChanged(float x) {
            //方向传感器回调方法
            //改变定位图标位置
            mCurrentx = x;
        }
    };


    /**
     * 查找路线
     */
    private void searchWay(LatLng target) {

        if (target == null)
            return;

        switch (way_type) {
            case 0:
                //查询步行
                WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
                walkOption.from(PlanNode.withLocation(new LatLng(mLocation.latitude, mLocation.longitude)));// 设置起点
                walkOption.to(PlanNode.withLocation(target));// 设置终点
                routePlanSearch.walkingSearch(walkOption);       // 发起步行路线规划
                break;
            case 1:
                //查询小车
                DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
                drivingOption.from(PlanNode.withLocation(new LatLng(mLocation.latitude, mLocation.longitude)));// 设置起点
                drivingOption.to(PlanNode.withLocation(target));// 设置终点
                routePlanSearch.drivingSearch(drivingOption);       // 发起步行路线规划
                break;
            case 2:
                //查询公交
                TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
                transitOption.from(PlanNode.withLocation(new LatLng(mLocation.latitude, mLocation.longitude)));// 设置起点
                transitOption.to(PlanNode.withLocation(target));// 设置终点
                transitOption.city(city);
                routePlanSearch.transitSearch(transitOption);       // 发起步行路线规划
                break;
        }

        //等待对话框
        waitDialog.show(monitorActivity.getSupportFragmentManager(), "LOADING_DIALOG");
        waitDialog.setCancelable(false);
        handler.sendEmptyMessageDelayed(HandlerUtil.SEAECH_ERROR, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();

    }

    private boolean isFirst = true;

    /**
     * 隐藏/显示 状态切换时触发
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //第一次显示时请求位置
        if (!hidden && isFirst) {
            //请求位置
            requestPosition();
            isFirst = false;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();

    }

    //弹出对话框输入要到达的地点
    public void doSearch() {
        //等待对话框
        inputDialog.show();
        inputDialog.setCancelable(false);
    }

    //    打开标记
    public void doMark() {
        //动画的方式显示布局文件
        head_layout.setVisibility(View.VISIBLE);
        bottom_layout.setVisibility(View.VISIBLE);
        Animation anmi = AnimationUtils.loadAnimation(monitorActivity, R.anim.head_coming);
        Animation anmi2 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottom_coming);
        head_layout.startAnimation(anmi);
        bottom_layout.setAnimation(anmi2);

        Animation anmi3 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottim_out);
        bottom_layout2.setVisibility(View.GONE);
        bottom_layout2.startAnimation(anmi3);

        //设置点击地图监听器
        mBaidumap.clear();
        mBaidumap.setOnMapClickListener(mapClickLis);
        isSelect = true;
    }

    public void doBack() {
        //动画的方式隐藏布局文件
        Animation anmi = AnimationUtils.loadAnimation(monitorActivity, R.anim.head_out);
        head_layout.startAnimation(anmi);
        head_layout.setVisibility(View.GONE);
        Animation anmi2 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottim_out);
        bottom_layout.startAnimation(anmi2);
        bottom_layout.setVisibility(View.GONE);
        Animation anmi3 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottom_coming);
        bottom_layout2.setVisibility(View.VISIBLE);
        bottom_layout2.startAnimation(anmi3);
        isSelect = false;
        mBaidumap.setOnMapClickListener(null);
        mBaidumap.clear();

    }

    /**
     * 更新对方当前位置
     */
    public void updateLocation(MyLocation location) {
        mLocation = location;
        centerToMyPosition();
    }

    /**
     * 地图点击监听器
     */

    BaiduMap.OnMapClickListener mapClickLis = new BaiduMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            //点击之后出现标记物
            mBaidumap.clear();      //先清除覆盖物
            //LatLng latling=new LatLng(28.2339710000,112.9453330000);
            //构造OverlayOptions（地图覆盖物选型基类）
            OverlayOptions options = new MarkerOptions()
                    .position(latLng)      // 设置marker的位置
                    .icon(mMarkerIcon)      // 设置marker的图标
                    .zIndex(5);             // 设置marker的所在层級

            // 在地图上添加marker，并显示
            marker = (Marker) mBaidumap.addOverlay(options);
            // 反地理编码  通过坐标查位置
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
            selectLatLng = latLng;
            searchWay(selectLatLng);

//            Log.d(TAG,latLng.toString());


        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            return false;
        }
    };

    /**
     * 按钮点击监听器
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mark_btn:
                more_btn.setChecked(false);
                doMark();
                break;
            case R.id.back_btn:
                doBack();
                break;
            case R.id.search_btn:
                doSearch();
                break;
            case R.id.refresh_btn:
                more_btn.setChecked(false);
                requestPosition();
                break;
            case R.id.way_btn:
                more_btn.setChecked(false);
                Intent intent = new Intent(monitorActivity, RouteActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;
            case R.id.goto_btn:
                more_btn.setChecked(false);
                doGoto();
                break;
            case R.id.protect_btn:
                more_btn.setChecked(false);
                break;

        }
    }

    /**
     * 点击到这去按钮
     */
    private void doGoto() {
        mLocationClient.start();
        isSelect = false;
        mBaidumap.setOnMapClickListener(null);
        //动画的方式显示布局文件
        head_layout.setVisibility(View.VISIBLE);
        bottom_layout.setVisibility(View.VISIBLE);
        Animation anmi = AnimationUtils.loadAnimation(monitorActivity, R.anim.head_coming);
        Animation anmi2 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottom_coming);
        head_layout.startAnimation(anmi);
        bottom_layout.setAnimation(anmi2);

        Animation anmi3 = AnimationUtils.loadAnimation(monitorActivity, R.anim.bottim_out);
        bottom_layout2.setVisibility(View.GONE);
        bottom_layout2.startAnimation(anmi3);

    }

    /**
     * 路线查找监听器
     */
    public OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

        /**
         * 步行路线结果回调
         */
        @Override
        public void onGetWalkingRouteResult(
                WalkingRouteResult walkingRouteResult) {
            mBaidumap.clear();
            handler.removeMessages(HandlerUtil.SEAECH_ERROR);
            waitDialog.dismiss();
            if (walkingRouteResult == null
                    || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(monitorActivity, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // TODO
                return;
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(
                        mBaidumap);
                walkingRouteOverlay.setData(walkingRouteResult.getRouteLines()
                        .get(0));
                mBaidumap.setOnMarkerClickListener(walkingRouteOverlay);
                walkingRouteOverlay.addToMap();
                walkingRouteOverlay.zoomToSpan();
                int totalLine = walkingRouteResult.getRouteLines().size();
                //Toast.makeText(MainActivity.this, "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_SHORT).show();

            }
        }

        /**
         * 换成路线结果回调
         */
        @Override
        public void onGetTransitRouteResult(
                TransitRouteResult transitRouteResult) {
            mBaidumap.clear();
            handler.removeMessages(HandlerUtil.SEAECH_ERROR);
            if (waitDialog != null)
                waitDialog.dismiss();
            if (transitRouteResult == null
                    || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(monitorActivity, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(
                        mBaidumap);
                transitRouteOverlay.setData(transitRouteResult.getRouteLines()
                        .get(0));// 设置一条驾车路线方案
                mBaidumap.setOnMarkerClickListener(transitRouteOverlay);
                transitRouteOverlay.addToMap();
                transitRouteOverlay.zoomToSpan();
                int totalLine = transitRouteResult.getRouteLines().size();
                Toast.makeText(monitorActivity,
                        "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_SHORT).show();

                // 通过getTaxiInfo()可以得到很多关于打车的信息
                Toast.makeText(
                        monitorActivity,
                        "该路线打车总路程"
                                + transitRouteResult.getTaxiInfo()
                                .getDistance(), Toast.LENGTH_SHORT).show();
            }

        }

        /**
         * 驾车路线结果回调 查询的结果可能包括多条驾车路线方案
         */
        @Override
        public void onGetDrivingRouteResult(
                DrivingRouteResult drivingRouteResult) {
            mBaidumap.clear();
            handler.removeMessages(HandlerUtil.SEAECH_ERROR);
            waitDialog.dismiss();
            if (isSelect)
                showSelectDialog();
            if (drivingRouteResult == null
                    || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(monitorActivity, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                int totalLine = drivingRouteResult.getRouteLines().size();
                for (int i = totalLine - 1; i >= 0; i--) {
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            mBaidumap);
                    if (i != 0) {
                        drivingRouteOverlay.setLineColor(Color.parseColor("#bdbdbd"));
                    } else {
                        drivingRouteOverlay.setLineColor(Color.parseColor("#2196f3"));
                    }
                    drivingRouteOverlay.setData(drivingRouteResult.getRouteLines()
                            .get(i));// 设置一条驾车路线方案

                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    //设置路线点击监听器
                    mBaidumap.setOnPolylineClickListener(drivingRouteOverlay);
                    //设置点击路标监听器
                    mBaidumap.setOnMarkerClickListener(drivingRouteOverlay);
                }


                Toast.makeText(monitorActivity,
                        "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_SHORT).show();

                // 通过getTaxiInfo()可以得到很多关于打车的信息

                //   Toast.makeText(MainActivity.this, "该路线打车总路程" + drivingRouteResult.getTaxiInfo()
                //                   .getDistance(), Toast.LENGTH_SHORT).show();
            }

        }
    };

    public void showSelectDialog() {
        GeoPoint gp = new GeoPoint(selectLatLng.latitudeE6, selectLatLng.longitudeE6);
        InfoWindow mInfoWindow;
        //将地图上的经纬度转化为屏幕的点坐标，
        Point p = mBaidumap.getProjection().toScreenLocation(selectLatLng);
        View v = monitorActivity.getLayoutInflater().inflate(R.layout.dialog_select, null);
        Button btn = (Button) v.findViewById(R.id.temp_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(monitorActivity, "点击相应成功", Toast.LENGTH_SHORT).show();
            }
        });
        mInfoWindow = new InfoWindow(v, selectLatLng, -47);
        //显示InfoWindow
        // mBaidumap.showInfoWindow(mInfoWindow);

    }

    /**
     * 定位监听器
     */
    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            location_message.setText("你的位置：" + location.getAddrStr());
            mLocationClient.stop();
            //显示路线
            mBaidumap.clear();      //先清除覆盖物
            //构造OverlayOptions（地图覆盖物选型基类）
            OverlayOptions options = new MarkerOptions()
                    .position(currentLocation)      // 设置marker的位置
                    .icon(mMarkerIcon)      // 设置marker的图标
                    .zIndex(5);             // 设置marker的所在层級

            // 在地图上添加marker，并显示
            marker = (Marker) mBaidumap.addOverlay(options);
            // 反地理编码  通过坐标查位置
            searchWay(currentLocation);
        }
    }
}
