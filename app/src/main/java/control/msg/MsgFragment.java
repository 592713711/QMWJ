package control.msg;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;

/**
 * 消息显示相关的碎片
 */
public class MsgFragment extends Fragment {

    private Monitor monitor;
    public MsgFragment(Monitor monitor) {
        this.monitor=monitor;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_msg, container, false);
    }


}
