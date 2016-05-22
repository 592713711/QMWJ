package control.map.locationRange;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.client.MsgHandle;
import com.ld.qmwj.message.request.LocRangeRequest;
import com.ld.qmwj.message.request.RemoveRangeRequest;
import com.ld.qmwj.model.LocationRange;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.view.popuplist.PopupList;
import com.ld.qmwj.view.popuplist.PopupListAdapter;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddLocationRangeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Monitor monitor;
    private Gson gson;
    private ArrayList<LocationRange> datas;
    private ListView listView;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> list;
    private CoordinatorLayout coordinatorLayout;
    public static String REMARK_KEY = "remark";
    public static String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_range);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initWindow();
        initData();
        initView();


    }

    private void initData() {
        gson = new Gson();
        list = new ArrayList<>();
        String from[] = {REMARK_KEY, LOCATION_KEY};
        int to[] = {R.id.remark_text, R.id.location_text};

        adapter = new SimpleAdapter(this, list, R.layout.range_item, from, to);
        updateRange();

        Log.d(Config.TAG, "安全区域数据：" + datas.toString());
    }

    private void updateRange() {
        datas = MyApplication.getInstance().getAuthDao().getLocationRanges(monitor.id);
        list.clear();
        for (LocationRange locRange : datas) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(REMARK_KEY, locRange.location_remark);
            if (locRange.latLng == null) {
                map.put(LOCATION_KEY, "手绘区域");
            } else {
                map.put(LOCATION_KEY, locRange.location_name);
            }
            list.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        listView = (ListView) findViewById(R.id.range_listview);

        listView.setAdapter(adapter);


        final List<String> popupMenuItemList = new ArrayList<>();
        popupMenuItemList.add("删除");

        PopupList.getInstance().initPopupList(this, listView, popupMenuItemList, new PopupListAdapter.OnPopupListClickListener() {
            @Override
            public void onPopupListItemClick(View contextView, int contextPosition, View view, int position) {
                doDelete(contextPosition);
            }
        });

        listView.setOnItemClickListener(this);
        //  listView.setOnItemLongClickListener(this);
    }

    private void doDelete(final int pos) {
        /**
         * 弹出snackbar
         */
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "确定要要删除此安全区域吗", Snackbar.LENGTH_LONG);

        snackbar.setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationRange locRange = datas.get(pos);

                if (MsgHandle.getInstance().channel != null) {
                    //网络正常
                    //删除数据库中的内容
                    MyApplication.getInstance().getAuthDao().removeLocationRange(monitor.id, locRange.rang_pos);

                    //请求服务器修改数据
                    RemoveRangeRequest request = new RemoveRangeRequest();
                    request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
                    request.into_id = monitor.id;
                    request.pos = locRange.rang_pos;
                    MyApplication.getInstance().getSendMsgUtil().sendMessageToServer(
                            MyApplication.getInstance().getGson().toJson(request)
                    );

                    updateRange();
                    Log.d(Config.TAG, "删除");
                } else {
                    Toast.makeText(AddLocationRangeActivity.this, "当前网络异常，尚未连接服务器", Toast.LENGTH_SHORT).show();
                }


            }
        });

        snackbar.show();
    }


    public void doAddRange(View view) {
        if (monitor.identify == Config.MAIN_GUARDIAN) {
            if (datas.size() >= 3) {
                //最多只能添加3个监护范围
                Toast.makeText(this, "最多只能添加3个安全区域", Toast.LENGTH_SHORT).show();
                return;
            }
            //主监护
            int pos = MyApplication.getInstance().getAuthDao().getEmptyPosition(monitor.id);
            if (pos == -1) {
                Toast.makeText(this, "最多只能添加3个安全区域", Toast.LENGTH_SHORT).show();
                updateRange();
                return;
            }
            Intent intent = new Intent(this, LocationRangeActivity.class);
            LocationRange locRange = new LocationRange();
            //设置新添加区域的  位置下标  总共是3个
            locRange.rang_pos = pos;
            MyLocation location = MyApplication.getInstance().getRelateDao().getLocation(monitor.id);
            locRange.latLng = new LatLng(location.latitude, location.longitude);
            locRange.range = 500;
            intent.putExtra("locationRange", gson.toJson(locRange));
            intent.putExtra("monitor", monitor);
            startActivity(intent);
        } else if (monitor.identify == Config.VICE_GUARDIAN_STATUS) {
            //副监护
            Toast.makeText(this, "只有主监护人才可以添加安全区域", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRange();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LocationRange locRange = datas.get(position);
        Intent intent = new Intent(this, LocationRangeActivity.class);
        intent.putExtra("locationRange", gson.toJson(locRange));
        intent.putExtra("monitor", monitor);
        startActivity(intent);


    }
    public void doQuit(View v) {
        finish();
    }

 /*   private ActionMode.Callback mCallback = new ActionMode.Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false;

        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete)
                doDelete(mode);
            return false;
        }


        @Override

        public void onDestroyActionMode(ActionMode mode) {
            //重置视图项的选中状态为false
            for (int i = 0; i < datas.size(); i++) {
                listView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
            }
            mActionMode = null;

        }


        @Override

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();

            inflater.inflate(R.menu.menu_delete_range, menu);


            return true;

        }

    };*/

  /*    @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (monitor.identify == Config.MAIN_GUARDIAN && mActionMode == null) {
            mActionMode = startActionMode(mCallback);
            selectRange = position;
            view.setBackgroundColor(Color.parseColor("#aaaaaa"));

        }
        return true;
    }

    public void doDelete(ActionMode mode) {
        mode.finish();

    }
    */
}
