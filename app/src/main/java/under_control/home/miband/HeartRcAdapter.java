package under_control.home.miband;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ld.qmwj.R;
import com.ld.qmwj.listener.MyRecycleViewItemListener;
import com.ld.qmwj.model.HeartData;
import com.ld.qmwj.util.TimeUtil;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/4/12.
 */
public class HeartRcAdapter extends RecyclerView.Adapter {
    private LayoutInflater inflater;
    private Context context;
    private ArrayList<HeartData> datas;
    private MyRecycleViewItemListener listener;

    public static int SIMPLE_TYPE = 0;
    public static int DOMOER_TYPE = 1;


    public HeartRcAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
    }

    public void updateData(ArrayList<HeartData> list) {
        datas.clear();
        datas.addAll(list);
        datas.add(null);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder=null;
        if (viewType == SIMPLE_TYPE) {
            View v = inflater.inflate(R.layout.heart_item, null);
            holder = new MyHolder(v);
        }else if(viewType==DOMOER_TYPE){
            View v=inflater.inflate(R.layout.domore_item,null);
            holder=new MoreHolder(v,listener);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeartData heartdata = datas.get(position);
        if(position==datas.size()-1){
            //最后一项
            return;
        }

        MyHolder myHolder = (MyHolder) holder;

        myHolder.heartdata.setText(heartdata.data + "");
        myHolder.time.setText(TimeUtil.getTimeStr3(heartdata.time));

        String heartState = "";
        if (heartdata.data >= 60 && heartdata.data <= 100)
            heartState = "心率正常";
        else if (heartdata.data > 100)
            heartState = "心率过快";
        else
            heartState = "心率过慢";

        myHolder.state.setText(heartState);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == datas.size() - 1)       //视图项最末一个
            return DOMOER_TYPE;
        else
            return SIMPLE_TYPE;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setItemOnClickListener(MyRecycleViewItemListener listener) {
        this.listener=listener;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView state;
        TextView heartdata;

        public MyHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.heart_time);
            state = (TextView) itemView.findViewById(R.id.heart_state);
            heartdata = (TextView) itemView.findViewById(R.id.heart_data);
        }
    }


    /**
     * 更多视图项
     */
    class MoreHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MyRecycleViewItemListener lis;
        public MoreHolder(View itemView,MyRecycleViewItemListener lis) {
            super(itemView);
            this.lis=lis;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            lis.onItemClick(v,getAdapterPosition());
        }
    }
}
