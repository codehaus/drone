/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.protocol.ServerMessage;
import java.util.ArrayList;

public class ModuleRunner extends Thread
{
	private Bot			mBot = null;
	private Module		mModule = null;
	private	boolean		mRunning = false;
	private ArrayList	mQueue = new ArrayList();
	
	public ModuleRunner(Bot bot, Module module)
	{
		if (null == bot)	throw new IllegalArgumentException("bot can't be null.");
		if (null == module)	throw new IllegalArgumentException("module can't be null.");
		
		mBot = bot;
		mModule = module;
	}
	
	public void setRunning(boolean running)
	{
		mRunning = running;
	}
	
	public boolean isRunning()
	{
		return mRunning;
	}
	
	public void addMessage(ModuleMessage message)
	{
		if (null == message)	throw new IllegalArgumentException("message can't be null.");
		
		synchronized (mQueue)
		{
			mQueue.add(message);
			mQueue.notifyAll();
		}
	}
	
	public void run()
	{
		mRunning = true;
		
		while (mRunning)
		{
			try
			{
				synchronized (mQueue)
				{
					processQueue();
					mQueue.wait();
				}
			}
			catch (InterruptedException e)
			{
				// do nothing
			}
			Thread.currentThread().yield();
		}
	}
	
	private void processQueue()
	{
		synchronized (mQueue)
		{
			ModuleMessage		module_message = null;
			ServerMessage		message = null;
			while (mQueue.size() > 0)
			{
				module_message = (ModuleMessage)mQueue.remove(0);
				module_message.execute(mBot, mModule);
			}
		}
	}
}
