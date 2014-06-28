package com.voice.communicate;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.R.integer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VoiceReceiver extends android.os.Handler
{
	public enum State
	{
		IDLE, RECV
	}

	private static final int SAMPLE_RATE_IN_HZ = Common.SAMPLE_RATE;
	private int peakThreshold = -65;
	private int minRunLength = 2;
	private VoiceEncoder coder = new VoiceEncoder();
	// private RingBuffer<Character> peakHistory = new RingBuffer<Character>(16);
	// private RingBuffer<Date> peakTimes = new RingBuffer<Date>(16);
	private IOnReceiveMessage callBack;
	private StringBuffer buffer = new StringBuffer();
	private State state = State.IDLE;
	private boolean isRunning = false;
	private VoiceReceiveThread recordThread = null;
	private int nyquist = SAMPLE_RATE_IN_HZ;
	private Handler messageHandler = new Handler(new Callback()
	{

		@Override
		public boolean handleMessage(Message msg)
		{
			if (null != callBack)
			{
				if (null != msg)
				{
					byte[] bytes = (byte[]) msg.obj;
					callBack.onReceive(bytes);
				}
			}
			return false;
		}
	});

	private void fireOnReceiveMessage(byte[] message)
	{
		if (null != message)
		{
			Message msg = new Message();
			msg.obj = message;
			messageHandler.sendMessage(msg);
		}
	}

	public void setOnReceiveMessage(IOnReceiveMessage callback)
	{
		this.callBack = callback;
	}

	public void start()
	{
		if (recordThread == null)
		{
			recordThread = new VoiceReceiveThread(VoiceReceiver.this, SAMPLE_RATE_IN_HZ);
			recordThread.start();
		}
		if (regThread == null)
		{
			regThread = new Thread(new Runnable()
			{
				private short[] buffer = new short[Common.MIN_BUFFER_SIZE];
				private short[] tmpQueueItem = null;
				private int bufferIndex = 0;
				private int queueItemIndex = 0;

				@Override
				public void run()
				{
					while (true)
					{
						if (mProducerQueue.size() > 0 || tmpQueueItem != null)
						{
							try
							{
								if (tmpQueueItem == null)
								{
									tmpQueueItem = mProducerQueue.take();
								}

								for (; bufferIndex < buffer.length && queueItemIndex < tmpQueueItem.length; bufferIndex++, queueItemIndex++)
								{
									buffer[bufferIndex] = tmpQueueItem[queueItemIndex];
								}

								if (queueItemIndex == tmpQueueItem.length)
								{
									tmpQueueItem = null;
									queueItemIndex = 0;
								}

								if (bufferIndex == buffer.length)
								{
									bufferIndex = 0;
									loop(buffer);
								}
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							try
							{
								Thread.sleep(10);
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
			regThread.start();
		}
	}

	public void stop()
	{
		if (recordThread != null)
		{
			recordThread.stopMe();
			recordThread = null;
		}
		if (regThread != null)
		{
			regThread.stop();
			regThread = null;
		}
	}

	// private void loop(byte[] bytes) {
	// float[] freqs = calcFFT(bytesToFloats(bytes));
	// int freq = getPeakFrequency(freqs);
	//
	// if (freq > -1) {
	// char c = this.coder.frequencyToChar(freq);
	// if (c != '0') {
	// Log.i("voice", freq + " " + c);
	// this.peakHistory.add(c);
	// this.peakTimes.add(new Date());
	// }
	// }
	// analysePeaks();
	// }

	private void loop(short[] bytes)
	{
		int tfreq = getPeakFrequency(calcFFT(bytes));
		// Log.i("^^^^", tfreq + "");
		// int framesize = bytes.length / 4;
		// int min = Integer.MAX_VALUE;
		// int max = -1;
		// int[] freqArray = new int[4];
		// short[] frame = new short[framesize];
		// for (int i = 0; i < bytes.length; i++)
		// {
		// frame[i % framesize] = bytes[i];
		// if ((i + 1) % framesize == 0)
		// {
		// float[] freqs = calcFFT(frame);
		// int tmp = getPeakFrequency(freqs);
		// freqArray[i / framesize] = tmp;
		// min = min > tmp ? tmp : min;
		// max = max > tmp ? max : tmp;
		// }
		// }
		//
		// boolean hasDelMin = false;
		// boolean hasDelMax = false;
		// int sumFreq = 0;
		// for (int i = 0; i < 4; i++)
		// {
		// int tmp = freqArray[i];
		// if (tmp == min && !hasDelMin)
		// {
		// hasDelMin = true;
		// continue;
		// }
		// if (tmp == max && !hasDelMax)
		// {
		// hasDelMax = true;
		// continue;
		// }
		// sumFreq += tmp;
		// }
		//
		// int avgFreq = sumFreq / 2;
		// int freq = Math.abs(avgFreq - tfreq) < 500 ? tfreq : avgFreq;
		int freq = tfreq;
		if (freq > -1)
		{
			char c = this.coder.frequencyToChar(freq);
			if (c != '.')
			{
				// Log.i("voice", tfreq + " " + freq + " " + c);
				analysePeaks(c);
				// this.peakHistory.add(c);
				// this.peakTimes.add(new Date());
			}
		}

	}

	private char lastchar;
	private int startcount = 0;
	private int endcount = 0;

	private void analysePeaks(char c)
	{
		// char c = getLastRun();
		if (c == -1)
			return;

		if (this.state == State.IDLE)
		{
			if (c == this.coder.getStartChar())
			{
				this.buffer.setLength(0);
				startcount++;
				if (startcount == 2)
				{
					startcount = 0;
					this.state = State.RECV;
				}
			}
		}
		else if (this.state == State.RECV)
		{
			if (c != this.coder.getStartChar() && c != this.coder.getEndChar() && c != lastchar)
			{
				lastchar = c;
				Log.i("voice", c + "");
				this.buffer.append(c);
			}

			if (c == this.coder.getEndChar())
			{
				endcount++;
				if (endcount == 2)
				{
					startcount = 0;
					endcount = 0;
					this.state = State.IDLE;
					Log.i("result", buffer.toString());
					String content = coder.decode(buffer.toString());
					if (content != null && content.length() != 0)
						this.fireOnReceiveMessage(content.getBytes());
					this.buffer.setLength(0);
				}
			}
		}

		// if (this.state == State.IDLE) {
		// if (c == this.coder.getStartChar()) {
		// this.buffer.setLength(0);
		// this.state = State.RECV;
		// }
		// } else if (this.state == State.RECV) {
		// if (c != lastchar && c != this.coder.getStartChar()
		// && c != this.coder.getEndChar()) {
		// this.buffer.append(c);
		// this.lastchar = c;
		// }
		//
		// if (c == this.coder.getEndChar()) {
		// this.lastchar = Character.MIN_VALUE;
		// this.state = State.IDLE;
		// Log.i("result", buffer.toString());
		// this.fireOnReceiveMessage(Base32.decode(buffer.toString()));
		// this.buffer.setLength(0);
		// }
		// }
	}

	// private char getLastRun()
	// {
	// Character obj = this.peakHistory.last();
	// if (null != obj)
	// {
	// char lastChar = obj;
	// int runLength = 0;
	// int i = this.peakHistory.length() - 2;
	// for (; i >= 0; i--)
	// {
	// char c = (char) this.peakHistory.get(i);
	// if (c == lastChar)
	// {
	// runLength += 1;
	// }
	// else
	// {
	// break;
	// }
	// }
	// if (runLength > this.minRunLength)
	// {
	// this.peakHistory.remove(i + 1, runLength + 1);
	// return lastChar;
	// }
	// }
	// return (char) -1;
	// }

	private int getPeakFrequency(float[] freqs)
	{
		int start = freqToIndex(this.coder.getFreqMin(), freqs.length);
		float max = -Float.MAX_VALUE;
		int index = -1;
		for (int i = start; i < freqs.length; i++)
		{
			if (freqs[i] > max)
			{
				max = freqs[i];
				index = i;
			}
		}

		// Only care about sufficiently tall peaks.
		if (max > this.peakThreshold)
		{
			return this.indexToFreq(index, freqs.length);
		}
		return -1;
	}

	private int indexToFreq(int index, int freqLength)
	{

		return (int) (nyquist * 1.0 / freqLength * index);
	}

	private int freqToIndex(int frequency, int freqLength)
	{
		return (int) Math.round(frequency * 1.0 / nyquist * freqLength);
	}

	private float[] bytesToFloats(byte[] bytes)
	{
		float[] micBufferData = new float[bytes.length / 2];
		final int bytesPerSample = 2; // As it is 16bit PCM
		// final double amplification = 100.0; // choose a number as you like
		for (int index = 0, floatIndex = 0; index < bytes.length - bytesPerSample + 1; index += bytesPerSample, floatIndex++)
		{
			float sample = 0;
			for (int b = 0; b < bytesPerSample; b++)
			{
				int v = bytes[index + b];
				if (b < bytesPerSample - 1 || bytesPerSample == 1)
				{
					v &= 0xFF;
				}
				sample += v << (b * 8);
			}
			float sample32 = (sample / 32768.0f);
			micBufferData[floatIndex] = sample32;
		}
		return micBufferData;
	}

	private float[] calcFFT(short[] buffers)
	{
		// Complex[] fftTempArray = new Complex[buffers.length];
		// for (int i = 0; i < buffers.length; i++) {
		// fftTempArray[i] = new Complex(buffers[i], 0);
		// }
		// fftTempArray = FFT.fft(fftTempArray);
		//
		// for (int i = 0; i < fftTempArray.length; i++) {
		// Complex item = fftTempArray[i];
		// buffers[i] = (float) Math.sqrt(item.re() * item.re() + item.im()
		// * item.im());
		// }

		float[] result = new float[buffers.length];
		double[] doubleBuffers = new double[buffers.length * 2];
		for (int i = 0; i < buffers.length; i++)
		{
			doubleBuffers[i] = buffers[i] / 32768.0f;
		}
		DoubleFFT_1D fft = new DoubleFFT_1D(buffers.length);
		fft.realForwardFull(doubleBuffers);

		for (int i = 0; i < buffers.length; i++)
		{
			result[i] = (float) Math.sqrt(doubleBuffers[2 * i] * doubleBuffers[2 * i] + doubleBuffers[2 * i + 1] * doubleBuffers[2 * i + 1]);
		}

		// float[] ia = new float[buffers.length];
		// FastFT.FFT(true, buffers, ia);
		//
		// for (int i = 0; i < buffers.length; i++) {
		// float re = buffers[i];
		// float im = ia[i];
		// buffers[i] = (float) Math.sqrt(re * re + im * im);
		// }

		// fft.calculate(buffers);

		return result;
	}

	@Override
	public void handleMessage(Message msg)
	{
		if (null != msg.obj)
		{
			short[] bytes = (short[]) msg.obj;
			// loop(bytes);
			try
			{
				mProducerQueue.put(bytes);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private BlockingQueue<short[]> mProducerQueue = new LinkedBlockingQueue<short[]>();

	private Thread regThread = null;
}
