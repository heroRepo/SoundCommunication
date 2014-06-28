package test;

import junit.framework.Assert;

import org.junit.Test;

import android.util.Log;

import com.voice.communicate.Base32;
import com.voice.communicate.RSCode;
import com.voice.communicate.SinWave;
import com.voice.communicate.VoiceEncoder;

public class app
{
	private static final int MM = 5;
	private static final int NN = 31;
	private static final int TT = 10;
	private static final int KK = 21;

	@Test
	public void testBase32()
	{
		String word1 = "hello HELLO 123 456 !@#$%^&*()_+|\\=-{}][:\"';?><,./ 汉字";
		// String word1E = Base32.encode(word1);
		// System.out.println(word1E);
		//
		// String word1D = new String(Base32.decode(word1E));
		// System.out.println(word1D);
		// Assert.assertEquals(word1, word1D);
		//
		// VoiceEncoder coder = new VoiceEncoder();
		// for (int i = 0; i < VoiceEncoder.getBasecoder().length(); i++) {
		// System.out.println(VoiceEncoder.getBasecoder().charAt(i));
		// int freq = coder.charToFrequency(VoiceEncoder.getBasecoder().charAt(i));
		// char c1 = coder.frequencyToChar(freq);
		// System.out.println(c1);
		// }
		
		VoiceEncoder coder = new VoiceEncoder();
		String encoded = coder.encode("AABCDEFGHIJKLMNOPQRSTUV01234");
		System.out.println("AABCDEFGHIJKLMNOPQRSTUV01234");
		System.out.println(encoded);
		System.out.println(coder.decode(encoded));
		 

//		RSCode rs = new RSCode();
//
//		// 输入要编码的数据
//		int i;
//		for (i = 0; i < KK; i++)
//		{
//			rs.data[i] = 0;
//		}
//		for (i = 0; i < KK-5; i++)
//		{
//			rs.data[i] =  10+i;
//		}
//
//		// 编码
//		rs.rsEncode();
//
//		for (i = 0; i < NN - KK; i++)
//			rs.recd[i] = rs.bb[i];
//		for (i = 0; i < KK; i++)
//			rs.recd[i + NN - KK] = rs.data[i];
//
//		// 主动弄点错误
//		rs.recd[10] = 1;
//		rs.recd[11] = 1;
//		rs.recd[12] = 1;
//		rs.recd[13] = 1;
//		rs.recd[14] = 1;   
//		
//
//		RSCode rs2 = new RSCode();
//		rs2.recd = rs.recd;
//
//		// 解码，纠错
//		rs2.rsDecode();
//
//		// 输出正确编码和纠错后的编码
//		System.out.println("i  data  recd");
//		for (i = 0; i < NN - KK; i++)
//		{
//			System.out.println(i + "   " + rs2.bb[i] + "   " + rs.recd[i]);
//		}
//		for (i = NN - KK; i < NN; i++)
//		{
//			System.out.println(i + "   " + rs2.data[i - NN + KK] + "   " + rs.recd[i]);
//		}
		
//		int samplerate = 44100;
//		int rate = 18000;
//		int waveLen = samplerate / rate;
////		byte[] wave1 = SinWave.sin(waveLen, 4096);
//		byte[] wave2 = SinWave.sin2(rate, samplerate, 4096);
////		for (int i = 0; i < wave1.length; i++)
////		{
////			System.out.print(wave1[i]);
////			System.out.print(",");
////		}
////		System.out.println();
//		for (int i = 0; i < wave2.length; i++)
//		{
//			System.out.print(wave2[i]);
//			System.out.print(",");
//		}
	}
}
