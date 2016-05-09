package control.phone.linkman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ld.qmwj.Config;
import com.ld.qmwj.R;
import com.ld.qmwj.model.PhoneState;
import com.ld.qmwj.util.TimeUtil;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/5/3.
 */
public class PhoneStateAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<PhoneState> calldata;

    public PhoneStateAdapter(Context context, ArrayList<PhoneState> calldata) {
        this.calldata = calldata;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return calldata.size();
    }

    @Override
    public Object getItem(int position) {
        return calldata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView type_img;
        TextView call_name;
        TextView call_phonenum;
        TextView call_time;
        TextView call_duration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhoneState ps = calldata.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.phonestate_item, null);
            viewHolder. call_name = (TextView) convertView.findViewById(R.id.call_name);
            viewHolder. call_phonenum = (TextView) convertView.findViewById(R.id.call_phonenum);
            viewHolder.  call_time = (TextView) convertView.findViewById(R.id.call_time);
            viewHolder.call_duration = (TextView) convertView.findViewById(R.id.call_duration);
            viewHolder. type_img = (ImageView) convertView.findViewById(R.id.type_img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.call_time.setText(TimeUtil.getTimeStr2(ps.getEndTime()));
        viewHolder.call_duration.setText("时长：" + TimeUtil.getTimeBySecond(ps.getDuration()));

        if (ps.getType() == Config.CALL_INTO) {
            // 打进来
            viewHolder.type_img.setImageResource(R.drawable.call_into);
        } else if (ps.getType() == Config.CALL_GOTO) {
            //拨出
            viewHolder.type_img.setImageResource(R.drawable.call_goto);
            // myholder.call_duration.setText(ps.getDuration());
        } else if (ps.getType() == Config.CALL_MISS) {
            //未接
            viewHolder.type_img.setImageResource(R.drawable.call_miss);
            viewHolder.call_duration.setText("未接");
        }

        String name = ps.getName();
        String phonenum = ps.getPhonenum();
        if (name == null) {
            name = phonenum;
            if(ps.getAddress()==null){
                phonenum="未知号码";
            }else{
                phonenum=ps.getAddress();
            }
        }

        viewHolder.call_name.setText(name);
        viewHolder.call_phonenum.setText(phonenum);
        return convertView;
    }
}
