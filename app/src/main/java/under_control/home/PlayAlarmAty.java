package under_control.home;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ld.qmwj.Config;
import com.ld.qmwj.R;
import com.ld.qmwj.model.Alarm;
import com.ld.qmwj.util.TimeUtil;

public class PlayAlarmAty extends Activity{
	MediaPlayer alarmMusic;
	Alarm alarm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_player_aty);
		setFinishOnTouchOutside(false);
		alarm= (Alarm) getIntent().getSerializableExtra("alarm");
		initView();
		wakeUpAndUnlock(this);
		initAlarmMusic();
		Log.d(Config.TAG,"响铃");
	}

	private void initView() {
		this.setTitle("闹钟");
		((TextView) findViewById(R.id.hint_text)).setText(alarm.alarm_hint);
		((TextView) findViewById(R.id.time_text)).setText(TimeUtil.getTimeStr4(alarm.alarm_time));
	}

	private void initAlarmMusic() {
		alarmMusic = MediaPlayer.create(this, R.raw.alarm1);
		//设置闹钟音乐循环
		alarmMusic.setLooping(true);
		alarmMusic.start();
	}

	/**
	 * 唤醒屏幕
	 * @param context
     */
	public static void wakeUpAndUnlock(Context context){
		Log.d(Config.TAG,"唤醒屏幕");
		KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		//解锁
		kl.disableKeyguard();
		//获取电源管理器对象
		PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
		//获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
		//点亮屏幕
		wl.acquire();
		//释放
		wl.release();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		alarmMusic.stop();
	}


	public void doCancle(View v){
		PlayAlarmAty.this.finish();
	}
}
