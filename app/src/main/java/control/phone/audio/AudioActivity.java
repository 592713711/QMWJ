package control.phone.audio;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.dd.morphingbutton.MorphingButton;
import com.dd.morphingbutton.impl.IndeterminateProgressButton;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.message.response.OpenVoiceResponse;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.util.HandlerUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AudioActivity extends AppCompatActivity {
    private Monitor monitor;
    private IndeterminateProgressButton btnMorph;
    //private int mMorphCounter = 1;
    private ImageView voice_icon;
    private TextView hint_text;
    private String hint_msg;
    private boolean isOpen = false;
    private VoiceLineView voiceLineView;

    private AudioWrapper audioWrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        //订阅事件
        EventBus.getDefault().register(this);
        initWindow();
        audioWrapper=new AudioWrapper();
        initView();
    }

    private void initView() {
        voice_icon = (ImageView) findViewById(R.id.voice_mic_view);
        voiceLineView= (VoiceLineView) findViewById(R.id.voicLine);
        voiceLineView.setVisibility(View.GONE);
        hint_text = (TextView) findViewById(R.id.hint);
        hint_text.setVisibility(View.GONE);
        btnMorph = (IndeterminateProgressButton) findViewById(R.id.btnMorph);
        btnMorph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMorphButtonClicked(btnMorph);
            }
        });


        morphToSquare(btnMorph, 0, "开启声音监控", color(R.color.mb_green));

    }

    private boolean isShouDong = false;       //是否是手动关闭

    private void onMorphButtonClicked(final IndeterminateProgressButton btnMorph) {
        if (isOpen) {
            //关闭监控
            hint_text.setVisibility(View.VISIBLE);
            hint_text.setText("关闭监控中");
            isOpen = false;
            isShouDong = true;
            requestCloseVoice();
            audioWrapper.stopListen();
            simulateProgress(btnMorph,3000);

            //morphToFailure(btnMorph, integer(R.integer.mb_animation));
        } else {
            //开启监控   只有收到端口号消息后 才设置mMorphCounter=0；
            //mMorphCounter = 0;
            hint_text.setVisibility(View.VISIBLE);
            hint_text.setText("开启监控中");
            isOpen = true;
            requestOpenVoice();
            simulateProgress(btnMorph,6000);

        }
    }

    private void requestCloseVoice() {
        Request request=new Request();
        request.tag= MessageTag.CLOSE_VOICE_REQ;
        request.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id=monitor.id;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
    }

    /**
     * 开启声音监控请求
     */
    private void requestOpenVoice() {
        Request request=new Request();
        request.tag= MessageTag.OPEN_VOICE_REQ;
        request.from_id=MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id=monitor.id;
        MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
    }

    /**
     * 变成按钮状态
     *
     * @param btnMorph
     * @param duration
     */
    private void morphToSquare(final IndeterminateProgressButton btnMorph, int duration, String msg, int bg_color) {
        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(dimen(R.dimen.mb_height_56)) // 56 dp
                .width(dimen(R.dimen.mb_width_250))
                .height(dimen(R.dimen.mb_height_56))
                .color(bg_color)
                .colorPressed(color(R.color.mb_blue_dark))
                .text(msg);
        btnMorph.morph(square);
    }


    /**
     * 变成进度条
     *
     * @param button
     */
    private void simulateProgress(@NonNull final IndeterminateProgressButton button,int duration) {
        int progressColor = color(R.color.mb_blue);
        int color = color(R.color.mb_gray);
        int progressCornerRadius = dimen(R.dimen.mb_corner_radius_4);
        int width = dimen(R.dimen.mb_width_200);
        int height = dimen(R.dimen.mb_height_8);
       

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.unblockTouch();
                //动画执行完后
                if (isOpen) {

                    if (MsgHandle.getInstance().channel == null) {
                        hint_text.setText("网络异常，开启失败");
                        morphToSquare(btnMorph, integer(R.integer.mb_animation), "开启声音监控", color(R.color.mb_green));
                        audioWrapper.stopListen();
                        return;
                    }
                    hint_text.setVisibility(View.GONE);
                    morphToSquare(btnMorph, integer(R.integer.mb_animation), "关闭声音监控", color(R.color.red));
                    //开启监控
                    hint_text.setVisibility(View.GONE);
                    voice_icon.setVisibility(View.GONE);


                    voiceLineView.setVisibility(View.VISIBLE);
                    //morphToSquare(btnMorph, integer(R.integer.mb_animation), "开启声音监控", color(R.color.mb_green));
                } else {
                    audioWrapper.stopListen();
                    voiceLineView.setVisibility(View.GONE);
                    voice_icon.setVisibility(View.VISIBLE);
                    if (isShouDong) {
                        isShouDong = false;
                        hint_text.setVisibility(View.GONE);

                    } else {
                        hint_text.setVisibility(View.VISIBLE);
                        if (MsgHandle.getInstance().channel == null) {
                            hint_text.setText("网络异常，开启失败");
                            return;
                        } else {
                            hint_text.setText(hint_msg);
                        }
                    }
                    morphToSquare(btnMorph, integer(R.integer.mb_animation), "开启声音监控", color(R.color.mb_green));

                }
                //morphToSuccess(button);


            }
        }, duration);

        button.blockTouch(); // prevent user from clicking while button is in progress
        button.morphToProgress(color, progressCornerRadius, width, height, 500, progressColor);
    }


    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.STATE_RESPONSE) {
            //更新对方状态
            monitor = MyApplication.getInstance().getRelateDao().getMonitorById(monitor.id);
            if (monitor.state == Config.NOT_ONLINE_STATE) {
                if(isOpen){
                    //处于打开状态 对方掉线
                    hint_msg = "对方网络问题,监控断开";
                }else {
                    //不在线
                    hint_msg = "对方网络问题,开启失败";
                }
                isOpen = false;
            }
        } else if (tag == HandlerUtil.OPEN_VOICE) {
            isOpen = true;
        }else if(tag==HandlerUtil.CLOSE_VOICE){
            isOpen=false;
            voiceLineView.setVisibility(View.GONE);
            hint_text.setVisibility(View.VISIBLE);
            voice_icon.setVisibility(View.VISIBLE);
            hint_text.setText("对方网路异常,监控断开");
            morphToSquare(btnMorph, integer(R.integer.mb_animation), "开启声音监控", color(R.color.mb_green));
        }
    }

    /**
     * 开启声音监听
     *
     * @param response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(OpenVoiceResponse response) {
        Log.d(Config.TAG,"开启语音监听");
        audioWrapper.setServerPort(response.port);
        audioWrapper.startListen();
    }

    /**
     * 开启声音监听
     *
     * @param dB 分贝
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upDateUI(Double dB) {
        voiceLineView.setVolume(dB.intValue());
    }

    public int integer(@IntegerRes int resId) {
        return getResources().getInteger(resId);
    }

    public int color(@ColorRes int resId) {
        return getResources().getColor(resId);
    }

    public int dimen(@DimenRes int resId) {
        return (int) getResources().getDimension(resId);
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
        tintManager.setStatusBarTintResource(R.color.record_bg);//通知栏所需颜色

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //订阅事件
        EventBus.getDefault().unregister(this);
        audioWrapper.stopRecord();
    }
}
