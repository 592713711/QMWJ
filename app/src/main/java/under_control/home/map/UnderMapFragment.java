package under_control.home.map;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.model.RouteWay;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.TimeUtil;
import com.ld.qmwj.util.WaitDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import control.map.overlayutil.DrivingRouteOverlay;
import control.map.overlayutil.TransitRouteOverlay;
import control.map.overlayutil.WalkingRouteOverlay;
import under_control.home.service.LocationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class UnderMapFragment extends Fragment implements View.OnClickListener{

    private Context context;
    private AppCompatActivity activity;
    private MapView mMapView = null;
    private BaiduMap mBaidumap;
    private RelativeLayout bottom_layout;
    private ImageButton close_way_btn;
    private MyLocation myLocation;
    private TextView location_message;     //对方位置信息
    //反编译地址坐标
    private GeoCoder geoCoder;      // 创建地理编码检索实例
    private MyLocationConfiguration.LocationMode mLocationModel;         //定位模式
    private BitmapDescriptor mLocationIcon; //定位的图标
    private WaitDialog waitDialog;      //等待对话框
    private RouteWay routeWayTemp;
    private RoutePlanSearch routePlanSearch;// 路径规划搜索接口

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerUtil.SEAECH_ERROR) {
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }
                Toast.makeText(MyApplication.getInstance(), "查找路线失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public UnderMapFragment(Context context, AppCompatActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(MyApplication.getInstance());
        EventBus.getDefault().register(this);
        initData();
    }

    private void initData() {
        //初始化路线
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch
                .setOnGetRoutePlanResultListener(routePlanResultListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_under_map, container, false);
        initView(v);
        initGeoCoder();
        return v;
    }


    private void initView(View v) {

        mMapView = (MapView) v.findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();
        //设置缩放等级 zoomTo
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaidumap.setMapStatus(msu);        //设置地图状态
        mBaidumap.setMyLocationEnabled(true);
        mMapView.removeViewAt(2);           //隐藏地图上比例尺

        waitDialog = new WaitDialog();
        bottom_layout = (RelativeLayout) v.findViewById(R.id.bottom2);
        close_way_btn= (ImageButton) v.findViewById(R.id.close_way_btn);
        close_way_btn.setVisibility(View.GONE);
        close_way_btn.setOnClickListener(this);
        location_message = (TextView) v.findViewById(R.id.location_text2);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.navi_map_gps_locked);
        mLocationIcon = BitmapDescriptorFactory.fromBitmap(bmp);
        mLocationModel = MyLocationConfiguration.LocationMode.NORMAL;


        //设置当前位置的图标
        MyLocationConfiguration config = new MyLocationConfiguration(
                mLocationModel,       //设置定位模式
                true, mLocationIcon);
        mBaidumap.setMyLocationConfigeration(config);

        centerToMyPosition(LocationService.myLocation);

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

                location_message.setText(result.getAddress());


            }

            // 地理编码查询结果回调函数  通过地理信息得到坐标
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                }
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
    private void updateMyPosition() {

        if (myLocation.latitude == 0 && myLocation.longitude == 0)
            return;
        MyLocationData data = new MyLocationData.Builder()
                .direction(myLocation.mCurrentx)
                .latitude(myLocation.latitude)
                .longitude(myLocation.longitude)
                .build();

        // 设置当前位置定位数据
        mBaidumap.setMyLocationData(data);


        //LatLng latLng = new LatLng(28.138451,113.000645);
        LatLng latLng = new LatLng(myLocation.latitude, myLocation.longitude);

        //更新底部显示的位置信息
        // 反地理编码  通过坐标查位置
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));

        if (!isMapWay) {
            mBaidumap.clear();
            CircleOptions options = new CircleOptions();
            options.center(new LatLng(myLocation.latitude, myLocation.longitude));
            options.radius(1000);
            options.fillColor(Color.argb(50, 99, 202, 248));
            mBaidumap.addOverlay(options);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        // 释放地理编码检索实例
        geoCoder.destroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();

    }

    private void centerToMyPosition(MyLocation mLocation) {
        if (mLocation.latitude == 0 && mLocation.longitude == 0) {
            //服务中暂无数据  推迟后更新
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    centerToMyPosition(LocationService.myLocation);
                }
            }, 5000);
            return;
        }
        LatLng latLng = new LatLng(mLocation.latitude, mLocation.longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);   //设置位置（经度和纬度）
        mBaidumap.animateMapStatus(msu);
    }


    /**
     * 更新当前地理位置
     *
     * @param myLocation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showLocation(MyLocation myLocation) {
        this.myLocation=myLocation;
        updateMyPosition();
    }

    private boolean isMapWay = false;

    public void showMapWay(MapWayMsg mapWayMsg) {
        isMapWay = true;
        routeWayTemp = mapWayMsg.routeWay;
        close_way_btn.setVisibility(View.VISIBLE);

        switch (routeWayTemp.way_type) {
            case 0:
                //查询步行
                WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
                walkOption.from(PlanNode.withLocation(routeWayTemp.startLocation));// 设置起点
                walkOption.to(PlanNode.withLocation(routeWayTemp.endLocation));// 设置终点
                routePlanSearch.walkingSearch(walkOption);       // 发起步行路线规划
                break;
            case 1:
                //查询小车
                DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
                drivingOption.from(PlanNode.withLocation(routeWayTemp.startLocation));// 设置起点
                drivingOption.to(PlanNode.withLocation(routeWayTemp.endLocation));// 设置终点
                routePlanSearch.drivingSearch(drivingOption);       // 发起步行路线规划
                break;
            case 2:
                //查询公交
                TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
                transitOption.from(PlanNode.withLocation(routeWayTemp.startLocation));// 设置起点
                transitOption.to(PlanNode.withLocation(routeWayTemp.endLocation));// 设置终点
                //transitOption.city(city);
                routePlanSearch.transitSearch(transitOption);       // 发起步行路线规划
                break;
        }

        //等待对话框
        waitDialog.show(activity.getSupportFragmentManager(), "LOADING_DIALOG");
        waitDialog.setCancelable(false);
        handler.sendEmptyMessageDelayed(HandlerUtil.SEAECH_ERROR, 10000);

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

            if (waitDialog != null)
                waitDialog.dismiss();


            if (walkingRouteResult == null
                    || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(context, "抱歉，未找到结果",
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
                Toast.makeText(context, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {

                TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(mBaidumap);
                transitRouteOverlay.setData(transitRouteResult.getRouteLines()
                        .get(routeWayTemp.routePos));// 设置一条驾车路线方案
                transitRouteOverlay.addToMap();
                transitRouteOverlay.zoomToSpan();
                return;


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
            if (waitDialog != null)
                waitDialog.dismiss();
            if (drivingRouteResult == null
                    || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(context, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                        mBaidumap, routeWayTemp.routePos);
                drivingRouteOverlay.setData(drivingRouteResult.getRouteLines()
                        .get(routeWayTemp.routePos));// 设置一条驾车路线方案
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();

            }

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };


    @Override
    public void onClick(View v) {
        close_way_btn.setVisibility(View.GONE);
        isMapWay=false;
        updateMyPosition();
    }
}
