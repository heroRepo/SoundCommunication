package com.voice.communicate;

import android.R.integer;

public class RSCode
{
	private static final int MM = 5;
	private static final int NN = 31;
	private static final int TT = 5;
	private static final int KK = 21;

	private static int[] pp = { 1, 0, 1, 1, 1, 0, 0, 0, 1 };
	private static int[] alphaTo = new int[NN + 1];
	private static int[] indexOf = new int[NN + 1];
	private static int[] gg = new int[NN - KK + 1];
	public int[] recd = new int[NN];
	public int[] data = new int[KK];
	public int[] bb = new int[NN - KK];
	private int errorCount = 0;

	/**
	 * ���캯��RSCode() ��ʼ������������GF�ռ�Ͷ�Ӧ�����ɶ���ʽ
	 */
	static
	{
		generateGF();
		generatePolynomial();
	}

	/**
	 * ����generateGF() ����GF(2^MM)�ռ�
	 */
	public static void generateGF()
	{
		int i, mask;
		mask = 1;
		alphaTo[MM] = 0;
		for (i = 0; i < MM; i++)
		{
			alphaTo[i] = mask;
			indexOf[alphaTo[i]] = i;
			if (pp[i] != 0)
			{
				alphaTo[MM] ^= mask;
			}
			mask <<= 1;
		}

		indexOf[alphaTo[MM]] = MM;
		mask >>= 1;

		for (i = MM + 1; i < NN; i++)
		{
			if (alphaTo[i - 1] >= mask)
			{
				alphaTo[i] = alphaTo[MM] ^ ((alphaTo[i - 1] ^ mask) << 1);
			}
			else
			{
				alphaTo[i] = alphaTo[i - 1] << 1;
			}
			indexOf[alphaTo[i]] = i;
		}

		indexOf[0] = -1;
		// ���GF�ռ�
		/*
		 * System.out.println("GF�ռ�:"); for(i=0; i<=NN; i++){ System.out.println(i + "   " + alphaTo[i] + "   " +
		 * indexOf[i]); }
		 */
	}// GenerateGF

	/**
	 * ����generatePolynomial() ������Ӧ�����ɶ���ʽ�ĸ���ϵ��
	 */
	public static void generatePolynomial()
	{
		int i, j;
		gg[0] = 2;
		gg[1] = 1;
		for (i = 2; i <= NN - KK; i++)
		{
			gg[i] = 1;
			for (j = i - 1; j > 0; j--)
			{
				if (gg[j] != 0)
					gg[j] = gg[j - 1] ^ alphaTo[(indexOf[gg[j]] + i) % NN];
				else
					gg[j] = gg[j - 1];
			}
			gg[0] = alphaTo[(indexOf[gg[0]] + i) % NN];
		}

		// ת���䵽
		for (i = 0; i <= NN - KK; i++)
		{
			gg[i] = indexOf[gg[i]];
		}

		// ������ɶ���ʽ�ĸ���ϵ��
		// System.out.println("���ɶ���ʽϵ��:");
		// for (i = 0; i <= NN - KK; i++)
		// {
		// System.out.println(gg[i]);
		// }
	}

	/**
	 * ����������ݽ�����Ӿ����룬����ĳ���Ϊ10
	 * 
	 * @param array
	 * @return
	 */
	public int[] encode(int[] array)
	{
		RSCode rs = new RSCode();

		// ����Ҫ���������
		int i;
		for (i = 0; i < KK; i++)
		{
			rs.data[i] = 0;
		}
		for (i = 0; i < array.length; i++)
		{
			rs.data[i] = array[i];
		}
		rs.rsEncode();

		int[] ret = new int[array.length + 10];

		for (i = 0; i < NN - KK; i++)
			rs.recd[i] = rs.bb[i];
		for (i = 0; i < KK; i++)
			rs.recd[i + NN - KK] = rs.data[i];

		for (int j = 0; j < ret.length; j++)
		{
			ret[j] = rs.recd[j];
		}
		return ret;
	}

	/**
	 * ������ĺ�����������ݽ��о���ԭ������ĳ���Ϊ11~20
	 * 
	 * @param array
	 * @return
	 */
	public int[] decode(int[] array)
	{
		if (array == null || array.length < 11 || array.length > 20)
			return null;
		RSCode rs = new RSCode();
		for (int i = 0; i < array.length; i++)
		{
			rs.recd[i] = array[i];
		}

		// ���룬����
		rs.rsDecode();

		int[] ret = new int[array.length - 10];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = rs.recd[i + 10];
		}
		return ret;
	}

	/**
	 * ����rsEncode() RS����
	 */
	public void rsEncode()
	{
		int i, j;
		int feedback;
		for (i = 0; i < NN - KK; i++)
		{
			bb[i] = 0;
		}
		for (i = KK - 1; i >= 0; i--)
		{
			// �𲽵Ľ���һ��Ҫ���ģ�����bb(i)
			// System.out.println("i:" + i + " data.length:" + data.length);
			// System.out.println("NN - KK - 1:" + (NN - KK - 1) + " bb.length:" + bb.length);
			// System.out.println("test:" + (data[i] ^ bb[NN - KK - 1]) + " data[i]:" + data[i] + " bb[]" + bb[NN - KK -
			// 1]);
			feedback = indexOf[data[i] ^ bb[NN - KK - 1]];
			if (feedback != -1)
			{
				for (j = NN - KK - 1; j > 0; j--)
				{
					if (gg[j] != -1)
						bb[j] = bb[j - 1] ^ alphaTo[(gg[j] + feedback) % NN];
					else
						bb[j] = bb[j - 1];
				}
				bb[0] = alphaTo[(gg[0] + feedback) % NN];
			}
			else
			{
				for (j = NN - KK - 1; j > 0; j--)
				{
					bb[j] = bb[j - 1];
				}
				bb[0] = 0;
			}
		}
		// ���������
		// System.out.println("������:");
		// for (i = 0; i < NN - KK; i++)
		// {
		// System.out.println(bb[i]);
		// }
	}

	/**
	 * ����rsDecode() RS����
	 */
	public void rsDecode()
	{
		errorCount = 0;
		int i, j, u, q;
		int[][] elp = new int[NN - KK + 2][NN - KK];
		int[] d = new int[NN - KK + 2];
		int[] l = new int[NN - KK + 2];
		int[] u_lu = new int[NN - KK + 2];
		int[] s = new int[NN - KK + 1];

		int count = 0;
		int syn_error = 0;
		int[] root = new int[TT];
		int[] loc = new int[TT];
		int[] z = new int[TT + 1];
		int[] err = new int[NN];
		int[] reg = new int[TT + 1];

		// ת����GF�ռ�
		for (i = 0; i < NN; i++)
		{
			if (recd[i] == -1)
				recd[i] = 0;
			else
				recd[i] = indexOf[recd[i]];
		}

		// ��������ʽ
		for (i = 1; i <= NN - KK; i++)
		{
			s[i] = 0;
			for (j = 0; j < NN; j++)
			{
				if (recd[j] != -1)
					s[i] ^= alphaTo[(recd[j] + i * j) % NN];
			}
			if (s[i] != 0)
				syn_error = 1;
			s[i] = indexOf[s[i]];
		}
		// System.out.println("syn_error=" + syn_error);

		// ����д�����о���
		if (syn_error == 1)
		{
			// BM������������ʽ��ϵ��
			d[0] = 0;
			d[1] = s[1];
			elp[0][0] = 0;
			elp[1][0] = 1;
			for (i = 1; i < NN - KK; i++)
			{
				elp[0][i] = -1;
				elp[1][i] = 0;
			}
			l[0] = 0;
			l[1] = 0;
			u_lu[0] = -1;
			u_lu[1] = 0;
			u = 0;
			do
			{
				u++;
				if (d[u] == -1)
				{
					l[u + 1] = l[u];
					for (i = 0; i <= l[u]; i++)
					{
						elp[u + 1][i] = elp[u][i];
						elp[u][i] = indexOf[elp[u][i]];
					}
				}
				else
				{
					q = u - 1;
					while ((d[q] == -1) && (q > 0))
					{
						q--;
					}
					if (q > 0)
					{
						j = q;
						do
						{
							j--;
							if ((d[j] != -1) && (u_lu[q] < u_lu[j]))
							{
								q = j;
							}
						}
						while (j > 0);
					}

					if (l[u] > l[q] + u - q)
					{
						l[u + 1] = l[u];
					}
					else
					{
						l[u + 1] = l[q] + u - q;
					}

					for (i = 0; i < NN - KK; i++)
					{
						elp[u + 1][i] = 0;
					}
					for (i = 0; i <= l[q]; i++)
					{
						if (elp[q][i] != -1)
							elp[u + 1][i + u - q] = alphaTo[(d[u] + NN - d[q] + elp[q][i]) % NN];
					}

					for (i = 0; i <= l[u]; i++)
					{
						elp[u + 1][i] ^= elp[u][i];
						elp[u][i] = indexOf[elp[u][i]];
					}
				}
				u_lu[u + 1] = u - l[u + 1];

				if (u < NN - KK)
				{
					if (s[u + 1] != -1)
					{
						d[u + 1] = alphaTo[s[u + 1]];
					}
					else
					{
						d[u + 1] = 0;
					}

					for (i = 1; i <= l[u + 1]; i++)
					{
						if ((s[u + 1 - i] != -1) && (elp[u + 1][i] != 0))
						{
							d[u + 1] ^= alphaTo[(s[u + 1 - i] + indexOf[elp[u + 1][i]]) % NN];
						}
					}
					d[u + 1] = indexOf[d[u + 1]];
				}
			}
			while ((u < NN - KK) && (l[u + 1] <= TT));
			u++;
			// System.out.println("������Ŀ:" + l[u]);
			errorCount = u;
			// �����λ�ã��Լ���������
			if (l[u] <= TT)
			{
				for (i = 0; i <= l[u]; i++)
				{
					elp[u][i] = indexOf[elp[u][i]];
				}
				// �����λ�ö���ʽ�ĸ�
				for (i = 1; i <= l[u]; i++)
				{
					reg[i] = elp[u][i];
				}
				count = 0;
				for (i = 1; i <= NN; i++)
				{
					q = 1;
					for (j = 1; j <= l[u]; j++)
					{
						if (reg[j] != -1)
						{
							reg[j] = (reg[j] + j) % NN;
							q ^= alphaTo[reg[j]];
						}
					}

					if (q == 0)
					{
						root[count] = i;
						loc[count] = NN - i;
						// System.out.println("����λ��:" + loc[count]);
						count++;
					}
				}

				//
				if (count == l[u])
				{
					for (i = 1; i <= l[u]; i++)
					{
						if ((s[i] != -1) && elp[u][i] != -1)
						{
							z[i] = alphaTo[s[i]] ^ alphaTo[elp[u][i]];
						}
						else if ((s[i] != -1) && (elp[u][i] == -1))
						{
							z[i] = alphaTo[s[i]];
						}
						else if ((s[i] == -1) && (elp[u][i] != -1))
						{
							z[i] = alphaTo[elp[u][i]];
						}
						else
						{
							z[i] = 0;
						}

						for (j = 1; j < i; j++)
						{
							if ((s[j] != -1) && (elp[u][i - j] != -1))
							{
								z[i] ^= alphaTo[(elp[u][i - j] + s[j]) % NN];
							}
						}

						z[i] = indexOf[z[i]];
					}

					// �������ͼ��
					for (i = 0; i < NN; i++)
					{
						err[i] = 0;
						if (recd[i] != -1)
							recd[i] = alphaTo[recd[i]];
						else
							recd[i] = 0;
					}
					for (i = 0; i < l[u]; i++)
					{
						err[loc[i]] = 1;
						for (j = 1; j <= l[u]; j++)
						{
							if (z[j] != -1)
								err[loc[i]] ^= alphaTo[(z[j] + j * root[i]) % NN];
						}

						if (err[loc[i]] != 0)
						{
							err[loc[i]] = indexOf[err[loc[i]]];
							q = 0;
							for (j = 0; j < l[u]; j++)
							{
								if (j != i)
									q += indexOf[1 ^ alphaTo[(loc[j] + root[i]) % NN]];
							}
							q = q % NN;
							err[loc[i]] = alphaTo[(err[loc[i]] - q + NN) % NN];
							recd[loc[i]] ^= err[loc[i]];
						}
					}
				}
				else
				{
					// ����̫�࣬�޷�����
					for (i = 0; i < NN; i++)
					{
						if (recd[i] != -1)
							recd[i] = alphaTo[recd[i]];
						else
							recd[i] = 0;
					}
				}
			}
			else
			{
				// ����̫�࣬�޷�����
				for (i = 0; i < NN; i++)
				{
					if (recd[i] != -1)
						recd[i] = alphaTo[recd[i]];
					else
						recd[i] = 0;
				}
			}
		}
		else
		{
			for (i = 0; i < NN; i++)
			{
				if (recd[i] != -1)
					recd[i] = alphaTo[recd[i]];
				else
					recd[i] = 0;
			}
		}
	}

	public int getErrorCount()
	{
		return errorCount;
	}
}
