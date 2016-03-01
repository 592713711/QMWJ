package control.home;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * 被监护方详细页面
 */
public class MonitorActivity extends AppCompatActivity {
    private Monitor monitor;
    private RadioButton msg_btn;
    private RadioButton phone_btn;
    private RadioButton map_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initView();
    }

    private void initView() {
        msg_btn = (RadioButton) findViewById(R.id.msg_btn);
        map_btn = (RadioButton) findViewById(R.id.map_btn);
        phone_btn= (RadioButton) findViewById(R.id.phone_btn);

    }


    public void doQuit(View v) {
        finish();
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


}
