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
import com.uwyn.drone.protocol.ServerMessage;
import com.uwyn.rife.database.exceptions.DatabaseException;

public class AddLogErrorException extends LogManagerException
{
	private	Bot				mBot = null;
	private Channel			mChannel = null;
	private ServerMessage	mServerMessage = null;
	
	public AddLogErrorException(Bot bot, Channel channel, ServerMessage serverMessage)
	{
		this(bot, channel, serverMessage, null);
	}

	public AddLogErrorException(Bot bot, Channel channel, ServerMessage serverMessage, DatabaseException cause)
	{
		super("Error while adding log with server message '"+serverMessage+"', bot '"+bot.getName()+"' and channel '"+channel.getName()+"'.", cause);
		
		mBot = bot;
		mChannel= channel;
		mServerMessage = serverMessage;
	}
	
	public Bot getBot()
	{
		return mBot;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
	
	public ServerMessage getServerMessage()
	{
		return mServerMessage;
	}
}

