package control.phone.alarm;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.AlterAlarmRequest;
import com.ld.qmwj.model.Alarm;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.TimeUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Calendar;

public class AlterAlarmActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private TextView hint_text;
    private Monitor monitor;
    public static final String ALTER_TYPR_ADD = "add";
    public static final String ALTER_TYPR_UPDATE = "update";
    private String alter_type;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        alter_type = getIntent().getStringExtra("alter_type");
        initView();
    }

    private void initView() {
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        hint_text = (TextView) findViewById(R.id.alarm_hint);

        if (alter_type.equals(ALTER_TYPR_ADD)) {
            this.setTitle("添加闹钟");
        } else {
            this.setTitle("修改闹钟");
            alarm = (Alarm) getIntent().getSerializableExtra("alarm");
            hint_text.setText(alarm.alarm_hint);
            timePicker.setCurrentHour(TimeUtil.getHour(alarm.alarm_time));
            timePicker.setCurrentMinute(TimeUtil.getMinute(alarm.alarm_time));
        }

    }

    public void doSave(View v) {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String hint = hint_text.getText().toString();

        if (alter_type.equals(ALTER_TYPR_ADD)) {
            Alarm alarm = new Alarm();
            alarm.alarm_time = calendar.getTimeInMillis();
            alarm.alarm_hint = hint;
            alarm.isopen = Alarm.ON;

            AddAlarm(alarm);


        } else {
            //修改闹钟
            alarm.alarm_time = calendar.getTimeInMillis();
            alarm.alarm_hint = hint;
            updateAlarm();
        }
    }

    private void AddAlarm(Alarm alarm) {
        //保存数据到数据库
        MyApplication.getInstance().getAlarmtDao().insertAlarm(monitor.id, alarm);

        alarm = MyApplication.getInstance().getAlarmtDao().getAlarmLast(monitor.id);

        //发送数据给服务器
        AlterAlarmRequest request = new AlterAlarmRequest(MessageTag.ADDALARM_REQ);
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.alarm = alarm;

        MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
        finish();
    }

    private void updateAlarm() {
        //保存数据到数据库
        MyApplication.getInstance().getAlarmtDao().updateAlarm(monitor.id, alarm);

        //发送数据给服务器
        AlterAlarmRequest request = new AlterAlarmRequest(MessageTag.UPDATEALARM_REQ);
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.alarm = alarm;

        MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
        finish();
    }

}
