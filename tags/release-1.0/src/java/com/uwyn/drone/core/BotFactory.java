/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core;

import java.util.HashMap;

public abstract class BotFactory
{
	private static HashMap	mBots = new HashMap();
	
	public static Bot get(String name)
	{
		if (null == name)		throw new IllegalArgumentException("name can't be null.");
		if (0 == name.length())	throw new IllegalArgumentException("name can't be empty.");

		Bot bot = null;
		
		synchronized (mBots)
		{
			bot = (Bot)mBots.get(name);
			
			if (null == bot)
			{
				bot = new Bot(name);
				mBots.put(name, bot);
			}
		}
		
		return bot;
	}
	
	public static boolean contains(String name)
	{
		return mBots.containsKey(name);
	}
}
