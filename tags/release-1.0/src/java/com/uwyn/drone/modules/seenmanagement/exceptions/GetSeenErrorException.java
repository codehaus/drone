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
import com.uwyn.rife.database.exceptions.DatabaseException;

public class GetSeenErrorException extends SeenManagerException
{
	private	Bot		mBot = null;
	private Channel	mChannel = null;
	private String	mNickname = null;
	
	public GetSeenErrorException(Bot bot, Channel channel, String nickname)
	{
		this(bot, channel, nickname, null);
	}

	public GetSeenErrorException(Bot bot, Channel channel, String nickname, DatabaseException cause)
	{
		super("Error while retrieving seen data for '"+nickname+"', bot '"+bot.getName()+"' and channel '"+channel.getName()+"'.", cause);
		
		mBot = bot;
		mChannel= channel;
		mNickname = nickname;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
	
	public String getString()
	{
		return mNickname;
	}
}

