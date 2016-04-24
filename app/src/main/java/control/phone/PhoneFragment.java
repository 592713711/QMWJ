package control.phone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;

import control.phone.health.MiBandActivity;
import under_control.home.miband.HeartActivity;
import control.phone.linkman.LinkManActivity;
import control.phone.sms.SmsListActivity;

/**
 * 手机相关的碎片
 */
public class PhoneFragment extends Fragment implements View.OnClickListener{

    private Monitor monitor;
    private Context context;
    public PhoneFragment(Monitor monitor,Context context) {
        this.monitor=monitor;
        this.context=context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_phone, container, false);
        initView(view);


        return view;
    }

    private void initView(View view) {
        ImageButton linkmanBtn= (ImageButton) view. findViewById(R.id.linkman_btn);
        linkmanBtn.setOnClickListener(this);
        ImageButton healthBtn= (ImageButton) view. findViewById(R.id.health_btn);
        healthBtn.setOnClickListener(this);
        ImageButton smsBtn= (ImageButton) view.findViewById(R.id.sms_btn);
        smsBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.linkman_btn:
                //打开联系人活动
                intent=new Intent(context, LinkManActivity.class);
                intent.putExtra("monitor",monitor);
                startActivity(intent);
                break;
            case R.id.health_btn:
                //打开联系人活动
                intent=new Intent(context, MiBandActivity.class);
                intent.putExtra("monitor",monitor);
                startActivity(intent);
                break;
            case  R.id.sms_btn:
                intent=new Intent(context, SmsListActivity.class);
                intent.putExtra("monitor",monitor);
                startActivity(intent);
                break;
        }
    }
}
