/*
 * Copyright 2002-2004 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Distributed under the terms of the GNU Lesser General Public
 * License, v2.1 or later
 *
 * $Id$
 */
package com.uwyn.drone.core.exceptions;

import com.uwyn.drone.core.Bot;
import com.uwyn.drone.core.Channel;

public class ChannelMessageErrorException extends CoreException
{
	private Channel	mChannel = null;
	private String	mMessage = null;
	
	public ChannelMessageErrorException(Channel channel, String message, Throwable cause)
	{
		super("Error sending the message '"+message+"' to channel '"+channel+"' on server '"+channel.getServer()+"'.", cause);
		
		mChannel = channel;
		mMessage = message;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
	
	public String getMessage()
	{
		return mMessage;
	}
}

