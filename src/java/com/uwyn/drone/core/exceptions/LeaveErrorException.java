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

public class LeaveErrorException extends CoreException
{
	private Channel	mChannel = null;
	
	public LeaveErrorException(Channel channel, Throwable cause)
	{
		super("Error while leaving channel '"+channel+"' on server '"+channel.getServer()+"'.", cause);
		
		mChannel = channel;
	}
	
	public Channel getChannel()
	{
		return mChannel;
	}
}

