package com.ld.qmwj.view.calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ld.qmwj.R;


/**
 * Created by zzc on 2016/1/20.
 */
public class CalenderDialog extends Dialog implements View.OnClickListener {

    private ViewPager mViewPager;
    private int mCurrentIndex = 498;
    private CalendarCard[] mShowViews;
    public  CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private CalendarCard.OnCellClickListener cellClickListener;

    private ImageButton preImgBtn, nextImgBtn;
    public TextView monthText;


    public CalenderDialog(Context context, int themeResId, CalendarCard.OnCellClickListener listener) {
        super(context, themeResId);

        setContentView(R.layout.calendar_layout);
        mViewPager = (ViewPager) findViewById(R.id.vp_calendar);
        preImgBtn = (ImageButton) findViewById(R.id.btnPreMonth);
        nextImgBtn = (ImageButton) findViewById(R.id.btnNextMonth);
        monthText = (TextView) findViewById(R.id.tvCurrentMonth);
        preImgBtn.setOnClickListener(this);
        nextImgBtn.setOnClickListener(this);
        this.cellClickListener = listener;



    }

    public void initDialog( Activity activity){
        CalendarCard[] views = new CalendarCard[3];
        for (int i = 0; i < 3; i++) {
            views[i] = new CalendarCard(getContext(), cellClickListener);
        }
        adapter = new CalendarViewAdapter<>(views);
        setViewPager();


        Window dialogWindow = getWindow();

        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = activity.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

        lp.y = (int) (d.getHeight() * 0.13); // 新位置y坐标
        lp.x = (int) (d.getWidth() * 0.02); // 新位置x坐标
        lp.width=d.getWidth();
        // lp.alpha = 0.8f; // 透明度

        // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
        // dialog.onWindowAttributesChanged(lp);
        dialogWindow.setAttributes(lp);


    }


    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE;
    }


    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                break;
            case R.id.btnNextMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                break;
            default:
                break;
        }
    }


    /**
     * 计算方向
     *
     * @param arg0
     */
    private void measureDirection(int arg0) {

        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;

        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }

    // 更新日历视图
    private void updateCalendarView(int arg0) {
        mShowViews = adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            mShowViews[arg0 % mShowViews.length].rightSlide();
        } else if (mDirection == SildeDirection.LEFT) {
            mShowViews[arg0 % mShowViews.length].leftSlide();
        }
        mDirection = SildeDirection.NO_SILDE;
    }

}
