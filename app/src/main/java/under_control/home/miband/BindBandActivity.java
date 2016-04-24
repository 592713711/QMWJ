package under_control.home.miband;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.model.User;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.WaitDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.NotifyListener;
import com.zhaoxiaodan.miband.model.UserInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import under_control.home.service.FuctionService;

/**
 * 扫秒蓝牙设备  绑定手环活动
 */
public class BindBandActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView device_list;
    private ArrayAdapter<String> adapter;
    private HashMap<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();
    private MiBand miBand;
    private WaitDialog waitDialog;      //等待对话框

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerUtil.STOP_SCAN) {
                miBand.stopScan(scanCallback);
                if (waitDialog != null)
                    waitDialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_band);
        miBand = FuctionService.miBand;
        initWindow();
        initView();

        startScan();
    }

    /**
     * 扫描蓝牙设备
     */
    private void startScan() {
        miBand.startScan(scanCallback);
        handler.sendEmptyMessageDelayed(HandlerUtil.STOP_SCAN, 5000);
        waitDialog.setMsg("扫描周围设备中");
        waitDialog.show(getSupportFragmentManager(), "scan_wait");
        waitDialog.setCancelable(false);


    }

    private void initView() {
        device_list = (ListView) findViewById(R.id.device_list);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        device_list.setAdapter(adapter);
        device_list.setOnItemClickListener(this);

        waitDialog = new WaitDialog();

    }

    /**
     * 扫描蓝牙设备
     */
    final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.d(Config.TAG,
                    "找到附近的蓝牙设备: name:" + device.getName() + ",uuid:"
                            + device.getUuids() + ",add:"
                            + device.getAddress() + ",type:"
                            + device.getType() + ",bondState:"
                            + device.getBondState() + ",rssi:" + rssi);
            final String item = device.getName() + "    |    " + device.getAddress();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    devices.put(item, device);
                    adapter.add(item);
                    adapter.notifyDataSetChanged();
                }
            });


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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Log.d(Config.TAG, "点击了" + adapter.getItem(position));
        String itemname = adapter.getItem(position);
        doConnect(devices.get(itemname));
    }


    /**
     * 连接小米手环
     */
    public void doConnect(final BluetoothDevice device) {
        waitDialog.setMsg("绑定设备中,闪烁时请轻拍手环");
        waitDialog.show(getSupportFragmentManager(), "bind_wait");
        miBand.connect(device, new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                //给手环设置用户信息   当 设置的 userid 跟之前一样时 手环无反应
                User user = MyApplication.getInstance().getSpUtil().getUser();
                UserInfo userInfo = new UserInfo(user.id, 1, 32, 180, 55, user.username, 0);
                miBand.setUserInfo(userInfo);

                waitDialog.dismiss();
                Log.d(Config.TAG, "连接成功!!!");
                handler.post(initRunnable("绑定手环成功"));
                miBand.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        Log.d(Config.TAG, "手环连接断开!!!");
                        EventBus.getDefault().post(HandlerUtil.DISCONNECTBAND);
                    }

                });

                //将设备信息写入选项存储中
                MyApplication.getInstance().getSpUtil().writeDevice(device);

                EventBus.getDefault().post(HandlerUtil.CONNECTBAND);
                finish();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                waitDialog.dismiss();
                handler.post(initRunnable("绑定手环失败，请确定绑定的是小米手环"));
                Log.d(Config.TAG, "connect fail, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    public Runnable initRunnable(final String msg) {
        return new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BindBandActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

}
