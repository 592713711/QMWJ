package com.ld.qmwj.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ld.qmwj.Config;
import com.ld.qmwj.R;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/5/8.
 */
public class VoiceDialog extends DialogFragment {
    ImageView voice_size;
    Bitmap bitmap1;
    Bitmap bitmap2;
    Bitmap bitmap3;
    Bitmap bitmap4;
    Bitmap bitmap5;
    Bitmap bitmap6;
    Bitmap bitmap7;
    ArrayList<Bitmap> bitmaps;
    Context context;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==88){
                voice_size.setImageBitmap(bitmaps.get(pos));
                pos++;
                if (pos > 6)
                    pos = 0;
                handler.sendEmptyMessageDelayed(88,300);
            }
        }
    };
    public VoiceDialog(Context context) {
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_voice, null);

        voice_size = (ImageView) view.findViewById(R.id.voice_size);
        initBitmap();
        Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        dialog.setContentView(view);
        return dialog;
    }

    private void initBitmap() {
        bitmaps = new ArrayList<>();
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_2);
        bitmap3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_3);
        bitmap4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_4);
        bitmap5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_5);
        bitmap6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_6);
        bitmap7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.voice_7);

        bitmaps.add(bitmap1);
        bitmaps.add(bitmap2);
        bitmaps.add(bitmap3);
        bitmaps.add(bitmap4);
        bitmaps.add(bitmap5);
        bitmaps.add(bitmap6);
        bitmaps.add(bitmap7);


    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        handler.sendEmptyMessage(88);

    }

    public void stopAnim() {
        pos = 0;
        handler.removeMessages(88);

    }

    int pos = 0;

}
