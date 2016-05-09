package control.msg;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.message.request.ChatRequest;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.model.chatmessage.ChatMessage;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.model.chatmessage.SimpleMsg;
import com.ld.qmwj.model.chatmessage.SmsMsg;
import com.ld.qmwj.util.HandlerUtil;
import com.ld.qmwj.util.VoiceDialog;
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
    private VoiceDialog voiceDialog;

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
        //sendBtn.setOnClickListener(this);
        ImageButton addBtn = (ImageButton) v.findViewById(R.id.input_add_btn);
        //sendBtn.setOnClickListener(this);

        // recycleViewMsg.scrollToPosition(data.size() - 1);

        voiceDialog=new VoiceDialog(context);

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
        EventBus.getDefault().post(HandlerUtil.CHAT_UPDATE);
        input_edit.setText("");
    }

    /**
     * 加载更多数据
     */
    private void loadMore() {
        currentCount = data.size();
        //计算recycleview要移动的距离
        int py=0;
        if (data.size()!=0) {
           py = recycleViewMsg.getHeight() - recycleViewMsg.findViewHolderForLayoutPosition(0).itemView.getHeight();
        }


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
        if (tag == HandlerUtil.CHAT_UPDATE || tag == HandlerUtil.CALLPHONE_RESPONSE || tag == HandlerUtil.SMSSTATE_RESPONSE) {
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
        if (chatMessage.msg_type == Config.CALL_MSG) {
            Intent intent = new Intent(context, LinkManActivity.class);
            intent.putExtra("type", Config.CALL_MSG);
            intent.putExtra("monitor", monitor);
            startActivity(intent);
        } else if (chatMessage.msg_type == Config.SMS_MSG) {
            Intent intent = new Intent(context, SmsContentActivity.class);
            SmsMsg smsMsg = (SmsMsg) chatMessage;
            String name = smsMsg.smsName;
            if (name == null)
                name = smsMsg.smsNum;
            Bundle bundle = new Bundle();
            //存入姓名
            bundle.putString("name", name);
            //存入电话号码
            bundle.putString("phonenum", smsMsg.smsNum);
            bundle.putSerializable("monitor", monitor);
            //传递Bundle
            intent.putExtra("SMS", bundle);
            startActivity(intent);
        }else if(chatMessage.msg_type==Config.MAPWAY_MSG){
            //进入地图界面 显示路径信息
            //通知MonitorActivity显示地图界面
            MapWayMsg mapWayMsg= (MapWayMsg) chatMessage;
            EventBus.getDefault().post(mapWayMsg);
        }else if(chatMessage.msg_type==Config.CHAT_MSG){
            //文本聊天转化为语音
            SimpleMsg simpleMsg= (SimpleMsg) chatMessage;
            doChangeVoice(simpleMsg.msg);
        }
    }

    /**
     * 将文本信息转化为语音
     */
    public void doChangeVoice(String text) {

        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(context, null);
//2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
//保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
//如果不需要保存合成音频，注释该行代码
      //  mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
//3.开始合成
        mTts.startSpeaking(text, mSynListener);
    }

    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            voiceDialog.dismiss();
            voiceDialog.stopAnim();
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
            voiceDialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "WAIT_DIALOG");
            voiceDialog.setCancelable(false);
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
