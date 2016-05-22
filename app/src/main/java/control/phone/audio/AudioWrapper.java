package control.phone.audio;


import android.util.Log;

import control.phone.audio.receiver.AudioReceiver;
import control.phone.audio.sender.AudioRecorder;

public class AudioWrapper {

	private AudioRecorder audioRecorder;
	private AudioReceiver audioReceiver;
	private int server_port;

	private static AudioWrapper instanceAudioWrapper;

	public AudioWrapper() {
	}

	public void setServerPort(int server_port){
		this.server_port=server_port;
	}

	public void startRecord() {
		if (null == audioRecorder) {
			audioRecorder = new AudioRecorder(server_port);
		}
		audioRecorder.setPost(server_port);
		audioRecorder.startRecording();
	}

	public void stopRecord() {
		if (audioRecorder != null)
			audioRecorder.stopRecording();
	}

	public void startListen() {
		if (null == audioReceiver) {
			audioReceiver = new AudioReceiver();
		}
		audioReceiver.setServerPort(server_port);
		audioReceiver.startRecieving();
	}

	public void stopListen() {
		if (audioReceiver != null)
			audioReceiver.stopRecieving();
	}
}
