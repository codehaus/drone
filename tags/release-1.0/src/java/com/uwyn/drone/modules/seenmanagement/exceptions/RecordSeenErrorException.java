/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.modules.seenmanagement.exceptions;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;
import com.uwyn.drone.modules.exceptions.SeenManagerException;
import com.uwyn.drone.modules.seenmanagement.SeenData;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class RecordSeenErrorException extends SeenManagerException
{
	private	Bot			mBot = null;
	private Channel		mChannel = null;
	private SeenData	mSeenData = null;
	
	public RecordSeenErrorException(Bot bot, Channel channel, SeenData seenData)
	{
		this(bot, channel, seenData, null);
	}

	public RecordSeenErrorException(Bot bot, Channel channel, SeenData seenData, DatabaseException cause)
	{
		super("Error while adding seen data '"+seenData+"' for bot '"+bot.getName()+"' and channel '"+channel.getName()+"'.", cause);
		
		mBot = bot;
		mChannel= channel;
		mSeenData = seenData;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
	
	public SeenData getServerMessage()
	{
		return mSeenData;
	}
}

