/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.TimedOutputStreamListener;
import com.uwyn.rife.tools.ArrayUtils;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;

public class TimedOutputStream extends FilterOutputStream
{
	private Consumer	mConsumer = null;
	private byte[]		mBuffer = null;
	private HashSet		mListeners = null;
	private Object		mListenersMonitor = new Object();
	
	public TimedOutputStream(OutputStream outputStream, int max, int amount, int interval)
	{
		super(outputStream);
		
		mConsumer = new Consumer(this, max, amount, interval);
		mConsumer.start();
		mBuffer = new byte[0];
		mListeners = new HashSet();
	}

	public void close()
	throws IOException
	{
		mConsumer.terminate();
		out.close();
	}

	public void flush()
	throws IOException
	{
		out.flush();
	}

	public void write(byte[] bytes)
	throws IOException
	{
		addToBuffer(bytes);
	}
	
	public void write(byte[] bytes, int offset, int length)
	throws IOException
	{
		byte[] bytes_part = new byte[length];
		System.arraycopy(bytes, offset, bytes_part, 0, length);
		addToBuffer(bytes_part);
	}

	public void write(int b)
	throws IOException
	{
		addToBuffer(new byte[] {(byte)b});
	}
	
	private void addToBuffer(byte[] bytes)
	throws IOException
	{
		synchronized (this)
		{
			mBuffer = ArrayUtils.join(mBuffer, bytes);
		}
	}
	
	private int writeBuffer(int size)
	throws IOException
	{
		synchronized (this)
		{
			if (size > mBuffer.length)
			{
				size = mBuffer.length;
			}
			out.write(mBuffer, 0, size);
			out.flush();
			
			int		remaining_size = mBuffer.length-size;
			byte[]	new_buffer = new byte[remaining_size];
			if (remaining_size > 0)
			{
				System.arraycopy(mBuffer, size, new_buffer, 0, remaining_size);
			}
			mBuffer = new_buffer;
			
			return size;
		}
	}
	
	private void clearBuffer()
	{
		synchronized (this)
		{
			mBuffer = new byte[0];
		}
	}

	private void fireExceptionThrown(IOException e)
	{
		Iterator	listeners = mListeners.iterator();
		
		while (listeners.hasNext())
		{
			((TimedOutputStreamListener)listeners.next()).exceptionThrow(e);
		}
	}

	public boolean addTimedOutputStreamListener(TimedOutputStreamListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mListenersMonitor)
		{
			if (!mListeners.contains(listener))
			{
				HashSet clone = (HashSet)mListeners.clone();
				result = clone.add(listener);
				mListeners = clone;
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mListeners.contains(listener);
		
		return result;
	}

	public boolean removeTimedOutputStreamListener(TimedOutputStreamListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mListenersMonitor)
		{
			HashSet clone = (HashSet)mListeners.clone();
			result = clone.remove(listener);
			mListeners = clone;
		}
		
		assert false == mListeners.contains(listener);
		
		return result;
	}
	
	private class Consumer extends Thread
	{
		private TimedOutputStream	mConsumedStream = null;
		private int					mMax = 0;
		private int					mAmount = 0;
		private int					mInterval = 0;
		private int					mInServerSpool = 0;
		private int					mWaitPeriod = mInterval / 2;
		private long				mLastConsume = 0;
		private	boolean				mRunning = true;
		
		public Consumer(TimedOutputStream consumedStream, int max, int amount, int interval)
		{
			assert consumedStream != null;
			assert max > 0;
			assert amount > 0;
			assert max > amount;
			assert interval > 0;
			
			mConsumedStream = consumedStream;
			mMax = max;
			mAmount = amount;
			mInterval = interval;
		}
		
		public void terminate()
		{
			synchronized (mConsumedStream)
			{
				mRunning = false;
				interrupt();
			}
		}
		
		public void run()
		{
			while (mRunning)
			{
				try
				{
					sleep(mInterval);
				}
				catch (InterruptedException e)
				{
					// do nothing
				}
				
				if (System.currentTimeMillis() - mLastConsume > mInterval)
				{
					consume();
				}
				
				synchronized (mConsumedStream)
				{
					mInServerSpool -= mAmount;
					if (mInServerSpool < 0)
					{
						mInServerSpool = 0;
					}
				}

				Thread.currentThread().yield();
			}
		}
		
		public void consume()
		{
			synchronized (mConsumedStream)
			{
				try
				{
					mInServerSpool += mConsumedStream.writeBuffer(mMax-mInServerSpool);
				}
				catch (IOException e)
				{
					clearBuffer();
					fireExceptionThrown(e);
				}
				mLastConsume = System.currentTimeMillis();
			}
		}
	}
}
