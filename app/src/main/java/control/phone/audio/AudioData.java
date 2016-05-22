package control.phone.audio;

import java.util.Arrays;

public class AudioData {
	int size;
	byte[] realData;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public byte[] getRealData() {
		return realData;
	}

	public void setRealData(byte[] realData) {
		this.realData = realData;
	}

	@Override
	public String toString() {
		return "AudioData{" +
				"size=" + size +
				", realData=" + Arrays.toString(realData) +
				'}';
	}
}
