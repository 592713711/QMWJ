package control.phone.audio.sender;

import android.media.AudioRecord;
import android.util.Log;

import control.phone.audio.AudioConfig;


public class AudioRecorder implements Runnable {

    String LOG = "Recorder ";

    private boolean isRecording = false;

    private static final int BUFFER_FRAME_SIZE = 480;
    private int audioBufSize = 0;
    private byte[] samples;// data
    private int bufferSize = 0;
    private AudioRecord audioRecord;
    private int server_port;

    public AudioRecorder(int server_port) {
        this.server_port = server_port;
    }

    public void setPost(int server_port) {
        this.server_port = server_port;
    }

    /*
     * start recording
     */
    public void startRecording() {


        bufferSize = BUFFER_FRAME_SIZE;

        audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.RECORDER_CHANNEL_CONFIG, AudioConfig.AUDIO_FORMAT);
        if (audioBufSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(LOG, "audioBufSize error");
            return;
        }

        // 初始化recorder
        if (null == audioRecord) {
            audioBufSize = AudioRecord.getMinBufferSize(8000, 16, 2);
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.RECORDER_CHANNEL_CONFIG,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);
            audioRecord = new AudioRecord(1, 8000, 16, 2, audioBufSize);

        }


        //录制结束

        //new Thread(this).start()
        // audioBufSize=400;
        samples = new byte[audioBufSize];
        audioRecord.startRecording();
        new Thread(this).start();


    }

    /*
     * stop
     */
    public void stopRecording() {
        this.isRecording = false;
    }

    int bufferRead;

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void run() {
        // start encoder before recording
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.setPort(server_port);
        encoder.startEncoding();
        System.out.println(LOG + "audioRecord startRecording()");

        System.out.println(LOG + "start recording");

        this.isRecording = true;
        while (isRecording) {
            samples = new byte[audioBufSize];
            bufferRead = audioRecord.read(samples, 0, bufferSize);
            //Log.e(LOG, "bufferRead：" + bufferRead + " " + audioBufSize + " " + bufferSize);
            if (bufferRead > 0) {
                // add data to encoder
                encoder.addData(samples, bufferRead);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(LOG + "end recording");
        audioRecord.stop();
        encoder.stopEncoding();
    }


    public void r3un() {
        while (true) {
            int bufferRead = audioRecord.read(samples, 0, audioBufSize);
            Log.e("temp", "bufferRead：" + bufferRead + " " + audioBufSize + " " + audioBufSize);
            if (bufferRead > 0) {
                // add data to encoder
                Log.e("temp", "record2");
                // encoder.addData(samples, bufferRead);
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
