package control.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.mapapi.model.LatLng;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.squareup.picasso.Picasso;

/**
 * 全景活动显示
 */
public class PanoramaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        double latitude=getIntent().getDoubleExtra("latitude",0);
        double longitude=getIntent().getDoubleExtra("longitude",0);
        PanoramaView panoramaView = (PanoramaView) findViewById(R.id.panorama);
        panoramaView.setPanorama(longitude,latitude);


    }
}
