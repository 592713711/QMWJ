package control.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;
import com.ld.qmwj.view.BadgeView;
import com.ld.qmwj.view.CircleImageView;

import java.util.ArrayList;

/**
 * recycleview 适配器
 * Created by zsg on 2016/2/23.
 */
public class RcAdapter extends RecyclerView.Adapter {
    ArrayList<Monitor> data;
    Context context;
    LayoutInflater inflater;
    RecycleViewOnItemClick lis;

    public RcAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public void updateData(ArrayList<Monitor> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 设置监听器
     *
     * @return
     */
    public void setItemOnClickListener(RecycleViewOnItemClick listener) {
        this.lis = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyHolder holder;
        View v = inflater.inflate(R.layout.monitor_list_item, null);
        holder = new MyHolder(v, lis);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyHolder myholder = (MyHolder) holder;
        Monitor monitor = data.get(position);
        if (monitor.remark_name == null)
            myholder.name.setText(monitor.username);
        else
            myholder.name.setText(monitor.remark_name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView msg;
        TextView time;
        BadgeView mBadgeView;
        CircleImageView head_btn;
        RecycleViewOnItemClick lis;

        public MyHolder(View itemView, RecycleViewOnItemClick lis) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            msg = (TextView) itemView.findViewById(R.id.message);
            time = (TextView) itemView.findViewById(R.id.time);
            mBadgeView = (BadgeView) itemView.findViewById(R.id.badgeView);
            head_btn = (CircleImageView) itemView.findViewById(R.id.headicon_view);

            this.lis = lis;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            lis.onItemClick(v, getAdapterPosition());
        }
    }

    /**
     * recycleview点击监听接口
     */
    public interface RecycleViewOnItemClick {
        public void onItemClick(View view, int postion);
    }
}
