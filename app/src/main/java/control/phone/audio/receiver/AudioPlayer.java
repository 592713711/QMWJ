package control.phone.audio.receiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import control.phone.audio.AudioConfig;
import control.phone.audio.AudioData;

public class AudioPlayer implements Runnable {
    String LOG = "AudioPlayer ";
    private static AudioPlayer player;

    private List<AudioData> dataList = null;
    private AudioData playData;
    private boolean isPlaying = false;

    private AudioTrack audioTrack;

    //
    private File file;
    private FileOutputStream fos;

    private AudioPlayer() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());

        file = new File("/sdcard/audio/decode.amr");
        try {
            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AudioPlayer getInstance() {
        if (player == null) {
            player = new AudioPlayer();
        }
        return player;
    }

    public void addData(byte[] rawData, int size) {
        AudioData decodedData = new AudioData();
        decodedData.setSize(size);
        byte[] tempData = new byte[size];
        if (size > rawData.length) {
            Log.e(LOG, "长度过长");
        } else {
            System.arraycopy(rawData, 0, tempData, 0, size);
            decodedData.setRealData(tempData);
            dataList.add(decodedData);
            Log.d(LOG, "Player添加一次数据 " + dataList.size());
        }

    }

    /*
     * init Player parameters
     */
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT);
        if (bufferSize < 0) {
            Log.d(LOG, LOG + "initialize error!");
            return false;
        }
        Log.d(LOG, "Player初始化的 buffersize是 " + bufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                AudioConfig.SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
        // set volume:设置播放音量
        audioTrack.setStereoVolume(1.0f, 1.0f);
        audioTrack.play();
        return true;
    }

    private void playFromList() throws IOException {
        while (isPlaying) {
            while (dataList.size() > 0) {
                playData = dataList.remove(0);
                Log.d(LOG, "播放一ddsdfasd次数据 " + dataList.size());
                int audiosize=audioTrack.write(playData.getRealData(), 0, playData.getSize());
                int v = 0;
                byte[] datas=playData.getRealData();
                // 将 buffer 内容取出，进行平方和运算
                for (int i = 0; i <datas .length; i++) {
                    // 这里没有做运算的优化，为了更加清晰的展示代码
                    v += datas[i] *datas[i];
                }
                // 平方和除以数据总长度，得到音量大小。可以获取白噪声值，然后对实际采样进行标准化。
                // 如果想利用这个数值进行操作，建议用 sendMessage 将其抛出，在 Handler 里进行处理。
                double dB = 20*Math.log10(v/(double)audiosize);
               //更改主界面的ui
                EventBus.getDefault().post(dB);

                // fos.write(playData.getRealData(), 0, playData.getSize());
                // fos.flush();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void startPlaying() {
        if (isPlaying) {
            Log.e(LOG, "验证播放器是否打开" + isPlaying);
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        this.isPlaying = true;
        if (!initAudioTrack()) {
            Log.i(LOG, "播放器初始化失败");
            return;
        }
        Log.e(LOG, "开始播放");
        try {
            playFromList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // while (isPlaying) {
        // if (dataList.size() > 0) {
        // playFromList();
        // } else {
        //
        // }
        // }
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
        Log.d(LOG, LOG + "end playing");
    }

    public void stopPlaying() {
        this.isPlaying = false;
    }
}
