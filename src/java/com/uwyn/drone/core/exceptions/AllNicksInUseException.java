/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Bot;

public class AllNicksInUseException extends CoreException
{
	private Bot	mBot = null;
	
	public AllNicksInUseException(Bot bot)
	{
		super("All the nick names of bot '"+bot.getNick()+"' are in use on server '"+bot.getServer()+"'.");
		
		mBot = bot;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
}

