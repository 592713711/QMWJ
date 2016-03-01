package com.ld.qmwj.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.LoadingDialog;

import control.home.HomeActivity;
import under_control.home.HomeActivity_under;

/**
 * 登陆活动
 */
public class LoginActivity extends AppCompatActivity {
    LoadingDialog mLoadingDialog;   //登陆等待对话框
    //登陆超时回调
    private Runnable mConnTimeoutCallback = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            Toast.makeText(LoginActivity.this, "登录超时，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerUtil.LOGIN_SUCCESS) {

                //隐藏对话框
                if (mLoadingDialog != null && mLoadingDialog.isVisible())
                    mLoadingDialog.dismiss();
                handler.removeCallbacks(mConnTimeoutCallback);

                //登陆成功
                User user = MyApplication.getInstance().getSpUtil().getUser();
                Intent intent;
                if (user.status == Config.GUARDIAN_STATUS)
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                else
                    intent = new Intent(LoginActivity.this, HomeActivity_under.class);
                startActivity(intent);
                finish();

            } else if (msg.what == HandlerUtil.LOGIN_FAIL) {
                //隐藏对话框
                if (mLoadingDialog != null && mLoadingDialog.isVisible())
                    mLoadingDialog.dismiss();
                handler.removeCallbacks(mConnTimeoutCallback);

                Toast.makeText(LoginActivity.this,"用户名或密码错误，请重新输入",Toast.LENGTH_SHORT).show();
            }

        }
    };

    private EditText edit_name;
    private EditText edit_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MsgHandle.getInstance().initHandle(handler);
        initView();
    }

    private void initView() {
        edit_name= (EditText) findViewById(R.id.user_text);
        edit_pwd= (EditText) findViewById(R.id.pwd_text);
        mLoadingDialog = new LoadingDialog();


    }

    public void doLogin(View v){
        String name=edit_name.getText().toString().trim();
        String pwd=edit_pwd.getText().toString();
        if(name.equals("")&&pwd.equals("")) {
            Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //显示等待对话框
        // 开启一个10秒后超时的Callback
        handler.postDelayed(mConnTimeoutCallback, 10000);
        mLoadingDialog.show(getSupportFragmentManager(), "LOADING_DIALOG");
        mLoadingDialog.setCancelable(false);
        MyApplication.getInstance().getSendMsgUtil().sendLoginRequest(name,pwd);
    }

}
