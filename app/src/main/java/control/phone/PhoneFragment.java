package control.phone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import control.phone.alarm.AlarmActivity;
import control.phone.audio.AudioActivity;
import control.phone.health.MiBandActivity;
import under_control.home.miband.HeartActivity;
import control.phone.linkman.LinkManActivity;
import control.phone.sms.SmsListActivity;

/**
 * 手机相关的碎片
 */
public class PhoneFragment extends Fragment implements AdapterView.OnItemClickListener {

    private Monitor monitor;
    private Context context;
    private GridView gview;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    // 图片封装为一个数组
    private int[] icon = {R.drawable.linkman_btn, R.drawable.sms_btn,
            R.drawable.health_btn, R.drawable.bluetooth_btn, R.drawable.setting_btn,
            R.drawable.clock_btn, R.drawable.cost_btn, R.drawable.close_btn};
    private String[] iconName = {"联系人", "短信监控", "健康心率", "蓝牙随行", "手机设置", "闹钟设置", "话费查询",
            "实时语音"};

    public PhoneFragment(Monitor monitor, Context context) {
        this.monitor = monitor;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        initView(view);


        return view;
    }

    private void initView(View view) {

        gview = (GridView) view.findViewById(R.id.gridview_function);
        //新建List
        data_list = new ArrayList<Map<String, Object>>();
        //获取数据
        getData();
        //新建适配器
        String[] from = {"image", "text"};
        int[] to = {R.id.image, R.id.text};
        sim_adapter = new SimpleAdapter(context, data_list, R.layout.grid_fun_item, from, to);
        //配置适配器
        gview.setAdapter(sim_adapter);
        gview.setOnItemClickListener(this);
    }

    public List<Map<String, Object>> getData() {
        //cion和iconName的长度是相同的，这里任选其一都可以
        for (int i = 0; i < icon.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", icon[i]);
            map.put("text", iconName[i]);
            data_list.add(map);
        }

        return data_list;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        switch (position) {
            case 0:
                //打开联系人活动
                intent = new Intent(context, LinkManActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;
            case 1:
                //打开短信活动
                intent = new Intent(context, SmsListActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;
            case 2:
                //打开心率活动
                intent = new Intent(context, MiBandActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;
            case 5:
                //打开闹钟活动
                intent = new Intent(context, AlarmActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;
            case 7:
                //打开闹钟活动
                intent = new Intent(context, AudioActivity.class);
                intent.putExtra("monitor", monitor);
                startActivity(intent);
                break;

        }
    }
}
