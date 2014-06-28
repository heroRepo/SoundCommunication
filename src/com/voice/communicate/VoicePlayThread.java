package com.voice.communicate;

import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class VoicePlayThread extends Thread {
	private AudioTrack audioTrack;
	protected byte[] m_out_bytes;
	protected boolean m_keep_running;
	private int minBufferSize;
	private ArrayList<short[]> playBuffers = new ArrayList<short[]>();
	private boolean isStarted = false;

	public VoicePlayThread(int sampleRate) {
		minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		//minBufferSize = 2048;
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, minBufferSize,
				AudioTrack.MODE_STREAM);
		audioTrack.setAuxEffectSendLevel(1);
	}

	public void play(short[] bytes) {
		if (!isStarted)
			start();
		synchronized (playBuffers) {
			playBuffers.add(bytes);
		}
	}

	private void play() {
		short[] bytes = null;
		if (playBuffers.size() > 0) {
			synchronized (playBuffers) {
				bytes = playBuffers.remove(0);
			}
		}
		if (null != bytes) {
			audioTrack.write(bytes, 0, bytes.length);
		}
	}

	@Override
	public void run() {
		audioTrack.play();
		while (isStarted) {
			play();
		}
	}

	@Override
	public synchronized void start() {
		if (isStarted)
			return;
		isStarted = true;
		super.start();
	}

	public int getMinBufferSize() {
		return minBufferSize;
	}

	public void stopMe() {
		if (isStarted) {
			isStarted = false;
		}
	}
}
