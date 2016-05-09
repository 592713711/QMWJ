package control.phone.linkman;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.BlackPhone;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.view.EditActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;

/**
 * 显示黑名单列表
 */
public class BlacklistActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView lv_blacklist = null;
    private ArrayList<BlackPhone> blackphonesList = null;
    private BlacklistAdapter blacklistAdapter = null;
    private Monitor monitor;
    private FloatingActionButton btn_add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        initWindow();
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        lv_blacklist = (ListView) findViewById(R.id.id_lv_blacklist);
        blackphonesList = new ArrayList<>();
        initData();
        blacklistAdapter = new BlacklistAdapter(BlacklistActivity.this, blackphonesList, monitor);
        lv_blacklist.setAdapter(blacklistAdapter);

        btn_add = (FloatingActionButton) findViewById(R.id.id_btn_add);
        btn_add.setOnClickListener(this);
    }

    private void initData() {
        blackphonesList.clear();
        blackphonesList.addAll(MyApplication.getInstance().getBlacklistDao().getBlackPhone(monitor.id));
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


    @Override
    public void onClick(View v) {
        addBlackPhone();
    }

    private void addBlackPhone() {
        if (monitor.identify == Config.MAIN_GUARDIAN) {
            Intent intent = new Intent(BlacklistActivity.this, EditActivity.class);
            intent.putExtra(Config.CONTACTS, Config.DOADD);
            intent.putExtra("monitor", monitor);
            startActivity(intent);
        } else {
            Toast.makeText(this, "只有主监护方才能添加黑名单", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
