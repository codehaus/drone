/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.BotsRunnerListener;
import com.uwyn.drone.core.exceptions.CoreException;
import com.uwyn.rife.tools.ExceptionUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

public class BotsRunner extends Thread implements BotListener
{
	private Collection	mBots = null;
	private Integer 	mBotsMonitor = new Integer(0);
	
	private Throwable	mBotError = null;
	private HashSet		mBotsRunnerListeners = null;
	private Integer	mBotsRunnerListenersMonitor = new Integer(0);
	
	public BotsRunner(Collection bots)
	{
		if (null == bots)	throw new IllegalArgumentException("bots can't be null.");
		
		mBots = bots;
		mBotsRunnerListeners = new HashSet();
	}
	
	public Collection getBots()
	{
		return mBots;
	}
	
	public void run()
	{
		Iterator	bots_it = null;
		Bot			bot = null;
		try
		{
			bots_it = mBots.iterator();
			while (bots_it.hasNext())
			{
				bot = (Bot)bots_it.next();
				
				bot.addBotListener(this);
				bot.logon();

				// wait for bot startup
				if (null == mBotError &&
					!bot.isLoggedOn())
				{
					try
					{
						synchronized (mBotsMonitor)
						{
							mBotsMonitor.wait();
						}
					}
					catch (InterruptedException e)
					{
						// do nothing, just let other threads execute
						Thread.currentThread().yield();
					}
				}
			}
			
			
			// wait for bot termination
			while (areBotsLoggedOn())
			{
				try
				{
					synchronized (mBotsMonitor)
					{
						mBotsMonitor.wait();
					}
				}
				catch (InterruptedException e)
				{
					// do nothing, just let other threads execute
					Thread.currentThread().yield();
				}
			}
			
			// disconnect the bots
			bots_it = mBots.iterator();
			while (bots_it.hasNext())
			{
				bot = (Bot)bots_it.next();
				bot.disconnect();
			}
		}
		catch (CoreException e)
		{
			Logger.getLogger("com.uwyn.drone").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
	}
	
	private boolean areBotsLoggedOn()
	{
		boolean result = false;
		
		Iterator	bots_it = mBots.iterator();
		while (bots_it.hasNext())
		{
			if (((Bot)bots_it.next()).isLoggedOn())
			{
				result = true;
				break;
			}
		}

		return result;
	}
	
	public void loggedOff(Bot bot)
	{
		synchronized (mBotsMonitor)
		{
			mBotsMonitor.notifyAll();
		}
	}
	
	public void loggedOn(Bot bot)
	{
		synchronized (mBotsMonitor)
		{
			mBotsMonitor.notifyAll();
		}
	}
	
	public void nickChanged(Bot bot)
	{
	}

	public void nickInUse(Bot bot, String nick)
	{
	}

	public void connectionError(Bot bot, Throwable e)
	{
		synchronized (mBotsMonitor)
		{
			mBotError = e;
			mBotsMonitor.notifyAll();
			Logger.getLogger("com.uwyn.drone").severe(ExceptionUtils.getExceptionStackTrace(e));
		}
	}

	private void fireFinished()
	{
		synchronized (mBotsRunnerListenersMonitor)
		{
			Iterator	listeners = mBotsRunnerListeners.iterator();
			
			while (listeners.hasNext())
			{
				((BotsRunnerListener)listeners.next()).finished(this);
			}
		}
	}

	public boolean addBotsRunnerListener(BotsRunnerListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

		boolean result = false;
		
		synchronized (mBotsRunnerListenersMonitor)
		{
			if (!mBotsRunnerListeners.contains(listener))
			{
				result = mBotsRunnerListeners.add(listener);
			}
			else
			{
				result = true;
			}
		}
		
		assert true == mBotsRunnerListeners.contains(listener);
		
		return result;
	}

	public boolean removeBotsRunnerListener(BotsRunnerListener listener)
	{
		if (null == listener)	throw new IllegalArgumentException("listener can't be null.");

        boolean result = false;
		
		synchronized (mBotsRunnerListenersMonitor)
		{
			result = mBotsRunnerListeners.remove(listener);
		}
		
		assert false == mBotsRunnerListeners.contains(listener);
		
		return result;
	}
}
