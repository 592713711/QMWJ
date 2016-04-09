package control.msg;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.message.Message;
import com.ld.qmwj.message.request.ChatRequest;
import com.ld.qmwj.message.request.Request;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.MyLocation;
import com.ld.qmwj.model.chatmessage.ChatMessage;
import com.ld.qmwj.model.chatmessage.SimpleMsg;
import com.ld.qmwj.model.chatmessage.SmsMsg;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.view.MyEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import control.phone.linkman.LinkManActivity;
import control.phone.sms.SmsContentActivity;

/**
 * 消息显示相关的碎片
 */
public class MsgFragment extends Fragment implements View.OnClickListener, MyRecycleViewItemListener {

    private Monitor monitor;
    private ArrayList<ChatMessage> data;
    private RecyclerView recycleViewMsg;      //显示信息的recycleview
    private MessageRcAdapter rcAdapter;
    private SwipeRefreshLayout refreshLayout;
    private Context context;
    private MyEditText input_edit;    //输入框
    private ImageButton sendBtn;
    private int currentPage;           // 当前页数
    private int pageSize;              // 每页数据量
    private int currentCount;        //当前数据个数  用于刷新后定位原来的位置

    public MsgFragment(Monitor monitor, Context context) {
        this.monitor = monitor;
        this.context = context;
        this.currentPage = 1;
        this.pageSize = 10;
        //订阅事件
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_msg, container, false);
        initView(view);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void initData() {
        //开始初始化 1页 10条数据
        data = MyApplication.getInstance().getMessageDao().getMsgById(monitor.id, 1, pageSize);
        Log.d(Config.TAG, "数据：" + data.toString());
        rcAdapter.updateData(data);

    }

    private void initView(View v) {
        recycleViewMsg = (RecyclerView) v.findViewById(R.id.recycleview_msg);
        rcAdapter = new MessageRcAdapter(context);
        rcAdapter.setItemOnclickListener(this);
        recycleViewMsg.setAdapter(rcAdapter);
        initData();

        recycleViewMsg.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        //必须放在setLayoutManager后面
        recycleViewMsg.scrollToPosition(data.size() - 1);

        input_edit = (MyEditText) v.findViewById(R.id.input_edit);
        sendBtn = (ImageButton) v.findViewById(R.id.doSend_btn);
        input_edit.init(sendBtn);
        sendBtn.setOnClickListener(this);


        //下拉记载更多
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshlayout);

        //设置颜色
        refreshLayout.setColorSchemeResources(R.color.r, R.color.g, R.color.b);
        //背景色
        // refreshLayout.setProgressBackgroundColorSchemeColor(Color.argb(66,58,123,55));

        //设置刷新的监听器
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMore();
            }
        });

        ImageButton voiceBtn = (ImageButton) v.findViewById(R.id.input_voice_btn);
        sendBtn.setOnClickListener(this);
        ImageButton addBtn = (ImageButton) v.findViewById(R.id.input_add_btn);
        sendBtn.setOnClickListener(this);

       // recycleViewMsg.scrollToPosition(data.size() - 1);
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.doSend_btn:
                doSendMsg();
                break;
            case R.id.input_voice_btn:
                break;
            case R.id.input_add_btn:
                break;
        }
    }

    /**
     * 向对方发送文本消息
     */
    private void doSendMsg() {
        String msg = input_edit.getText().toString();
        if (msg.trim().isEmpty())
            return;
        SimpleMsg simpleMsg = new SimpleMsg();
        simpleMsg.is_coming = Config.TO_MSG;
        simpleMsg.time = System.currentTimeMillis();
        simpleMsg.msg = msg;
        //放入数据库中
        MyApplication.getInstance().getMessageDao().addMessage(
                monitor.id, simpleMsg, simpleMsg.msg
        );

        //发送缓存消息
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        chatRequest.into_id = monitor.id;
        chatRequest.msg = msg;
        chatRequest.time = System.currentTimeMillis();

        MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                MyApplication.getInstance().getGson().toJson(chatRequest)
        );
        EventBus.getDefault().post(HandlerUtil.CAHT_UPDATE);
        input_edit.setText("");
    }

    /**
     * 加载更多数据
     */
    private void loadMore() {
        currentCount = data.size();
        //计算recycleview要移动的距离
        int py = recycleViewMsg.getHeight() - recycleViewMsg.findViewHolderForLayoutPosition(0).itemView.getHeight();


        currentPage++;
        ArrayList<ChatMessage> temp = MyApplication.getInstance().getMessageDao().getMsgById(monitor.id, currentPage, pageSize);
        temp.addAll(data);
        data = temp;
        rcAdapter.updateData(data);

        if (currentCount != data.size()) {
            recycleViewMsg.scrollToPosition(data.size() - currentCount);
            recycleViewMsg.scrollBy(0, py - 50);
        } else {
            Toast.makeText(context, "暂无之前的记录", Toast.LENGTH_SHORT).show();
        }


        //停止刷新
        refreshLayout.setRefreshing(false);
    }


    /**
     * 处理传递来的handler消息
     *
     * @param tag
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfo(Integer tag) {
        if (tag == HandlerUtil.CAHT_UPDATE || tag == HandlerUtil.CALLPHONE_RESPONSE || tag == HandlerUtil.SMSSTATE_RESPONSE) {
            initData();
        }
    }


    /**
     * recycleview 视图项点击监听器
     *
     * @param v
     * @param position
     */
    @Override
    public void onItemClick(View v, int position) {
        ChatMessage chatMessage = data.get(position);
        // Log.d(Config.TAG,"点击了："+chatMessage);
        if (chatMessage.msg_type == Config.CALL_MSG){
            Intent intent=new Intent(context, LinkManActivity.class);
            intent.putExtra("type",Config.CALL_MSG);
            intent.putExtra("monitor",monitor);
            startActivity(intent);
        }else if (chatMessage.msg_type == Config.SMS_MSG) {
            Intent intent=new Intent(context, SmsContentActivity.class);
            SmsMsg smsMsg=(SmsMsg)chatMessage;
            String name=smsMsg.smsName;
            if(name == null)
                name=smsMsg.smsNum;
            Bundle bundle = new Bundle();
            //存入姓名
            bundle.putString("name", name);
            //存入电话号码
            bundle.putString("phonenum", smsMsg.smsNum);
            bundle.putSerializable("monitor", monitor);
            //传递Bundle
            intent.putExtra("SMS", bundle);
            startActivity(intent);
        }
    }
}
