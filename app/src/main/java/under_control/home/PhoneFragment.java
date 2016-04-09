package under_control.home;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import under_control.home.miband.MiBandActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context context;
    private ListView listview;
    private SimpleAdapter adapter;
    private String[] funStrs = {"我的手环","权限管理", "关联用户", "系统设置"};      //每个功能的名字
    private int funIcon[] = {R.drawable.ic_menu_mili};

    public PhoneFragment(Context context) {
        this.context = context;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_under, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        listview = (ListView) view.findViewById(R.id.listview);
        List<Map<String, Object>> datas = new ArrayList();
        for (int i = 0; i < funIcon.length; i++) {
            Map<String, Object> data = new HashMap();
            data.put("icon", funIcon[i]);
            data.put("text", funStrs[i]);
            datas.add(data);
        }

        String from[] = {"icon", "text"};
        int to[] = {R.id.icon_view, R.id.text};
        adapter = new SimpleAdapter(context, datas, R.layout.fun_item, from, to);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(position){
            case 0:
                Intent intent=new Intent(context, MiBandActivity.class);
                startActivity(intent);
                break;
        }
    }
}
