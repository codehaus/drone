/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.logmanagement.exceptions;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.LogManagerException;
import com.uwyn.rife.database.exceptions.DatabaseException;
import java.util.Calendar;

public class GetLogMessagesErrorException extends LogManagerException
{
	private	Bot			mBot = null;
	private Channel		mChannel = null;
	private Calendar	mDay = null;
	
	public GetLogMessagesErrorException(Bot bot, Channel channel, Calendar day)
	{
		this(bot, channel, day, null);
	}

	public GetLogMessagesErrorException(Bot bot, Channel channel, Calendar day, DatabaseException cause)
	{
		super("Error while retrieving the log messages of '"+day.getTime()+"' for bot '"+bot.getName()+"' and channel '"+channel.getName()+"'.", cause);
		
		mBot = bot;
		mChannel= channel;
		mDay = day;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
	
	public Calendar getDay()
	{
		return mDay;
	}
}

