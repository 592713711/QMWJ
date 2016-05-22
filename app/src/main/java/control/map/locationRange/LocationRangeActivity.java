package control.map.locationRange;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.request.LocRangeRequest;
import com.ld.qmwj.model.LocationRange;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.TimeUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LocationRangeActivity extends AppCompatActivity implements View.OnClickListener {
    private Monitor monitor;
    private Gson gson;
    private MapView mMapView = null;
    private BaiduMap mBaidumap;
    private LocationRange locationRange;
    private EditText mark_edit;
    private TextView location_msg_text;
    private SeekBar seekBar;
    private TextView radius_text;
    private RelativeLayout head_layout;     //用于动画显示
    private RelativeLayout bottom_layout;
    private RelativeLayout bottom_layout2;      //手绘
    private CoordinatorLayout coordinatorLayout;

    //标记相关
    BitmapDescriptor mMarkerIcon;

    //反编译
    GeoCoder geoCoder;      // 创建地理编码检索实例

    //输入对话框
    AlertDialog rangeDialog;
    Dialog addressDialog;

    private EditText address_edit;
    private EditText city_edit;
    private EditText range_edit;

    //手绘
    private CheckBox draw_box;
    private ArrayList<LatLng> points;    //存放手动点击的
    private ArrayList<BitmapDescriptor> bitmapDescriptorList;        //覆盖物位图
    private ArrayList<Marker> markerList;       //覆盖物集合
    private ArrayList<Polyline> polylineList;   //连线集合


    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_range);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(MyApplication.getInstance());
        gson = new Gson();
        locationRange = gson.fromJson(getIntent().getStringExtra("locationRange"), LocationRange.class);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initView();
        initData();
        initDialog();

    }


    private void initData() {
        initGeoCoder();
        points = new ArrayList<>();
        markerList = new ArrayList<>();
        polylineList = new ArrayList<>();

        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.turn_start);
        Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.turn_dest);
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        bmp1 = Bitmap.createBitmap(bmp1, 0, 0, bmp1.getWidth(), bmp1.getHeight(), matrix, true);
        bmp2 = Bitmap.createBitmap(bmp2, 0, 0, bmp2.getWidth(), bmp2.getHeight(), matrix, true);

        bitmapDescriptorList = new ArrayList<>();
        bitmapDescriptorList.add(BitmapDescriptorFactory.fromBitmap(bmp1));
        bitmapDescriptorList.add(BitmapDescriptorFactory.fromBitmap(bmp2
        ));


        seekBar.setMax(Config.MAXRANG - Config.MINRANG);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        if (locationRange.latLng != null) {
            location_msg_text.setText(locationRange.location_name);     //地址
            mark_edit.setText(locationRange.location_remark);       //地址备注
            seekBar.setProgress(locationRange.range - Config.MINRANG);
            if (locationRange.latLng.latitude != 0 && locationRange.latLng.longitude != 0)
                moveMapTo(locationRange.latLng);
            drawOverlay(locationRange.latLng, locationRange.range);

            // 反地理编码  通过坐标查位置
            if (locationRange.location_remark == null) {
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(locationRange.latLng));
            }

        } else {
            seekBar.setProgress(500 - Config.MINRANG);
            //绘制手绘区域
            location_msg_text.setText("");     //地址
            mark_edit.setText(locationRange.location_remark);       //地址备注

            draw_box.setChecked(true);
            points = locationRange.points;
            mBaidumap.clear();
            //构建用户绘制多边形  可以填充
            OverlayOptions polygonOption = new PolygonOptions()
                    .points(points)
                    .fillColor(Color.argb(50, 99, 202, 248));
            //在地图上添加多边形Option，用于显示
            mBaidumap.addOverlay(polygonOption);
            moveMapTo(points.get(0));

        }

        if (monitor.identify != Config.MAIN_GUARDIAN) {
            //不是主监护方 不能更改数据
            mark_edit.setEnabled(false);
            seekBar.setEnabled(false);
            draw_box.setEnabled(false);
            mBaidumap.setOnMapClickListener(null);

        }


    }

    private void initView() {
        mark_edit = (EditText) findViewById(R.id.mark_edit);
        location_msg_text = (TextView) findViewById(R.id.location_msg);
        seekBar = (SeekBar) findViewById(R.id.seekbar_range);
        radius_text = (TextView) findViewById(R.id.radius_text);
        radius_text.setOnClickListener(this);
        head_layout = (RelativeLayout) findViewById(R.id.location_msg_content);
        bottom_layout = (RelativeLayout) findViewById(R.id.bottom);
        bottom_layout2 = (RelativeLayout) findViewById(R.id.bottom2);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        (findViewById(R.id.cancel_btn)).setOnClickListener(this);
        (findViewById(R.id.sure_btn)).setOnClickListener(this);
        (findViewById(R.id.cancel_btn2)).setOnClickListener(this);
        (findViewById(R.id.sure_btn2)).setOnClickListener(this);
        (findViewById(R.id.search_btn)).setOnClickListener(this);
        (findViewById(R.id.undo_btn)).setOnClickListener(this);


        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();

        //设置缩放等级 zoomTo
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaidumap.setMapStatus(msu);        //设置地图状态
        mBaidumap.setMyLocationEnabled(true);
        mMapView.removeViewAt(2);           //隐藏地图上比例尺

        //设置地图点击监听器
        mBaidumap.setOnMapClickListener(mapClickLis);
        mBaidumap.setOnMarkerClickListener(onMarkerClickListener);
        //初始化标记物
        mMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.mark2);

        draw_box = (CheckBox) findViewById(R.id.draw_btn);
        draw_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //手动绘制
                    bottom_layout.setVisibility(View.GONE);
                    bottom_layout2.setVisibility(View.VISIBLE);     // 显示绘制的画笔
                    mBaidumap.clear();
                    drawMarkers();
                    drawLines();
                } else {
                    mBaidumap.clear();
                    bottom_layout.setVisibility(View.VISIBLE);
                    bottom_layout2.setVisibility(View.GONE);
                    drawOverlay(locationRange.latLng, locationRange.range);
                }
                showLayoutAnim();
            }
        });
    }


    /**
     * 将地图移动到指定位置
     *
     * @param latLng
     */
    public void moveMapTo(LatLng latLng) {

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);   //设置位置（经度和纬度）
        mBaidumap.animateMapStatus(msu);      //使用动画的方式更新地图  不是瞬间跳到当前的位置 而是有个动画移动的效果

    }


    /**
     * 在指定位置覆盖物  绘制标记  绘制圆
     *
     * @param latLng
     * @param range
     */
    public void drawOverlay(LatLng latLng, int range) {
        if (latLng != null) {

            mBaidumap.clear();
            //构造OverlayOptions（地图覆盖物选型基类）
            OverlayOptions options = new MarkerOptions()
                    .position(latLng)      // 设置marker的位置
                    .icon(mMarkerIcon)      // 设置marker的图标
                    .zIndex(5);             // 设置marker的所在层級

            // 在地图上添加marker，并显示
            mBaidumap.addOverlay(options);

            CircleOptions options_circle = new CircleOptions();
            options_circle.center(latLng);
            options_circle.radius(range);
            options_circle.fillColor(Color.argb(50, 99, 202, 248));
            mBaidumap.addOverlay(options_circle);
        }
    }

    /**
     * 以动画的方式显示布局
     */
    public void showLayoutAnim() {
        Animation anmi = AnimationUtils.loadAnimation(this, R.anim.head_coming);
        Animation anmi2 = AnimationUtils.loadAnimation(this, R.anim.bottom_coming);
        anmi.setDuration(1000);
        anmi2.setDuration(1000);
        head_layout.startAnimation(anmi);
        if (draw_box.isChecked()) {
            bottom_layout2.setAnimation(anmi2);
        } else {
            bottom_layout.setAnimation(anmi2);
        }
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
                String msg = "";
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    msg = "抱歉，未能找到结果";
                } else {
                    msg = result.getAddress();

                }
                locationRange.location_name = msg;
                location_msg_text.setText(msg);

            }

            // 地理编码查询结果回调函数  通过地理信息得到坐标
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                //输入地址后回调
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocationRangeActivity.this, "未能找到指定位置，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //查找到结果
                    locationRange.latLng = result.getLocation();
                    locationRange.location_name = result.getAddress();
                    location_msg_text.setText(result.getAddress());
                    moveMapTo(result.getLocation());
                    drawOverlay(result.getLocation(), locationRange.range);
                }
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);


        // 释放地理编码检索实例
        // geoCoder.destroy();
    }


    /**
     * 地图点击监听器
     */

    BaiduMap.OnMapClickListener mapClickLis = new BaiduMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {

            if (!draw_box.isChecked()) {
                //地图移动到点击位置
                moveMapTo(latLng);
                locationRange.latLng = latLng;
                //绘制覆盖物
                drawOverlay(latLng, locationRange.range);
                // 反地理编码  通过坐标查位置
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
            } else {
                //手动绘制
                points.add(latLng);
                drawLine();
                drawMarker(latLng, false);
            }
        }

        /**
         * 地图自带覆盖物点击回调
         * @param mapPoi
         * @return
         */
        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            if (draw_box.isChecked()) {
                //手动绘制
                points.add(mapPoi.getPosition());
                drawLine();
                drawMarker(mapPoi.getPosition(), false);
            }
            return false;
        }
    };

    /**
     * 覆盖物点击监听器
     */
    public BaiduMap.OnMarkerClickListener onMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.d(Config.TAG, "点击了覆盖物");
            LatLng firstLatlng = points.get(0);
            LatLng clickLatlng = marker.getPosition();
            if (clickLatlng.latitude == firstLatlng.latitude && clickLatlng.longitude == firstLatlng.longitude) {
                points.add(clickLatlng);
                drawLine();
                drawMarker(clickLatlng, true);
            }
            return false;
        }
    };

    /**
     * 根据点击的点绘制线段
     */
    private void drawLine() {
        if (points.size() >= 2) {
            List<LatLng> lines = points.subList(points.size() - 2, points.size());
            OverlayOptions ooPolyline = new PolylineOptions()
                    .points(lines)
                    .width(7)

                    .color(Color.parseColor("#5ecee4"));
            //在地图上添加多边形Option，用于显示
            Polyline polyline = (Polyline) mBaidumap.addOverlay(ooPolyline);
            polylineList.add(polyline);

        }
    }

    /**
     * 绘制所有线段
     */
    private void drawLines() {
        polylineList.clear();
        for (int i = 0; i < points.size() - 1; i++) {
            ArrayList<LatLng> temp = new ArrayList<>();
            temp.add(points.get(i));
            temp.add(points.get(i + 1));
            OverlayOptions ooPolyline = new PolylineOptions()
                    .points(temp)
                    .width(7)
                    .color(Color.parseColor("#5ecee4"));
            polylineList.add((Polyline) mBaidumap.addOverlay(ooPolyline));
        }


    }

    /**
     * 在指定坐标绘制标记物
     *
     * @param latLng
     * @param isEnd
     */
    public void drawMarker(LatLng latLng, boolean isEnd) {
        BitmapDescriptor bitmapDescriptor;
        if (points.size() == 1 || isEnd) {
            bitmapDescriptor = bitmapDescriptorList.get(0);
        } else
            bitmapDescriptor = bitmapDescriptorList.get(1);

        OverlayOptions option = new MarkerOptions()
                .icon(bitmapDescriptor)
                .anchor(0.5f, 0.5f)
                .animateType(MarkerOptions.MarkerAnimateType.grow)
                .perspective(true)
                .position(latLng);

        Marker marker = (Marker) mBaidumap.addOverlay(option);
        markerList.add(marker);

        if (isEnd)
            addRange2();
    }

    /**
     * 绘制所有标记物
     */
    public void drawMarkers() {
        markerList.clear();
        for (int i = 0; i < points.size(); i++) {
            LatLng latlng = points.get(i);
            MarkerOptions option = new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .animateType(MarkerOptions.MarkerAnimateType.grow)
                    .perspective(true)
                    .position(latlng);
            if (i == 0)
                option.icon(bitmapDescriptorList.get(0));
            else
                option.icon(bitmapDescriptorList.get(1));
            Marker marker = (Marker) mBaidumap.addOverlay(option);
            markerList.add(marker);
        }

    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int range = progress + Config.MINRANG;
            radius_text.setText(range + "米");
            locationRange.range = range;
            drawOverlay(locationRange.latLng, locationRange.range);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel_btn || id == R.id.cancel_btn2) {
            finish();
        } else if (id == R.id.sure_btn) {
            if (monitor.identify != Config.MAIN_GUARDIAN) {
                Toast.makeText(this, "只有主监护方才可以修改数据", Toast.LENGTH_SHORT).show();
                return;
            }

            //添加到数据库，并发送数据到服务端
            addRange();

        } else if (id == R.id.sure_btn2) {
            if (monitor.identify != Config.MAIN_GUARDIAN) {
                Toast.makeText(this, "只有主监护方才可以修改数据", Toast.LENGTH_SHORT).show();
                return;
            }
            //添加手动绘制，并发送数据到数据库
            addRange2();

        } else if (id == R.id.radius_text) {
            if (monitor.identify != Config.MAIN_GUARDIAN) {
                Toast.makeText(this, "只有主监护方才可以修改数据", Toast.LENGTH_SHORT).show();
                return;
            }
            //自己手动设置范围
            rangeDialog.show();
            rangeDialog.setCancelable(false);
        } else if (id == R.id.search_btn) {
            if (monitor.identify != Config.MAIN_GUARDIAN) {
                Toast.makeText(this, "只有主监护方才可以修改数据", Toast.LENGTH_SHORT).show();
                return;
            }
            //等待对话框
            addressDialog.show();
            addressDialog.setCancelable(false);
        } else if (id == R.id.undo_btn) {
            if (monitor.identify != Config.MAIN_GUARDIAN) {
                Toast.makeText(this, "只有主监护方才可以修改数据", Toast.LENGTH_SHORT).show();
                return;
            }
            //返回上一次绘制的状态
            if (points.size() > 0 && polylineList.size() > 0 && markerList.size() > 0) {

                points.remove(points.size() - 1);
                if (polylineList.size() > 0) {
                    polylineList.get(polylineList.size() - 1).remove();
                    polylineList.remove(polylineList.size() - 1);
                }
                markerList.get(markerList.size() - 1).remove();
                markerList.remove(markerList.size() - 1);
            } else {
                if (points.size() == 1) {
                    points.remove(points.size() - 1);
                    markerList.remove(markerList.size() - 1);
                }
                mBaidumap.clear();
                drawMarkers();
                drawLines();

            }
        }
    }

    /**
     * 通过手绘监护区 添加到服务器
     */
    private void addRange2() {

        if (points.size() <= 2) {
            Toast.makeText(this, "必须要超过2个点才能构成安全区域", Toast.LENGTH_LONG).show();
            return;
        }
        //取消点击监听
        mBaidumap.setOnMapClickListener(null);

        LatLng endLatLng = points.get(points.size() - 1);
        LatLng startLatng = points.get(0);
        //如果最后一个点是起点的话 删除
        //多边形不能有相同点的点
        if (endLatLng.latitude == startLatng.latitude && endLatLng.longitude == startLatng.longitude) {
            //删除点
            points.remove(points.size() - 1);
            //删除线段
            polylineList.remove(polylineList.size() - 1);
            //删除覆盖物
            markerList.remove(markerList.size() - 1);
        }


        mBaidumap.clear();
        //构建用户绘制多边形  可以填充
        OverlayOptions polygonOption = new PolygonOptions()
                .points(points)
                .fillColor(Color.argb(50, 99, 202, 248));
        //在地图上添加多边形Option，用于显示
        mBaidumap.addOverlay(polygonOption);

        /**
         * 弹出snackbar
         */
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "是否提交数据", Snackbar.LENGTH_LONG);

        snackbar.setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送消息
                if (mark_edit.getText().toString().isEmpty()) {
                    Toast.makeText(LocationRangeActivity.this, "请为监护区域设置备注", Toast.LENGTH_SHORT).show();
                } else {
                    //发送数据到服务端
                    if (MsgHandle.getInstance().channel != null) {
                        //网络正常  添加到数据库
                        locationRange.points = points;
                        locationRange.latLng = null;
                        locationRange.location_remark = mark_edit.getText().toString();
                        MyApplication.getInstance().getAuthDao().updateLacationRange(monitor.id, locationRange);

                        sendRangetoServer();

                        finish();
                        return;
                    } else {
                        Toast.makeText(LocationRangeActivity.this, "当前网络异常，尚未连接服务器", Toast.LENGTH_SHORT).show();
                    }
                }
                mBaidumap.clear();
                drawLines();
                drawMarkers();
                mBaidumap.setOnMapClickListener(mapClickLis);

            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                /**
                 * 不是点击消失
                 */
                if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event ==
                        DISMISS_EVENT_CONSECUTIVE) {
                    mBaidumap.clear();
                    drawLines();
                    drawMarkers();
                    mBaidumap.setOnMapClickListener(mapClickLis);
                }
            }
        });
        snackbar.show();


    }

    /**
     * 添加监护区域
     */
    private void addRange() {

        if (locationRange.latLng == null)
            Toast.makeText(this, "单击地图设置监护中心", Toast.LENGTH_SHORT).show();
        else if (mark_edit.getText().toString().isEmpty())
            Toast.makeText(this, "请为监护区域设置备注", Toast.LENGTH_SHORT).show();
        else {
            //设置监护区域
            Log.d(Config.TAG, "安全区域" + locationRange.toString());

            if (MsgHandle.getInstance().channel != null) {
                //网络正常  添加到数据库
                locationRange.points = null;
                locationRange.location_remark = mark_edit.getText().toString();
                MyApplication.getInstance().getAuthDao().updateLacationRange(monitor.id, locationRange);

                sendRangetoServer();

                finish();
            } else {
                Toast.makeText(this, "当前网络异常，尚未连接服务器", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 发送数据给服务端
     */
    public void sendRangetoServer() {
        LocRangeRequest request = new LocRangeRequest();
        request.locationRange = locationRange;
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
    }

    /**
     * 初始化输入对话框
     */
    private void initDialog() {

        //初始化对话框
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = inflater.inflate(R.layout.dialog_input2, null);
        address_edit = (EditText) view.findViewById(R.id.address_input);
        city_edit = (EditText) view.findViewById(R.id.city_input);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {//下面三句控制弹框的关闭
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    if (address_edit.getText().toString().isEmpty()) {
                        Toast.makeText(LocationRangeActivity.this, "请输入位置", Toast.LENGTH_SHORT).show();
                        //不关闭对话框
                        field.set(dialog, false);//true表示要关闭
                        return;
                    }
                    geoCoder.geocode(new GeoCodeOption()
                            .city(city_edit.getText().toString())
                            .address(address_edit.getText().toString()));
                    field.set(dialog, true);//true表示要关闭
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {//下面三句控制弹框的关闭
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);//true表示要关闭
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setView(view);
        addressDialog = builder.create();

        //半径输入框
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        View view2 = inflater.inflate(R.layout.dialog_input, null);
        ((TextView) view2.findViewById(R.id.textView7)).setText("输入安全区域半径");
        range_edit = (EditText) view2.findViewById(R.id.address_input);
        range_edit.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {//下面三句控制弹框的关闭
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    if (range_edit.getText().toString().isEmpty()) {
                        Toast.makeText(LocationRangeActivity.this, "请输入半径", Toast.LENGTH_SHORT).show();
                        field.set(dialog, false);//true表示要关闭
                        return;
                    }
                    int range = Integer.parseInt(range_edit.getText().toString());
                    if (range > seekBar.getMax() - Config.MINRANG) {
                        //移动的是滑动条的数值  监听器中会自动转换成实际数值显示
                        seekBar.setMax(range - Config.MINRANG);
                        seekBar.setProgress(range - Config.MINRANG);
                    } else if (range < Config.MINRANG) {
                        seekBar.setProgress(0);
                    } else {
                        seekBar.setProgress(range - Config.MINRANG);
                    }

                    field.set(dialog, true);//true表示要关闭
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder2.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {//下面三句控制弹框的关闭
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);//true表示要关闭
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder2.setView(view2);
        rangeDialog = builder2.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        showLayoutAnim();

    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        // 释放地理编码检索实例
        geoCoder.destroy();
    }

    public void doQuit(View v) {
        finish();
    }

}
