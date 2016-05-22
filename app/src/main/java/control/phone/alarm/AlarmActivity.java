package control.phone.alarm;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.Alarm;
import com.ld.qmwj.model.Monitor;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;

public class AlarmActivity extends AppCompatActivity {

    private HorizontalSlideDeleteListView mlistView;
    private Monitor monitor;
    private ArrayList<Alarm> alarms = new ArrayList<>();
    private AlarmAdapter adapter;

    //适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initView();
    }

    public void updateData() {
        alarms.clear();
        alarms.addAll(MyApplication.getInstance().getAlarmtDao().getAlarmList(monitor.id));
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        // TODO Auto-generated method stub
        mlistView = (HorizontalSlideDeleteListView) findViewById(R.id.alarm_list);
        adapter = new AlarmAdapter(this, alarms,monitor);
        mlistView.setAdapter(adapter);
        updateData();


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }

    /**
     * 添加闹钟
     *
     * @param v
     */
    public void doAddAlarm(View v) {
        Intent intent = new Intent(this, AlterAlarmActivity.class);
        intent.putExtra("monitor", monitor);
        intent.putExtra("alter_type", AlterAlarmActivity.ALTER_TYPR_ADD);
        startActivity(intent);
    }

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

    public void doQuit(View v) {
        finish();
    }
}
