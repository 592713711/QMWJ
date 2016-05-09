package control.map.overlayutil;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;

import java.util.ArrayList;
import java.util.List;

import control.map.MapFragment;

/**
 * 用于显示换乘路线的Overlay，自3.4.0版本起可实例化多个添加在地图中显示
 */
public class TransitRouteOverlay extends OverlayManager {

    private TransitRouteLine mRouteLine = null;

    boolean focus = false;
    private int lineColor = 0;
    private int pos;
    private RouteonClickListener routeonClickListener;
    private TransitRouteResult transitRouteResult;
    private MapFragment mapFragment;

    public void setPos(int i) {
        this.pos = i;
    }

    public int getpos() {
        return this.pos;
    }

    public void setRouteonClickListener(RouteonClickListener listener) {
        this.routeonClickListener = listener;
    }

    public void setTransitRouteResult(TransitRouteResult result) {
        this.transitRouteResult = result;
    }

    /**
     * 构造函数
     *
     * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
     */
    public TransitRouteOverlay(BaiduMap baiduMap, int pos, MapFragment mapFragment) {
        super(baiduMap);
        this.pos = pos;
        this.mapFragment=mapFragment;
    }

    public TransitRouteOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {

        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
        // step node
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {

            for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                Bundle b = new Bundle();
                b.putInt("index", mRouteLine.getAllStep().indexOf(step));
                if (step.getEntrance() != null) {
                    overlayOptionses.add((new MarkerOptions())
                            .position(step.getEntrance().getLocation())
                            .anchor(0.5f, 0.5f).zIndex(10).extraInfo(b)
                            .icon(getIconForStep(step)));
                }
                // 最后路段绘制出口点
                if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                        .getAllStep().size() - 1) && step.getExit() != null) {
                    overlayOptionses.add((new MarkerOptions())
                            .position(step.getExit().getLocation())
                            .anchor(0.5f, 0.5f).zIndex(10)
                            .icon(getIconForStep(step)));
                }
            }
        }

        if (mRouteLine.getStarting() != null) {
            overlayOptionses.add((new MarkerOptions())
                    .position(mRouteLine.getStarting().getLocation())
                    .icon(getStartMarker() != null ? getStartMarker() :
                            BitmapDescriptorFactory
                                    .fromAssetWithDpi("Icon_start.png")).zIndex(10));
        }
        if (mRouteLine.getTerminal() != null) {
            overlayOptionses
                    .add((new MarkerOptions())
                            .position(mRouteLine.getTerminal().getLocation())
                            .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                    BitmapDescriptorFactory
                                            .fromAssetWithDpi("Icon_end.png"))
                            .zIndex(10));
        }
        // polyline
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {

            for (TransitRouteLine.TransitStep step : mRouteLine.getAllStep()) {
                if (step.getWayPoints() == null) {
                    continue;
                }
                int color = 0;
                if (step.getStepType() != TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING) {
//                    color = Color.argb(178, 0, 78, 255);
                    color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255);
                } else {
//                    color = Color.argb(178, 88, 208, 0);
                    color = getLineColor() != 0 ? getLineColor() : Color.argb(178, 88, 208, 0);
                }
                overlayOptionses.add(new PolylineOptions()
                        .points(step.getWayPoints()).width(10).color(color)
                        .zIndex(0));
            }
        }
        return overlayOptionses;
    }

    private BitmapDescriptor getIconForStep(TransitRouteLine.TransitStep step) {
        switch (step.getStepType()) {
            case BUSLINE:
                return BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png");
            case SUBWAY:
                return BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png");
            case WAKLING:
                return BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png");
            default:
                return null;
        }
    }

    /**
     * 设置路线数据
     *
     * @param routeOverlay 路线数据
     */
    public void setData(TransitRouteLine routeOverlay) {
        this.mRouteLine = routeOverlay;
    }

    /**
     * 覆写此方法以改变默认起点图标
     *
     * @return 起点图标
     */
    public BitmapDescriptor getStartMarker() {
        return null;
    }

    /**
     * 覆写此方法以改变默认终点图标
     *
     * @return 终点图标
     */
    public BitmapDescriptor getTerminalMarker() {
        return null;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int color) {
        //Log.d(MainActivity.TAG,"set "+color);
        this.lineColor = color;
    }


    /**
     * 覆写此方法以改变起默认点击行为
     *
     * @param i 被点击的step在
     *          {@link TransitRouteLine#getAllStep()}
     *          中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onRouteNodeClick(int i) {
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().get(i) != null) {
            Log.i("baidumapsdk", "TransitRouteOverlay onRouteNodeClick");
        }
        return false;
    }

    @Override
    public final boolean onMarkerClick(Marker marker) {
        for (Overlay mMarker : mOverlayList) {
            if (mMarker instanceof Marker && mMarker.equals(marker)) {
                if (marker.getExtraInfo() != null) {
                    onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                }
            }
        }
        return true;
    }

    /**
     * 点击路线监听器
     *
     * @param polyline
     * @return
     */
    @Override
    public boolean onPolylineClick(Polyline polyline) {
        boolean flag = false;
        for (Overlay mPolyline : mOverlayList) {
            if (mPolyline instanceof Polyline) {
                if (mPolyline.equals(polyline)) {
                    routeonClickListener.onRouteCLick(this.pos);
                    drawRoute(pos);
                }
            }

        }
        //setFocus(flag);

        return true;
    }

    private void drawRoute(int pos) {
        mBaiduMap.clear();


        int totalLine = transitRouteResult.getRouteLines().size();
        for (int i = totalLine - 1; i >= 0; i--) {
            if (i != pos) {
                TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(
                        mBaiduMap, i,mapFragment);
                transitRouteOverlay.setRouteonClickListener(routeonClickListener);
                transitRouteOverlay.setTransitRouteResult(transitRouteResult);

                //改变路线未被选中
                transitRouteOverlay.setFocus(false);
                //设置路线的层级 注意要比选中路线的层级低
                transitRouteOverlay.setLineColor(Color.parseColor("#bdbdbd"));

                transitRouteOverlay.setData(transitRouteResult.getRouteLines()
                        .get(i));// 设置一条驾车路线方案

                transitRouteOverlay.addToMap();
                transitRouteOverlay.zoomToSpan();
                //设置路线点击监听器
                mBaiduMap.setOnPolylineClickListener(transitRouteOverlay);
                //设置点击路标监听器
                mBaiduMap.setOnMarkerClickListener(transitRouteOverlay);
            }
        }

        //最后添加在所有图层之上
        TransitRouteOverlay selectOverlay = new TransitRouteOverlay(
                mBaiduMap, pos,mapFragment);
        selectOverlay.setData(transitRouteResult.getRouteLines()
                .get(pos));// 设置一条驾车路线方案
        selectOverlay.setRouteonClickListener(routeonClickListener);
        selectOverlay.setTransitRouteResult(transitRouteResult);
        selectOverlay.setLineColor(Color.parseColor("#2196f3"));
        selectOverlay.addToMap();
        selectOverlay.zoomToSpan();
        //设置路线点击监听器
        mBaiduMap.setOnPolylineClickListener(selectOverlay);
        //设置点击路标监听器
        mBaiduMap.setOnMarkerClickListener(selectOverlay);

        mapFragment.showSelectDialog();

    }

    public void setFocus(boolean flag) {
        focus = flag;
        for (Overlay mPolyline : mOverlayList) {
            if (mPolyline instanceof Polyline) {
                // 选中
                ((Polyline) mPolyline).setFocus(flag);

                break;
            }
        }

    }

}
