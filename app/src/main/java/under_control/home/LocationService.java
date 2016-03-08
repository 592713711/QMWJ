package under_control.home;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import control.map.MyOrientationListener;

public class LocationService extends Service {
    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    public static BDLocation bdLocation=null;
    private float mCurrentx;             //当前位置 x方向的值
    //传感器
    private MyOrientationListener myOrientationListener;

    public LocationService() {
        initLocation();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //启动方向传感器
        myOrientationListener.start();
        if (!mLocationClient.isStarted())
            mLocationClient.start();
        Log.d(Config.TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myOrientationListener.stop();
        if (mLocationClient.isStarted())
            mLocationClient.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationClient = new LocationClient(MyApplication.getInstance());
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        //初始化定位配置
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");       //设置坐标类型
        option.setIsNeedAddress(true);      //设置true后可以得到位置的地址
        option.setOpenGps(true);
        //option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);           //设置定位模式  高精度模式支持室内和室外  gps不支持室内

        option.setScanSpan(5000);       //设置5秒钟刷新一次
        mLocationClient.setLocOption(option);

        myOrientationListener = new MyOrientationListener(this);
        //设置方向传感器回调方法
        myOrientationListener.setOnOrientationListener(onOrientationListener);

    }

    /**
     * 定位监听器
     */
    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            bdLocation=location;
            //routeDB.addRoute(mCurrentlatitude,mCurrentlongitude);
            Log.d(Config.TAG, "开始定位：" + location.getLatitude() + "经度" + location.getLongitude());
            //Log.d(TAG,"距离："+MyUtil.getDistance(new LatLng(savelatitude, savelongitude),new LatLng(location.getLatitude(), location.getLongitude())));

        }
    }

    public MyOrientationListener.OnOrientationListener onOrientationListener = new MyOrientationListener.OnOrientationListener() {
        @Override
        public void onOrientationChanged(float x) {
            //方向传感器回调方法
            //改变定位图标位置
            mCurrentx = x;
        }
    };
}
