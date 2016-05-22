package control.phone.audio.sender;

import android.util.Log;

import com.ld.qmwj.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import control.phone.audio.AudioData;


public class AudioSender implements Runnable {
    String LOG = "AudioSender ";

    private boolean isSendering = true;
    private List<AudioData> dataList;

    DatagramSocket socket;
    DatagramPacket dataPacket;
    private InetAddress ip;
    private int port;

    public AudioSender(int server_port) {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        try {
            try {
                ip = InetAddress.getByName(Config.serverIP);
                Log.d(LOG, "服务端地址是 " + ip.toString());
                port = server_port;
                socket = new DatagramSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void addData(byte[] data, int size) {
        AudioData encodedData = new AudioData();
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        encodedData.setRealData(tempData);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    private void sendData(byte[] data, int size) {
        try {
            dataPacket = new DatagramPacket(data, size, ip, port);
            dataPacket.setData(data);
            Log.d(Config.TAG, "发送一段数据 " + data.length);
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * start sending data
     */
    public void startSending() {
        new Thread(this).start();
    }

    /*
     * stop sending data
     */
    public void stopSending() {
        this.isSendering = false;
    }

    // run
    public void run() {
        this.isSendering = true;
        System.out.println(LOG + "start....");
        while (isSendering) {
         //   Log.e(LOG,"dataList.size()："+dataList.size());
            if (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                sendData(encodedData.getRealData(), encodedData.getSize());
            }
        }

        System.out.println(LOG + "stop!!!!");
    }
}