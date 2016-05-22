package com.ld.qmwj.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.SendMessageUtil;
import com.ld.qmwj.util.SharePreferenceUtil;

import control.home.HomeActivity;
import under_control.home.HomeActivity_under;

/**
 * 欢迎活动  整个程序的入口
 */
public class WelcomeActivity extends AppCompatActivity {

    Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            startHomeActivity();
        }
    };
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        transparencyBar();
        handler.postDelayed(startRunnable, 2000);
    }


    private void startHomeActivity() {

        SharePreferenceUtil spUtil = MyApplication.getInstance().getSpUtil();
        //查找sp中有无用户信息
        User user = spUtil.getUser();

//             Intent intent = new Intent(this, LoginActivity.class);
        //        startActivity(intent);
        if (user == null) {
            //用户未登录
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        } else {
            //发送自动登录
            SendMessageUtil sendMsgUtil = MyApplication.getInstance().getSendMsgUtil();
            sendMsgUtil.sendLoginRequest(null, null);

            //跳转到主页面
            Intent intent;
            if (user.status == Config.GUARDIAN_STATUS)
                intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            else
                intent = new Intent(WelcomeActivity.this, HomeActivity_under.class);
            startActivity(intent);
        }

        finish();
    }


    /**
     * 修改状态栏为全透明
     */
    @TargetApi(19)
    public void transparencyBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


}
