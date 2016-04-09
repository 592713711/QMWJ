package com.ld.qmwj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.ld.qmwj.Config;
import com.ld.qmwj.R;

/**
 * 手环连接动画
 * Created by zsg on 2016/4/8.
 */
public class BandConnectView extends ImageView {
    private Thread thread;
    private int position = 0;
    private int images[] = {R.drawable.ic_mili1, R.drawable.ic_mili2, R.drawable.ic_mili3};
    private boolean flag = false;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (flag) {
                position++;
                if (position > 2)
                    position = 0;
                postInvalidate();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public BandConnectView(Context context) {
        this(context, null);
    }

    public BandConnectView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setImageResource(images[position]);
    }

    public void startAnim() {
        flag = true;
        thread = new Thread(runnable);
        position = 0;
        thread.start();
    }

    public void stopAnim() {
        if (thread != null)
            thread.interrupt();
        flag = false;
    }

}
