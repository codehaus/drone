/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Bot;

public class LogoffErrorException extends CoreException
{
	private Bot	mBot = null;
	
	public LogoffErrorException(Bot bot, Throwable cause)
	{
		super("Error while logging off bot '"+bot.getNick()+"' from server '"+bot.getServer()+"'.", cause);
		
		mBot = bot;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
}

