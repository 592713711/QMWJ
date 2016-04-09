package com.ld.qmwj.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.ld.qmwj.Config;

/**
 * Created by zsg on 2016/4/7.
 */

/**
 * 动画显示数字
 * Created by fhp on 15/1/7.
 */
public class NumberView extends TextView{
    //动画时长 ms
    int duration = 1000;
    float number;
    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void showNumberWithAnimation(int number) {
        //修改number属性，会调用setNumber方法
        ObjectAnimator objectAnimator=ObjectAnimator.ofInt(this, "number", 0, number);
        objectAnimator.setDuration(duration);
        //加速器，从慢到快到再到慢
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();
    }
    public float getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
        setText(String.format("%d%%", number));
    }
}
