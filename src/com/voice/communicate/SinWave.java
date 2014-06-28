package com.voice.communicate;

public class SinWave {
	/** ���Ҳ��ĸ߶� **/
	public static final int HEIGHT = 127;
	/** 2PI **/
	public static final double TWOPI = 2 * Math.PI;

	/**
	 * �������Ҳ�
	 * 
	 * @param wave
	 * @param waveLen
	 *            ÿ�����Ҳ��ĳ���
	 * @param length
	 *            �ܳ���
	 * @return
	 */
	public static byte[] sin(int mRate, int sampleRate, int duration) {
		// byte[] wave = new byte[length];
		// for (int i = 0; i < length; i++) {
		// wave[i] = (byte) (HEIGHT * (1 - Math.sin(TWOPI
		// * ((i / waveLen - (int) (i / waveLen))))));
		// }

		int totalCount = (duration * sampleRate) / 1000;
		double per = (mRate / (double) sampleRate) * 2 * Math.PI;
		double d = 0;
		byte[] wave = new byte[totalCount];
		for (int i = 0; i < wave.length; i++) {
			wave[i] = (byte) ((int)(Math.sin(d) * 128) & 0xff);
			d += per;
		}

		return wave;
	}
	
	public static short[] sin2(int mRate, int sampleRate, int count) {
		// byte[] wave = new byte[length];
		// for (int i = 0; i < length; i++) {
		// wave[i] = (byte) (HEIGHT * (1 - Math.sin(TWOPI
		// * ((i / waveLen - (int) (i / waveLen))))));
		// }

		//int totalCount = (duration * sampleRate) / 1000;
		double per = (mRate / (double) sampleRate) * 2 * Math.PI;
		double d = 0;
		short[] wave = new short[count];
		for (int i = 0; i < wave.length; i++) {
			wave[i] = (short) ((int)((Math.sin(d)) * 32700) );
			d += per;
		}

		return wave;
	}
}
