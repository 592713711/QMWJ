package under_control.home;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ld.qmwj.R;

public class HomeActivity_under extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        //开启定位服务
        Intent intent=new Intent(this,LocationService.class);
        startService(intent);
    }


}
