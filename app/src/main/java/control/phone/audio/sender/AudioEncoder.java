package control.phone.audio.sender;

import android.util.Log;

import com.googlecode.androidilbc.Codec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import control.phone.audio.AudioData;


public class AudioEncoder implements Runnable {
	String LOG = "AudioEncoder";
	private int server_port;
	private static AudioEncoder encoder;
	private boolean isEncoding = false;

	private List<AudioData> dataList = null;

	public static AudioEncoder getInstance() {
		if (encoder == null) {
			encoder = new AudioEncoder();
		}
		return encoder;
	}

	private AudioEncoder() {
		dataList = Collections.synchronizedList(new LinkedList<AudioData>());
	}

	public void addData(byte[] data, int size) {
		AudioData rawData = new AudioData();
		rawData.setSize(size);
		byte[] tempData = new byte[size];
		System.arraycopy(data, 0, tempData, 0, size);
		rawData.setRealData(tempData);
		dataList.add(rawData);


	}

	/*
	 * start encoding
	 */
	public void startEncoding() {
		System.out.println(LOG + "start encode thread");
		if (isEncoding) {
			Log.e(LOG, "encoder has been started  !!!");
			return;
		}
		new Thread(this).start();
	}

	/*
	 * end encoding
	 */
	public void stopEncoding() {
		this.isEncoding = false;
	}

	public void run() {
		// start sender before encoder
		AudioSender sender = new AudioSender(server_port);
		sender.startSending();

		int encodeSize = 0;
		byte[] encodedData = new byte[256];


		// initialize audio encoder:mode is 30


		isEncoding = true;
		while (isEncoding) {
			//Log.e(LOG, "dataList22.add:"+dataList.size());
			if (dataList.size() == 0) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (isEncoding) {
				AudioData rawData = dataList.remove(0);
				//Log.e(LOG, rawData.toString());
				encodedData = new byte[rawData.getSize()];

				//
				encodeSize = Codec.instance().encode(rawData.getRealData(), 0,
						rawData.getSize(), encodedData, 0);
				//Log.e(LOG, "rawData:"+rawData.getSize()+"   encodeSize:"+encodeSize);
				if (encodeSize > 0) {
					sender.addData(encodedData, encodeSize);
					// clear data
					encodedData = new byte[encodedData.length];
				}
			}
		}
		System.out.println(LOG + "end encoding");
		sender.stopSending();
	}

	public void setPort(int port) {
		this.server_port = port;
	}
}