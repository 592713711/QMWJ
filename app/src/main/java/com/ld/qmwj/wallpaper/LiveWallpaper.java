package com.ld.qmwj.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.HeartDataRequest;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.BandState;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.TimeUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * 桌面壁纸
 *
 * @author zsg
 */
public class LiveWallpaper extends WallpaperService {
    // 实现WallpaperService必须实现的抽象方法
    private ArrayList<Monitor> monitors;
    // 定义一个Handler
    Handler mHandler = new Handler();
    // 定义一个周期性执行的任务
    private final Runnable updateMonitor = new Runnable() {
        public void run() {
            //更新监护人信息
            monitors = MyApplication.getInstance().getRelateDao().getList();
            mHandler.postDelayed(updateMonitor, 5000);

        }
    };

    private final Runnable requestLocationRunable = new Runnable() {
        public void run() {
            //更新监护人信息
            requestPosition();
            mHandler.postDelayed(requestLocationRunable, 30000);

        }
    };

    public Engine onCreateEngine() {
        monitors = MyApplication.getInstance().getRelateDao().getList();
        mHandler.postDelayed(updateMonitor, 5000);
        mHandler.post(requestLocationRunable);
        // 返回自定义的Engine
        return new MyEngine();
    }

    class MyEngine extends Engine {
        // 记录程序界面是否可见
        private boolean mVisible;
        private Bitmap bg_bitmap;       //桌面背景
        private Bitmap snow_bitmap;     //雪花
        private Bitmap note_bitmap;     //note
        private RectF bg_rectf;         //用来缩放背景的rectf
        private RectF note_rectf[]=new RectF[3];       //用来缩放note的rectf

        private Integer[] offsetY = {5, 6, 7, 8, 9};
        private Integer[] offsetx = {-2, 2, -3, -4, 4, 3};
        MyFlower flowers[] = new MyFlower[18];
        Random random;
        int width;          //屏幕长宽
        int height;
        float density;      //屏幕像素密度
        float fontScale;    //sp放大值
        int note_startx[]=new int[3];    //note 背景起始坐标  x
        int note_startY[]=new int[3];    //note 背景起始坐标  y
        String noteTip[] = {"连接状态:", "地理位置:", "位置状态:", "手环状态:", "心率:"};

        // 定义画笔
        private Paint mPaint = new Paint();
        private Paint mSnowpaint = new Paint();
        private Paint warnPaint = new Paint();
        private Paint greenPaint = new Paint();
        private Paint normalPaint = new Paint();
        private Paint markname_Paint = new Paint();

        // 定义一个周期性执行的任务
        private final Runnable drawTarget = new Runnable() {
            public void run() {
                // 动态地绘制图形
                drawFrame();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            getDensity();
            // 初始化画笔
            mPaint.setColor(Color.BLACK);
            mPaint.setAntiAlias(true);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setStrokeWidth(1);
            mPaint.setTextSize(fontScale);
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            //警告画笔
            warnPaint.setColor(getResources().getColor(R.color.not_online_state));
            warnPaint.setAntiAlias(true);
            warnPaint.setTypeface(Typeface.DEFAULT_BOLD);
            warnPaint.setStrokeWidth(1);
            warnPaint.setTextSize(fontScale);
            warnPaint.setStrokeCap(Paint.Cap.ROUND);

            //正常画笔
            normalPaint.setColor(Color.parseColor("#a0a0a0"));
            normalPaint.setAntiAlias(true);
            normalPaint.setTypeface(Typeface.DEFAULT_BOLD);
            normalPaint.setStrokeWidth(1);
            normalPaint.setTextSize(fontScale);
            normalPaint.setStrokeCap(Paint.Cap.ROUND);

            //绿色画笔
            greenPaint.setColor(getResources().getColor(R.color.online_state));
            greenPaint.setAntiAlias(true);
            greenPaint.setTypeface(Typeface.DEFAULT_BOLD);
            greenPaint.setStrokeWidth(1);
            greenPaint.setTextSize(fontScale);
            greenPaint.setStrokeCap(Paint.Cap.ROUND);

            markname_Paint.setColor(getResources().getColor(R.color.statusbar_bg));
            markname_Paint.setAntiAlias(true);
            markname_Paint.setTypeface(Typeface.DEFAULT_BOLD);
            markname_Paint.setStrokeWidth(1);
            markname_Paint.setTextSize(fontScale+fontScale/2);
            markname_Paint.setStrokeCap(Paint.Cap.ROUND);


            // 设置处理触摸事件
            setTouchEventsEnabled(true);

            bg_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.screen_bg);
            snow_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snow);
            note_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.note2);
            random = new Random();

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            // 删除回调
            mHandler.removeCallbacks(drawTarget);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            // 当界面可见时候，执行drawFrame()方法。
            if (visible) {
                // 动态地绘制图形
                drawFrame();
            } else {
                // 如果界面不可见，删除回调
                mHandler.removeCallbacks(drawTarget);
            }
        }


        public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
                                     float yStep, int xPixels, int yPixels) {
            Log.d("gg", "onOffsetsChanged");
            //drawFrame();
        }

        boolean isFirst = true;

        // 定义绘制图形的工具方法
        private void drawFrame() {

            // 获取该壁纸的SurfaceHolder
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            // 对画布加锁
            c = holder.lockCanvas();
            if (isFirst) {
                //第一次得到画布的宽高
                width = c.getWidth();
                height = c.getHeight();
                Log.d("gg", width + " " + height);
                addRect();
                note_startx[0] = width / 2 - note_bitmap.getWidth() / 2;
                note_startY[0] = 100;

                note_startx[1] = width / 2 - note_bitmap.getWidth() / 2;
                note_startY[1] = (int) (130+150*density);

                note_startx[2] = width / 2 - note_bitmap.getWidth() / 2;
                note_startY[2] = (int) (160+300*density);

                isFirst = false;
                DisplayMetrics dm = getResources().getDisplayMetrics();
                int W = dm.widthPixels;
                int H = dm.heightPixels;
                bg_rectf = new RectF(0, 0, W, H);
                note_rectf[0] = new RectF(note_startx[0], note_startY[0], note_startx[0] + 200 * density, note_startY[0] + 150 * density);
                note_rectf[1] = new RectF(note_startx[1], note_startY[1], note_startx[1] + 200 * density, note_startY[1] + 150 * density);
                note_rectf[2] = new RectF(note_startx[2], note_startY[2], note_startx[2] + 200 * density, note_startY[2] + 150 * density);
            }

            // 绘制背景
            c.drawBitmap(bg_bitmap, null, bg_rectf, null);

            //绘制被监护状态信息
            for (int i = 0; i < monitors.size(); i++) {
                c.drawBitmap(note_bitmap, null, note_rectf[i], null);
                //绘制每个提示信息
                for (int j = 0; j < noteTip.length; j++) {
                    c.drawText(noteTip[j], note_startx[i] + 15 * density, note_startY[i] + 50 * density + j * 20 * density, mPaint);
                }


                Monitor monitor = monitors.get(i);
                c.drawText(monitor.remark_name, note_startx[i] + 130 * density, note_startY[i] +40 * density,markname_Paint);
                if(MsgHandle.getInstance().channel==null){
                    warnPaint.setTextSize(fontScale+fontScale/3);
                    c.drawText("尚未连接服务器", note_startx[i] + 70 * density, note_startY[i] + 80 * density,warnPaint);
                    continue;
                }
                warnPaint.setTextSize(fontScale);
                if (monitor.state == Config.ONLINE_STATE) {
                    //在线
                    c.drawText("监护中", note_startx[i] + 65 * density, note_startY[i] + 50 * density, greenPaint);
                    //位置状态
                    c.drawText(monitor.location_msg, note_startx[i] + 65 * density, note_startY[i] + 50 * density + 1 * 20 * density, normalPaint);
                    //位置状态
                    if (monitor.safe == Config.SAFE_LOCATION)
                        c.drawText("安全", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 2 * 20 * density, greenPaint);
                    else if (monitor.safe == Config.UNSAFE_LOCATION)
                        c.drawText("危险", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 2 * 20 * density, warnPaint);
                    else
                        c.drawText("", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 2 * 20 * density, greenPaint);


                    if (monitor.bandstate == BandState.UNBIND)
                        c.drawText("未绑定", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 3 * 20 * density, normalPaint);
                    else if (monitor.bandstate == BandState.UNCONNECT)
                        //手环未连接
                        c.drawText("未连接", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 3 * 20 * density, normalPaint);
                    else if (monitor.bandstate == BandState.CONNECT) {
                        c.drawText("已连接", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 3 * 20 * density, greenPaint);
                    }

                    if (monitor.bandstate == BandState.CONNECT) {
                        if (monitor.heart_rate <= 110 && monitor.heart_rate >= 60)
                            c.drawText(monitor.heart_rate + "", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 4 * 20 * density, greenPaint);
                        else
                            c.drawText(monitor.heart_rate + "", note_startx[i] + 65 * density, note_startY[i] + 50 * density + 4 * 20 * density, warnPaint);
                    }

                } else {
                    c.drawText("未连接", note_startx[i] + 65 * density, note_startY[i] + 50 * density, warnPaint);
                }
            }


            //绘制雪花
            for (int i = 0; i < flowers.length; i++) {
                MyFlower rect = flowers[i];
                rect.time--;
                if (rect.time >= 0) {
                    rect.y += rect.speed_y;
                    rect.x += rect.speed_x;
                    //c.save();
                    mSnowpaint.setAlpha(rect.alpha);    //设置透明度
                    c.drawBitmap(rect.snow, rect.x, rect.y, mSnowpaint);
                    //c.restore();

                    if (rect.y >= height) {
                        rect.init();
                    } else if (rect.x >= width || rect.x < -60) {
                        rect.init();
                    }
                } else {
                    rect.init();

                }

            }
            holder.unlockCanvasAndPost(c);
            mHandler.postDelayed(drawTarget, 100);
        }

        public void addRect() {
            for (int i = 0; i < flowers.length; i++) {
                flowers[i] = new MyFlower();
                flowers[i].y += random.nextInt(700);
            }
        }

        class MyFlower {
            int x;        //坐标
            int y;
            float s;    //缩放比例
            int alpha;        //透明度
            int time;        //持续时间
            int speed_y;        //y 速度
            int speed_x;
            Bitmap snow;

            public void init() {
                float aa = random.nextFloat();
                this.x = random.nextInt(width) + 20;
                this.y = random.nextInt(80);
                if (aa >= 0.7) {
                    this.s = 0.7f;
                } else if (aa <= 0.2) {
                    this.s = 0.3f;
                } else {
                    this.s = aa;
                }

                this.snow = scaleBitmap(this.s);
                this.alpha = random.nextInt(180) + 60;
                this.time = random.nextInt(100) + 20;
                this.speed_y = offsetY[random.nextInt(5)];
                this.speed_x = offsetx[random.nextInt(6)];

            }

            public MyFlower() {
                super();
                init();
            }

            private Bitmap scaleBitmap(float scale) {
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale); //长和宽放大缩小的比例
                Bitmap resizeBmp = Bitmap.createBitmap(snow_bitmap, 0, 0, snow_bitmap.getWidth(), snow_bitmap.getHeight(), matrix, true);
                return resizeBmp;
            }

            @Override
            public String toString() {
                return "MyFlower{" +
                        "x=" + x +
                        ", y=" + y +
                        ", s=" + s +
                        ", alpha=" + alpha +
                        ", time=" + time +
                        ", speed_y=" + speed_y +
                        ", speed_x=" + speed_x +
                        '}';
            }
        }

        private void getDensity() {
            //获得屏幕dpi 像素密度
            DisplayMetrics metric = new DisplayMetrics();
            //getResources().getWindowManager().getDefaultDisplay().getMetrics(metric);
            density = getResources().getDisplayMetrics().density;
            fontScale = getResources().getDisplayMetrics().scaledDensity * 11;
            Log.d("gg", "fontScale:" + fontScale);
            // int densityDpi = metric.densityDpi;  // 密度DPI（120 / 160 / 240）
        }
    }

    public void requestPosition() {
        for (Monitor monitor : monitors) {
            //请求对方
            MyApplication.getInstance().getSendMsgUtil().sendLocationRequest(
                    MyApplication.getInstance().getSpUtil().getUser().id, monitor.id);
            // 向对方请求当前手环状态
            Request request = new Request();
            request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
            request.into_id = monitor.id;
            request.tag = MessageTag.BANDSTATE_REQ;
            MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(MyApplication.getInstance().getGson().toJson(request));

            //请求最近心率
            HeartDataRequest request2 = new HeartDataRequest();
            request2.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
            request2.into_id = monitor.id;
            String date = TimeUtil.getNowDateStr();
            long from_time = TimeUtil.timeStrToLong(date);
            long to_time = TimeUtil.timeStrToLong2(date);
            request2.from_time = from_time;
            request2.to_timie = to_time;
            MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(MyApplication.getInstance().getGson().toJson(request2));

        }
    }


}