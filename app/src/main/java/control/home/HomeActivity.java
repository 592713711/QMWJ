package control.home;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.view.BadgeView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    BadgeView mBadgeView;
    RecyclerView recyclerView;
    RcAdapter adapter;
    ArrayList<Monitor> list;        //监护列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initWindow();
        initView();

    }

    private void initView() {

        //初始化BadgeView
        mBadgeView = (BadgeView) findViewById(R.id.badgeView);
        mBadgeView.setVisibility(View.VISIBLE);
        mBadgeView.setBadgeCount(5);

        //初始化recycleView
        recyclerView = (RecyclerView) findViewById(R.id.recycleview_list);
        adapter = new RcAdapter(this);
        list = MyApplication.getInstance().getRelateDao().getList();
        Log.d(Config.TAG, list.toString());
        adapter.updateData(list);
        adapter.setItemOnClickListener(itemClick);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


    }


    /**
     * 添加监护者
     */
    public void doAdd(View v) {

    }

    /**
     * 查看信息
     */
    public void doMsg(View v) {

    }


    /**
     * recycleview  视图项点击监听器
     */
    RcAdapter.RecycleViewOnItemClick itemClick = new RcAdapter.RecycleViewOnItemClick() {
        @Override
        public void onItemClick(View view, int postion) {
            Intent intent = new Intent(HomeActivity.this, MonitorActivity.class);
            intent.putExtra("monitor", list.get(postion));
            startActivity(intent);
        }
    };

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
