package com.voice.communicate;

import java.util.ArrayList;

import android.R.integer;
import android.os.Message;
import android.util.Log;

public class VoiceSender
{
	private static final int SAMPLE_RATE_IN_HZ = Common.SAMPLE_RATE;
	private VoiceEncoder coder;
	private float charDuration = 0.2f;
	private VoicePlayThread playThread = null;
	private int nyquist = SAMPLE_RATE_IN_HZ / 2;

	private VoiceReceiver receiver;

	public VoiceSender()
	{
		coder = new VoiceEncoder();
	}

	public VoiceSender(VoiceReceiver receiver)
	{
		this();
		this.receiver = receiver;
	}

	public void start()
	{
		if (playThread == null)
		{
			playThread = new VoicePlayThread(SAMPLE_RATE_IN_HZ);
			playThread.start();
		}
	}

	public void stop()
	{
		if (playThread != null)
		{
			playThread.stopMe();
			playThread = null;
		}
	}

	public String send(String code)
	{
		String content = coder.getStartChar() + coder.encode(code) + coder.getEndChar();
		Log.i("voice", content);
		// ArrayList<short[]> wavesArrayList = new ArrayList<short[]>();
		ArrayList<Short> waveCollection = new ArrayList<Short>();
		// int[] freqs = new int[content.length() * 2];
		for (int i = 0; i < content.length(); i += 1)
		{
			char c1 = content.charAt(i);
			int freq = this.coder.charToFrequency(c1);
			char c = coder.frequencyToChar(freq);
			Log.i("voice", c1 + "," + freq + "," + c);
			// float waveLength = SAMPLE_RATE_IN_HZ * 1.0f / freq;
			// // int length = 2048;
			// int length = 100 * SAMPLE_RATE_IN_HZ / 1000;
			// int waveLen = SAMPLE_RATE_IN_HZ * 1.0 / freq;
			// byte[] wave = SinWave.sin2(25000, SAMPLE_RATE_IN_HZ, 4096);
			int packLength = (i == 0 || i == content.length() - 1) ? 8192 : 1024;
			short[] wave = SinWave.sin2(freq, SAMPLE_RATE_IN_HZ, packLength);
			// byte[] wave = SinWave.sin(waveLen, 4096);
			// wavesArrayList.add(wave);
			for (int j = 0; j < wave.length; j++)
			{
				waveCollection.add(wave[j]);
			}
		}

		if (waveCollection.size() > 0)
		{
			if (receiver != null)
			{
				Message message = new Message();
				message.obj = waveCollection.toArray();
				receiver.handleMessage(message);
			}
			else
			{
				short[] wave = new short[waveCollection.size()];
				for (int i = 0; i < wave.length; i++)
				{
					wave[i] = waveCollection.get(i);
				}
				playThread.play(wave);
			}
		}
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// }
		return content;
	}

	private int indexToFreq(int index)
	{

		return nyquist / playThread.getMinBufferSize() * index;
	}

	private int freqToIndex(int frequency)
	{
		return Math.round(frequency / nyquist * playThread.getMinBufferSize());
	}
}
