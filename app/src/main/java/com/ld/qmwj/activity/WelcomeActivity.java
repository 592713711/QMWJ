package com.ld.qmwj.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

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
        handler.postDelayed(startRunnable, 2000);
    }


    private void startHomeActivity() {

        SharePreferenceUtil spUtil = MyApplication.getInstance().getSpUtil();
        //查找sp中有无用户信息
        User user = spUtil.getUser();

        //     Intent intent = new Intent(this, LoginActivity.class);
        //     startActivity(intent);
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





}
