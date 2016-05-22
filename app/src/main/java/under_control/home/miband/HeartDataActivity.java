package under_control.home.miband;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.util.TimeUtil;
import com.ld.qmwj.util.WayUtil;
import com.ld.qmwj.view.calendar.CalendarCard;
import com.ld.qmwj.view.calendar.CalenderDialog;
import com.ld.qmwj.view.calendar.CustomDate;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;

/**
 * 根据日期显示心跳数据
 */
public class HeartDataActivity extends AppCompatActivity implements View.OnClickListener {
    protected LineChart mChart;
    protected ArrayList<HeartData> datas;
    private TextView date_text;     //当前日期显示
    private CalenderDialog calenderDialog;  //日期对话框
    protected String selectDate;  //当前选择日期
    protected int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_data);
        id = getIntent().getIntExtra("monitor_id", 0);
        initWindow();
        initView();
        initData();
        initLineChart();
    }

    protected void initView() {
        selectDate = TimeUtil.getNowDateStr();
        date_text = (TextView) findViewById(R.id.date_text);
        date_text.setText(selectDate);
        date_text.setOnClickListener(this);

        calenderDialog = new CalenderDialog(this, R.style.dialog, cellClickListener);
        calenderDialog.initDialog(this);
    }

    protected void initData() {
        long fromtime = TimeUtil.timeStrToLong(selectDate);
        long totime = TimeUtil.timeStrToLong2(selectDate);
        datas = MyApplication.getInstance().getHeartDao().getAllHeartDataListByTime(id, fromtime, totime);
    }

    protected void initLineChart() {
        mChart = (LineChart) findViewById(R.id.heart_chart);
        // no description text
        mChart.setDescription("");//设置图表描述信息
        //没有数据时显示
        mChart.setNoDataTextDescription("暂时没有当天心跳数据");

        // enable touch gestures
        mChart.setTouchEnabled(true);       // 设置是否可以触摸

        // enable scaling and dragging
        mChart.setDragEnabled(true);        // 是否可以拖拽
        mChart.setScaleEnabled(true);       // 是否可以缩放
        mChart.setDrawGridBackground(false);         //设置是否显示表格

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(getResources().getColor(R.color.red));


        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();      // 设置比例图标示，就是那个一组y的value的

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();//设置是否显示表格
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);//设置是否显示X轴表格
        xl.setAvoidFirstLastClipping(true);//设置x轴起点和终点label不超出屏幕
        xl.setSpaceBetweenLabels(5);// 设置x轴label间隔
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);       //设置X轴的显示位置


        //设置两条警戒线
        LimitLine ll1 = new LimitLine(100, "偏高");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(8f);
        ll1.setTextColor(Color.rgb(244, 246, 16));
        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(60, "偏低");
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(8f);
        ll2.setTextColor(Color.rgb(244, 246, 16));
        ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();//得到图表的左侧Y轴实例
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(200f);// 设置Y轴最大值
        leftAxis.setAxisMinValue(30f);// 设置Y轴最小值。
        leftAxis.addLimitLine(ll1);     //设置警戒线
        leftAxis.addLimitLine(ll2);
        leftAxis.setDrawGridLines(true);
        // leftAxis.enableGridDashedLine(10f, 10f, 0f); //设置横向表格为虚线

        // limit lines are drawn behind data (and not on top)
        // leftAxis.setDrawLimitLinesBehindData(true);


        YAxis rightAxis = mChart.getAxisRight();//设置右侧Y轴不可用（这里可以向得到左侧Y轴那样，得到右侧Y轴实例去处理）
        rightAxis.setEnabled(false);

        // addEntry();


    }


    /**
     * 添加数据到图表
     */
    protected void addEntry(HeartData heartData) {

        LineData data = mChart.getData();
        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }


            data.addXValue(TimeUtil.getTimeStr4(heartData.time));
            data.addEntry(new Entry(heartData.data, set.getEntryCount()), 0);

            // add a new x-value first
            //data.addXValue(mMonths[data.getXValCount() % 12] + " "
            //       + (year + data.getXValCount() / 12));
            //data.addEntry(new Entry((float) (Math.random() * 40) + 30f, set.getEntryCount()), 0);


            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries   每段最多显示的数据数量
            mChart.setVisibleXRangeMaximum(15);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry  移动到视图最后一段
            mChart.moveViewToX(data.getXValCount() - 16);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            //YAxis.AxisDependency.LEFT);
        }
    }

    protected void updateData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        if (datas.size() == 0)
            mChart.clear();

        animateAdd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mChart.animateX(3000);      //动画

        updateData();
    }

    protected LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setColor(Color.rgb(247, 141, 16));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(Color.rgb(247, 141, 16));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);

        return set;
    }


    /**
     * 以动画的的形式添加数据
     * 一个个添加   然后更新视图
     */
    protected void animateAdd() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < datas.size(); i++) {
                    final HeartData heartData = datas.get(i);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry(heartData);
                        }
                    });

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 初始化通知栏颜色
     */
    protected void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.red);//通知栏所需颜色

    }

    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
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
        if (v.getId() == R.id.date_text)
            calenderDialog.show();
    }

    /**
     * 日期点击监听器
     */
    CalendarCard.OnCellClickListener cellClickListener = new CalendarCard.OnCellClickListener() {
        @Override
        public void clickDate(CustomDate date) {

            Log.d(Config.TAG, "点击：" + date.toString());
            selectDate = date.toString().replace("_", "-");
            date_text.setText(selectDate);
            long fromtime = TimeUtil.timeStrToLong(selectDate);
            long totime = TimeUtil.timeStrToLong2(selectDate);
            datas = MyApplication.getInstance().getHeartDao().getAllHeartDataListByTime(id, fromtime, totime);
            calenderDialog.dismiss();
            updateData();
        }

        @Override
        public void changeDate(CustomDate date) {
            if (calenderDialog != null)
                calenderDialog.monthText.setText(date.month + "月");
        }
    };
    public void doQuit(View v) {
        finish();
    }

}
