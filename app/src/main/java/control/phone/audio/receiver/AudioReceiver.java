package control.phone.audio.receiver;

import android.util.Log;
;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AudioReceiver implements Runnable {
    String LOG = "AudioReceiver";
    int port = Config.CLIENT_RECORD_PORT;// 接收的端口
    DatagramSocket socket;
    DatagramPacket packet;
    boolean isRunning = false;

    private byte[] packetBuf = new byte[1024];
    private int packetSize = 1024;
    private int serverPort;     //服务端 端口

    /*
     * 开始接收数据
     */
    public void startRecieving() {

        socket = MyApplication.getInstance().getSocket();
        packet = new DatagramPacket(packetBuf, packetSize);


//        Log.d(Config.TAG, "地址：" + socket.getLocalSocketAddress().toString() + " " + socket.getLocalPort());
        new Thread(this).start();
    }

    /*
     * 停止接收数据
     */
    public void stopRecieving() {
        isRunning = false;
    }

    /*
     * 释放资源
     */
    private void release() {
        if (packet != null) {
            packet = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void run() {
        // 在接收前，要先启动解码器
        AudioDecoder decoder = AudioDecoder.getInstance();
        decoder.startDecoding();
        //发送数据给服务器 让其知道客户端的地址和端口号（打洞）  服务器得到的端口号再外网和本地的不一样
        socket = MyApplication.getInstance().getSocket();
        try {
            String send = MyApplication.getInstance().getSpUtil().getUser().id+"";
            DatagramPacket dataPacket = new DatagramPacket(send.getBytes(), send.getBytes().length, InetAddress.getByName(Config.serverIP), serverPort);
            Log.d(Config.TAG, "发送 打洞 " + dataPacket.getAddress().getHostAddress() + " " + dataPacket.getPort());
            socket.send(dataPacket);
            socket.send(dataPacket);
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = true;
        try {
            while (isRunning) {
                socket.receive(packet);
                // Log.i(LOG, "收到一个包..." + packet.getLength());
                Log.d(LOG, "收到一段数据 " + packet.getLength());
                // 每接收一个UDP包，就交给解码器，等待解码
                decoder.addData(packet.getData(), packet.getLength());
            }

        } catch (IOException e) {
            Log.e(LOG, "RECIEVE ERROR!");
        }
        // 接收完成，停止解码器，释放资源
        decoder.stopDecoding();
        //release();
        Log.e(LOG, "stop recieving");
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
