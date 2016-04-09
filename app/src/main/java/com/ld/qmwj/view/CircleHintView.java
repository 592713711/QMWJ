package com.ld.qmwj.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.ld.qmwj.Config;
import com.ld.qmwj.R;

/**
 * Created by zsg on 2016/4/5.
 */
public class CircleHintView extends View {
    /**
     * 整个View的宽高
     */
    private int mTotalHeight, mTotalWidth;

    //圆心坐标 半径
    private int x, y, radius;

    //画笔
    private Paint paint_out_cir;
    private Paint level_paint;      //电量圈画笔

    private Bitmap bitmap_icon;

    private int start_angle = 282;      //圆  起始角度
    private int all_angle = 338;

    /**
     * 圆环大小 矩形
     */
    private RectF mRectf;

    private int level = 0;          //电池电量
    private int duration = 1500;      //电量外圈动画时间

    public CircleHintView(Context context) {
        this(context, null);
    }

    public CircleHintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint_out_cir = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_out_cir.setColor(Color.parseColor("#99f0f0f0"));                    //设置画笔颜色
        paint_out_cir.setStrokeWidth((float) 8.0);              //线宽
        paint_out_cir.setStyle(Paint.Style.STROKE);

        level_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        level_paint.setColor(Color.parseColor("#f0f0f0"));                    //设置画笔颜色
        level_paint.setStrokeWidth((float) 12.0);              //线宽
        level_paint.setStyle(Paint.Style.STROKE);

        bitmap_icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dynamic_step);
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f); //长和宽放大缩小的比例
        bitmap_icon = Bitmap.createBitmap(bitmap_icon, 0, 0, bitmap_icon.getWidth(), bitmap_icon.getHeight(), matrix, true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalHeight = h;
        mTotalWidth = w;
        x = w / 2;
        radius = (mTotalHeight - bitmap_icon.getHeight() / 2) / 2 - 5;
        y = radius + bitmap_icon.getHeight() / 2;
        mRectf = new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawCircle(x, y, radius, paint_out_cir);
        canvas.drawBitmap(bitmap_icon, mTotalWidth / 2 - bitmap_icon.getWidth() / 2, 0, null);
        canvas.drawArc(mRectf, start_angle, all_angle, false, paint_out_cir);

        if (level > 0) {
            int level_angle = (int) ((float) level / 100 * all_angle);
            canvas.drawArc(mRectf, start_angle, level_angle, false, level_paint);
        }

    }

    public void LevelToAnimator(int number) {
        this.level = number;
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "level", 0, number);
        objectAnimator.setDuration(duration);
        objectAnimator.start();
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(int number) {
        this.level = number;
        invalidate();
    }
}
