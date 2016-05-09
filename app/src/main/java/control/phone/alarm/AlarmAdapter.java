package control.phone.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.AlterAlarmRequest;
import com.ld.qmwj.model.Alarm;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;

/**
 * Created by Chalmers on 2016-05-06 16:35.
 * email:qxinhai@yeah.net
 */
public class AlarmAdapter extends BaseAdapter {

    private ArrayList<Alarm> alarmList = null;
    private LayoutInflater layoutInflater = null;
    private Context context;
    private Monitor monitor;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;

    /**
     * 删除按钮事件
     */
    private DeleteButtonOnclickImpl mDelOnclickImpl;
    /**
     * HorizontalScrollView左右滑动事件
     */
    private ScrollViewScrollImpl mScrollImpl;

    /**
     * 布局参数,动态让HorizontalScrollView中的TextView宽度包裹父容器
     */
    private LinearLayout.LayoutParams mParams;

    /**
     * 记录滑动出删除按钮的itemView
     */
    public HorizontalScrollView mScrollView;

    /**
     * touch事件锁定,如果已经有滑动出删除按钮的itemView,就屏蔽下一整次(down,move,up)的onTouch操作
     */
    public boolean mLockOnTouch = false;


    public AlarmAdapter(Context context, ArrayList<Alarm> alarmList, Monitor monitor) {
        this.alarmList = alarmList;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.monitor = monitor;


        // 搞到屏幕宽度
        Display defaultDisplay = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mParams = new LinearLayout.LayoutParams(mScreenWidth,
                LinearLayout.LayoutParams.MATCH_PARENT);
        // 初始化删除按钮事件与item滑动事件
        mDelOnclickImpl = new DeleteButtonOnclickImpl();
        mScrollImpl = new ScrollViewScrollImpl();
    }

    @Override
    public int getCount() {
        return alarmList.size();
    }

    @Override
    public Object getItem(int position) {
        return alarmList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.alarm_item, parent, false);
            viewHolder = new ViewHolder(convertView, (HorizontalScrollView) convertView);
            viewHolder.content_layout.setLayoutParams(mParams);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.bindData(alarmList.get(position));
        viewHolder.position = position;
        viewHolder.scrollView.scrollTo(0, 0);
        viewHolder.deleteButton.setTag(viewHolder);

        return convertView;
    }

    class ViewHolder implements View.OnClickListener{
        private TextView tv_hint;
        private TextView tv_time;
        private TextView tv_shiduan;
        private SwitchButton aSwitch;
        private HorizontalScrollView scrollView;
        private Button deleteButton;
        private RelativeLayout content_layout;
        private int position;

        public ViewHolder(View view, HorizontalScrollView scrollView) {
            tv_hint = (TextView) view.findViewById(R.id.alarm_hint);
            tv_shiduan = (TextView) view.findViewById(R.id.alarm_shiduan);
            tv_time = (TextView) view.findViewById(R.id.alarm_time);
            aSwitch = (SwitchButton) view.findViewById(R.id.alarm_switch);
            deleteButton = (Button) view.findViewById(R.id.item_delete);
            content_layout = (RelativeLayout) view.findViewById(R.id.content_layout);
            content_layout.setOnClickListener(this);

            this.scrollView = scrollView;
            this.scrollView.setOnTouchListener(mScrollImpl);

            deleteButton.setOnClickListener(mDelOnclickImpl);

            tv_shiduan.setTextColor(Color.parseColor("#d0d0d0"));
            tv_time.setTextColor(Color.parseColor("#d0d0d0"));
            tv_hint.setTextColor(Color.parseColor("#d0d0d0"));
            aSwitch.setChecked(false);
        }

        public void bindData(Alarm alarm) {

            tv_hint.setText(alarm.alarm_hint);
            tv_time.setText(TimeUtil.getTimeStr4(alarm.alarm_time));
            tv_shiduan.setText(TimeUtil.getTimeShidaun(alarm.alarm_time));
            //设置Switch开关
            int isOpen = alarm.isopen;
            if (isOpen == Alarm.ON) {
                aSwitch.setChecked(true);
                tv_time.setTextColor(Color.parseColor("#000000"));
                tv_shiduan.setTextColor(Color.parseColor("#000000"));
                tv_hint.setTextColor(Color.parseColor("#666666"));
            } else {
                aSwitch.setChecked(false);
                tv_time.setTextColor(Color.parseColor("#d0d0d0"));
                tv_shiduan.setTextColor(Color.parseColor("#d0d0d0"));
                tv_hint.setTextColor(Color.parseColor("#d0d0d0"));
            }

            //添加监听器
            aSwitch.setOnCheckedChangeListener(new SwitchListener(alarm, this));


        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, AlterAlarmActivity.class);
            intent.putExtra("monitor", monitor);
            intent.putExtra("alter_type", AlterAlarmActivity.ALTER_TYPR_UPDATE);
            intent.putExtra("alarm",alarmList.get(position));
            context.startActivity(intent);
        }

        class SwitchListener implements CompoundButton.OnCheckedChangeListener {

            //获得Switch对应的Alarm对象
            private Alarm alarm = null;
            private ViewHolder viewHolder;

            public SwitchListener(Alarm alarm, ViewHolder viewHolder) {
                this.alarm = alarm;
                this.viewHolder = viewHolder;


            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //开关打开
                if (isChecked) {
                    alarm.isopen = Alarm.ON;
                    tv_time.setTextColor(Color.parseColor("#000000"));
                    tv_shiduan.setTextColor(Color.parseColor("#000000"));
                    tv_hint.setTextColor(Color.parseColor("#666666"));
                } else {
                    alarm.isopen = Alarm.OFF;
                    tv_time.setTextColor(Color.parseColor("#d0d0d0"));
                    tv_shiduan.setTextColor(Color.parseColor("#d0d0d0"));
                    tv_hint.setTextColor(Color.parseColor("#d0d0d0"));
                }

                /**
                 * 根据Alarm对象数据修改数据库数据
                 */
                MyApplication.getInstance().getAlarmtDao().updateAlarm(monitor.id, alarm);

                //发送数据给服务器
                AlterAlarmRequest request = new AlterAlarmRequest(MessageTag.UPDATEALARM_REQ);
                request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
                request.into_id = monitor.id;
                request.alarm = alarm;

                MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                        MyApplication.getInstance().getGson().toJson(request)
                );
            }
        }
    }



    /**
     * HorizontalScrollView的滑动事件
     */
    private class ScrollViewScrollImpl implements View.OnTouchListener {
        /**
         * 记录开始时的坐标
         */
        private float startX = 0;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 如果有划出删除按钮的itemView,就让他滑回去并且锁定本次touch操作,解锁会在父组件的dispatchTouchEvent中进行
                    if (mScrollView != null) {
                        scrollView(mScrollView, HorizontalScrollView.FOCUS_LEFT);
                        mScrollView = null;
                        mLockOnTouch = true;
                        return true;
                    }
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    HorizontalScrollView view = (HorizontalScrollView) v;
                    // 如果滑动了>50个像素,就显示出删除按钮
                    if (startX > event.getX() + 50) {
                        startX = 0;// 因为公用一个事件处理对象,防止错乱,还原startX值
                        scrollView(view, HorizontalScrollView.FOCUS_RIGHT);
                        mScrollView = view;
                    } else {
                        scrollView(view, HorizontalScrollView.FOCUS_LEFT);
                    }
                    break;
            }
            return false;
        }
    }

    /**
     * HorizontalScrollView左右滑动
     */
    public void scrollView(final HorizontalScrollView view, final int parameter) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.pageScroll(parameter);
            }
        });
    }


    /**
     * 删除事件
     */
    private class DeleteButtonOnclickImpl implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            Animation animation = AnimationUtils.loadAnimation(context,
                    R.anim.anim_item_delete);
            holder.scrollView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //数据库中删除
                    MyApplication.getInstance().getAlarmtDao().deleteAlarm(monitor.id, alarmList.get(holder.position));

                    notifyDataSetChanged();

                    //发送数据给服务器
                    AlterAlarmRequest request = new AlterAlarmRequest(MessageTag.DELETEALARM_REQ);
                    request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
                    request.into_id = monitor.id;
                    request.alarm = alarmList.get(holder.position);

                    MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                            MyApplication.getInstance().getGson().toJson(request)
                    );

                    alarmList.remove(holder.position);
                }
            });

        }
    }
}