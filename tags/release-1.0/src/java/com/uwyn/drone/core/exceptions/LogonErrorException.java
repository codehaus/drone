/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Bot;

public class LogonErrorException extends CoreException
{
	private Bot	mBot = null;
	
	public LogonErrorException(Bot bot, Throwable cause)
	{
		super("Error while logging on bot '"+bot.getNick()+"' to server '"+bot.getServer()+"'.", cause);
		
		mBot = bot;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
}

