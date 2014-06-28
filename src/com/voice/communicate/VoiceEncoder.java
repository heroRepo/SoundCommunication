package com.voice.communicate;

import android.util.Log;

public class VoiceEncoder
{
	 private final static String BASECODER = "^ABCDEFGHIJKLMNOPQRSTUVWXYZ234567$";
	 private final static String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

//	private final static String BASECODER = "^|ABCDEFGHIJKLMNOPQRSTUV0123456789$";
//	private final static String base32Chars = "ABCDEFGHIJKLMNOPQRSTUV0123456789";

	public static String getBasecoder()
	{
		return BASECODER;
	}

	private int freqMin = 1500;

	private int freqMax = 5500;

	private int freqError = 50;

	private int freqRange = freqMax - freqMin;

	private char startChar = '^';

	private char endChar = '$';

	private char splitChar = '|';

	public VoiceEncoder()
	{
	}

	public VoiceEncoder(int freqMin, int freqMax, int freqError)
	{
		this.freqMin = freqMin;
		this.freqMax = freqMax;
		this.freqError = freqError;
	}

	/**
	 * 将字符转变成声音传输的频率
	 * 
	 * @param c
	 * @return
	 */
	public int charToFrequency(char c)
	{
		float index = BASECODER.indexOf(c);
		if (-1 == index)
		{
			Log.w("voice", "found invalid character");
			index = BASECODER.length() - 1;
		}

		float percent = index / BASECODER.length();
		int freqOffset = Math.round(freqRange * percent);

		return this.freqMin + freqOffset;
	}

	/**
	 * 将某一频率信息转变为字符
	 * 
	 * @param freq
	 * @return
	 */
	public char frequencyToChar(int freq)
	{
		if (!(this.freqMin <= freq && freq <= this.freqMax))
		{
			if (Math.abs(this.freqMin - freq) < this.freqError)
			{
				freq = this.freqMin;
			}
			else if (Math.abs(freq - this.freqMax) < this.freqError)
			{
				freq = this.freqMax;
			}
			else
			{
				return '.';
			}
		}

		float percent = (freq - this.freqMin) * 1.0f / freqRange;
		int index = Math.round((BASECODER.length()) * percent);
		if (index >= 0 && index < BASECODER.length())
			return BASECODER.charAt(index);
		return '.';
	}

	public int getFreqError()
	{
		return freqError;
	}

	public int getFreqMax()
	{
		return freqMax;
	}

	public int getFreqMin()
	{
		return freqMin;
	}

	public int getFreqRange()
	{
		return freqRange;
	}

	public void setFreqError(int freqError)
	{
		this.freqError = freqError;
	}

	public void setFreqMax(int freqMax)
	{
		this.freqMax = freqMax;
	}

	public void setFreqMin(int freqMin)
	{
		this.freqMin = freqMin;
	}

	public void setFreqRange(int freqRange)
	{
		this.freqRange = freqRange;
	}

	public char getStartChar()
	{
		return startChar;
	}

	public void setStartChar(char startChar)
	{
		this.startChar = startChar;
	}

	public char getEndChar()
	{
		return endChar;
	}

	public void setEndChar(char endChar)
	{
		this.endChar = endChar;
	}

	public String encodeBase32(byte[] bytes)
	{
		String base32 = Base32.encode(bytes);
		StringBuilder sb = new StringBuilder();
		RSCode coder = new RSCode();
		for (int i = 0; i < base32.length(); i += 10)
		{
			int start = i;
			int end = start + (base32.length() - start - 1 > 9 ? 9 : base32.length() - start - 1);
			String fragmentString = base32.substring(start, end + 1);
			int[] charArray = new int[fragmentString.length()];
			for (int j = 0; j < fragmentString.length(); j++)
			{
				charArray[j] = getCharIndex(fragmentString.charAt(j));
			}
			charArray = coder.encode(charArray);
			for (int k = 0; k < charArray.length; k++)
			{
				sb.append(base32Chars.charAt(charArray[k]));
			}
		}

		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < sb.length() - 1; i++)
		{
			ret.append(sb.charAt(i));
			if(sb.charAt(i) == sb.charAt(i+1))
				ret.append(splitChar);
		}
		return ret.toString();
	}

	public byte[] decodeBase32(String content)
	{
		content = content.replace(splitChar+"", "");
		RSCode coder = new RSCode();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < content.length(); i += 20)
		{
			int start = i;
			int end = start + (content.length() - start - 1 > 19 ? 19 : content.length() - start - 1);
			String fragmentString = content.substring(start, end + 1);
			int[] charArray = new int[fragmentString.length()];
			for (int j = 0; j < fragmentString.length(); j++)
			{
				charArray[j] = getCharIndex(fragmentString.charAt(j));
			}
			charArray = coder.decode(charArray);
			for (int k = 0; k < charArray.length; k++)
			{
				sb.append(base32Chars.charAt(charArray[k]));
			}
		}
		// return Base32.decode(sb.toString());
		return Base32.decode(sb.toString());
	}

	public String encode(String content)
	{
		String base32 = Base32.encode(content);
		StringBuilder sb = new StringBuilder();
		try
		{
			RSCode coder = new RSCode();
			for (int i = 0; i < base32.length(); i += 9)
			{
				int start = i;
				int end = start + (base32.length() - start - 1 > 8 ? 8 : base32.length() - start - 1);
				String fragmentString = base32.substring(start, end + 1);
				int[] charArray = new int[fragmentString.length() + 1];
				charArray[0] = charArray.length - 1;
				for (int j = 0; j < fragmentString.length(); j++)
				{
					charArray[j + 1] = getCharIndex(fragmentString.charAt(j));
				}
				charArray = coder.encode(charArray);
				for (int k = 0; k < charArray.length; k++)
				{
					sb.append(base32Chars.charAt(charArray[k]));
				}
			}
		}
		catch (Exception e)
		{
			sb.setLength(0);
		}
		
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < sb.length(); i++)
		{
			ret.append(sb.charAt(i));
			if(i < sb.length()-1 && sb.charAt(i) == sb.charAt(i+1))
				ret.append(splitChar);
		}
		return ret.toString();
//		return sb.toString();
	}

	public String decode(String content)
	{
		content = content.replace(splitChar+"", "");
		StringBuilder sb = new StringBuilder();
		try
		{
			RSCode coder = new RSCode();
			for (int i = 0; i < content.length(); i += 20)
			{
				int start = i;
				int end = start + (content.length() - start - 1 > 19 ? 19 : content.length() - start - 1);
				String fragmentString = content.substring(start, end + 1);
				int[] charArray = new int[fragmentString.length()];
				for (int j = 0; j < fragmentString.length(); j++)
				{
					charArray[j] = getCharIndex(fragmentString.charAt(j));
				}
				charArray = coder.decode(charArray);
				if (coder.getErrorCount() > 5)
				{
					sb.setLength(0);
					return sb.toString();
				}
				int count = charArray[0];
				for (int k = 0; k < count; k++)
				{
					sb.append(base32Chars.charAt(charArray[k + 1]));
				}
			}
		}
		catch(Exception e){
			sb.setLength(0);
		}
		return new String(Base32.decode(sb.toString()));
		//return sb.toString();
	}

	private int getCharIndex(char c)
	{
		for (int i = 0; i < base32Chars.length(); i++)
		{
			if (base32Chars.charAt(i) == c)
			{
				return i;
			}
		}
		return -1;
	}
}
