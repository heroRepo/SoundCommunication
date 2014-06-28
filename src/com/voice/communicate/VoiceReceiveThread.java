package com.voice.communicate;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

public class VoiceReceiveThread extends Thread
{

	private AudioRecord auditRecord;
	private Handler loadBufferLoop;
	private boolean isStarted = false;
	private int minBufferSize;

	public int getMinBufferSize()
	{
		return minBufferSize;
	}

	private short[] buffer;

	public VoiceReceiveThread(Handler handler, int sampleRate)
	{
		super();
		this.loadBufferLoop = handler;

		minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		auditRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
		buffer = new short[minBufferSize];
	}

	@Override
	public void run()
	{
		auditRecord.startRecording();
		while (isStarted)
		{
			int r = auditRecord.read(buffer, 0, minBufferSize);
			if (r > 0)
			{
				short[] bytes = Arrays.copyOf(buffer, r); // buffer.clone();// Arrays.copyOf(buffer, r);
				Message msg = new Message();
				msg.obj = bytes;
				loadBufferLoop.handleMessage(msg);
			}
		}
	}

	@Override
	public synchronized void start()
	{
		if (isStarted)
			return;
		isStarted = true;
		super.start();
	}

	public synchronized void stopMe()
	{
		if (isStarted)
		{
			isStarted = false;
		}
	}
}
