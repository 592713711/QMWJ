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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.R;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.model.RouteWay;
import com.ld.qmwj.model.chatmessage.ChatMessage;
import com.ld.qmwj.model.chatmessage.MapWayMsg;
import com.ld.qmwj.model.chatmessage.PhoneStateMsg;
import com.ld.qmwj.model.chatmessage.RecordMsg;
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
    private MyRecycleViewItemListener myRcClickListener;

    public static final int CHAT_FROM_TYPE = 0;
    public static final int CHAT_SEND_TYPE = 1;
    public static final int HINT_FROM_TYPE = 2;
    public static final int MAPWAY_SEND_TYPE = 3;
    public static final int MAPWAY_FROM_TYPE = 4;
    public static final int VOICE_FROM_TYPE = 5;
    public static final int VOICE_SEND_TYPE = 6;

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

    public void setItemOnclickListener(MyRecycleViewItemListener listener) {
        this.myRcClickListener = listener;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = null;
        View v;
        switch (viewType) {
            case CHAT_FROM_TYPE:
                v = inflater.inflate(R.layout.chat_from_item, null);
                holder = new ChatFromHolder(v, myRcClickListener);
                break;
            case CHAT_SEND_TYPE:
                v = inflater.inflate(R.layout.chat_send_item, null);
                holder = new ChatSendHolder(v, myRcClickListener);
                break;
            case HINT_FROM_TYPE:
                v = inflater.inflate(R.layout.hint_from_item, null);
                holder = new HintFromHolder(v, myRcClickListener);
                break;
            case MAPWAY_SEND_TYPE:
                v = inflater.inflate(R.layout.mapway_send_item, null);
                holder = new MapWaySendHolder(v, myRcClickListener);
                break;
            case MAPWAY_FROM_TYPE:
                v = inflater.inflate(R.layout.mapway_from_item, null);
                holder = new MapWaySendHolder(v, myRcClickListener);
                break;
            case VOICE_FROM_TYPE:
                v = inflater.inflate(R.layout.voice_from_item, null);
                holder = new HintFromHolder(v, myRcClickListener);
                break;
            case VOICE_SEND_TYPE:
                v = inflater.inflate(R.layout.voice_send_item, null);
                holder = new HintFromHolder(v, myRcClickListener);
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
                StringBuffer hint1 = new StringBuffer();
                String name;
                if (phoneStateMsg.phonename == null)
                    name = (phoneStateMsg.phoneNum);
                else
                    name = (phoneStateMsg.phonename);
                switch (phoneStateMsg.call_Type) {
                    case Config.CALL_INTO:
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.call_into);
                        hint1.append("接听电话");
                        break;
                    case Config.CALL_GOTO:
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.call_goto);
                        hint1.append("拨打电话给");
                        break;
                    case Config.CALL_MISS:
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.call_miss);
                        hint1.append("未接电话");
                        break;
                }

                hintFromHolder.hintText1.setText(hint1);
                hintFromHolder.hintText2.setText(name);
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
                    hintFromHolder.hintText2.setText(smsMsg.smsNum);
                else
                    hintFromHolder.hintText2.setText(smsMsg.smsName);
                hintFromHolder.hintText1.setText(hint);
                hintFromHolder.iconImage.setImageBitmap(icon);

            }else if(cm.msg_type == Config.RECORD_MSG){
                //语音信息
                RecordMsg recordMsg= (RecordMsg) cm;
                int time=recordMsg.duration;
                hintFromHolder.hintText2.setText(time+"”");

                //控制信息宽度
                if(time<5&&time>=3)
                    hintFromHolder.hintText1.setText("\t");
                else  if(time<8&&time>=5)
                    hintFromHolder.hintText1.setText("\t\t\t");
                else if(time<12&&time>=8)
                    hintFromHolder.hintText1.setText("\t\t\t\t\t");
                else if(time<15&&time>=12)
                    hintFromHolder.hintText1.setText("\t\t\t\t\t\t");
                else if(time<20&&time>=15)
                    hintFromHolder.hintText1.setText("\t\t\t\t\t\t\t");
                else if(time<30&&time>=20)
                    hintFromHolder.hintText1.setText("\t\t\t\t\t\t\t\t");
                else if(time>30)
                    hintFromHolder.hintText1.setText("\t\t\t\t\t\t\t\t\t");

            }
        }

        if (holder instanceof MapWaySendHolder) {
            MapWaySendHolder mapHolder = (MapWaySendHolder) holder;
            MapWayMsg mapWayMsg = (MapWayMsg) cm;
            RouteWay routeWay = mapWayMsg.routeWay;
            String routetype = "";
            if (routeWay.way_type == Config.ROUTE_WALK)
                routetype = "步行";
            if (routeWay.way_type == Config.ROUTE_DRIVE)
                routetype = "驾车";
            if (routeWay.way_type == Config.ROUTE_BUS)
                routetype = "换乘";
            mapHolder.hintText1.setText(routetype + "路径消息");
            mapHolder.hintText2.setText(routeWay.endAddress);
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
                case Config.MAPWAY_MSG:
                    return MAPWAY_FROM_TYPE;
                case Config.RECORD_MSG:
                    return VOICE_FROM_TYPE;
            }

        } else {
            if (cm.msg_type == Config.CHAT_MSG) {
                return CHAT_SEND_TYPE;
            } else if (cm.msg_type == Config.MAPWAY_MSG) {
                return MAPWAY_SEND_TYPE;
            } else if (cm.msg_type == Config.RECORD_MSG) {
                return VOICE_SEND_TYPE;
            }
        }

        return super.getItemViewType(position);
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView time;
        public ImageView headView;
        public MyRecycleViewItemListener onClickListener;


        public MyViewHolder(View itemView, MyRecycleViewItemListener onClickListener) {
            super(itemView);
            this.onClickListener = onClickListener;
            time = (TextView) itemView.findViewById(R.id.chat_createDate);
            headView = (ImageView) itemView.findViewById(R.id.chat_icon);

            (itemView.findViewById(R.id.chat_content)).setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null)
                onClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class ChatFromHolder extends MyViewHolder {
        public TextView msgText;

        public ChatFromHolder(View itemView, MyRecycleViewItemListener onClickListener) {
            super(itemView, onClickListener);
            msgText = (TextView) itemView.findViewById(R.id.chat_content);

        }
    }

    class ChatSendHolder extends MyViewHolder {
        public TextView msgText;

        public ChatSendHolder(View itemView, MyRecycleViewItemListener onClickListener) {
            super(itemView, onClickListener);
            msgText = (TextView) itemView.findViewById(R.id.chat_content);

        }


    }

    class HintFromHolder extends MyViewHolder implements View.OnClickListener {
        ImageView iconImage;
        TextView hintText1;
        TextView hintText2;

        public HintFromHolder(View itemView, MyRecycleViewItemListener onClickListener) {
            super(itemView, onClickListener);

            iconImage = (ImageView) itemView.findViewById(R.id.hint_icon);
            hintText1 = (TextView) itemView.findViewById(R.id.hintmsg1);
            hintText2 = (TextView) itemView.findViewById(R.id.hintmsg2);

        }

    }


    class MapWaySendHolder extends MyViewHolder implements View.OnClickListener {
        TextView hintText1;
        TextView hintText2;
        RelativeLayout layout;

        public MapWaySendHolder(View itemView, MyRecycleViewItemListener onClickListener) {
            super(itemView, onClickListener);

            hintText1 = (TextView) itemView.findViewById(R.id.hintmsg1);
            hintText2 = (TextView) itemView.findViewById(R.id.hintmsg2);

        }

    }

}
