package control.phone;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ld.qmwj.R;
import com.ld.qmwj.model.Monitor;

/**
 * 手机相关的碎片
 */
public class PhoneFragment extends Fragment {

    private Monitor monitor;
    public PhoneFragment(Monitor monitor) {
        this.monitor=monitor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone, container, false);
    }







}
