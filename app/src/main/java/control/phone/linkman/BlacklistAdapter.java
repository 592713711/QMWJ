package control.phone.linkman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.message.request.DeleteBlackPhoneReq;
import com.ld.qmwj.model.BlackPhone;
import com.ld.qmwj.model.Monitor;

import java.util.ArrayList;

/**
 * Created by Chalmers on 2016-05-04 12:25.
 * email:qxinhai@yeah.net
 */
public class BlacklistAdapter extends BaseAdapter {

    private ArrayList<BlackPhone> blackContactsList = null;
    private LayoutInflater layoutInflater = null;
    private Monitor monitor;
    private Context context;

    public BlacklistAdapter(Context context, ArrayList<BlackPhone> blackContactsList, Monitor monitor) {
        this.blackContactsList = blackContactsList;
        this.layoutInflater = LayoutInflater.from(context);
        this.monitor = monitor;
        this.context=context;
    }

    @Override
    public int getCount() {
        return blackContactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return blackContactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.blacklist_item, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.bindData(blackContactsList.get(position));

        return convertView;
    }

    class ViewHolder {
        private TextView tv_num;
        private TextView tv_location;
        private Button btn_delete;

        public ViewHolder(View v) {
            tv_num = (TextView) v.findViewById(R.id.id_tv_num);
            tv_location = (TextView) v.findViewById(R.id.id_tv_location);
            btn_delete = (Button) v.findViewById(R.id.id_btn_delete);
        }

        /**
         * 绑定数据
         */
        public void bindData(BlackPhone bp) {
            tv_num.setText(bp.phonenum);
            tv_location.setText(bp.address);
            btn_delete.setOnClickListener(new ButtonListener(bp));
        }

        class ButtonListener implements View.OnClickListener {
            private BlackPhone bp = null;

            public ButtonListener(BlackPhone bp) {
                this.bp = bp;
            }

            @Override
            public void onClick(View v) {
                if (monitor.identify == Config.MAIN_GUARDIAN) {
                    //从显示列表中移除掉要删除的对象
                    blackContactsList.remove(bp);

                    /**
                     * 从数据表中移除该号码信息
                     */
                    MyApplication.getInstance().getBlacklistDao().deleteBlackPhone(monitor.id, bp.phonenum);

                    /**
                     * 向服务器发送删除信息
                     */
                    DeleteBlackPhoneReq request = new DeleteBlackPhoneReq();
                    request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
                    request.into_id = monitor.id;
                    request.blackPhone = bp;
                    MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                            MyApplication.getInstance().getGson().toJson(request)
                    );


                    //刷新列表
                    BlacklistAdapter.this.notifyDataSetChanged();
                }else{
                    Toast.makeText(context,"只有主监护方才能修改黑名单",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
