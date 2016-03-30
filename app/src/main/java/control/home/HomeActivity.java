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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.view.BadgeView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    BadgeView mBadgeView;
    RecyclerView recyclerView;
    RcAdapter adapter;
    ArrayList<Monitor> list;        //监护列表
    RelativeLayout hintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initWindow();
        initView();
        //订阅事件
        EventBus.getDefault().register(this);


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
        hintLayout = (RelativeLayout) findViewById(R.id.hint);
        updateHint();


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
     * 更新被监护方列表
     */
    public void updateList() {
        list = MyApplication.getInstance().getRelateDao().getList();
        Log.d(Config.TAG, list.toString());
        adapter.updateData(list);
    }

    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.STATE_RESPONSE) {
            //收到对方状态响应
            updateList();
        } else if (tag == HandlerUtil.CONNECT_SUC || tag == HandlerUtil.CONNECT_FAIL) {
            updateHint();
        }
    }

    /**
     * 更新提示消息
     */
    public void updateHint() {
        //判断当前客户端是否已经连接上了服务器
        if (MsgHandle.getInstance().channel != null) {
            hintLayout.setVisibility(View.GONE);
        } else {
            hintLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消订阅
        EventBus.getDefault().unregister(this);
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
