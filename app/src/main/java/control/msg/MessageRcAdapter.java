package control.msg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.R;
import com.ld.qmwj.model.chatmessage.ChatMessage;
import com.ld.qmwj.model.chatmessage.PhoneStateMsg;
import com.ld.qmwj.model.chatmessage.SimpleMsg;
import com.ld.qmwj.model.chatmessage.SmsMsg;
import com.ld.qmwj.util.TimeUtil;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/3/25.
 */
public class MessageRcAdapter extends RecyclerView.Adapter {
    private ArrayList<ChatMessage> data;
    private Context context;
    private LayoutInflater inflater;

    public static final int CHAT_FROM_TYPE = 0;
    public static final int CHAT_SEND_TYPE = 1;
    public static final int HINT_FROM_TYPE = 2;

    public MessageRcAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public void updateData(ArrayList<ChatMessage> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = null;
        View v;
        switch (viewType) {
            case CHAT_FROM_TYPE:
                v = inflater.inflate(R.layout.chat_from_item, null);
                holder = new ChatFromHolder(v);
                break;
            case CHAT_SEND_TYPE:
                v = inflater.inflate(R.layout.chat_send_item, null);
                holder = new ChatSendHolder(v);
                break;
            case HINT_FROM_TYPE:
                v = inflater.inflate(R.layout.hint_from_item, null);
                holder = new HintFromHolder(v);
                break;

        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage cm = data.get(position);
        if (holder instanceof MyViewHolder) {
            MyViewHolder myHolder = (MyViewHolder) holder;
            myHolder.time.setText(TimeUtil.getTimeStr2(cm.time));
        }

        //文本聊天  from
        if (holder instanceof ChatFromHolder) {
            ChatFromHolder chatfromHolder = (ChatFromHolder) holder;
            SimpleMsg simpleMsg = (SimpleMsg) cm;
            chatfromHolder.msgText.setText(simpleMsg.msg);
        }

        //文本聊天  send
        if (holder instanceof ChatSendHolder) {
            ChatSendHolder chatsendHolder = (ChatSendHolder) holder;
            SimpleMsg simpleMsg = (SimpleMsg) cm;
            chatsendHolder.msgText.setText(simpleMsg.msg);
        }

        //消息提醒
        if (holder instanceof HintFromHolder) {
            HintFromHolder hintFromHolder = (HintFromHolder) holder;

            if (cm.msg_type == Config.CALL_MSG) {   //通话记录
                PhoneStateMsg phoneStateMsg = (PhoneStateMsg) cm;
                Bitmap icon = null;
                StringBuffer hint = new StringBuffer();
                String name;
                if (phoneStateMsg.phonename == null)
                    name=(phoneStateMsg.phoneNum);
                else
                    name=(phoneStateMsg.phonename);
                switch (phoneStateMsg.call_Type){
                    case Config.CALL_INTO:
                        icon=BitmapFactory.decodeResource(context.getResources(),R.drawable.call_into);
                        hint.append("接听来自 "+name+" 的电话");
                        break;
                    case Config.CALL_GOTO:
                        icon=BitmapFactory.decodeResource(context.getResources(),R.drawable.call_goto);
                        hint.append("拨打电话给 "+name);
                        break;
                    case Config.CALL_MISS:
                        icon=BitmapFactory.decodeResource(context.getResources(),R.drawable.call_miss);
                        hint.append("未接到 "+name+" 的电话");
                        break;
                }

                hintFromHolder.hintText.setText(hint);
                hintFromHolder.iconImage.setImageBitmap(icon);
            } else if (cm.msg_type == Config.SMS_MSG) {
                //短信监听记录
                SmsMsg smsMsg = (SmsMsg) cm;
                Bitmap icon = null;
                StringBuffer hint = new StringBuffer();
                switch (smsMsg.smsType) {
                    case Config.SMS_INTO:
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.sms_from);
                        hint.append("收到一条短信来自");
                        break;
                    case Config.SMS_GOTO:
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.sms_send);
                        hint.append("发送一条短信到");
                        break;
                }

                if (smsMsg.smsName == null)
                    hint.append(smsMsg.smsNum);
                else
                    hint.append(smsMsg.smsName);
                hintFromHolder.hintText.setText(hint);
                hintFromHolder.iconImage.setImageBitmap(icon);

            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage cm = data.get(position);
        if (cm.is_coming == Config.FROM_MSG) {
            switch (cm.msg_type) {
                case Config.CHAT_MSG:
                    return CHAT_FROM_TYPE;
                case Config.CALL_MSG:
                case Config.SMS_MSG:
                    return HINT_FROM_TYPE;
            }

        } else {
            if (cm.msg_type == Config.CHAT_MSG) {
                return CHAT_SEND_TYPE;
            }
        }

        return super.getItemViewType(position);
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public ImageView headView;

        public MyViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.chat_createDate);
            headView = (ImageView) itemView.findViewById(R.id.chat_icon);
        }
    }

    class ChatFromHolder extends MyViewHolder {
        public TextView msgText;

        public ChatFromHolder(View itemView) {
            super(itemView);
            msgText = (TextView) itemView.findViewById(R.id.chat_content);
        }
    }

    class ChatSendHolder extends MyViewHolder {
        public TextView msgText;

        public ChatSendHolder(View itemView) {
            super(itemView);
            msgText = (TextView) itemView.findViewById(R.id.chat_content);
        }
    }

    class HintFromHolder extends MyViewHolder {
        ImageView iconImage;
        TextView hintText;

        public HintFromHolder(View itemView) {
            super(itemView);
            iconImage = (ImageView) itemView.findViewById(R.id.hint_icon);
            hintText = (TextView) itemView.findViewById(R.id.hintmsg);
        }
    }


}
