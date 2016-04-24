package under_control.home.map;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.util.TimeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import under_control.home.service.LocationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class UnderMapFragment extends Fragment {

    private Context context;
    private MapView mMapView = null;
    private BaiduMap mBaidumap;
    private RelativeLayout bottom_layout;
    private TextView location_message;     //对方位置信息
    //反编译地址坐标
    private GeoCoder geoCoder;      // 创建地理编码检索实例
    private MyLocationConfiguration.LocationMode mLocationModel;         //定位模式
    private BitmapDescriptor mLocationIcon; //定位的图标

    public Handler handler=new Handler();

    public UnderMapFragment(Context context) {

        this.context=context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(MyApplication.getInstance());
        EventBus.getDefault().register(this);
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

        bottom_layout = (RelativeLayout) v.findViewById(R.id.bottom2);
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
    private void updateMyPosition(MyLocation mLocation) {
        mBaidumap.clear();
        if (mLocation.latitude == 0 && mLocation.longitude == 0)
            return;
        MyLocationData data = new MyLocationData.Builder()
                .direction(mLocation.mCurrentx)
                .latitude(mLocation.latitude)
                .longitude(mLocation.longitude)
                .build();

        // 设置当前位置定位数据
        mBaidumap.setMyLocationData(data);


        //LatLng latLng = new LatLng(28.138451,113.000645);
        LatLng latLng = new LatLng(mLocation.latitude, mLocation.longitude);

        //更新底部显示的位置信息
        // 反地理编码  通过坐标查位置
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));


        CircleOptions options=new CircleOptions();
        options.center(new LatLng(mLocation.latitude,mLocation.longitude));
        options.radius(1000);
        options.fillColor(Color.argb(50,99,202,248));
        mBaidumap.addOverlay(options);
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
            },5000);
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
       updateMyPosition(myLocation);
    }


}
