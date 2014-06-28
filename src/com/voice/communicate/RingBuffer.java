package com.voice.communicate;

import java.util.ArrayList;

public class RingBuffer<E> {
	private ArrayList<E> array;
	private int maxLength;

	public RingBuffer(int maxLength) {
		this.maxLength = maxLength;
		array = new ArrayList<E>();
	}

	public synchronized E get(int index) {
		if (index > array.size())
			return null;
		return this.array.get(index);
	}

	public synchronized void add(E e) {
		this.array.add(e);
		if (array.size() > this.maxLength)
			this.array.remove(this.maxLength);
	}

	public synchronized E last() {
		if (this.array.size() == 0)
			return null;
		return this.array.get(this.array.size() - 1);
	}

	public synchronized int length() {
		return this.array.size();
	}

	public synchronized void clear() {
		this.array = new ArrayList<E>();
	}

	public synchronized RingBuffer<E> copy() {
		RingBuffer<E> out = new RingBuffer<E>(this.maxLength);
		out.array = new ArrayList<E>(array);
		return out;
	}

	public synchronized void remove(int index, int length) {
		if (this.array.size() >= index + length) {
			for (int i = 0; i < length; i++) {
				this.array.remove(index);
			}
		}
	}
}
