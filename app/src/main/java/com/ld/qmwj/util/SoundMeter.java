package com.ld.qmwj.util;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SoundMeter {
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    public void start(String name) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(MyApplication.getInstance().getRecordPath() + name);
            try {
                mRecorder.prepare();
                mRecorder.start();

                mEMA = 0.0;
            } catch (IllegalStateException e) {
                System.out.print(e.getMessage());
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }

        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void pause() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    public byte[] getRecordData(String filename) {
        byte[] data = null;
        File file = new File(MyApplication.getInstance().getRecordPath()+filename);
        if (!file.exists()) {
            //文件不存在
            return data;
        }
        Log.d(Config.TAG,"读取文件："+file.getAbsolutePath());
        int length= (int) file.length();
        data=new byte[length];
        try {

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
           randomAccessFile.read(data);
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Config.TAG,"文件内容:"+data.toString());
        return data;
    }
}
